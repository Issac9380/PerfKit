@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ========================================
echo   研发作业平台 - 停止脚本
echo ========================================
echo.

echo [1/2] 停止后端服务...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080 " ^| findstr "LISTENING"') do (
    echo 找到后端进程 PID: %%a
    echo 正在终止...
    taskkill /F /PID %%a >nul 2>&1
    if !errorlevel! equ 0 (
        echo 后端进程已终止
    )
)

echo.
echo [2/2] 停止前端服务...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":3000 " ^| findstr "LISTENING"') do (
    echo 找到前端进程 PID: %%a
    echo 正在终止...
    taskkill /F /PID %%a >nul 2>&1
    if !errorlevel! equ 0 (
        echo 前端进程已终止
    )
)

echo.
echo ========================================
echo   停止完成！
echo ========================================
pause >nul
