# SpaceOps — Spacecraft Health Monitoring System

A minimal Spring Boot app that shows a dark-themed dashboard for the spacecraft **Explorer-01**
(battery, temperature, fuel, status) with randomly generated telemetry.
It demonstrates Maven, Docker, Jenkins and Kubernetes.

| URL | Description |
|-----|-------------|
| `http://localhost:8080/` | Dashboard |
| `http://localhost:8080/health` | Health check (`{"status":"UP",...}`) |
| `http://localhost:8080/api/telemetry` | Telemetry JSON (new random values on each call) |

Status is `WARNING` if battery < 20%, fuel < 20%, temperature > 80 °C or < -10 °C; otherwise `NORMAL`.

## Project layout

```
SpaceOps/
├── pom.xml
├── src/main/java/com/spaceops/        (SpaceOpsApplication, TelemetryController)
├── src/main/resources/static/         (index.html, style.css, app.js)
├── src/test/java/com/spaceops/        (TelemetryControllerTest)
├── Dockerfile, .dockerignore, .gitignore
├── Jenkinsfile
├── k8s/deployment.yaml, k8s/service.yaml
├── PPT_CONTENT.md
└── package.sh
```

## Prerequisites

- JDK 17 or newer (`java -version`)
- Maven 3.8+ (`mvn -v`)
- Docker (for the Docker section)
- kubectl + a cluster such as minikube, kind or Docker Desktop Kubernetes (for the Kubernetes section)
- Jenkins (for the Jenkins section)

## Build

```bash
cd SpaceOps
mvn clean package
```

This runs the tests and creates the executable JAR at `target/spaceops.jar`.

## Run locally (no Docker, no Kubernetes)

```bash
java -jar target/spaceops.jar
```

Or without building the JAR first:

```bash
mvn spring-boot:run
```

Open http://localhost:8080 and click **Refresh Telemetry**.

```bash
curl http://localhost:8080/health
curl http://localhost:8080/api/telemetry
```

## Docker

Build the JAR first (`mvn clean package`), then:

```bash
docker build -t spaceops:latest .
docker run --rm -p 8080:8080 spaceops:latest
```

Open http://localhost:8080.

## Kubernetes

The manifests use the local image `spaceops:latest` (`imagePullPolicy: IfNotPresent`), 2 replicas,
and a `NodePort` service on port `30080`.

### minikube

```bash
minikube start
docker build -t spaceops:latest .
minikube image load spaceops:latest
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl rollout status deployment/spaceops
minikube service spaceops --url
```

### kind

```bash
kind create cluster
docker build -t spaceops:latest .
kind load docker-image spaceops:latest
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl port-forward service/spaceops 8080:8080
```

Then open http://localhost:8080.

### Docker Desktop Kubernetes

```bash
docker build -t spaceops:latest .
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
```

Open http://localhost:30080.

### Remote cluster

Push the image to a registry, edit `image:` in `k8s/deployment.yaml` to that name
(e.g. `youruser/spaceops:1`), then apply both files.

### Clean up

```bash
kubectl delete -f k8s/service.yaml -f k8s/deployment.yaml
```

## Jenkins

The `Jenkinsfile` stages: **Checkout → Build JAR → Test → Build Docker Image → Push Docker Image (optional) → Deploy to Kubernetes (optional)**.

### Jenkins setup

1. The Jenkins agent must have **JDK 17+, Maven, Docker and kubectl** on its `PATH`.
   The `jenkins` user must be allowed to run Docker (`sudo usermod -aG docker jenkins`, then restart Jenkins).
2. Push this project to a Git repository (GitHub, GitLab, ...).
3. In Jenkins: **New Item → Pipeline** → *Pipeline script from SCM* → Git → your repo URL → Script Path: `Jenkinsfile`.
4. Click **Build with Parameters**.

### Parameters and credentials

| Parameter / credential | When needed | Details |
|------------------------|-------------|---------|
| `DOCKER_REPO` (parameter) | Only to push the image | e.g. `youruser/spaceops`. Empty = image stays on the Jenkins machine and the push stage is skipped. |
| `dockerhub-creds` (credential) | Only if `DOCKER_REPO` is set | Jenkins credential of kind **Username with password** (Docker Hub username + password or access token). |
| `DEPLOY_TO_K8S` (parameter) | Only to deploy | Default `false`. When `true`, the deploy stage runs `kubectl apply`. |
| `kubeconfig` (credential) | Only if `DEPLOY_TO_K8S` is true | Jenkins credential of kind **Secret file** containing a kubeconfig that can reach your cluster. |

Notes:

- With the defaults (`DOCKER_REPO` empty, `DEPLOY_TO_K8S` false) the pipeline checks out, builds, tests and builds the Docker image only. Deployment is **not** attempted.
- Deploying with an empty `DOCKER_REPO` only works if the cluster can see the local image
  (e.g. Jenkins runs on the same machine as Docker Desktop Kubernetes). For a remote cluster, set `DOCKER_REPO`.
- The deploy stage fails the build if `kubectl` cannot reach the cluster or the rollout does not finish within 120 s.

## Package into SpaceOps.zip

```bash
chmod +x package.sh
./package.sh
```

This builds the JAR (if Maven is installed) and creates `SpaceOps.zip` in the folder **above** `SpaceOps/`.
The ZIP contains the source and `target/spaceops.jar` when the build succeeded.
