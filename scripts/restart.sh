#!/bin/bash
# ========================================
#   研发作业平台 - 重启脚本
# ========================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

echo "========================================"
echo "   重启前后端服务"
echo "========================================"
echo ""

echo "[1/3] 停止现有进程..."
PID=$(lsof -ti:8080 2>/dev/null)
if [ -n "$PID" ]; then
    echo "终止后端进程 PID: $PID"
    kill -9 $PID
fi

PID=$(lsof -ti:3000 2>/dev/null)
if [ -n "$PID" ]; then
    echo "终止前端进程 PID: $PID"
    kill -9 $PID
fi

echo "等待端口释放..."
sleep 3

echo ""
echo "[2/3] 启动后端服务..."
cd "$SCRIPT_DIR/backend"
nohup mvn spring-boot:run > logs/startup.log 2>&1 &
echo "后端启动成功！"

cd "$SCRIPT_DIR"
echo ""
echo "[3/3] 启动前端服务..."
cd "$SCRIPT_DIR/frontend"
nohup npm run dev > ../logs/frontend.log 2>&1 &
echo "前端启动成功！"

cd "$SCRIPT_DIR"
echo ""
echo "========================================"
echo "   重启完成！"
echo "   后端: http://localhost:8080"
echo "   前端: http://localhost:3000"
echo "========================================"
