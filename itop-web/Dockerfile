# Multi-stage build for iTop Vue 3 Frontend
# Stage 1: Build
FROM node:20-alpine AS builder

WORKDIR /build

# Optional proxy support: pass --build-arg HTTP_PROXY=http://host.docker.internal:7897 if needed
# npm automatically respects HTTP_PROXY/HTTPS_PROXY environment variables
ARG HTTP_PROXY=
ARG HTTPS_PROXY=
ENV HTTP_PROXY=${HTTP_PROXY} \
    HTTPS_PROXY=${HTTPS_PROXY}

# Copy package files
COPY itop-web/src/main/frontend/package*.json ./

# Install dependencies with cache mount (faster rebuilds)
RUN --mount=type=cache,target=/root/.npm \
    npm install --registry=https://registry.npmmirror.com

# Copy source code
COPY itop-web/src/main/frontend/ .

# Build application
RUN npm run build

# Stage 2: Runtime with Nginx
FROM nginx:alpine

LABEL maintainer="iTop Java Team"
LABEL version="1.0.0"
LABEL description="iTop Java CMDB Web Server"

# Remove default nginx config
RUN rm -rf /usr/share/nginx/html/*

# Copy built files from builder
COPY --from=builder /build/dist /usr/share/nginx/html

# Copy nginx configuration
COPY itop-web/nginx.conf /etc/nginx/nginx.conf

# Add healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget -q -O /dev/null http://127.0.0.1:80/health || exit 1

# Expose port
EXPOSE 80

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]