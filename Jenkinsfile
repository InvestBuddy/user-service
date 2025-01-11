pipeline {
    agent any

    tools {
        maven 'Maven_3.9.9'  // Ensure this matches the name of your Maven installation in Jenkins
        jdk 'JDK_21'        // Ensure this matches the name of your JDK installation in Jenkins
    }

    environment {
        IMAGE_NAME = "abakhar217/user-service:user-service-${BUILD_NUMBER}"
        DEPLOYMENT_NAME = 'user-service-deployment'
        // qualityGateFailed = false // Flag to track Quality Gate failure
    }

    stages {
        stage('Checkout') {
            steps {
                // Checkout the code from the repository using the configured Git credentials
                checkout([$class: 'GitSCM',
                          branches: [[name: 'master']],
                          userRemoteConfigs: [[url: 'https://github.com/InvestBuddy/user-service.git', credentialsId: 'git']]])
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
                    if (!fileExists('target/user-service-1.0-SNAPSHOT.jar')) {
                        error "user-service-1.0-SNAPSHOT.jar not found! Build failed."
                    }
                }
            }
        }
       
       // stage('Build and SonarQube Analysis') {
       //      steps {
       //          withSonarQubeEnv('SonarQubeServer') {
       //              bat 'mvn sonar:sonar -Dsonar.login=%SONAR_TOKEN%'
       //          }
       //      }
       //  }
	// stage('Quality Gate') {
	//     steps {
	//         script {
	//             def qualityGate = waitForQualityGate()
	//             // Wait for the task to finish for up to 15 minutes
	//             def startTime = System.currentTimeMillis()
	//             def timeoutDuration = 5 * 60 * 1000  // you can increasse 5 minutes to  15 minutes
	
	//             while (qualityGate.status == 'PENDING' && (System.currentTimeMillis() - startTime) < timeoutDuration) {
	//                 echo "SonarQube analysis is still pending. Waiting..."
	//                 sleep(30)  // Wait for 30 seconds before checking again
	//                 qualityGate = waitForQualityGate()  // Re-check status
	//             }
	
	//             if (qualityGate.status == 'PENDING') {
	//                 echo "SonarQube analysis is still pending after the timeout duration."
	//                 currentBuild.result = 'UNSTABLE'  // Mark the build as unstable
	//                 qualityGateFailed = true
	//             } else if (qualityGate.status != 'OK') {
	//                 echo "Warning: Quality Gate failed with status: ${qualityGate.status}. Continuing pipeline execution."
	//                 qualityGateFailed = true
	//                 currentBuild.result = 'UNSTABLE'
	//             }
	//         }
	//     }
	// }

        
        // stage('Next Stage') {
        //     steps {
        //         script {
        //             if (qualityGateFailed) {
        //                 echo "Quality Gate failed, proceeding with caution."
        //             } else {
        //                 echo "Quality Gate passed, proceeding normally."
        //             }
        //         }
        //     }
        // }


        

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
                        bat "echo %DOCKER_PASSWORD% | docker login -u %DOCKER_USERNAME% -p %DOCKER_PASSWORD% "
                        // Push the image to Docker Hub
                        bat "docker push ${IMAGE_NAME}"
                    }
                }
            }
        }

		stage('Deploy to Kubernetes') {
		    steps {
		        script {
		            withKubeConfig([credentialsId: 'kubectl']) {
		        	bat 'kubectl apply -f user-service.yml'
		            }
		        }
		    }
		}
    }

	post {
	    always {
	        echo "Pipeline completed. Final status: ${currentBuild.currentResult}"
	        bat 'docker system prune -f'
	    }
	    success {
	        echo "Pipeline succeeded! Build number: ${env.BUILD_NUMBER}, Job name: ${env.JOB_NAME}"
	    }
	    unstable {
	        echo "Pipeline marked as UNSTABLE. Possible cause: Quality Gate failure or warnings."
	    }
	    failure {
	        echo "Pipeline failed!"
	        echo "Error Details: ${currentBuild.description ?: 'No detailed error provided.'}"
	        script {
	            currentBuild.description = "Failure occurred during ${env.STAGE_NAME}. Check logs."
	        }
	    }
	    aborted {
	        echo "Pipeline was aborted by user or timeout."
	    }
	}


}
