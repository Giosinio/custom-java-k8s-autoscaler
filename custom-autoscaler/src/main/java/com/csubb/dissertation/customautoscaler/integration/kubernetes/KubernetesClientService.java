package com.csubb.dissertation.customautoscaler.integration.kubernetes;

import com.csubb.dissertation.customautoscaler.domain.ScaledDeployment;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodCondition;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1PodStatus;
import io.kubernetes.client.openapi.models.V1Scale;
import io.kubernetes.client.openapi.models.V1ScaleSpec;
import io.kubernetes.client.util.Config;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Class that allows interaction with Kubernetes API. In our case:
 * - scaling a deployment
 * - checking if the pods from a deployment are in Ready state
 */
@Slf4j
@Service
public class KubernetesClientService {

    private final AppsV1Api appsV1Api;
    private final CoreV1Api coreV1Api;

    public KubernetesClientService() {
        try {
            ApiClient client = Config.defaultClient();
            io.kubernetes.client.openapi.Configuration.setDefaultApiClient(client);
            this.appsV1Api = new AppsV1Api(client);
            this.coreV1Api = new CoreV1Api(client);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create Kubernetes API client", e);
        }
    }

    /**
     * Scale a deployment by replacing its scale sub-resource.
     * @param namespace Kubernetes namespace where the deployment resides
     * @param deploymentName name of the deployment
     * @param updatedReplicaCount the number of replicas to scale by (positive to scale up, negative to scale down)
     * @return the new replica count (after scaling)
     * @throws ApiException if the API request fails
     */
    public ScaleOperationResponse scaleDeployment(String namespace, String deploymentName, int updatedReplicaCount) throws ApiException {
        // read current scale (method signature: name, namespace)
        V1Scale currentScale = appsV1Api.readNamespacedDeploymentScale(deploymentName, namespace).execute();

        V1ScaleSpec spec = Objects.nonNull(currentScale) ? Objects.requireNonNull(currentScale.getSpec()) : new V1ScaleSpec();
        spec.setReplicas(updatedReplicaCount);

        currentScale = Objects.nonNull(currentScale) ? currentScale : new V1Scale();
        currentScale.setSpec(spec);
        appsV1Api.replaceNamespacedDeploymentScale(deploymentName, namespace, currentScale).execute();

        return new ScaleOperationResponse(true, updatedReplicaCount);
    }

    /**
     * Checks if all the pods are ready for a deployment.
     * @param scaledDeployment {@link ScaledDeployment}
     * @return true - if all pods are ready, false otherwise
     */
    public boolean checkAllPodsAreReadyForDeployment(@NotNull ScaledDeployment scaledDeployment, @NotNull Integer targetReplicas) {
        V1PodList podList = getPodsForDeployment(scaledDeployment.getNamespace(), scaledDeployment.getService());
        if(Objects.isNull(podList)) {
            log.error("Failed to fetch pods for deployment {}, cannot check readiness", scaledDeployment.getService());
            return false;
        }
        else if (podList.getItems().size() != targetReplicas) {
            return false;
        }

        log.info("Checking readiness for pods of deployment {}: found {} pods", scaledDeployment.getService(), podList.getItems().size());

        long readyPodsCount = podList.getItems().stream().filter(checkPodIsReady()).count();
        if(readyPodsCount < podList.getItems().size()) {
            log.info("{}/{} pods are ready for deployment {}", readyPodsCount, podList.getItems().size(), scaledDeployment.getService());
            return false;
        }
        return true;
    }

    public Integer getDeploymentReplicaCount(String namespace, String deploymentName) throws ApiException {
        V1Scale currentScale = appsV1Api.readNamespacedDeploymentScale(deploymentName, namespace).execute();
        return Optional.ofNullable(currentScale).map(V1Scale::getSpec).map(V1ScaleSpec::getReplicas).orElse(null);
    }

    private V1PodList getPodsForDeployment(String namespace, String deploymentName) {
        String labelSelector = String.format("app=%s", deploymentName);
        try {
            return coreV1Api
                    .listNamespacedPod(namespace)
                    .labelSelector(labelSelector)
                    .execute();
        } catch (ApiException e) {
            log.error("Failed to check pod readiness for deployment {}: {}", deploymentName, e.getResponseBody(), e);
            return null;
        }
    }

    private static Predicate<V1Pod> checkPodIsReady() {
        return pod -> {
            List<V1PodCondition> conditions = Optional.ofNullable(pod.getStatus()).map(V1PodStatus::getConditions).orElse(List.of());
            return conditions.stream().anyMatch(c -> "Ready".equals(c.getType()) && "True".equals(c.getStatus()));
        };
    }
}
