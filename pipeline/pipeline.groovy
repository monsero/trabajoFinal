pipeline {

    environment {
        BASE_GIT_URL = 'https://github.com/monsero'
        APP_REPO_URL = "${env.BASE_GIT_URL}/${nombre_repo}.git"
        TRAB_REPO_URL = "${env.BASE_GIT_URL}/trabajofinal.git"
        DOCKER_IMAGE = "sepulveda/${nombre_repo}"
        DEPLOY_FOLDER = "deploy/kubernete/${nombre_repo}"
    }

    agent any
    stages {
        stage("Checkout app-code") {
            steps {
            //se esta crando una carpeta....
               dir('app') {
                    git url:"${env.APP_REPO_URL}" , branch: "${version}"
                } 
            }
        }
         
         stage("Checkout deploy-code") {
            steps {
               dir('deploy') {
                    git url:"${env.TRAB_REPO_URL}" , branch: "master"
                } 
            }
        }
        
         stage("Build image") {
            steps {
                dir('app') {
                    script {
                        dockerImage = docker.build("${env.DOCKER_IMAGE}:${tag}")
                    }
                }
            }
        }
        
        stage("Push image") {
            steps {
                script {
                    docker.withRegistry('', 'monicadocker') {
                    dockerImage.push()
                    }
                }
            }
        }
        
        stage('Deploy') {
            steps{
                sh "sed -i 's:DOCKER_IMAGE:${env.DOCKER_IMAGE}:g' ${DEPLOY_FOLDER}/deployment.yaml"
                sh "sed -i 's:TAG:${tag}:g' ${DEPLOY_FOLDER}/deployment.yaml"
                
                step([$class: 'KubernetesEngineBuilder', 
                        projectId: "wired-effort-288603",
                        clusterName: " cluster-test",
                        zone: "us-central1-c",
                        manifestPattern: "${DEPLOY_FOLDER}/",
                        credentialsId: "My first project",
                        verifyDeployments: true])
            }
        }
    }
}