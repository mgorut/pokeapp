# syntax=docker/dockerfile:1
# ------------------------------------------------------------------
# Multi-stage build: bundle with Node 20 (Vite), serve static files
# with Nginx. Nginx also reverse-proxies /api to the backend container
# so the browser only ever talks to one origin (no CORS in prod).
# ------------------------------------------------------------------

# Build arg for Vite env vars (VITE_TOKEN_KEY from root .env)
ARG VITE_TOKEN_KEY

# ---------- Stage 1: build ----------
FROM node:20-alpine AS build
WORKDIR /app

# Cache npm deps first: copy manifests before sources.
COPY package.json package-lock.json ./
RUN npm install --no-audit --no-fund

# Build the production bundle (tsc -b runs as part of `npm run build`,
# so a type error fails the image build — intentional quality gate).
COPY . .
# Write .env for Vite build with VITE_TOKEN_KEY
RUN echo "VITE_TOKEN_KEY=${VITE_TOKEN_KEY}" > .env
RUN npm run build

# ---------- Stage 2: runtime ----------
FROM nginx:1.27-alpine
LABEL org.opencontainers.image.title="pokemanager-frontend"

# SPA routing config + /api proxy (see nginx.conf at repo path pokeui/nginx.conf).
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

# Nginx returns 200 for index.html if the SPA is served correctly.
HEALTHCHECK --interval=15s --timeout=5s --start-period=10s --retries=3 \
    CMD wget -qO- http://127.0.0.1/ >/dev/null 2>&1 || exit 1

CMD ["nginx", "-g", "daemon off;"]
