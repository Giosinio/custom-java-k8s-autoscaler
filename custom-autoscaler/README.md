How to run this project:
1. run `mvn clean package` in terminal to build the JAR
2. run `docker build -t custom-autoscaler:0.0.1 .` to build the docker image
3. run `minikube image load custom-autoscaler:0.0.1` to load the image into minikube
4. run `helm upgrade --install custom-autoscaler helm/custom-autoscaler` to deploy the application to minikube