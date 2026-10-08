# Copyright (c) 2026 Manuel Gorut. All Rights Reserved.
# This source code is licensed under the Restricted Use License found in the
# LICENSE.md file in the root directory of this source tree.

ARG VITE_TOKEN_KEY
FROM node:20-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm install --no-audit --no-fund
COPY . .
RUN echo "VITE_TOKEN_KEY=${VITE_TOKEN_KEY}" > .env
RUN npm run build
FROM nginx:1.27-alpine
LABEL org.opencontainers.image.title="pokemanager-frontend"
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80
HEALTHCHECK --interval=15s --timeout=5s --start-period=10s --retries=3 \
    CMD wget -qO- http://127.0.0.1/ >/dev/null 2>&1 || exit 1

CMD ["nginx", "-g", "daemon off;"]
