FROM tomcat:9.0.50-jdk8

WORKDIR /usr/local/tomcat/
RUN cp -r webapps.dist/manager webapps/manager
COPY context.xml webapps/manager/META-INF/context.xml
COPY tomcat-users.xml conf/tomcat-users.xml