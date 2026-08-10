#!/bin/bash

# Quick start script for first-time deployment
set -e

echo "================================"
echo "iTop Java Quick Start"
echo "================================"
echo ""

# Check Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed!"
    echo "Please install Docker Desktop: https://www.docker.com/products/docker-desktop"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose is not installed!"
    exit 1
fi

echo "✅ Docker is installed"
echo ""

# Create .env
if [ ! -f .env ]; then
    echo "📝 Creating .env file..."
    cp .env.example .env
    echo "✅ .env file created"
else
    echo "✅ .env file already exists"
fi

echo ""
echo "🔨 Building Docker images..."
docker-compose build

echo ""
echo "🚀 Starting services..."
docker-compose up -d

echo ""
echo "⏳ Waiting for services to be ready..."
sleep 10

# Health check
echo "🔍 Checking service health..."
if curl -f http://localhost:8080/api/actuator/health &> /dev/null; then
    echo "✅ API is healthy"
else
    echo "⚠️  API is starting... (this may take 30-60 seconds)"
fi

echo ""
echo "================================"
echo "🎉 Deployment Complete!"
echo "================================"
echo ""
echo "📱 Access the application:"
echo "   Frontend:  http://localhost"
echo "   API:       http://localhost:8080/api"
echo "   Swagger:   http://localhost:8080/api/swagger-ui.html"
echo ""
echo "🔑 Default credentials:"
echo "   Username: admin"
echo "   Password: admin123"
echo ""
echo "📊 View logs:"
echo "   ./deploy.sh logs"
echo ""
echo "🛑 Stop services:"
echo "   ./deploy.sh stop"
echo ""
echo "================================"