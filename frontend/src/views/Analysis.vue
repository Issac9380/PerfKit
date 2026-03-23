<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">性能分析</h1>
        <p class="page-subtitle">执行 jstack、jmap 等命令进行性能分析</p>
      </div>
    </div>

    <!-- 执行表单 -->
    <div class="card analyze-form">
      <el-form :model="form" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="集群">
              <el-select v-model="form.clusterId" placeholder="选择集群" style="width: 100%" @change="loadNamespaces">
                <el-option v-for="c in clusters" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="6">
            <el-form-item label="命名空间">
              <el-select v-model="form.namespace" placeholder="选择命名空间" style="width: 100%" @change="loadPods">
                <el-option v-for="ns in namespaces" :key="ns" :label="ns" :value="ns" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="6">
            <el-form-item label="Pod">
              <el-select v-model="form.podName" placeholder="选择 Pod" style="width: 100%" @change="loadContainers">
                <el-option v-for="p in pods" :key="p" :label="p" :value="p" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="6">
            <el-form-item label="容器">
              <el-select v-model="form.containerName" placeholder="选择容器" style="width: 100%">
                <el-option v-for="c in containers" :key="c" :label="c" :value="c" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="命令模板">
              <el-select v-model="form.templateId" placeholder="选择命令模板" style="width: 100%">
                <el-option
                  v-for="t in templates"
                  :key="t.id"
                  :label="`${t.name} - ${t.description}`"
                  :value="t.id"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="参数">
              <el-input v-model="form.params" placeholder="请输入参数，如: pid=12345" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item>
          <el-button type="primary" :loading="executing" @click="executeCommand">
            <el-icon><Cpu /></el-icon>
            执行分析
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 命令模板列表 -->
    <div class="card templates-section">
      <div class="section-header">
        <span class="section-title">可用命令模板</span>
      </div>
      <el-table :data="templates" size="default">
        <el-table-column prop="name" label="名称" width="180" />
        <el-table-column prop="commandType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.commandType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="template" label="模板" />
        <el-table-column prop="description" label="描述" width="200" />
      </el-table>
    </div>

    <!-- 执行结果 -->
    <div v-if="result" class="card result-section">
      <div class="result-header">
        <div class="result-icon">
          <el-icon><Finished /></el-icon>
        </div>
        <span class="result-title">执行结果</span>
      </div>

      <div class="command-info">
        <span class="label">执行的命令:</span>
        <code>{{ result.command }}</code>
      </div>

      <div class="result-content">
        <!-- 根据结果类型显示不同内容 -->
        <template v-if="result.resultType === 'table'">
          <div class="output-section">
            <div class="output-header">
              <span>表格视图</span>
              <el-tag size="small" type="info">表格</el-tag>
            </div>
            <pre class="output-content table-view">{{ result.output || '无输出' }}</pre>
          </div>
        </template>

        <template v-else-if="result.resultType === 'json'">
          <div class="output-section">
            <div class="output-header">
              <span>JSON 视图</span>
              <el-tag size="small" type="success">JSON</el-tag>
            </div>
            <pre class="output-content json-view">{{ formatJson(result.output) }}</pre>
          </div>
        </template>

        <template v-else-if="result.resultType === 'log'">
          <div class="output-section">
            <div class="output-header">
              <span>日志视图</span>
              <el-tag size="small" type="warning">日志</el-tag>
            </div>
            <pre class="output-content log-view">{{ result.output || '无输出' }}</pre>
          </div>
        </template>

        <template v-else-if="result.resultType === 'flamegraph'">
          <div class="output-section">
            <div class="output-header">
              <span>火焰图</span>
              <el-tag size="small" type="danger">火焰图</el-tag>
              <el-button size="small" @click="toggleFlamegraphView">
                {{ showGraphView ? '查看原始数据' : '查看图形' }}
              </el-button>
            </div>
            <div v-if="!showGraphView" class="flamegraph-tip">火焰图数据已生成</div>
            <!-- 图形化火焰图 -->
            <div v-if="showGraphView && flamegraphData" class="flamegraph-container" ref="flamegraphContainer"></div>
            <!-- 原始数据 -->
            <pre v-else class="output-content flamegraph-view">{{ result.output || '无输出' }}</pre>
          </div>
        </template>

        <template v-else-if="result.resultType === 'code'">
          <div class="output-section">
            <div class="output-header">
              <span>代码视图</span>
              <el-tag size="small" type="info">代码</el-tag>
            </div>
            <pre class="output-content code-view">{{ result.output || '无输出' }}</pre>
          </div>
        </template>

        <template v-else-if="result.resultType === 'file'">
          <div class="output-section">
            <div class="output-header">
              <span>文件内容</span>
              <el-tag size="small" type="primary">文件</el-tag>
            </div>
            <pre class="output-content file-view">{{ result.output || '无输出' }}</pre>
          </div>
        </template>

        <template v-else>
          <!-- 默认文本视图 -->
          <div v-if="result.success" class="output-section">
            <div class="output-header">
              <span>输出</span>
              <el-tag size="small">文本</el-tag>
            </div>
            <pre class="output-content">{{ result.output || '无输出' }}</pre>
          </div>
        </template>

        <div v-if="result.error" class="error-section">
          <div class="error-header">错误:</div>
          <pre class="error-content">{{ result.error }}</pre>
        </div>

        <div v-if="!result.success && !result.error" class="no-output">
          命令执行完成，无输出
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Cpu, Finished } from '@element-plus/icons-vue'
import * as d3 from 'd3'
import api from '@/api'

