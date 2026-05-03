This project proposes to build an autoscaler that can be deployed in any
Kubernetes cluster. The autoscaler is written in Java and needs Prometheus
deployed in the cluster to collect the metrics. The autoscaler also uses
Kubernetes API to get the current state of the cluster and to scale the
deployments.

The module task-service is a dummy service that simulates a real
application. This is the application/deployment that will be scaled.


The module custom-autoscaler is the actual implementation of the autoscaler.

Steps to start the cluster and to deploy the application and the autoscaler:
1. (for dry run) run `minikube delete --all` to delete the previous config
2. run `minikube start --cpus=4 --memory=7864` to start minikube (or any combination of cpus and memory)
3. run `minikube addons enable ingress` to enable ingress controller in minikube
4. enable Prometheus and Grafana in the Minikube cluster:
    - run `helm repo add prometheus-community https://prometheus-community.github.io/helm-charts ; helm repo update`
    - run `helm install prometheus prometheus-community/kube-prometheus-stack`
5. install metrics-service in K8s cluster (for K9s to correctly show the CPU and memory usage of the pods):
    - run `kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml`
    - add the following configs to the metrics-service deployment (because of ssl validation error):
      - run `kubectl edit deployment metrics-server -n kube-system`
      - make sure the following configs are under `spec.template.spec.containers.args`:
        - `- --kubelet-insecure-tls`
        - `- --kubelet-preferred-address-types=InternalIP,ExternalIP,Hostname`
6. deploy task-service to the cluster, follow the steps from the task-service/README.md file
7. deploy custom-autoscaler to the cluster, follow the steps from the custom-autoscaler/README.md file
8. for running tests using k6, run `k6 run test-scenarios/demo_test.js` ()

Useful terminal commands:
- `kubectl get deployments` → list all deployments
- `kubectl delete deployment task-service` → delete the current deployment from K8s
- `minikube ssh -- docker images` → list all docker images in minikube
- `minikube ssh -- docker rmi task-service:0.0.1`→ remove a docker image from minikube
- `kubectl port-forward svc/prometheus-kube-prometheus-prometheus 9090:9090` → exposes Prometheus UI on localhost:9090
- `kubectl port-forward svc/prometheus-grafana 3000:80` → exposes Grafana dashboard on localhost:3000