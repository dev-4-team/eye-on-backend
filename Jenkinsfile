pipeline {
    agent any
    environment {
        DOCKER_HUB_USERNAME = "${env.DOCKER_HUB_USR}"
        IMAGE_TAG = "${env.IMAGE_TAG}"
        DOCKER_IMAGE = "${DOCKER_HUB_USERNAME}/${APP_NAME}"
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
                sh "printenv"
                echo 'Testing...'
            }
        }
        stage('Clean Running Container') {
            steps {
                sh '''
                    # 기존 컨테이너가 존재하는지 확인
                    if docker ps -a | grep -q ${CONTAINER_NAME}; then
                        echo "기존 컨테이너 ${CONTAINER_NAME}을 중지하고 삭제합니다."
                        docker stop ${CONTAINER_NAME} || true
                        docker rm ${CONTAINER_NAME} || true
                    else
                        echo "실행 중인 ${CONTAINER_NAME} 컨테이너가 없습니다."
                    fi
                '''
            }
        }
        stage('Build Docker Image') {
            steps {
                sh """
                    docker image prune || true
                    docker build -t $DOCKER_IMAGE:$IMAGE_TAG .          
                    """
            }
        }
        stage('Push Docker Image') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub', passwordVariable: 'password', usernameVariable: 'username')]) {
                    sh "echo $password | docker login -u $username --password-stdin"
                    sh """docker push $DOCKER_IMAGE:$IMAGE_TAG"""
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
            withCredentials([string(credentialsId: 'discord-webhook', variable: 'WEBHOOK_URL')]) {
                discordSend description: "[백엔드] 배포가 성공적으로 완료되었습니다.",
                link: env.BUILD_URL, result: currentBuild.currentResult,
                title: "Jenkins 배포 성공",
                webhookURL: "$WEBHOOK_URL"
            }
        }
        failure {
            echo '배포 중 오류가 발생했습니다!'
            withCredentials([string(credentialsId: 'discord-webhook', variable: 'WEBHOOK_URL')]) {
                discordSend description: "[백엔드] 배포가 실패하였습니다.",
                link: env.BUILD_URL, result: currentBuild.currentResult,
                title: "Jenkins 배포 실패",
                webhookURL: "$WEBHOOK_URL"
            }
        }
    }
}