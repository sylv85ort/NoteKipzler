pipeline {
    agent any  // This defines that the pipeline can run on any available agent.

    environment {
        DATADOG_API_KEY = credentials('DATADOG_API_KEY')  // Use Jenkins credentials to securely store the Datadog API key.
        }

    stages {
        stage('Checkout Code') {
            steps {
                // Checkout the code from your Git repository
                git 'https://github.com/sylv85ort/NoteKipzler.git'  // Replace with your actual Git repository URL
            }
        }

        stage('Build with Maven') {
            steps {
                // Execute Maven clean package command
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
                // Install Datadog Agent and configure it
                sh '''
                    DD_API_KEY=${DATADOG_API_KEY} DD_SITE="us5.datadoghq.com" bash -c "$(curl -L https://s3.amazonaws.com/dd-agent/scripts/install_script.sh)"
                    echo "api_key: ${DATADOG_API_KEY}" | sudo tee /etc/datadog-agent/datadog.yaml
                    sudo systemctl restart datadog-agent
                '''
            }
        }

        stage('Post Build Task - Datadog Event') {
            steps {
                // Post build status to Datadog
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
                // Run the Java application with JMX options
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