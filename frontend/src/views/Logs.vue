<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">日志分析</h1>
        <p class="page-subtitle">智能分析容器日志，结合 AI 大模型</p>
      </div>
    </div>

    <!-- 分析表单 -->
    <div class="card analyze-form">
      <el-form :model="form" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="集群">
              <el-select v-model="form.clusterId" placeholder="选择集群" style="width: 100%">
                <el-option
                  v-for="c in clusters"
                  :key="c.id"
                  :label="c.name"
                  :value="c.id"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="命名空间">
              <el-select v-model="form.namespace" placeholder="先选择集群" style="width: 100%">
                <el-option
                  v-for="ns in namespaces"
                  :key="ns"
                  :label="ns"
                  :value="ns"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="Pod">
              <el-select v-model="form.podName" placeholder="选择 Pod" style="width: 100%">
                <el-option
                  v-for="p in pods"
                  :key="p"
                  :label="p"
                  :value="p"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="容器">
              <el-select v-model="form.containerName" placeholder="选择容器" style="width: 100%">
                <el-option
                  v-for="c in containers"
                  :key="c"
                  :label="c"
                  :value="c"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="容器类型">
              <el-select v-model="form.containerType" placeholder="选择类型" style="width: 100%">
                <el-option label="Nginx" value="nginx" />
                <el-option label="JDK" value="jdk" />
                <el-option label="Tomcat" value="tomcat" />
                <el-option label="自定义" value="custom" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="AI 分析">
              <el-switch v-model="form.useAI" />
              <span class="ai-hint" v-if="form.useAI">使用 AI 大模型分析</span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item>
          <el-button type="primary" :loading="analyzing" @click="analyzeLog">
            <el-icon><Search /></el-icon>
            开始分析
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 分析结果 -->
    <div v-if="result" class="result-section">
      <!-- 匹配案例 -->
      <div v-if="result.matchedCase" class="card result-card">
        <div class="result-header">
          <div class="result-icon success">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <span class="result-title">匹配案例</span>
        </div>
        <h3>{{ result.matchedCase.title }}</h3>
        <div class="result-content">
          <p><strong>问题模式:</strong> {{ result.matchedCase.problemPattern }}</p>
          <p><strong>解决方案:</strong> {{ result.matchedCase.solution }}</p>
        </div>
      </div>

      <!-- AI 分析结果 -->
      <div v-if="result.aiAnalysis" class="card result-card">
        <div class="result-header">
          <div class="result-icon info">
            <el-icon><ChatDotRound /></el-icon>
          </div>
          <span class="result-title">AI 分析</span>
        </div>
        <div class="ai-content">
          <pre>{{ result.aiAnalysis }}</pre>
        </div>
      </div>

      <!-- 日志摘要 -->
      <div class="card result-card">
        <div class="result-header">
          <div class="result-icon">
            <el-icon><Document /></el-icon>
          </div>
          <span class="result-title">日志摘要</span>
        </div>
        <pre class="log-summary">{{ result.summary || '无摘要信息' }}</pre>
      </div>

      <!-- 原始日志 -->
      <div class="card result-card">
        <div class="result-header">
          <div class="result-icon">
            <el-icon><Document /></el-icon>
          </div>
          <span class="result-title">原始日志</span>
        </div>
        <div class="log-content">
          <pre>{{ result.logContent }}</pre>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, CircleCheck, ChatDotRound, Document } from '@element-plus/icons-vue'
import api from '@/api'

const analyzing = ref(false)
const clusters = ref([])
const namespaces = ref([])
const pods = ref([])
const containers = ref([])
const result = ref(null)

const form = reactive({
  clusterId: null,
  namespace: '',
  podName: '',
  containerName: '',
  containerType: 'jdk',
  useAI: false,
  aiModel: 'claude'
})

watch(() => form.clusterId, async (newVal) => {
  if (newVal) {
    try {
      const res = await api.get(`/clusters/${newVal}/namespaces`)
      namespaces.value = res.data.data || []
    } catch (error) {
      console.error(error)
    }
  }
})

watch(() => form.namespace, async (newVal) => {
  if (newVal && form.clusterId) {
    try {
      const res = await api.get(`/clusters/${form.clusterId}/pods?namespace=${newVal}`)
      pods.value = res.data.data || []
    } catch (error) {
      console.error(error)
    }
  }
})

watch(() => form.podName, async (newVal) => {
  if (newVal && form.namespace && form.clusterId) {
    try {
      const res = await api.get(
        `/clusters/${form.clusterId}/pods/${newVal}/containers?namespace=${form.namespace}`
      )
      containers.value = res.data.data || []
    } catch (error) {
      console.error(error)
    }
  }
})

async function analyzeLog() {
  if (!form.clusterId || !form.namespace || !form.podName || !form.containerName) {
    ElMessage.warning('请填写完整的表单信息')
    return
  }

  analyzing.value = true
  result.value = null

  try {
    const res = await api.post('/log/analyze', form)
    result.value = res.data.data
    ElMessage.success('分析完成')
  } catch (error) {
    ElMessage.error('分析失败')
  } finally {
    analyzing.value = false
  }
}

async function loadClusters() {
  try {
    const res = await api.get('/clusters')
    clusters.value = res.data.data || []
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  loadClusters()
})
</script>

<style scoped>
.analyze-form {
  margin-bottom: 24px;
}

.ai-hint {
  margin-left: 12px;
  color: #94A3B8;
  font-size: 13px;
}

.result-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.result-card {
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
  background: #F1F5F9;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.result-icon.success {
  background: #D1FAE5;
  color: #10B981;
}

.result-icon.info {
  background: #E0E7FF;
  color: #6366F1;
}

.result-title {
  font-size: 16px;
  font-weight: 600;
  color: #1E293B;
}

.result-card h3 {
  color: #2563EB;
  margin-bottom: 12px;
}

.result-content p {
  margin-bottom: 8px;
  color: #64748B;
  font-size: 14px;
}

.ai-content pre {
  font-family: 'JetBrains Mono', monospace;
  font-size: 13px;
  color: #64748B;
  white-space: pre-wrap;
  background: #F8FAFC;
  padding: 16px;
  border-radius: 12px;
}

.log-summary {
  font-family: 'JetBrains Mono', monospace;
  font-size: 13px;
  color: #64748B;
  white-space: pre-wrap;
}

.log-content {
  max-height: 400px;
  overflow: auto;
  background: #F8FAFC;
  border-radius: 12px;
  padding: 16px;
}

.log-content pre {
  font-family: 'JetBrains Mono', monospace;
  font-size: 12px;
  color: #64748B;
  white-space: pre-wrap;
  margin: 0;
}
</style>
