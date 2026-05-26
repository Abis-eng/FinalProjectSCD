# E-Clinic Web App — WAR deployed on Apache Tomcat 10 (Jakarta EE)
# Evaluation note: Servlet/JSP projects use WAR + Tomcat (not executable JAR).
# Build first: mvn clean package
# Run:      docker build -t elcinic-web:latest .
#           docker run -p 8080:8080 elcinic-web:latest
# Or use:   docker compose up --build

FROM tomcat:10.1-jdk17-temurin

LABEL org.opencontainers.image.title="E-Clinic"
LABEL org.opencontainers.image.description="E-Clinic hospital management WAR on Tomcat 10"

RUN rm -rf /usr/local/tomcat/webapps/*

COPY target/elcinic.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

CMD ["catalina.sh", "run"]
