// ClassSync CI/CD Pipeline — Automated via GitHub Webhook
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

        // Stage 5: Run Selenium E2E tests against the full stack
        stage('E2E Tests') {
            steps {
                // Start the full stack (PostgreSQL, Spring Boot, Angular)
                sh 'docker-compose up -d --build'

                // Wait for backend to be ready
                sh '''
                    echo "Waiting for backend to be ready..."
                    for i in $(seq 1 30); do
                        if curl -s http://localhost:8081/api/auth/login > /dev/null 2>&1; then
                            echo "Backend is ready!"
                            break
                        fi
                        echo "Attempt $i/30 — waiting 5s..."
                        sleep 5
                    done
                '''

                // Wait for frontend to be ready
                sh '''
                    echo "Waiting for frontend to be ready..."
                    for i in $(seq 1 20); do
                        if curl -s http://localhost:4200 > /dev/null 2>&1; then
                            echo "Frontend is ready!"
                            break
                        fi
                        echo "Attempt $i/20 — waiting 5s..."
                        sleep 5
                    done
                '''

                // Run Selenium E2E tests in a Maven container with Chrome installed
                sh '''
                    docker run --rm --network host \
                        -v $(pwd)/backend:/app \
                        -w /app \
                        -e SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/classsync \
                        -e SPRING_DATASOURCE_USERNAME=postgres \
                        -e SPRING_DATASOURCE_PASSWORD=password \
                        maven:3.9-eclipse-temurin-17 \
                        bash -c "
                            apt-get update && apt-get install -y wget gnupg2 unzip &&
                            wget -q -O - https://dl.google.com/linux/linux_signing_key.pub | apt-key add - &&
                            echo 'deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main' > /etc/apt/sources.list.d/google-chrome.list &&
                            apt-get update && apt-get install -y google-chrome-stable &&
                            mvn test -Dtest='com.hodali.classsync.e2e.**' -pl .
                        "
                '''
            }
            post {
                always {
                    sh 'docker-compose down -v || true'
                }
            }
        }
    }

    post {
        // Always clean up the test database container, compose stack, and prune dangling images
        always {
            sh 'docker stop classsync-test-db || true'
            sh 'docker rm classsync-test-db || true'
            sh 'docker-compose down -v || true'
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
