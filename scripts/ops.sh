#!/bin/bash
# ========================================
#   研发作业平台 - 管理脚本
# ========================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

ACTION=${1:-help}

case "$ACTION" in
    start)
        echo "========================================"
        echo "   启动前后端服务"
        echo "========================================"
        echo ""

        echo "[1/2] 启动后端..."
        cd "$SCRIPT_DIR/backend"
        PID=$(lsof -ti:8080 2>/dev/null)
        if [ -n "$PID" ]; then
            echo "端口 8080 已被占用，PID: $PID"
            kill -9 $PID 2>/dev/null
            sleep 2
        fi
        nohup mvn spring-boot:run > logs/startup.log 2>&1 &
        echo "后端启动成功！"

        cd "$SCRIPT_DIR"
        echo ""
        echo "[2/2] 启动前端..."
        cd "$SCRIPT_DIR/frontend"
        PID=$(lsof -ti:3000 2>/dev/null)
        if [ -n "$PID" ]; then
            echo "端口 3000 已被占用，PID: $PID"
            kill -9 $PID 2>/dev/null
            sleep 2
        fi
        nohup npm run dev > ../logs/frontend.log 2>&1 &
        echo "前端启动成功！"

        echo ""
        echo "启动成功！"
        echo "后端: http://localhost:8080"
        echo "前端: http://localhost:3000"
        ;;

    stop)
        echo "========================================"
        echo "   停止前后端服务"
        echo "========================================"
        echo ""

        echo "[1/2] 停止后端..."
        PID=$(lsof -ti:8080 2>/dev/null)
        if [ -n "$PID" ]; then
            echo "终止后端进程 PID: $PID"
            kill -9 $PID
            echo "已停止"
        else
            echo "后端未运行"
        fi

        echo ""
        echo "[2/2] 停止前端..."
        PID=$(lsof -ti:3000 2>/dev/null)
        if [ -n "$PID" ]; then
            echo "终止前端进程 PID: $PID"
            kill -9 $PID
            echo "已停止"
        else
            echo "前端未运行"
        fi
        ;;

    restart)
        echo "========================================"
        echo "   重启前后端服务"
        echo "========================================"
        echo ""

        echo "[1/3] 停止现有进程..."
        PID=$(lsof -ti:8080 2>/dev/null)
        [ -n "$PID" ] && kill -9 $PID
        PID=$(lsof -ti:3000 2>/dev/null)
        [ -n "$PID" ] && kill -9 $PID

        echo "等待端口释放..."
        sleep 3

        echo ""
        echo "[2/3] 启动后端..."
        cd "$SCRIPT_DIR/backend"
        nohup mvn spring-boot:run > logs/startup.log 2>&1 &

        cd "$SCRIPT_DIR"
        echo ""
        echo "[3/3] 启动前端..."
        cd "$SCRIPT_DIR/frontend"
        nohup npm run dev > ../logs/frontend.log 2>&1 &

        echo ""
        echo "重启成功！"
        echo "后端: http://localhost:8080"
        echo "前端: http://localhost:3000"
        ;;

    status)
        echo "========================================"
        echo "   服务状态"
        echo "========================================"
        echo ""

        echo "检查后端端口 8080 ..."
        PID=$(lsof -ti:8080 2>/dev/null)
        if [ -n "$PID" ]; then
            echo "  [后端] 运行中 - PID: $PID"
        else
            echo "  [后端] 未运行"
        fi

        echo ""
        echo "检查前端端口 3000 ..."
        PID=$(lsof -ti:3000 2>/dev/null)
        if [ -n "$PID" ]; then
            echo "  [前端] 运行中 - PID: $PID"
        else
            echo "  [前端] 未运行"
        fi

        echo ""
        echo "后端: http://localhost:8080"
        echo "前端: http://localhost:3000"
        ;;

    help|*)
        echo "========================================"
        echo "   研发作业平台 - 管理脚本"
        echo "========================================"
        echo ""
        echo "用法: $0 [command]"
        echo ""
        echo "命令:"
        echo "  start    - 启动前后端服务"
        echo "  stop     - 停止前后端服务"
        echo "  restart  - 重启前后端服务"
        echo "  status   - 查看服务状态"
        echo ""
        echo "示例:"
        echo "  $0 start"
        echo "  $0 restart"
        echo "  $0 status"
        ;;
esac
