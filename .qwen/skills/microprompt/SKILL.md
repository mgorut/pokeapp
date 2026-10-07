---
name: microprompt 
description: Usar esta skill para definir prompt para realizar microaplicaciones. 
license: Complete terms in LICENSE.txt 
---

## Partes del microfrontend 

## Design guidelines (for `frontend-design` skill)
- Ligth background with dark text for readability
- Customer-facing: clean, modern storefront feel
- bold typography, clear CTAs Color palette: pick one accent color and stay consistent
- No images anywhere
- use category-colored icon placeholders (CSS only)
- Mobile-responsive layouts

## Arquitectura Base de datos
- Como base de datos elegiremos mongodb con el driver nativo.
- El mongodb esta instalado en el ordenador.

## Arquitectura Storage de pdf, videos, audios, etc.
- Aws s3 librerias usando Rustfs que esta en docker.
- Crear el bucket si no existe. 

## Arquitectura Mail
- Usar maithog que esta instalados en docker. 

## Arequitectura Frontend
- Usar nextjs con typescript.
- No usar ya el fichero `middleware.tsx`, sino usar `proxy` en su lugar.
- Usar un globalcontext para almacenar el estado global de la aplicacion, como et usuario autenticado, preferencias, etc. Evitar prop drilling.
- Hacer npm run build cuando se haya acabado la codificacion, no cada vez. 

## Repositorio en github:
- Hacer commit con mensajes claros y descriptivos.
- Organiza el codigo en carpetas logicas (components, pages, lib, etc.).
- Usa ramas para nuevas features o fixes, y haz merge a main solo cuando esten completas

# Coding rules
1. Read the Next.js dots in `node_modules/next/dist/docsr` before using any API
2. All DB access goes through `clib/db.ts` singleton - never create a new 'MongoClient' inline
3. All money values stored and computed in **cents** (integers) - format for display only at render time
4. API routes return `.{ error: string }` on failure with the appropriate HTTP status
5. No any types - use proper TypeScript interfaces in `libitypes.ts`
6. Server Components fetch data directly from MongoDB; Client Components call API routes
7. Use `frontend-design` skill for every new `page/component` - do not write plain unstyled HTML 

# Testing rules 
1. usa playwright para pruebas end-to-end de la aplicacion, cubriendo flujos criticos como registro, login, compra, etc. 
2. Usa Jest para pruebas unitarias de funciones criticas en `lib/` como procesamiento de pagos, validacion de datos, etc. 
3. Escribe pruebas antes de implementar nuevas features (TDD) para asegurar cobertura y diseno testable 
4. Configura CI pare ejecutar pruebas automaticamente.
 
## Environment variables 
``` 
# Database
MONGODB_URI=mongodb://localhost:27017
MONGODB_DB=ia4devs_db

# AWS S3 / Rustfs
AWS_USERNAME=rustfsadmin
AWS_PASSWORD=rustfsadmin
AWS_REGION=us-east-1
AWS_URL=http://localhost:9001
AWS_BUCKET=ia4devs-storage

# Email (Mailhog)
MAILHOG_HOST=localhost 
MAILHOG_PORT=1025

# Next.js
NODE_ENV=development
NEXT_PUBLIC_API_URL=http://localhost:3000
``` 

## Estructura de carpetas recomendada 
``` 
src/
├── app/                  # Next.js 13+ app router
│   ├── api/              # API routes
│   ├── auth/             # Route groups para auth
│   ├── dashboard/        # Route groups para dashboard
│   └── layout.tsx        # Root layout
├── components/           # React components reutilizables
│   ├── common/           # Botones, inputs, etc.
│   └── features/         # Componentes por feature
├── lib/                  # Lógica compartida
│   ├── db.ts             # MongoDB singleton
│   ├── types.ts          # TypeScript interfaces
│   ├── auth.ts           # Lógica de autenticación
│   └── s3.ts             # Configuración AWS S3
├── context/              # Global Context
│   └── AuthContext.tsx
└── styles/               # CSS global
    └── globals.css
```
