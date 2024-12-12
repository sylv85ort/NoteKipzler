pipeline {
    agent any

    environment {
        DATADOG_API_KEY = credentials('DATADOG_API_KEY')
    }

    040ce763-020d-4ca8-9af4-01b14afcc604

    stage('Checkout Code') {
        steps {
            git credentialsId: 'your-credentials-id', url: 'https://github.com/sylv85ort/NoteKipzler.git'
        }


        stage('Build with Maven') {
            steps {
                sh 'mvn clean package'
            }
        }

        stage('Run JMeter Test') {
            environment {
                // Appending JMeter's bin directory to the PATH using withEnv
                PATH = "/opt/jmeter/bin:${env.PATH}"
            }
            steps {
                // Run JMeter test plan
                sh 'jmeter -n -t test_plan.jmx -l results.jtl'
            }
        }

        stage('Install and Configure Datadog Agent') {
            steps {
                sh '''
                    DD_API_KEY=${DATADOG_API_KEY} DD_SITE="datadoghq.com" bash -c "$(curl -L https://s3.amazonaws.com/dd-agent/scripts/install_script.sh)"
                    echo "api_key: ${DATADOG_API_KEY}" | sudo tee /etc/datadog-agent/datadog.yaml
                    sudo systemctl restart datadog-agent
                '''
            }
        }

        stage('Post Build Task - Datadog Event') {
            steps {
                script {
                    def buildStatus = currentBuild.currentResult == 'SUCCESS' ? 'success' : 'error'
                    sh """
                        curl -X POST "https://api.datadoghq.com/api/v1/events" \
                        -H "Content-Type: application/json" \
                        -H "DD-API-KEY: ${DATADOG_API_KEY}" \
                        -d '{
                            "title": "Jenkins Build ${BUILD_NUMBER}",
                            "text": "Build status: ${buildStatus}",
                            "alert_type": "${buildStatus}",
                            "tags": ["service:your-service", "env:production"]
                        }'
                    """
                }
            }
        }

        stage('Run Java Application') {
            steps {
                sh 'java -Dcom.sun.management.jmxremote \
                    -Dcom.sun.management.jmxremote.port=9010 \
                    -Dcom.sun.management.jmxremote.local.only=false \
                    -Dcom.sun.management.jmxremote.authenticate=false \
                    -Dcom.sun.management.jmxremote.ssl=false \
                    -jar target/NoteKipzler-1.0-SNAPSHOT.jar'
            }
        }
    }

    post {
        success {
            echo 'Build completed successfully!'
        }
        failure {
            echo 'Build failed!'
        }
    }
}
