pipeline {
    agent any

    tools {
        maven 'Maven_3.9.9'  // Ensure this matches the name of your Maven installation in Jenkins
        jdk 'JDK_21'        // Ensure this matches the name of your JDK installation in Jenkins
    }

    environment {
        IMAGE_NAME = "abakhar217/user-service:user-service-${BUILD_NUMBER}"
        DEPLOYMENT_NAME = 'user-service-deployment'
    }

    stages {
        stage('Checkout') {
            steps {
                // Checkout the code from the repository using the configured Git credentials
                checkout([$class: 'GitSCM',
                          branches: [[name: 'main']],
                          userRemoteConfigs: [[url: 'https://github.com/InvestBuddy/user-service.git', credentialsId: 'Git']]])
            }
        }

        stage('Build JAR') {
            steps {
                script {
                    // Run mvn clean package to build the application
                    bat 'mvn clean package -Dmaven.test.failure.ignore=true'
                }
            }
        }

        stage('Check if JAR exists') {
            steps {
                script {
                    // Ensure the JAR file exists before proceeding
                    if (!fileExists('target/user-service.jar')) {
                        error "user-service.jar not found! Build failed."
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    // Build the Docker image with the .jar file inside the image
                    bat "docker build -t ${IMAGE_NAME} ."
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                script {
                    // Use the credentials stored in Jenkins for Docker Hub
                    withCredentials([usernamePassword(credentialsId: 'dockerhub', passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
                        // Log in to Docker Hub
                        bat "echo %DOCKER_PASSWORD% | docker login -u %DOCKER_USERNAME% --password-stdin"
                        // Push the image to Docker Hub
                        bat "docker push ${IMAGE_NAME}"
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                script {
                    withKubeConfig([credentialsId: 'kubectl1']) {
                        // Ensure kubectl is configured and available in the Jenkins environment
                        bat """
                            kubectl apply -f deployment.yml
                            kubectl apply -f service.yml
                        """
                    }
                }
            }
        }
    }

    post {
        always {
            // Clean up any images or containers that were created
            bat 'docker system prune -f'
        }
        failure {
            // Notify in case of failure (if required, add email or other notifications here)
            echo "Build or deployment failed."
        }
    }
}
