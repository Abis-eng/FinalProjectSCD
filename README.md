# E-Clinic Web Application

Maven-based hospital / e-clinic system using **Servlets**, **JSP**, **JDBC**, and **MySQL** (MVC architecture per SCD guidelines).

## Roles & Features

| Role | Features |
|------|----------|
| **Patient** | Register, login, book appointments (doctor or nurse), view records, see assigned doctor |
| **Doctor** | View/update appointments, manage patients, create medical records |
| **Nurse** | View/update assigned appointments |
| **Admin** | Manage users, assign doctors to patients, full appointment & record oversight |

## Project Structure

```
src/main/java/com/elcinic/
  model/         # Entities
  view/          # (JSP under webapp)
  controller/    # Servlets
  service/       # Business logic
  repository/    # JDBC data access
  utility/       # DB, validation, security
src/main/webapp/
  WEB-INF/web.xml
  WEB-INF/views/
  css/
sql/schema.sql
```

## Prerequisites

- JDK 17+
- Maven 3.9+
- XAMPP (MySQL running on port 3306)
- IntelliJ with **Smart Tomcat** (Tomcat 10+ for Jakarta EE)

## Database Setup (XAMPP)

1. Start **Apache** and **MySQL** in XAMPP.
2. Open phpMyAdmin or MySQL CLI and run:

```bash
mysql -u root < sql/schema.sql
```

3. Edit `src/main/resources/db.properties` if your MySQL password is not empty:

```properties
db.url=jdbc:mysql://localhost:3306/elcinic?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.username=root
db.password=
```

## Accounts

| Username | Password | Role | Notes |
|----------|----------|------|-------|
| admin | admin123 | Admin | Created automatically on first deploy only |

**Self-registration** (`/register`): patients can log in immediately; doctors and nurses stay **pending** until an admin approves them under **Verify Staff**.

## Build & Test

```bash
mvn clean package
mvn test
```

WAR output: `target/elcinic.war`

## Run with one click (Smart Tomcat — same as lab WebApp)

1. **XAMPP**: start **MySQL** only (database is created automatically on first run).
2. Open this project in **IntelliJ**.
3. Top toolbar: select **E-Clinic (Smart Tomcat)** → click **Run** (green play).
4. Browser opens (or go to):

   **http://localhost:8080/elcinic/login**

Tomcat path is pre-set to the same install as your lab project  
(`apache-tomcat-10.1.55`). Change it in **Run → Edit Configurations** if yours is elsewhere.

**No manual SQL import required** — `ElcinicDbListener` creates `elcinic`, schema, and the default admin on first deploy.

## Docker deployment (evaluation)

This is a **web application** (`packaging: war`). Docker uses **Tomcat 10 + JDK 17**, not an executable JAR.

| Evaluation item | Implementation |
|-----------------|----------------|
| Build artifact | `mvn clean package` → `target/elcinic.war` |
| Docker image | `Dockerfile` (Tomcat base) |
| Full stack | `docker-compose.yml` (MySQL + app) |

### Tomcat only (app container)

```bash
mvn clean package
docker build -t elcinic-web:latest .
docker run -p 8080:8080 elcinic-web:latest
```

> Requires MySQL (XAMPP or separate container).

### MySQL + Tomcat (recommended for Docker demo)

```bash
mvn clean package
docker compose up --build
```

Open **http://localhost:8080/login** — admin: `admin` / `admin123`

See [docs/DOCKER-EVALUATION.md](docs/DOCKER-EVALUATION.md) for JAR vs WAR notes (SCD rubric).

## CI/CD (GitHub Actions)

Repository: [Abis-eng/FinalProjectSCD](https://github.com/Abis-eng/FinalProjectSCD.git)

Workflow: `.github/workflows/ci.yml`

| Step | Action |
|------|--------|
| 1 | Checkout code |
| 2 | Set up Java 17 |
| 3 | `mvn clean package` |
| 4 | `mvn test` (JUnit) |
| 5 | Upload `elcinic.war` artifact |
| 6 | Docker build (on push) |

### Push to GitHub

```bash
git init
git add .
git commit -m "E-Clinic: servlets, MySQL, Docker, CI/CD"
git branch -M main
git remote add origin https://github.com/Abis-eng/FinalProjectSCD.git
git push -u origin main
```

After push, open **Actions** tab on GitHub to see the pipeline run.

## UML (submit separately)

Prepare: Use Case, Class, Sequence (recommended), Activity diagrams for your report.
