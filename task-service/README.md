How to run this project:
1. run `mvn clean package` in terminal to build the JAR
2. run `docker build -t task-service:0.0.1 .` to build the docker image
3. run `minikube image load task-service:0.0.1` to load the image into minikube
4. run `helm upgrade --install task-service helm/task-service` to deploy the application to minikube
5. run `minikube tunnel` to create a tunnel for the ingress controller
6. you should be able to call now POST `http://task-service.local/api/task-service/execute-task`