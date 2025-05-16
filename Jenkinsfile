pipeline {
    agent any

    environment {
        IMAGE_NAME = "harbor.shoong.store/webhook/${env.JOB_BASE_NAME}"
        TAG = "${BUILD_NUMBER}"
        HARBOR_CREDENTIALS_ID = "Harbor"
        MANIFEST_REPO = "https://github.com/SHOONG-SHOONG/k8s-manifests.git"
        SERVICE_NAME = "${env.JOB_BASE_NAME}" 
    }

    stages {
        stage('Clone Repo') {
            steps {
                git url: 'https://github.com/SHOONG-SHOONG/jenkins.git', branch: 'main'
            }
        }

        stage('Build Docker Image') {
          steps {
            sh "docker build -t ${IMAGE_NAME}:latest ."
            sh "docker tag ${IMAGE_NAME}:latest ${IMAGE_NAME}:${TAG}"
          }
        }

        stage('Login to Harbor') {
            steps {
                withCredentials([usernamePassword(credentialsId: "${HARBOR_CREDENTIALS_ID}", usernameVariable: 'HARBOR_USER', passwordVariable: 'HARBOR_PASS')]) {
                    sh "echo $HARBOR_PASS | docker login http://harbor.shoong.store -u $HARBOR_USER --password-stdin"
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                sh "docker push ${IMAGE_NAME}:latest"
                sh "docker push ${IMAGE_NAME}:${TAG}"
            }
        }
    }

    post {
        success {
            echo "🎀 ${env.SERVICE_NAME} 자동 CI/CD 완료"
        }
        failure {
            echo "😡 ${env.SERVICE_NAME} 오류 발생"
        }
    }
}
