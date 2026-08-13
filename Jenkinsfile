pipeline {
    agent any

    environment {
        DEPLOY_DIR = '/home/mh/itop-java'
        GIT_REPO   = 'https://github.com/xiaojunzhe32-ai/IT-Request.git'
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 10, unit: 'MINUTES')
    }

    stages {

        stage('拉取代码') {
            steps {
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/main']],
                    userRemoteConfigs: [[url: "${GIT_REPO}"]]
                ])
            }
        }

        stage('同步到部署目录') {
            steps {
                sh '''
                # 确保部署目录存在
                mkdir -p ${DEPLOY_DIR}

                # 同步代码（排除 .git、target、node_modules、dist）
                rsync -av --delete \
                    --exclude='.git' \
                    --exclude='target' \
                    --exclude='node_modules' \
                    --exclude='dist' \
                    ./ ${DEPLOY_DIR}/
                '''
            }
        }

        stage('构建镜像') {
            steps {
                sh '''
                cd ${DEPLOY_DIR}
                docker compose build --no-cache itop-api itop-web
                '''
            }
        }

        stage('部署') {
            steps {
                sh '''
                cd ${DEPLOY_DIR}
                docker compose up -d --no-deps itop-api itop-web

                # 等待 API 健康检查通过
                for i in $(seq 1 30); do
                    if docker exec itop-api wget -q -O /dev/null http://localhost:8080/api/actuator/health 2>/dev/null; then
                        echo "API is healthy"
                        exit 0
                    fi
                    echo "Waiting for API... ($i/30)"
                    sleep 5
                done
                echo "API health check failed"
                exit 1
                '''
            }
        }
    }

    post {
        success {
            echo '✅ 部署成功: itop-api + itop-web 已更新'
        }
        failure {
            echo '❌ 部署失败，请检查日志'
        }
        always {
            echo "构建完成: ${currentBuild.result ?: 'SUCCESS'}"
        }
    }
}
