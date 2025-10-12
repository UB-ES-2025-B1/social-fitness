# social-fitness

### 1. Crear Backend amb Spring Boot

```powershell
# Navegar a la carpeta del projecte
cd C:\ruta\a\tu\proyecto\social-fitness

# Descarregar i crear el backend
curl https://start.spring.io/starter.zip -d dependencies=web,data-jpa,postgresql,security,validation -d type=maven-project -d language=java -d bootVersion=3.4.1 -d baseDir=backend -d groupId=com.example -d artifactId=backend -d name=backend -d packageName=com.example.backend -d javaVersion=17 -o backend.zip

# Extreure
Expand-Archive -Path backend.zip -DestinationPath . -Force
Remove-Item backend.zip
```

## 2. Base de Dades PostgreSQL

```powershell
# 1. Assegurar-se d'estar a l'arrel del projecte
cd C:\ruta\a\tu\proyecto\social-fitness

# 2. Crear directori per a PostgreSQL
New-Item -ItemType Directory -Force -Path .\postgresql

# 3. Descarregar PostgreSQL
$url = "https://get.enterprisedb.com/postgresql/postgresql-16.4-1-windows-x64-binaries.zip"
$output = ".\postgresql\postgresql.zip"
Invoke-WebRequest -Uri $url -OutFile $output

# 4. Extreure el ZIP
Expand-Archive -Path $output -DestinationPath .\postgresql -Force

# 5. Inicialitzar la base de dades (posar contrasenya quan ho demani)
.\postgresql\pgsql\bin\initdb.exe -D .\postgresql\data -U postgres -W

# 6. Iniciar el servidor
.\postgresql\pgsql\bin\pg_ctl.exe -D .\postgresql\data start

# 7. Crear la base de dades
.\postgresql\pgsql\bin\psql.exe -U postgres -c "CREATE DATABASE socialfitness;"
```

## 3. Configurar application.properties

Editar `backend/src/main/resources/application.properties`:

```properties
spring.application.name=backend

# PostgreSQL Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/socialfitness
spring.datasource.username=postgres
spring.datasource.password=LA_TEVA_CONTRASENYA
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true
```

## 4. Executar l'aplicació

```powershell
# Iniciar PostgreSQL  
.\postgresql\pgsql\bin\pg_ctl.exe -D .\postgresql\data start

# Iniciar el Backend
cd backend
.\mvnw.cmd spring-boot:run
```

## 5. Accedir

Obre el navegador a: **http://localhost:8080**

## Comandes útils

```powershell
# Aturar PostgreSQL
.\postgresql\pgsql\bin\pg_ctl.exe -D .\postgresql\data stop

# Veure estat de PostgreSQL
.\postgresql\pgsql\bin\pg_ctl.exe -D .\postgresql\data status

# Accedir a la base de dades
.\postgresql\pgsql\bin\psql.exe -U postgres -d socialfitness
```