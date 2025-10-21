# QA Report – Backend (Sprint 0)

## 🔍 Objectiu
Validar el **comportament correcte de la configuració del servidor backend**, la connexió a la base de dades (PostgreSQL) i els REST endpoints existents.

## 🧪 Proves executades

BackendApplicationTests.java: Assegura que el context de Spring Boot es carregui i que la connexió a la base de dades sigui accessible. ✅

BackendSecuritySmokeTest.java: Comprova que la ruta arrel (/) retorni un estat no trobat (no hi ha mapping). ✅

AuthControllerTest.java: Valida el comportament dels endpoints d'autenticació d'usuari (/auth/register i /auth/login). ✅

## ✅ Conclusió
Proves de connexió a la base de dades superades satisfactoriament.
L'entorn de proves del backend està **operatiu**.
