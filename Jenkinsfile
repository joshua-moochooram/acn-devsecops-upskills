pipeline{
    agent {label 'Jenkins-Agent'}
    tools {
        jdk 'JDK17'
        maven 'MAVEN3'
    }
    environment {
        SCANNER_HOME=tool 'sonar-scanner'
    }
    stages{
        stage ('clean Workspace'){
            steps{
                cleanWs()
            }
        }
        stage('Get Previous Successfully Build Number') {
          steps {
            script {

               def previousBuildNumber = currentBuild.previousSuccessfulBuild?.number
               if (previousBuildNumber == null) {
                   env.IMAGE_TAG_VERSION = 'latest'
               } else {
                   def imageTag = previousBuildNumber ?: 'latest'
                   env.IMAGE_TAG_VERSION = imageTag
                   echo "Previous build TAG set as env variable: ${env.IMAGE_TAG_VERSION}"
                   sh "sudo docker rmi joshuamoochooram/tasksmanager:${env.IMAGE_TAG_VERSION} -f"
               }
            }
          }
        }

        stage('Clean existing containers') {
            steps {
               script {
                  sh '''
                  if [ ! "$(docker ps -a -q -f name=tasksmanager)" ]; then
                      echo "Found running container ID"
                      if [ "$(docker ps -aq -f name=tasksmanager)" ]; then
                          echo "Found running container"
                          docker rm -f $(sudo docker ps -aq)
                      else
                        echo "No matching container found."
                      fi
                  fi'''
               }
            }
        }
        stage ('checkout scm') {
            steps {
                script {
                    git branch: 'main',
                    credentialsId: 'joshua-github-user-credentials',
                    url: 'https://github.com/joshua-moochooram/acn-devsecops-upskills.git'
                }
            }
        }
//         stage ('maven compile') {
//             steps {
//                 sh 'mvn clean compile'
//             }
//         }
//         stage ('maven Test') {
//             steps {
//                 sh 'mvn test'
//             }
//         }

//         stage ('maven clean verify') {
//             steps {
//                 sh 'mvn clean verify'
//             }
//         }

//         stage("Sonarqube Analysis "){
//             steps{
//                 withSonarQubeEnv('sonar-server') {
//                     sh ''' $SCANNER_HOME/bin/sonar-scanner -Dsonar.projectName=merlin-acn-upskills \
//                     -Dsonar.java.binaries=. \
//                     -Dsonar.projectKey=merlin-acn-upskills-key '''
//                 }
//             }
//         }
//         stage("Sonarqube Analysis "){
//             steps{
//                 withSonarQubeEnv('joshua-sonar-server') {
//                     sh ''' mvn sonar:sonar \
//                     -Dsonar.projectName=joshua-acn-upskills \
//                     -Dsonar.java.binaries=. \
//                     -Dsonar.projectKey=joshua-acn-upskills-key '''
//                 }
//             }
//         }
//
//         stage("quality gate"){
//             steps {
//                 script {
//                   waitForQualityGate abortPipeline: false, credentialsId: 'joshua-sonar-token'
//                 }
//            }
//         }
        stage ('Build Jar file'){
            steps{
                sh 'mvn clean install'
                sh 'mkdir -p src/main/resources/static/jacoco'
                sh 'cp -r target/site/jacoco/* src/main/resources/static/jacoco/'
            }
        }

//         stage('TRIVY FS SCAN') {
//            steps {
//                sh '''
//                     trivy fs --format table .
//                     trivy fs --format table --exit-code 1 --severity CRITICAL .
//                '''
//            }
//         }


//         stage("OWASP Dependency Check"){
//             steps{
//                 withCredentials([string(credentialsId: 'nvd-api-key', variable: 'NVD_API_KEY')]) {
//                     dependencyCheck additionalArguments: "--scan ./ --format XML --nvdApiKey ${NVD_API_KEY}", odcInstallation: 'DPD-Check'
//                     dependencyCheckPublisher pattern: '**/dependency-check-report.xml'
//                 }
//             }
//         }

        stage('Build and Push Docker Image') {
           environment {
             DOCKER_IMAGE = "joshuamoochooram/tasksmanager:${BUILD_NUMBER}"
             REGISTRY_CREDENTIALS = credentials('joshua-docker')
           }
           steps {
             script {
                sh "tree"
                "sudo docker images | grep joshuamoochooram/tasksmanager*"
                sh "sudo docker build -t ${DOCKER_IMAGE} ."
                def dockerImage = docker.image("${DOCKER_IMAGE}")
                 docker.withRegistry('https://index.docker.io/v1/', "joshua-docker") {
                     dockerImage.push()
                 }
             }
           }
        }
//         stage("TRIVY DOCKER IMAGE SCAN"){
//             steps{
//                 sh "trivy image joshuamoochooram/tasksmanager:${BUILD_NUMBER} --format table"
//                 //sh "trivy image joshuamoochooram/tasksmanager:${BUILD_NUMBER} --format table --exit-code 1 --severity CRITICAL"
//             }
//         }

//         stage ('Deploy to container'){
//             steps{
//                 sh """
//                     sudo docker ps -a --filter name=joshua-tasksmanager -q | xargs -r sudo docker stop
//                     sudo docker ps -a --filter name=joshua-tasksmanager -q | xargs -r sudo docker rm -f
//                     sudo docker images joshuamoochooram/tasksmanager -q | xargs -r sudo docker rmi -f
//                     sudo docker run -d --name joshua-tasksmanager -p 8089:8082 joshuamoochooram/tasksmanager:${BUILD_NUMBER}
//                 """
//             }
//         }
//

        stage('Update Deployment File') {
                environment {
                    GIT_REPO_NAME = "acn-devsecops-upskills"
                    GIT_USER_NAME = "joshua-moochooram"
                }
                steps {
                    withCredentials([string(credentialsId: 'joshua-gitops-user-secret-text', variable: 'GITHUB_TOKEN')]) {
                        sh '''
                            git config user.email "joshua.moochooram@accenture.com"
                            git config user.name "joshua.moochooram"
                            BUILD_NUMBER=${BUILD_NUMBER}
                            sed -i "s/36/${BUILD_NUMBER}/g" k8s/manifests/deployment.yml
                            git add k8s/manifests/deployment.yml
                            git add .
                            git commit -m "Update deployment image to version ${BUILD_NUMBER}"
                            git push https://${GITHUB_TOKEN}@github.com/${GIT_USER_NAME}/${GIT_REPO_NAME} HEAD:main
                        '''
                    }
                }
        }

        stage('Run Selenium Tests') {
            steps {
                sh 'sleep 30'
                sh 'mvn -Dtest=TaskManagerSeleniumTests test'
            }
        }
    }

//     post {
//         always {
//             //archiveArtifacts artifacts: 'trivyfs.txt', fingerprint: true
//         }
//
//         failure {
//             //echo 'Build failed due to HIGH or CRITICAL vulnerabilities found by Trivy.'
//         }
//     }
}