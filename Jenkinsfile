```
pipeline {
    agent any
    environment {
        AWS_REGION = 'us-east-1'
        AWS_ACCOUNT_ID = credentials('aws-account-id')
        ECR_REGISTRY = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
        IMAGE_NAME = 'healthcare-app'
        IMAGE_TAG = "${env.BUILD_NUMBER}-${env.GIT_COMMIT.take(7)}"
        NEXUS_URL = 'nexus.corporate.local:8081'
        NEXUS_CREDS = credentials('nexus-credentials')
        VAULT_ADDR = 'https://vault.corporate.local'
        VAULT_TOKEN = credentials('vault-token')
        SONARQUBE_URL = 'http://sonarqube.corporate.local:9000'
        SONARQUBE_TOKEN = credentials('sonarqube-token')
        SES_EMAIL = 'alerts@corporate.local'
    }
    options {
        buildDiscarder(logRotator(numToKeepStr: '30', artifactNumToKeepStr: '10'))
        disableConcurrentBuilds()
        timeout(time: 1, unit: 'HOURS')
        timestamps()
    }
    stages {
        stage('Checkout') {
            steps {
                script {
                    try {
                        git branch: 'main', url: 'https://github.com/corporate/healthcare-app.git', credentialsId: 'github-credentials'
                        sh 'git clean -fdx'
                    } catch (Exception e) {
                        error "Checkout failed: ${e.message}"
                    }
                }
            }
        }
        stage('Build') {
            steps {
                script {
                    try {
                        sh 'mvn clean package -DskipTests'
                        sh 'docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .'
                    } catch (Exception e) {
                        error "Build failed: ${e.message}"
                    }
                }
            }
        }
        stage('SonarQube Analysis') {
            steps {
                script {
                    try {
                        withSonarQubeEnv('SonarQube') {
                            sh "mvn sonar:sonar -Dsonar.host.url=${SONARQUBE_URL} -Dsonar.login=${SONARQUBE_TOKEN}"
                        }
                        timeout(time: 5, unit: 'MINUTES') {
                            waitForQualityGate abortPipeline: true
                        }
                    } catch (Exception e) {
                        error "SonarQube analysis failed: ${e.message}"
                    }
                }
            }
        }
        stage('Security Scans') {
            parallel {
                stage('OWASP Dependency-Check') {
                    steps {
                        script {
                            try {
                                sh '''
                                    dependency-check.sh --scan . --format JSON --out dependency-check-report.json
                                    aws securityhub batch-import-findings --findings file://dependency-check-report.json --region ${AWS_REGION}
                                '''
                            } catch (Exception e) {
                                error "OWASP scan failed: ${e.message}"
                            }
                        }
                    }
                }
                stage('Trivy Container Scan') {
                    steps {
                        script {
                            try {
                                sh '''
                                    trivy image --format json --output trivy-report.json ${IMAGE_NAME}:${IMAGE_TAG}
                                    aws securityhub batch-import-findings --findings file://trivy-report.json --region ${AWS_REGION}
                                '''
                            } catch (Exception e) {
                                error "Trivy scan failed: ${e.message}"
                            }
                        }
                    }
                }
            }
        }
        stage('Push Artifacts') {
            steps {
                script {
                    try {
                        sh """
                            mvn deploy -DaltDeploymentRepository=nexus::default::http://${NEXUS_URL}/repository/maven-releases/
                            aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY}
                            docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${ECR_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}
                            docker push ${ECR_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}
                        """
                    } catch (Exception e) {
                        error "Artifact push failed: ${e.message}"
                    }
                }
            }
        }
        stage('Deploy to EKS') {
            steps {
                script {
                    try {
                        withVault(configuration: [vaultUrl: "${VAULT_ADDR}", vaultCredentialId: 'vault-token'], vaultSecrets: [
                            [path: 'secret/healthcare-app', secretValues: [
                                [envVar: 'DB_PASSWORD', vaultKey: 'db_password']
                            ]]
                        ]) {
                            sh '''
                                helm upgrade --install healthcare-app ./helm/healthcare-app \
                                    --set backend.image=${ECR_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG} \
                                    --set backend.dbPassword=${DB_PASSWORD} \
                                    --namespace healthcare
                            '''
                        }
                    } catch (Exception e) {
                        error "EKS deployment failed: ${e.message}"
                    }
                }
            }
        }
        stage('Inspector Scan') {
            steps {
                script {
                    try {
                        sh '''
                            TEMPLATE_ARN=$(aws inspector list-assessment-templates --region ${AWS_REGION} --query 'assessmentTemplateArns[0]' --output text)
                            RUN_ARN=$(aws inspector start-assessment-run --assessment-template-arn ${TEMPLATE_ARN} --region ${AWS_REGION} --query assessmentRunArn --output text)
                            aws inspector wait assessment-run-completed --assessment-run-arn ${RUN_ARN} --region ${AWS_REGION}
                        '''
                    } catch (Exception e) {
                        error "Inspector scan failed: ${e.message}"
                    }
                }
            }
        }
    }
    post {
        success {
            script {
                sh """
                    aws ses send-email \
                        --from ${SES_EMAIL} \
                        --to ${SES_EMAIL} \
                        --subject "Pipeline Success: Build #${env.BUILD_NUMBER}" \
                        --text "Deployment of healthcare-app:${IMAGE_TAG} succeeded" \
                        --region ${AWS_REGION}
                """
            }
        }
        failure {
            script {
                sh """
                    aws ses send-email \
                        --from ${SES_EMAIL} \
                        --to ${SES_EMAIL} \
                        --subject "Pipeline Failure: Build #${env.BUILD_NUMBER}" \
                        --text "Pipeline failed: ${currentBuild.result}" \
                        --region ${AWS_REGION}
                """
            }
        }
        always {
            archiveArtifacts artifacts: 'dependency-check-report.json, trivy-report.json', allowEmptyArchive: true
            cleanWs()
        }
    }
}
```
