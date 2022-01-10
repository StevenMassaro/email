pipeline {
    agent {
        label 'master'
    }

    tools {
        maven 'M3'
    }

    environment {
        pom = readMavenPom().getVersion()
    }

    stages {
        stage('Build') {
            steps {
                sh 'curl https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/sendMessage -d chat_id=${TELEGRAM_CHAT_ID} -d text="${JOB_BASE_NAME} - ${BUILD_NUMBER} started" || true'
                sh 'mvn clean install -P ui'
            }
        }
        stage('Docker') {
            steps {
                script {
                    image = docker.build("stevenmassaro/email:latest")
                    docker.withRegistry('', 'DockerHub') {
                        image.push()
                        image.push(pom)
                    }
                }
            }
        }
        stage('Results') {
            steps {
                junit '**/target/surefire-reports/TEST-*.xml'
                archiveArtifacts 'target/*.jar'
                sh 'curl https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/sendMessage -d chat_id=${TELEGRAM_CHAT_ID} -d text="${JOB_BASE_NAME} - ${BUILD_NUMBER} finished" || true'
            }
        }
        stage('GitHub mirror'){
            steps {
                script {
                    cleanWs()
                    def scmUrl = scm.getUserRemoteConfigs()[0].getUrl()

                    dir('gh') {
                        sh 'git config --global credential.helper cache'
                        sh "git config --global credential.helper 'cache --timeout=3600'"
                        git credentialsId: 'GitBucketcReadOnly', url: scmUrl

                        sh "git clone --bare ${scmUrl}"
                        dir('music.git') {
                            withCredentials([usernameColonPassword(credentialsId: 'GitHubStevenMassaro', variable: 'USERPASS')]) {
                                def mirrorUrl = "https://${USERPASS}@github.com/StevenMassaro/email.git"
                                sh "git push --mirror ${mirrorUrl}"
                            }
                        }
                    }
                }
            }
        }
    }
}
