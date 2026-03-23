#!/bin/bash
# ========================================
#   研发作业平台 - 启动脚本
# ========================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

echo "========================================"
echo "   启动前后端服务"
echo "========================================"
echo ""

echo "[1/2] 启动后端服务..."
cd "$SCRIPT_DIR/backend"

# 检查端口是否被占用
PID=$(lsof -ti:8080 2>/dev/null)
if [ -n "$PID" ]; then
    echo "端口 8080 被占用，PID: $PID"
    echo "正在终止进程..."
    kill -9 $PID 2>/dev/null
    sleep 2
fi

nohup mvn spring-boot:run > logs/startup.log 2>&1 &
echo "后端启动成功！"

cd "$SCRIPT_DIR"
echo ""
echo "[2/2] 启动前端服务..."
cd "$SCRIPT_DIR/frontend"

# 检查端口是否被占用
PID=$(lsof -ti:3000 2>/dev/null)
if [ -n "$PID" ]; then
    echo "端口 3000 被占用，PID: $PID"
    echo "正在终止进程..."
    kill -9 $PID 2>/dev/null
    sleep 2
fi

nohup npm run dev > ../logs/frontend.log 2>&1 &
echo "前端启动成功！"

cd "$SCRIPT_DIR"
echo ""
echo "========================================"
echo "   启动完成！"
echo "   后端: http://localhost:8080"
echo "   前端: http://localhost:3000"
echo "========================================"
