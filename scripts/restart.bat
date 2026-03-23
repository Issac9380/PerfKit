@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ========================================
echo   研发作业平台 - 重启脚本
echo ========================================
echo.

set "SCRIPT_DIR=%~dp0"

echo [1/3] 停止现有进程...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080 " ^| findstr "LISTENING"') do (
    echo 终止后端进程 PID: %%a
    taskkill /F /PID %%a >nul 2>&1
)

for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":3000 " ^| findstr "LISTENING"') do (
    echo 终止前端进程 PID: %%a
    taskkill /F /PID %%a >nul 2>&1
)

echo 等待端口释放...
timeout /t 3 /nobreak >nul

echo.
echo [2/3] 启动后端服务...
cd /d "%SCRIPT_DIR%backend"
start "Backend" cmd /k "cd /d "%CD%" && mvn spring-boot:run"

cd /d "%SCRIPT_DIR%"
echo.
echo [3/3] 启动前端服务...
cd /d "%SCRIPT_DIR%frontend"
start "Frontend" cmd /k "cd /d "%CD%" && npm run dev"

cd /d "%SCRIPT_DIR%"
echo.
echo ========================================
echo   重启完成！
echo   后端: http://localhost:8080
echo   前端: http://localhost:3000
echo ========================================
pause >nul
