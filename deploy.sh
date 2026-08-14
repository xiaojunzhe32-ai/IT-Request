#!/bin/bash

# iTop Java Deployment Script
# Usage: ./deploy.sh [command]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Log function
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Docker is installed
check_docker() {
    if ! command -v docker &> /dev/null; then
        log_error "Docker is not installed. Please install Docker first."
        exit 1
    fi

    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose is not installed. Please install Docker Compose first."
        exit 1
    fi

    log_success "Docker and Docker Compose are installed."
}

# Create .env file if not exists
create_env_file() {
    if [ ! -f .env ]; then
        log_info "Creating .env file from .env.example..."
        cp .env.example .env
        log_success ".env file created. Please modify it as needed."
    else
        log_info ".env file already exists."
    fi
}

# Build images
build() {
    log_info "Building Docker images..."
    docker-compose build --no-cache
    log_success "Docker images built successfully."
}

# Start services
start() {
    log_info "Starting iTop Java services..."
    docker-compose up -d
    log_success "Services started."
    show_status
}

# Stop services
stop() {
    log_info "Stopping iTop Java services..."
    docker-compose down
    log_success "Services stopped."
}

# Restart services
restart() {
    log_info "Restarting iTop Java services..."
    stop
    start
}

# Show status
show_status() {
    log_info "Service status:"
    docker-compose ps
}

# View logs
logs() {
    if [ -z "$1" ]; then
        docker-compose logs -f
    else
        docker-compose logs -f "$1"
    fi
}

# Clean up
clean() {
    log_warning "This will remove all containers, volumes, and images."
    read -p "Are you sure? (y/N): " confirm
    if [ "$confirm" = "y" ] || [ "$confirm" = "Y" ]; then
        log_info "Cleaning up..."
        docker-compose down -v --rmi all
        log_success "Cleanup completed."
    else
        log_info "Cleanup cancelled."
    fi
}

# Backup database
backup_db() {
    log_info "Backing up database..."
    BACKUP_FILE="itop_backup_$(date +%Y%m%d_%H%M%S).sql"
    docker-compose exec -T postgres pg_dump -U itop itop > "$BACKUP_FILE"
    log_success "Database backup created: $BACKUP_FILE"
}

# Restore database
restore_db() {
    if [ -z "$1" ]; then
        log_error "Please specify backup file: ./deploy.sh restore-db <backup-file>"
        exit 1
    fi

    if [ ! -f "$1" ]; then
        log_error "Backup file not found: $1"
        exit 1
    fi

    log_info "Restoring database from $1..."
    docker-compose exec -T postgres psql -U itop itop < "$1"
    log_success "Database restored."
}

# Show help
show_help() {
    echo "iTop Java Deployment Script"
    echo ""
    echo "Usage: ./deploy.sh [command]"
    echo ""
    echo "Commands:"
    echo "  build        Build Docker images"
    echo "  start        Start services"
    echo "  stop         Stop services"
    echo "  restart      Restart services"
    echo "  status       Show service status"
    echo "  logs [svc]   View logs (optional: specify service name)"
    echo "  clean        Remove all containers, volumes, and images"
    echo "  backup-db    Backup database"
    echo "  restore-db   Restore database from backup file"
    echo "  help         Show this help message"
    echo ""
    echo "Examples:"
    echo "  ./deploy.sh build"
    echo "  ./deploy.sh start"
    echo "  ./deploy.sh logs itop-api"
    echo "  ./deploy.sh backup-db"
}

# Main script
check_docker
create_env_file

case "$1" in
    build)
        build
        ;;
    start)
        start
        ;;
    stop)
        stop
        ;;
    restart)
        restart
        ;;
    status)
        show_status
        ;;
    logs)
        logs "$2"
        ;;
    clean)
        clean
        ;;
    backup-db)
        backup_db
        ;;
    restore-db)
        restore_db "$2"
        ;;
    help|--help|-h|"")
        show_help
        ;;
    *)
        log_error "Unknown command: $1"
        show_help
        exit 1
        ;;
esac
