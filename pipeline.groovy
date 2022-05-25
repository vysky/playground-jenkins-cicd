pipeline {
    agent any
    triggers {
        // check scm every minute
        pollSCM '* * * * *'
    }
    tools {
        // must follow the name set in jenkins global tool config
        maven 'maven-3.8.5'
    }
    stages {
        stage("Git") {
            steps {
                // git will not work if want poll scm, must use checkout instead
                // https://stackoverflow.com/questions/52151969/git-poll-setup-for-jenkins-groovy-scripted-pipeline
                checkout([$class: 'GitSCM', branches: [[name: 'main']], extensions: [], userRemoteConfigs: [[credentialsId: 'github', url: 'https://github.com/vysky/sample-maven-war']]])
            }
        }
        stage("Build and Scan") {
            steps {
                // must follow the sonarqube server name set in jenkins configuration system
                withSonarQubeEnv('sonarqube') {
                    sh 'mvn clean install sonar:sonar'
                }
            }
        }
        stage("Quality Gate") {
            steps {
                // must create a webhook in sonarqube server for this step to work
                timeout(time: 1, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        stage("Deploy") {
            steps {
                // use host ip as the url, do not use localhost (will not work)
                deploy adapters: [tomcat9(credentialsId: 'tomcat9', path: '', url: 'http://192.168.1.151:8888/')], contextPath: 'sample-maven-war', war: '**/*.war'
            }
        }
        stage("Upload") {
            steps {
                nexusPublisher nexusInstanceId: 'nexus', nexusRepositoryId: 'jenkins-maven-repo', packages: [[$class: 'MavenPackage', mavenAssetList: [[classifier: '', extension: '', filePath: 'target/simple-maven-war.war']], mavenCoordinate: [artifactId: 'simple-maven-war', groupId: 'com.sample', packaging: 'war', version: '1.0']]]
            }
        }
    }
}