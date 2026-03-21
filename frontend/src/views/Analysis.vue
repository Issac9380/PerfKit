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
        <div v-if="result.success" class="output-section">
          <div class="output-header">输出:</div>
          <pre class="output-content">{{ result.output || '无输出' }}</pre>
        </div>

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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Cpu, Finished } from '@element-plus/icons-vue'
import api from '@/api'

const executing = ref(false)
const clusters = ref([])
const namespaces = ref([])
const pods = ref([])
const containers = ref([])
const templates = ref([])
const result = ref(null)

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
</style>
