pipeline {
    agent any
    parameters {
    }
    tools {
        // must follow the name set in jenkins global tool config
        maven 'maven-3.8.5'
    }
    stages {
        stage("Git") {
            steps {
                git branch: 'main', url: 'https://github.com/vysky/sample-maven-war.git'
            }
        }
        stage("Build and Scan") {
            steps {
                // must follow the name set in jenkins global tool config
                withSonarQubeEnv('sonarqube') {
                    sh 'mvn clean install sonar:sonar'
                }
            }
        }
        stage("Quality Gate") {
            steps {
                timeout(time: 1, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        stage("Deploy") {
            steps {
                // must install deploy to continer plugin
                // use host ip as the url, do not use localhost (will not work)
                // sh 'curl "http://admin:11@192.168.1.155:8888/manager/text/deploy?war=file:/var/jenkins_home/workspace/tomcat-free/target/spring-petclinic-2.6.0-SNAPSHOT.war"'
                deploy adapters: [tomcat9(credentialsId: 'tomcat9', path: '', url: 'http://192.168.1.151:8888/')], contextPath: 'sample-maven-war', war: '**/*.war'
            }
        }
    }
}