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
        CRED_GITHUB = 'github'
        CRED_TOMCAT = 'tomcat9'
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
                checkout([$class: 'GitSCM', branches: [[name: env.GITHUB_BRANCH]], extensions: [], userRemoteConfigs: [[credentialsId: env.CRED_GITHUB, url: env.GITHUB_REPO]]])
                echo CRED_GITHUB
            }
        }
        stage("Build and Scan") {
            steps {
                // must follow the sonarqube server name set in jenkins configuration system
                withSonarQubeEnv(env.SONAR_NAME) {
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
                deploy adapters: [tomcat9(credentialsId: env.CRED_TOMCAT, path: '', url: env.TOMCAT_URL)], contextPath: env.TOMCAT_CONTEXT_PATH, war: '**/*.war'
            }
        }
        stage("Upload") {
            steps {
                nexusPublisher nexusInstanceId: env.NEXUS_INSTANCE_ID, nexusRepositoryId: env.NEXUS_REPO_ID, packages: [[$class: 'MavenPackage', mavenAssetList: [[classifier: '', extension: '', filePath: env.NEXUS_FILE_PATH]], mavenCoordinate: [artifactId: env.NEXUS_ARTIFACT_ID, groupId: env.NEXUS_GROUP_ID, packaging: 'war', version: env.NEXUS_VER]]]
            }
        }
    }
}