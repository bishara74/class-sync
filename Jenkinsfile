pipeline {
    agent any

    environment {
        APP_NAME = 'classsync'
    }

    stages {
        // Stage 1: Checkout the source code and print build info
        stage('Checkout') {
            steps {
                checkout scm
                echo "Branch: ${env.BRANCH_NAME}"
                echo "Build Number: ${env.BUILD_NUMBER}"
            }
        }

        // Stage 2: Build the Spring Boot backend JAR using Maven in a Docker container
        stage('Build') {
            steps {
                sh '''
                    docker run --rm \
                        -v $(pwd)/backend:/app \
                        -w /app \
                        maven:3.9-eclipse-temurin-17 \
                        mvn clean package -DskipTests
                '''
            }
        }

        // Stage 3: Run unit tests against a real PostgreSQL database
        stage('Test') {
            steps {
                // Start a PostgreSQL container for tests
                sh '''
                    docker run -d --name classsync-test-db \
                        --network host \
                        -e POSTGRES_DB=classsync \
                        -e POSTGRES_USER=postgres \
                        -e POSTGRES_PASSWORD=password \
                        postgres:15
                '''

                // Wait for PostgreSQL to be ready
                sh 'sleep 5'

                // Run unit tests only (not Selenium E2E tests which need a running app + browser)
                sh '''
                    docker run --rm \
                        --network host \
                        -v $(pwd)/backend:/app \
                        -w /app \
                        -e SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/classsync \
                        -e SPRING_DATASOURCE_USERNAME=postgres \
                        -e SPRING_DATASOURCE_PASSWORD=password \
                        maven:3.9-eclipse-temurin-17 \
                        mvn test -pl . -Dtest="com.hodali.classsync.service.**"
                '''
            }
        }

        // Stage 4: Build and tag the Docker image for the backend
        stage('Build Docker Image') {
            steps {
                dir('backend') {
                    sh "docker build -t classsync-backend:${env.BUILD_NUMBER} ."
                    sh "docker tag classsync-backend:${env.BUILD_NUMBER} classsync-backend:latest"
                }
            }
        }
    }

    post {
        // Always clean up the test database container and prune dangling images
        always {
            sh 'docker stop classsync-test-db || true'
            sh 'docker rm classsync-test-db || true'
            sh 'docker image prune -f'
        }
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed. Check the logs for details.'
        }
    }
}
