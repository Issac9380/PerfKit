@echo off
chcp 65001 >nul

echo ========================================
echo   研发作业平台 - 状态检查
echo ========================================
echo.

echo 检查后端端口 8080 ...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080 " ^| findstr "LISTENING"') do (
    echo   [后端] 运行中 - PID: %%a
    goto :check_frontend
)
echo   [后端] 未运行

:check_frontend
echo.
echo 检查前端端口 3000 ...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":3000 " ^| findstr "LISTENING"') do (
    echo   [前端] 运行中 - PID: %%a
    goto :end
)
echo   [前端] 未运行

:end
echo.
echo ========================================
echo   后端: http://localhost:8080
echo   前端: http://localhost:3000
echo ========================================
pause >nul
