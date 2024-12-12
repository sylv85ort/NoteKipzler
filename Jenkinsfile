pipeline {
    agent any

    environment {
        DATADOG_API_KEY = credentials('datadog-api-key')
        JAVA_HOME = tool name: 'JDK-21'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                withMaven(maven: 'Maven') {
                    sh 'mvn clean package'
                }
            }
        }

        stage('DataDog Integration') {
            steps {
                script {
                    // Install DataDog Agent
                    sh '''
                        DD_API_KEY=${DATADOG_API_KEY} DD_SITE="datadoghq.com" bash -c "$(curl -L https://s3.amazonaws.com/dd-agent/scripts/install_script.sh)"
                    '''

                    // Configure DataDog Agent
                    sh '''
                        echo "api_key: ${DATADOG_API_KEY}" | sudo tee /etc/datadog-agent/datadog.yaml
                        sudo systemctl restart datadog-agent
                    '''
                }
            }
        }

        stage('JMeter Performance Tests') {
            steps {
                script {
                    // Run JMeter tests with DataDog monitoring
                    sh '''
                        jmeter -n -t test-plan.jmx -l results.jtl
                    '''
                }
            }
            post {
                always {
                    perfReport 'results.jtl'
                }
            }
        }

        stage('Deploy') {
            steps {
                // Your deployment steps
                sh 'mvn spring-boot:run'
            }
        }
    }

    post {
        success {
            // Send notification to DataDog
            script {
                sh "curl -X POST https://api.datadoghq.com/api/v1/events -H 'Content-Type: application/json' -H 'DD-API-KEY: ${DATADOG_API_KEY}' -d '{\"title\":\"Deployment Successful\",\"text\":\"Jenkins deployment completed\",\"alert_type\":\"success\"}'"
            }
        }

        failure {
            // Send failure notification
            script {
                sh "curl -X POST https://api.datadoghq.com/api/v1/events -H 'Content-Type: application/json' -H 'DD-API-KEY: ${DATADOG_API_KEY}' -d '{\"title\":\"Deployment Failed\",\"text\":\"Jenkins deployment failed\",\"alert_type\":\"error\"}'"
            }
        }
    }
}