const executing = ref(false)
const clusters = ref([])
const namespaces = ref([])
const pods = ref([])
const containers = ref([])
const templates = ref([])
const result = ref(null)
const showGraphView = ref(false)
const flamegraphData = ref(null)
const flamegraphContainer = ref(null)

const form = reactive({
  clusterId: null,
  namespace: '',
  podName: '',
  containerName: '',
  templateId: null,
  params: ''
})

async function loadClusters() {
  try {
    const res = await api.get('/clusters')
    clusters.value = res.data.data || []
  } catch (error) {
    console.error(error)
  }
}

async function loadTemplates() {
  try {
    const res = await api.get('/analysis/templates')
    templates.value = res.data.data || []
  } catch (error) {
    console.error(error)
  }
}

async function loadNamespaces() {
  if (!form.clusterId) return
  try {
    const res = await api.get(`/clusters/${form.clusterId}/namespaces`)
    namespaces.value = res.data.data || []
  } catch (error) {
    console.error(error)
  }
}

async function loadPods() {
  if (!form.namespace || !form.clusterId) return
  try {
    const res = await api.get(`/clusters/${form.clusterId}/pods?namespace=${form.namespace}`)
    pods.value = res.data.data || []
  } catch (error) {
    console.error(error)
  }
}

async function loadContainers() {
  if (!form.podName || !form.namespace || !form.clusterId) return
  try {
    const res = await api.get(
      `/clusters/${form.clusterId}/pods/${form.podName}/containers?namespace=${form.namespace}`
    )
    containers.value = res.data.data || []
  } catch (error) {
    console.error(error)
  }
}

async function executeCommand() {
  if (!form.clusterId || !form.namespace || !form.podName || !form.containerName || !form.templateId) {
    ElMessage.warning('请填写完整的表单信息')
    return
  }

  executing.value = true
  result.value = null

  try {
    const params = {}
    if (form.params) {
      form.params.split(',').forEach(p => {
        const [key, value] = p.split('=')
        if (key && value) {
          params[key.trim()] = value.trim()
        }
      })
    }

    const res = await api.post('/analysis/execute', {
      clusterId: form.clusterId,
      namespace: form.namespace,
      podName: form.podName,
      containerName: form.containerName,
      templateId: form.templateId,
      params
    })

    result.value = res.data.data
    ElMessage.success('执行完成')
  } catch (error) {
    ElMessage.error('执行失败')
  } finally {
    executing.value = false
  }
}

// 格式化JSON输出
function formatJson(jsonStr) {
  try {
    const obj = JSON.parse(jsonStr)
    return JSON.stringify(obj, null, 2)
  } catch (e) {
    return jsonStr
  }
}

// 切换火焰图视图
function toggleFlamegraphView() {
  showGraphView.value = !showGraphView.value
  if (showGraphView.value) {
    nextTick(() => {
      parseAndRenderFlamegraph()
    })
  }
}

// 解析并渲染火焰图
function parseAndRenderFlamegraph() {
  if (!result.value?.output || !flamegraphContainer.value) return

  try {
    // 尝试解析 JSON 数据
    let data = null
    try {
      data = JSON.parse(result.value.output)
    } catch (e) {
      // 如果不是 JSON，尝试解析为火焰图格式
      data = parseFlamegraphText(result.value.output)
    }

    if (data) {
      flamegraphData.value = data
      renderFlamegraph(data)
    }
  } catch (e) {
    console.error('解析火焰图数据失败:', e)
  }
}

