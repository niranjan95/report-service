pipeline {
    agent any
    environment{
        PATH = "/opt/apache-maven-3.9.2/bin:$PATH"
        DOCKER_TAG = getDockerTag()
    }
    stages{
        stage("Maven Build"){
            steps{
                sh "mvn clean package"
            }
        }
        stage('Static Code Analysis') {
          environment {
            SONAR_URL = "http://44.201.199.69:9000"
          }
          steps {
            withCredentials([string(credentialsId: 'sonarqube', variable: 'SONAR_AUTH_TOKEN')]) {
            sh 'mvn sonar:sonar -Dsonar.login=$SONAR_AUTH_TOKEN -Dsonar.host.url=${SONAR_URL}'
            }
          }
        }

        stage('Build Docker Image'){
            steps{
                sh "docker build . -t nk95/validation-service:${DOCKER_TAG}"
            }
        }
        stage('Docker Hub Push'){
            steps{
                withCredentials([string(credentialsId: 'docker-hub', variable: 'dockerHubPwd')]) {
                    sh "docker login -u nk95 -p ${dockerHubPwd}"
                    sh "docker push nk95/validation-service:${DOCKER_TAG}"
                }
            }
        }
        stage('Deploy to k8s'){
            steps{
               sh "chmod +x changeTag.sh"
               sh "./changeTag.sh ${DOCKER_TAG}"
               script{
                   try{
                       sh "kubectl apply -f app-deployment.yaml"
                   }catch(error){
                       sh "kubectl create -f app-deployment.yaml"
                   }
                   try{
                       sh "kubectl apply -f service.yaml"
                   }catch(error){
                       sh "kubectl create -f service.yaml"
                   }
               }
            }
        }
    }
}

def getDockerTag(){
    def tag  = sh script: 'git rev-parse HEAD', returnStdout: true
    return tag
}
