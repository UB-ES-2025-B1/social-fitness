# Social Fitness Frontend (React + Vite)

Frontend for the Social Fitness project, built with React and Vite. It includes hot module replacement (HMR) and ESLint.

Plugins
- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Babel](https://babeljs.io/) (or [oxc](https://oxc.rs) when used in [rolldown-vite](https://vite.dev/guide/rolldown)) for Fast Refresh.
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/) for Fast Refresh.

## React Compiler

The React Compiler is disabled in this template. To enable it, see the official guide: [installation docs](https://react.dev/learn/react-compiler/installation).

## ESLint and TypeScript

For a TypeScript setup with type-aware lint rules, see the [React + TS template](https://github.com/vitejs/vite/tree/main/packages/create-vite/template-react-ts) and [`typescript-eslint`](https://typescript-eslint.io).

## Project scope

This folder contains the frontend (client) for Social Fitness. It includes the UI and client-side code. The backend (authentication, data, APIs) is provided by a separate service.

How to run locally:

1. Install dependencies:

```bash
cd frontend
npm install
```

2. Start the dev server:

```bash
npm run dev
```

The app is served by Vite (usually at http://localhost:5173). For production, build with `npm run build` and deploy the `dist/` folder to a static host or a CDN.
