# social-fitness

### 1. Crear Backend amb Spring Boot

```powershell
# Navegar a la carpeta del projecte
cd C:\ruta\a\tu\proyecto\social-fitness

# Descarregar i crear el backend
& curl.exe "https://start.spring.io/starter.zip" `
  -d dependencies=web,data-jpa,postgresql,security,validation `
  -d type=maven-project `
  -d language=java `
  -d bootVersion=3.4.1 `
  -d baseDir=backend `
  -d groupId=com.example `
  -d artifactId=backend `
  -d name=backend `
  -d packageName=com.example.backend `
  -d javaVersion=17 `
  -o backend.zip
 

# Verificar y descomprimir la carpeta backend.zip
Get-ChildItem .\backend.zip
Expand-Archive -Path .\backend.zip -DestinationPath . -Force
Remove-Item .\backend.zip


#Arranca el backend
cd .\backend
.\mvnw.cmd -v       # deberia detectar una versió de Java compatible
.\mvnw.cmd spring-boot:run
```

## 2. Base de Dades PostgreSQL

```powershell
# 1. Assegurar-se d'estar a l'arrel del projecte


# 2. Crear directori per a PostgreSQL
New-Item -ItemType Directory -Path ".\PostgreSQL" 

# 3. Descarregar PostgreSQL
$url = "https://get.enterprisedb.com/postgresql/postgresql-16.4-1-windows-x64-binaries.zip"
$output = ".\postgresql\postgresql.zip"
Invoke-WebRequest -Uri $url -OutFile $output

# 3.1 # Comprobar que la descàrrega és correcta
Test-Path .\postgresql\postgresql.zip
    # Ver tamaño aproximado (debería ser ~320 MB)
"{0:N1} MB" -f ((Get-Item .\postgresql\postgresql.zip).Length / 1MB)

# Ens situem al directori potgresql
cd postgresql

#descomprimir postgresql.zip
Expand-Archive -Path .\postgresql.zip -DestinationPath . -Force
Remove-Item .\postgresql.zip


 # Guarda las rutas en variables para no tener que escribirlas a mano
$PgRoot= ".\pgsql"
$PgBin  = "$PgRoot\bin"

# 4. Inicializa la base de datos (te pedirá establecer una contraseña para el usuario 'postgres')
& "$PgBin\initdb.exe" -D "$PgRoot\data" -U postgres -W -A scram-sha-256 -E UTF8


# 5. Iniciar el servidor
& "$PgBin\pg_ctl.exe" -D "$PgRoot\data" -l "$PgRoot\postgres.log" start
    # deberá salir "waiting for server to start.... done    server started"

# 6. Crear la base de dades
& "$PgBin\createdb.exe" -U postgres socialfitness #introduir la teva contrassenya

#comprovar l'estat de la base de dades
& "$PgBin\pg_ctl.exe" -D "$PgRoot\data" status
    #deberá salir: "pg_ctl: server is running (PID xxxx)"

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
.\postgresql\pgsql\bin\pg_ctl.exe -D .\postgresql\pgsql\data start

# comprobar (hay que introducir contraseña de nuevo)
.\postgresql\pgsql\bin\psql.exe -U postgres -d socialfitness -c "SELECT version();"
# si está correcto saldrá: 
# "PostgreSQL 16.4, compiled by Visual C++ build 1940, 64-bit"

# Iniciar el Backend
cd backend
.\mvnw.cmd spring-boot:run
```

## 5. Accedir

Obre el navegador a: **http://localhost:8080** 

Es visualitza un formulari per defecte de Spring Security.

## Comandes útils

```powershell
# Iniciar PostgreSQL  
.\postgresql\pgsql\bin\pg_ctl.exe -D .\postgresql\pgsql\data start

# Aturar PostgreSQL
.\postgresql\pgsql\bin\pg_ctl.exe -D .\postgresql\pgsql\data stop

# Veure estat de PostgreSQL
.\postgresql\pgsql\bin\pg_ctl.exe -D .\postgresql\pgsql\data status

# Accedir a la base de dades
.\postgresql\pgsql\bin\psql.exe -U postgres -d socialfitness
```