@echo off
REM iTop Java Deployment Script for Windows
REM Usage: deploy.bat [command]

setlocal enabledelayedexpansion

REM Colors (limited on Windows)
set "INFO=[INFO]"
set "SUCCESS=[SUCCESS]"
set "WARNING=[WARNING]"
set "ERROR=[ERROR]"

REM Check if Docker is installed
:check_docker
docker --version >nul 2>&1
if errorlevel 1 (
    echo %ERROR% Docker is not installed. Please install Docker Desktop first.
    exit /b 1
)

docker-compose --version >nul 2>&1
if errorlevel 1 (
    echo %ERROR% Docker Compose is not installed.
    exit /b 1
)

echo %SUCCESS% Docker and Docker Compose are installed.

REM Create .env file if not exists
if not exist .env (
    echo %INFO% Creating .env file from .env.example...
    copy .env.example .env >nul
    echo %SUCCESS% .env file created. Please modify it as needed.
) else (
    echo %INFO% .env file already exists.
)

REM Parse command
set COMMAND=%1

if "%COMMAND%"=="" goto help
if "%COMMAND%"=="build" goto build
if "%COMMAND%"=="start" goto start
if "%COMMAND%"=="stop" goto stop
if "%COMMAND%"=="restart" goto restart
if "%COMMAND%"=="status" goto status
if "%COMMAND%"=="logs" goto logs
if "%COMMAND%"=="clean" goto clean
if "%COMMAND%"=="backup-db" goto backup_db
if "%COMMAND%"=="restore-db" goto restore_db
if "%COMMAND%"=="help" goto help
goto unknown_command

:build
echo %INFO% Building Docker images...
docker-compose build --no-cache
echo %SUCCESS% Docker images built successfully.
goto end

:start
echo %INFO% Starting iTop Java services...
docker-compose up -d
echo %SUCCESS% Services started.
docker-compose ps
goto end

:stop
echo %INFO% Stopping iTop Java services...
docker-compose down
echo %SUCCESS% Services stopped.
goto end

:restart
echo %INFO% Restarting iTop Java services...
call :stop
call :start
goto end

:status
echo %INFO% Service status:
docker-compose ps
goto end

:logs
if "%2"=="" (
    docker-compose logs -f
) else (
    docker-compose logs -f %2
)
goto end

:clean
echo %WARNING% This will remove all containers, volumes, and images.
set /p CONFIRM="Are you sure? (y/N): "
if /i "%CONFIRM%"=="y" (
    echo %INFO% Cleaning up...
    docker-compose down -v --rmi all
    echo %SUCCESS% Cleanup completed.
) else (
    echo %INFO% Cleanup cancelled.
)
goto end

:backup_db
echo %INFO% Backing up database...
for /f "tokens=2 delims==" %%I in ('wmic os get localdatetime /value') do set DATETIME=%%I
set BACKUP_FILE=itop_backup_%DATETIME:~0,8%_%DATETIME:~8,6%.sql
docker-compose exec -T postgres pg_dump -U itop itop > %BACKUP_FILE%
echo %SUCCESS% Database backup created: %BACKUP_FILE%
goto end

:restore_db
if "%2"=="" (
    echo %ERROR% Please specify backup file: deploy.bat restore-db ^<backup-file^>
    exit /b 1
)
if not exist "%2" (
    echo %ERROR% Backup file not found: %2
    exit /b 1
)
echo %INFO% Restoring database from %2...
docker-compose exec -T postgres psql -U itop itop < %2
echo %SUCCESS% Database restored.
goto end

:help
echo iTop Java Deployment Script
echo.
echo Usage: deploy.bat [command]
echo.
echo Commands:
echo   build        Build Docker images
echo   start        Start services
echo   stop         Stop services
echo   restart      Restart services
echo   status       Show service status
echo   logs [svc]   View logs ^(optional: specify service name^)
echo   clean        Remove all containers, volumes, and images
echo   backup-db    Backup database
echo   restore-db   Restore database from backup file
echo   help         Show this help message
echo.
echo Examples:
echo   deploy.bat build
echo   deploy.bat start
echo   deploy.bat logs itop-api
echo   deploy.bat backup-db
goto end

:unknown_command
echo %ERROR% Unknown command: %COMMAND%
goto help

:end
endlocal