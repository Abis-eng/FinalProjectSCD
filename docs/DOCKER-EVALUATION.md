# Docker Deployment (SCD Evaluation)

## Web App vs Desktop App

| Type | Artifact | Docker base | This project |
|------|----------|-------------|--------------|
| **Desktop app** | `app.jar` | `FROM openjdk:17` + `java -jar` | Not applicable (no standalone JAR) |
| **Web app (this project)** | `elcinic.war` | Tomcat 10 + JDK 17 | **Implemented** |

This E-Clinic project is a **Servlet/JSP WAR** application. The evaluation’s JAR example applies to desktop/Spring Boot apps. Here we deploy the **WAR** on **Tomcat** (advanced web deployment).

### Evaluation-style JAR example (reference only)

```dockerfile
FROM openjdk:17
COPY target/app.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
```

### This project (WAR + Tomcat)

See root `Dockerfile`:

```dockerfile
FROM tomcat:10.1-jdk17-temurin
COPY target/elcinic.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
CMD ["catalina.sh", "run"]
```

## Build & run (Docker only)

```bash
mvn clean package
docker build -t elcinic-web:latest .
docker run -p 8080:8080 elcinic-web:latest
```

Requires MySQL reachable (use XAMPP locally or `docker compose` below).

## Full stack (MySQL + Tomcat)

```bash
mvn clean package
docker compose up --build
```

- App: http://localhost:8080/login  
- MySQL host port: `3307` (container internal: `mysql:3306`)  
- Default admin: `admin` / `admin123`

## CI/CD

GitHub Actions workflow: `.github/workflows/ci.yml`

Steps: checkout → Java 17 → `mvn clean package` → `mvn test` → upload `elcinic.war` → Docker build (on push).

Repository: https://github.com/Abis-eng/FinalProjectSCD.git