// 解析文本格式的火焰图数据
function parseFlamegraphText(text) {
  const lines = text.trim().split('\n')
  const root = { name: 'root', value: 0, children: [] }

  // 处理 speedscope 格式
  if (text.includes('"frames"') || text.includes('"events"')) {
    try {
      const json = JSON.parse(text)
      if (json.events) {
        return convertSpeedscopeToHierarchy(json)
      }
    } catch (e) {}
  }

  // 处理简单格式: functionName;functionName;... count
  let currentStack = []
  for (const line of lines) {
    const match = line.match(/^(.+?)\s+(\d+)$/)
    if (match) {
      const stack = match[1].split(';').map(s => s.trim()).filter(s => s)
      const count = parseInt(match[2])

      // 找到共同的祖先
      let common = 0
      for (let i = 0; i < Math.min(currentStack.length, stack.length); i++) {
        if (currentStack[i] === stack[i]) {
          common = i + 1
        } else {
          break
        }
      }

      // 移除不再需要的层级
      currentStack = currentStack.slice(0, common)

      // 添加新的层级
      let node = root
      for (const name of currentStack) {
        let child = node.children.find(c => c.name === name)
        if (!child) {
          child = { name, value: 0, children: [] }
          node.children.push(child)
        }
        node = child
      }

      // 添加当前帧
      const frameName = stack[stack.length - 1]
      let frame = node.children.find(c => c.name === frameName)
      if (!frame) {
        frame = { name: frameName, value: 0, children: [] }
        node.children.push(frame)
      }
      frame.value += count
      currentStack = stack
    }
  }

  // 计算总计
  function calcTotal(node) {
    if (!node.children || node.children.length === 0) return node.value
    node.value = node.children.reduce((sum, c) => sum + calcTotal(c), 0)
    return node.value
  }
  calcTotal(root)

  return root
}

// 转换 speedscope 格式
function convertSpeedscopeToHierarchy(json) {
  if (!json.events || !json.events.length) return null

  const root = { name: 'total', value: 0, children: [] }

  for (const event of json.events) {
    if (event.type === 'complete' || event.type === 'OpenFrame') {
      const frame = json.frames[event.frame]
      if (frame && frame.name) {
        let node = root
        const names = Array.isArray(frame.name) ? frame.name : [frame.name]
        for (const name of names) {
          let child = node.children.find(c => c.name === name)
          if (!child) {
            child = { name, value: 0, children: [] }
            node.children.push(child)
          }
          node = child
        }
        if (event.type === 'complete') {
          node.value += event.value || 0
        }
      }
    }
  }

  return root
}

