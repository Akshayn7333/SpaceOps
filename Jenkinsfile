// SpaceOps CI/CD pipeline.
// Jenkins agent needs: JDK 17+, Maven, Docker, kubectl.
// See README.md section "Jenkins" for credentials and setup.
pipeline {
    agent any

    parameters {
        string(name: 'DOCKER_REPO', defaultValue: '',
               description: 'Optional registry repo to push to, e.g. youruser/spaceops. Leave empty to keep the image local.')
        booleanParam(name: 'DEPLOY_TO_K8S', defaultValue: false,
               description: 'Deploy to Kubernetes. Needs a Jenkins "Secret file" credential with ID: kubeconfig')
    }

    environment {
        IMAGE_NAME = 'spaceops'
        IMAGE_TAG  = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build JAR') {
            steps {
                sh 'mvn -B clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn -B test'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'docker build -t ${IMAGE_NAME}:${IMAGE_TAG} -t ${IMAGE_NAME}:latest .'
            }
        }

        stage('Push Docker Image') {
            when {
                expression { params.DOCKER_REPO?.trim() }
            }
            steps {
                // Needs a Jenkins "Username with password" credential with ID: dockerhub-creds
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds',
                        usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh '''
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${DOCKER_REPO}:${IMAGE_TAG}
                        docker push ${DOCKER_REPO}:${IMAGE_TAG}
                    '''
                }
            }
        }

        stage('Deploy to Kubernetes') {
            when {
                expression { params.DEPLOY_TO_K8S }
            }
            steps {
                withCredentials([file(credentialsId: 'kubeconfig', variable: 'KUBECONFIG')]) {
                    sh '''
                        if [ -n "${DOCKER_REPO}" ]; then
                            IMAGE="${DOCKER_REPO}:${IMAGE_TAG}"
                        else
                            IMAGE="${IMAGE_NAME}:${IMAGE_TAG}"
                        fi
                        sed "s|image: spaceops:latest|image: ${IMAGE}|" k8s/deployment.yaml | kubectl apply -f -
                        kubectl apply -f k8s/service.yaml
                        kubectl rollout status deployment/spaceops --timeout=120s
                    '''
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline finished. Deployment only ran if DEPLOY_TO_K8S was enabled.'
        }
        failure {
            echo 'Pipeline failed. Check the stage logs above.'
        }
    }
}
