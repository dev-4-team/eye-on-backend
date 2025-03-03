pipeline {
    agent any
    environment {
        DOCKER_HUB_CREDS = credentials('dockerhub')
        APP_NAME = 'eye-on'
        DOCKER_IMAGE = "${DOCKER_HUB_USERNAME}/${APP_NAME}"
        IMAGE_TAG = "${env.BUILD_NUMBER}"
        DEPLOY_SERVER = "3.35.41.235"
        DEPLOY_USER = "ubuntu"
        APP_PATH = "/eye-on-backend"
    }
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Build') {
            steps {
                sh './gradlew clean build -x test'
            }
        }
        stage('Test') {
            steps {
                echo 'Testing...'
            }
        }
        stage('Build Docker Image') {
            steps {
                sh """docker build -t $DOCKER_HUB_USERNAME/eye-on:${IMAGE_TAG:
                -latest} ."""
            }
        }
        stage('Push Docker Image') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub',
                        passwordVariable: 'password',
                        usernameVariable: 'username')]) {
                    sh "echo \${password} | docker login -u \${username} --password-stdin"
                    sh """docker push $DOCKER_HUB_USERNAME/eye-on:${IMAGE_TAG:
                    -latest}"""
                }
            }
        }
        stage('Deploy to Production') {
            steps {
                sh """
                        chmod +x ./scripts/deploy.sh
                        ./scripts/deploy.sh
                    """
            }
        }
    }
    post {
        always {
            sh "docker logout || true"
            cleanWs()
        }
        success {
            echo '배포가 성공적으로 완료되었습니다!'
            // 슬랙 같은 메시징 서비스로 알림 전송 가능
            // slackSend channel: '#deployments', color: 'good', message: "${APP_NAME} 배포 성공"
        }
        failure {
            echo '배포 중 오류가 발생했습니다!'
            // slackSend channel: '#deployments', color: 'danger', message: "${APP_NAME} 배포 실패"
        }
    }
}