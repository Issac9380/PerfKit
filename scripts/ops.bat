@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

set "SCRIPT_DIR=%~dp0"
set "ACTION=%~1"
if "%ACTION%"=="" set "ACTION=help"

cd /d "%SCRIPT_DIR%"
cd ..
set "PROJECT_DIR=%CD%"
cd /d "%SCRIPT_DIR%"

goto :%ACTION%

:help
echo ========================================
echo   PerfKit 研发作业平台 - 管理脚本
echo ========================================
echo.
echo 用法: ops.bat [command]
echo.
echo 命令:
echo   start    - 启动前后端服务
echo   stop     - 停止前后端服务
echo   restart  - 重启前后端服务
echo   status   - 查看服务状态
echo.
echo 示例:
echo   ops.bat start
echo   ops.bat restart
echo   ops.bat status
echo.
goto :end

:start
echo ========================================
echo   启动前后端服务
echo ========================================
echo.

echo [1/2] 启动后端...
cd /d "%PROJECT_DIR%\backend"
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080 " ^| findstr "LISTENING"') do (
    echo 后端端口 8080 已被占用，PID: %%a
    taskkill /F /PID %%a >nul 2>&1
    timeout /t 2 /nobreak >nul
)
start "Backend" cmd /k "cd /d "%CD%" && mvn spring-boot:run"

cd /d "%SCRIPT_DIR%"
echo.
echo [2/2] 启动前端...
cd /d "%PROJECT_DIR%\frontend"
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":3000 " ^| findstr "LISTENING"') do (
    echo 前端端口 3000 已被占用，PID: %%a
    taskkill /F /PID %%a >nul 2>&1
    timeout /t 2 /nobreak >nul
)
start "Frontend" cmd /k "cd /d "%CD%" && npm run dev"

cd /d "%SCRIPT_DIR%"
echo.
echo 启动成功！
echo 后端: http://localhost:8080
echo 前端: http://localhost:3000
goto :end

:stop
echo ========================================
echo   停止前后端服务
echo ========================================
echo.

echo [1/2] 停止后端...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080 " ^| findstr "LISTENING"') do (
    echo 终止后端进程 PID: %%a
    taskkill /F /PID %%a >nul 2>&1
)

echo.
echo [2/2] 停止前端...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":3000 " ^| findstr "LISTENING"') do (
    echo 终止前端进程 PID: %%a
    taskkill /F /PID %%a >nul 2>&1
)

echo.
echo 停止完成！
goto :end

:restart
echo ========================================
echo   重启前后端服务
echo ========================================
echo.

echo [1/3] 停止现有进程...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080 " ^| findstr "LISTENING"') do (
    taskkill /F /PID %%a >nul 2>&1
)
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":3000 " ^| findstr "LISTENING"') do (
    taskkill /F /PID %%a >nul 2>&1
)

echo 等待端口释放...
timeout /t 3 /nobreak >nul

echo.
echo [2/3] 启动后端...
cd /d "%PROJECT_DIR%\backend"
start "Backend" cmd /k "cd /d "%CD%" && mvn spring-boot:run"

cd /d "%SCRIPT_DIR%"
echo.
echo [3/3] 启动前端...
cd /d "%PROJECT_DIR%\frontend"
start "Frontend" cmd /k "cd /d "%CD%" && npm run dev"

cd /d "%SCRIPT_DIR%"
echo.
echo 重启完成！
echo 后端: http://localhost:8080
echo 前端: http://localhost:3000
goto :end

:status
echo ========================================
echo   服务状态
echo ========================================
echo.

for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080 " ^| findstr "LISTENING"') do (
    echo [后端] 运行中 - PID: %%a
    goto :check_frontend
)
echo [后端] 未运行

:check_frontend
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":3000 " ^| findstr "LISTENING"') do (
    echo [前端] 运行中 - PID: %%a
    goto :end
)
echo [前端] 未运行

:end
echo.
echo 后端: http://localhost:8080
echo 前端: http://localhost:3000
endlocal