// 使用 D3 渲染火焰图
function renderFlamegraph(data) {
  const container = flamegraphContainer.value
  if (!container) return

  // 清空容器
  container.innerHTML = ''

  const width = container.clientWidth || 800
  const height = 400
  const margin = { top: 20, right: 30, bottom: 30, left: 60 }

  // 创建 SVG
  const svg = d3.select(container)
    .append('svg')
    .attr('width', width)
    .attr('height', height)

  // 创建分层布局
  const root = d3.hierarchy(data)
    .sum(d => d.value)
    .sort((a, b) => b.value - a.value)

  // 创建树形布局
  const treemap = d3.treemap()
    .size([width - margin.left - margin.right, height - margin.top - margin.bottom])
    .padding(1)
    .round(true)

  treemap(root)

  // 颜色比例
  const color = d3.scaleOrdinal(d3.schemeTableau10)

  // 创建分组
  const g = svg.append('g')
    .attr('transform', `translate(${margin.left},${margin.top})`)

  // 绘制矩形
  const nodes = g.selectAll('g')
    .data(root.leaves())
    .join('g')
    .attr('transform', d => `translate(${d.x0},${d.y0})`)

  nodes.append('rect')
    .attr('width', d => Math.max(0, d.x1 - d.x0))
    .attr('height', d => Math.max(0, d.y1 - d.y0))
    .attr('fill', (d, i) => color(d.data.name))
    .attr('opacity', 0.8)
    .attr('rx', 2)
    .attr('ry', 2)
    .on('mouseover', function(event, d) {
      d3.select(this).attr('opacity', 1)
      tooltip.style('display', 'block')
        .html(`<strong>${d.data.name}</strong><br/>耗时: ${d.value}ms`)
        .style('left', (event.pageX + 10) + 'px')
        .style('top', (event.pageY - 10) + 'px')
    })
    .on('mouseout', function() {
      d3.select(this).attr('opacity', 0.8)
      tooltip.style('display', 'none')
    })

  // 添加文本标签
  nodes.append('text')
    .attr('x', 3)
    .attr('y', 14)
    .text(d => {
      const width = d.x1 - d.x0
      if (width < 40) return ''
      const name = d.data.name
      return name.length > width / 7 ? name.substring(0, Math.floor(width / 7)) + '...' : name
    })
    .attr('font-size', '10px')
    .attr('fill', '#fff')
    .attr('pointer-events', 'none')

  // 添加提示框
  const tooltip = d3.select(container)
    .append('div')
    .attr('class', 'flamegraph-tooltip')
    .style('display', 'none')
    .style('position', 'absolute')
    .style('background', '#1E293B')
    .style('color', '#fff')
    .style('padding', '8px 12px')
    .style('border-radius', '4px')
    .style('font-size', '12px')
    .style('pointer-events', 'none')
    .style('z-index', '1000')
    .style('box-shadow', '0 2px 8px rgba(0,0,0,0.3)')

  // 添加标题
  svg.append('text')
    .attr('x', width / 2)
    .attr('y', 15)
    .attr('text-anchor', 'middle')
    .attr('font-size', '14px')
    .attr('font-weight', 'bold')
    .attr('fill', '#1E293B')
    .text(`火焰图 - 总耗时: ${root.value}ms`)
}

// 监听结果变化，自动渲染火焰图
watch(result, (newResult) => {
  if (newResult?.resultType === 'flamegraph') {
    showGraphView.value = true
    nextTick(() => {
      parseAndRenderFlamegraph()
    })
  }
})

onMounted(() => {
  loadClusters()
  loadTemplates()
})
</script>

<style scoped>
.analyze-form {
  margin-bottom: 24px;
}

.templates-section {
  margin-bottom: 24px;
}

.section-header {
  margin-bottom: 16px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1E293B;
}

.result-section {
  padding: 24px;
}

.result-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.result-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #2563EB 0%, #3B82F6 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #FFFFFF;
}

.result-title {
  font-size: 16px;
  font-weight: 600;
}

.command-info {
  margin-bottom: 16px;
  padding: 14px;
  background: #F8FAFC;
  border-radius: 10px;
}

.command-info .label {
  color: #94A3B8;
  margin-right: 8px;
}

.command-info code {
  font-family: 'JetBrains Mono', monospace;
  color: #2563EB;
}

.output-section, .error-section {
  margin-bottom: 16px;
}

.output-header, .error-header {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
}

.output-header {
  color: #10B981;
  display: flex;
  align-items: center;
  gap: 12px;
}

.output-header .el-tag {
  margin-left: auto;
}

.error-header {
  color: #EF4444;
}

.output-content, .error-content {
  font-family: 'JetBrains Mono', monospace;
  font-size: 12px;
  padding: 16px;
  border-radius: 12px;
  overflow-x: auto;
}

.output-content {
  background: #F8FAFC;
  color: #64748B;
}

.error-content {
  background: #FEE2E2;
  color: #EF4444;
}

.no-output {
  color: #94A3B8;
  font-style: italic;
}

/* 不同结果类型的样式 */
.output-content.table-view {
  background: #F8FAFC;
  color: #1E293B;
  white-space: pre;
}

.output-content.json-view {
  background: #1E293B;
  color: #10B981;
}

.output-content.log-view {
  background: #1E293B;
  color: #22C55E;
  line-height: 1.6;
}

.output-content.code-view {
  background: #F1F5F9;
  color: #7C3AED;
}

.output-content.file-view {
  background: #F8FAFC;
  color: #64748B;
}

.output-content.flamegraph-view {
  background: #1E293B;
  color: #F59E0B;
}

.flamegraph-tip {
  padding: 12px;
  background: #FEF3C7;
  color: #D97706;
  border-radius: 8px;
  margin-bottom: 12px;
  font-size: 14px;
}

.flamegraph-container {
  width: 100%;
  min-height: 400px;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
}
</style>
