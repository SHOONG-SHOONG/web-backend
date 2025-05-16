pipeline {
    agent any

    environment {
        IMAGE_NAME = "harbor.shoong.store/shoong-backend/${env.JOB_BASE_NAME}"
        TAG = "${BUILD_NUMBER}"
        HARBOR_CREDENTIALS_ID = "Harbor"
        MANIFEST_REPO = "https://github.com/SHOONG-SHOONG/k8s-manifests.git"
        SERVICE_NAME = "${env.JOB_BASE_NAME}" 
    }

    stages {
        stage('Clone Repo') {
            steps {
                git url: 'https://github.com/SHOONG-SHOONG/web-backend.git', branch: 'develop'
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

        
        stage('Update Manifest Repo') {
          steps {
            withCredentials([usernamePassword(credentialsId: 'webhook', usernameVariable: 'GIT_USER', passwordVariable: 'GIT_TOKEN')]) {
              sh '''
                echo "🔁 Manifest 레포 업데이트 시작"
        
                # 1. clone manifest repo
                rm -rf k8s-manifests
                git clone https://${GIT_USER}:${GIT_TOKEN}@github.com/SHOONG-SHOONG/k8s-manifests.git
        
                # 2. 서비스 경로 이동 (backend, frontend 자동 분기)
                cd k8s-manifests/${SERVICE_NAME}
        
                # 3. 이미지 경로 변경
                sed -i "s|image: harbor.shoong.store/.*/.*/.*|image: ${IMAGE_NAME}:${TAG}|" deployment.yaml
        
                # 4. git commit & push
                git config user.name "jenkins-bot"
                git config user.email "jenkins@shoong.store"
                git add deployment.yaml
                git commit -m "☑️ ${SERVICE_NAME}: Update image tag to ${TAG}"
                git push origin main
        
                echo "🐬 manifest 업데이트 및 푸시 완료"
              '''
            }
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
