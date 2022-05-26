pipeline {
    agent any
    environment {
        // github
        GITHUB_REPO = 'https://github.com/vysky/sample-maven-war'
        GITHUB_BRANCH = 'main'
        // sonarqube
        SONAR_NAME = 'sonarqube'
        // tomcat
        TOMCAT_URL = 'http://192.168.1.151:8888/'
        TOMCAT_CONTEXT_PATH = 'sample-maven-war'
        // nexux
        NEXUS_INSTANCE_ID = 'nexus'
        NEXUS_REPO_ID = 'jenkins-maven-repo'
        NEXUS_FILE_PATH = 'target/simple-maven-war.war'
        NEXUS_ARTIFACT_ID = 'simple-maven-war'
        NEXUS_GROUP_ID = 'com.sample'
        NEXUS_VER = '1.0'
        // credentials
        CRED_GITHUB = credentials('github')
        CRED_TOMCAT = credentials('tomcat9')
    }
    tools {
        // must follow the name set in jenkins global tool config
        maven 'maven-3.8.5'
    }
    triggers {
        // check scm every minute
        pollSCM '* * * * *'
    }
    stages {
        stage("Git") {
            steps {
                // git will not work if want poll scm, must use checkout instead
                // https://stackoverflow.com/questions/52151969/git-poll-setup-for-jenkins-groovy-scripted-pipeline
                checkout([$class: 'GitSCM', branches: [[name: ${GITHUB_BRANCH}]], extensions: [], userRemoteConfigs: [[credentialsId: ${CRED_GITHUB}, url: ${GITHUB_REPO}]]])
            }
        }
        stage("Build and Scan") {
            steps {
                // must follow the sonarqube server name set in jenkins configuration system
                withSonarQubeEnv(${SONAR_NAME}) {
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
                deploy adapters: [tomcat9(credentialsId: ${CRED_TOMCAT}, path: '', url: ${TOMCAT_URL})], contextPath: ${TOMCAT_CONTEXT_PATH}, war: '**/*.war'
            }
        }
        stage("Upload") {
            steps {
                nexusPublisher nexusInstanceId: 'nexus', nexusRepositoryId: 'jenkins-maven-repo', packages: [[$class: 'MavenPackage', mavenAssetList: [[classifier: '', extension: '', filePath: 'target/simple-maven-war.war']], mavenCoordinate: [artifactId: 'simple-maven-war', groupId: 'com.sample', packaging: 'war', version: '1.0']]]
            }
        }
    }
}