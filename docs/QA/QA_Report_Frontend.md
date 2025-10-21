# QA Report – Frontend (Sprint 0)

## 🔍 Objectiu
Verificar la **configuració correcta del marc de proves** (**Vitest** + **React Testing Library**) i assegurar-se que els components interactius principals es comportin correctament segons els fluxos d'usuari esperats.

---

## 🧪 Proves executades

LoginForm.test.jsx (LoginForm): Comprova que els controladors s'activin en escriure i enviar. ✅

RegisterForm.test.jsx (RegisterForm): Comprova que onChange i onSubmit funcionin correctament. ✅

ProfileConfigurator.test.jsx (ProfileConfigurator): Comprova les preferències esportives seleccionades abans de continuar, la validació del camp i l'operació de desar. ✅

EventExplorer.test.jsx (EventExplorer): Verifica el flux de treball d'exploració d'esdeveniments: carrega la llista d'esdeveniments, aplica filtres i s'uneix a un esdeveniment. ✅

---

## ✅ Conclusió
L'entorn de proves del frontend està **operatiu**. Totes les proves actuals a nivell de component s'han aprovat correctament, verificant les **interaccions bàsiques dels usuaris** (formularis d'autenticació i funcionalitat d'exploració d'esdeveniments).
