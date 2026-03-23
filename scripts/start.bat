@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ========================================
echo   研发作业平台 - 启动脚本
echo ========================================
echo.

set "SCRIPT_DIR=%~dp0"
cd /d "%SCRIPT_DIR%"

echo [1/2] 启动后端服务...
cd /d "%SCRIPT_DIR%backend"

echo 检查端口 8080 是否被占用...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080 " ^| findstr "LISTENING"') do (
    echo 端口 8080 被占用，PID: %%a
    echo 正在终止进程...
    taskkill /F /PID %%a >nul 2>&1
    timeout /t 2 /nobreak >nul
)

start "Backend" cmd /k "cd /d "%CD%" && mvn spring-boot:run"

cd /d "%SCRIPT_DIR%"
echo.
echo [2/2] 启动前端服务...
cd /d "%SCRIPT_DIR%frontend"

start "Frontend" cmd /k "cd /d "%CD%" && npm run dev"

cd /d "%SCRIPT_DIR%"
echo.
echo ========================================
echo   启动完成！
echo   后端: http://localhost:8080
echo   前端: http://localhost:3000
echo ========================================
echo 按任意键关闭此窗口...
pause >nul
