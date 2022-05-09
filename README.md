# playground-jenkins-cicd

this is a cicd pipeline using jenkins, maven, sonarqube, nexus repository and tomcat.

## getting started

### prerequisites

### docker

1. install docker desktop
2. docker compose (to experiment when this pipeline is finished)
3. docker pull image (to expand on this)

### tomcat

1. build a custom tomcat image using the dockerfile provided
2. `git clone` this repo and `cd` into the dockerfile folder (not the tomcat folder)
3. (optional) you may change the username and password in the tomcat-users.xml file before building the custom image
4. type in `"docker build tomcat -t "tomcat-custom"`
5. this will build a custom tomcat image with the neccessary configurations

start a tomcat container using
`docker run -d --name "tomcat" -p 8888:8080 tomcat-custom`

### maven war project (optional)

1. you may fidn or prepare your own maven war project
2. ensure it is uploaded to github
3. replace the github link in the `pipeline.groovy` script when configuring jenkins

### jenkins

start a jenkins container using
`docker run -d --name "jenkins" -p 50000:50000 8080:8080 jenkins/jenkins:lts`

access jenkins at `localhost:8080`

#### unlock jenkins

if using docker desktop
1. the secret can be found in the docker desktop
2. click on the jenkins container to view the log
3. find the secret and use it to unlock jenkins

#### plugins

1. install suggested plugins
2. install `maven integration` and `deploy to container` plugins

#### configuration

jdk
1. under global tool configuration
2. click "add jdk" and untick "install automatically"
3. enter the name, "java" for exmaple
4. enter `/opt/java/openjdk` for java_home

maven
1. under global tool configuration
2. click "add maven" and enter the name (use in script)
3. for exmaple, "maven-3.8.5"

tomcat credential
1. under manage credentials
2. create a new credential in the default store scoped
3. enter the username, password and id
4. username and password must be the same as the one in `tomcat-users.xml`
5. id is for easy reference and is use in script

#### create pipeline

create a pipeline project and use the `pipeline.groovy` as the script

### sonarqube

default username and password for sonarqube container
username `admin`
password `admin`

token
1. generate a user token by clicking `administration > security > users > token icon`
2. enter a name and click generate to generate the token (to use in jenkins)

webhooks
1. create a webhooks by clicking `administration > configuration > webhooks > create`
2. enter a name
3. enter the tomcat (or jenkins) url and append `sonarqube-webhook` behind the link
4. for example, http://192.168.1.155:8888/sonarqube-webhook

### nexus repository

default username and password for nexus repository container
username `admin`
password `/nexus-data/admin.password`

### relevant links
https://www.linkedin.com/pulse/how-deploy-war-file-from-jenkins-tomcat-using-docker-de-avila-julio

curl --upload-file target\debug.war "http://tomcat:tomcat@localhost:8088/manager/deploy?path=/debug&update=true"
curl -T "myapp.war" "http://manager:manager@localhost:8080/manager/text/deploy?path=/myapp&update=true"
curl "http://admin:11@localhost:8888/manager/text/deploy?path=/petclinic&war=file:/var/jenkins_home/workspace/tomcat-free/target/spring-petclinic-2.6.0-SNAPSHOT.war"