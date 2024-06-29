pipeline {
    agent any
    tools {
            maven 'maven'
            jdk 'jdk8'
        }
    stages {
        stage('Build') {
            steps {
                sh 'mvn -B  clean package'
            }
        }
    }
}
