# playground-jenkins-cicd

this is a cicd pipeline using jenkins, maven, sonarqube, nexus repository and tomcat.

## getting started

### docker

1. install docker
2. docker compose (wip)
3. docker pull image (wip)

---

### tomcat

1. `git clone` this repo
2. (optional) you can change the **username** and **password** in the *tomcat-users.xml* file
3. `cd` into the dockerfile folder (not the tomcat folder)
4. enter `docker build tomcat -t "tomcat-custom"` to build a tomcat image with the neccessary configurations
5. start a tomcat container using `docker run -d --name "tomcat" -p 8888:8080 tomcat-custom`
6. access tomcat manager at `http://localhost:8888/manager`
7. access tomcat host-manager at `http://localhost:8888/host-manager`

---

### jenkins

1. start a jenkins container using `docker run -d --name "jenkins" -p 50000:50000 8080:8080 jenkins/jenkins:lts`
2. access jenkins at `http://localhost:8080`
3. unlock jenkins by either checking the docker log or `/var/jenkins_home/secrets/initialAdminPassword`
4. install the **suggested plugins**, then install `maven integration` and `deploy to container` plugins

#### add jdk config
1. go to *manage jenkins > global tool configuration*
2. click *add jdk* and untick *install automatically*
3. enter the name (for example, "java")
4. enter `/opt/java/openjdk` for java_home

#### add maven config
1. go to *manage jenkins > global tool configuration*
2. click *add maven* and enter the name (for exmaple, "maven-3.8.5", will be used in script)

#### add tomcat credential
1. go to *manage jenkins > manage credentials*
2. create a new credential in the default jenkins store scoped
3. enter the username, password and id
4. username and password must be the same as the one in *tomcat-users.xml*
5. id is for easy reference and is use in script

---

### sonarqube

1. start a sonarqube container using `docker run -d --name "sonarqube" -p 9000:9000 sonarqube:lts`
2. access sonarqube at `http://localhost:9000`
3. default username and password for sonarqube container
    username `admin`
    password `admin`

generate token
1. go to sonarqube, generate a user token by clicking *administration > security > users > token icon*
2. enter a name and click *generate* to generate the token (to use in jenkins)
3. go to jenkins, add the token as a secret text credential
4. go to *manage jenkins > configure system*, find the sonarqube servers section and enter all the required information

create webhook
1. go to sonarqube, create a webhooks by clicking *administration > configuration > webhooks > create*
2. enter a name but leave secret field empty
3. enter the jenkins url with ports and append `/sonarqube-webhook` behind the link
4. for example, http://192.168.1.100:8888/sonarqube-webhook

---

### nexus repository

1. start a nexus repo container using `docker run -d --name "nexus" -p 8081:8081 sonartype/nexus3:3.38.1`
2. access nexus repo at `http://localhost:8081`
3. default username and password for nexus repository container
    username `admin`
    password `/nexus-data/admin.password`

create repo
1. go to nexus repo, click *gear icon > repositories > create repository > maven 2 (hosted)*
2. enter the name (exmaple **jenkins-maven-repo**), version policy (select **mixed**), deployment policy (select **allow redeploy**) and click *create repository*

1. go to jenkins, click *manage jenkins > configure system*

---

#### create pipeline

create a pipeline project and use the `pipeline.groovy` as the script






### relevant links
https://www.linkedin.com/pulse/how-deploy-war-file-from-jenkins-tomcat-using-docker-de-avila-julio

curl --upload-file target\debug.war "http://tomcat:tomcat@localhost:8088/manager/deploy?path=/debug&update=true"
curl -T "myapp.war" "http://manager:manager@localhost:8080/manager/text/deploy?path=/myapp&update=true"
curl "http://admin:11@localhost:8888/manager/text/deploy?path=/petclinic&war=file:/var/jenkins_home/workspace/tomcat-free/target/spring-petclinic-2.6.0-SNAPSHOT.war"