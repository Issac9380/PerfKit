<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">热部署</h1>
    </div>

    <!-- 上传文件 -->
    <div class="card upload-section">
      <h3 class="section-title">上传文件</h3>
      <el-upload
        ref="uploadRef"
        class="upload-area"
        drag
        :action="uploadUrl"
        :auto-upload="false"
        :limit="1"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        accept=".class,.jar"
      >
        <el-icon class="upload-icon"><UploadFilled /></el-icon>
        <div class="upload-text">
          拖拽 .class 或 .jar 文件到此处
        </div>
        <template #tip>
          <div class="upload-tip">
            支持 .class 和 .jar 文件，最大 50MB
          </div>
        </template>
      </el-upload>

      <el-button type="primary" class="upload-btn" @click="submitUpload">
        上传文件
      </el-button>
    </div>

    <!-- 部署表单 -->
    <div class="card deploy-section">
      <h3 class="section-title">执行热部署</h3>

      <el-form :model="form" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="集群">
              <el-select v-model="form.clusterId" placeholder="选择集群" style="width: 100%" @change="loadNamespaces">
                <el-option v-for="c in clusters" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="命名空间">
              <el-select v-model="form.namespace" placeholder="选择命名空间" style="width: 100%" @change="loadPods">
                <el-option v-for="ns in namespaces" :key="ns" :label="ns" :value="ns" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="Pod">
              <el-select v-model="form.podName" placeholder="选择 Pod" style="width: 100%" @change="loadContainers">
                <el-option v-for="p in pods" :key="p" :label="p" :value="p" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="容器">
              <el-select v-model="form.containerName" placeholder="选择容器" style="width: 100%">
                <el-option v-for="c in containers" :key="c" :label="c" :value="c" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="文件ID">
              <el-input v-model="form.fileId" placeholder="上传后获得" />
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="类名">
              <el-input v-model="form.className" placeholder="com.example.MyClass" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item>
          <el-button type="danger" :loading="deploying" @click="executeDeploy">
            <el-icon><Lightning /></el-icon>
            执行热部署
          </el-button>
        </el-form-item>
      </el-form>

      <el-alert
        v-if="deployResult"
        :title="deployResult.success ? '热部署成功' : '热部署失败'"
        :type="deployResult.success ? 'success' : 'error'"
        :description="deployResult.message"
        show-icon
        :closable="false"
        class="deploy-result"
      />
    </div>

    <!-- 说明 -->
    <div class="card info-section">
      <h3 class="section-title">使用说明</h3>
      <div class="info-content">
        <ol>
          <li>上传需要热部署的 .class 或 .jar 文件</li>
          <li>选择目标集群、命名空间、Pod 和容器</li>
          <li>输入完整的类名（包括包路径）</li>
          <li>点击"执行热部署"按钮</li>
        </ol>
        <div class="warning-box">
          <el-icon><Warning /></el-icon>
          <span>注意：热部署仅在当前容器运行期间生效，容器重启后将恢复原始类。</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled, Lightning, Warning } from '@element-plus/icons-vue'
import api from '@/api'

const uploadRef = ref(null)
const clusters = ref([])
const namespaces = ref([])
const pods = ref([])
const containers = ref([])
const deploying = ref(false)
const deployResult = ref(null)

const uploadUrl = '/api/v1/deploy/upload'

const form = reactive({
  clusterId: null,
  namespace: '',
  podName: '',
  containerName: '',
  fileId: '',
  className: '',
  methodName: ''
})

async function loadClusters() {
  try {
    const res = await api.get('/clusters')
    clusters.value = res.data.data || []
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

function submitUpload() {
  uploadRef.value?.submit()
}

function handleUploadSuccess(response) {
  if (response.code === 200) {
    form.fileId = response.data
    ElMessage.success('文件上传成功，文件ID: ' + response.data)
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

function handleUploadError(error) {
  ElMessage.error('上传失败')
  console.error(error)
}

async function executeDeploy() {
  if (!form.clusterId || !form.namespace || !form.podName || !form.containerName || !form.fileId || !form.className) {
    ElMessage.warning('请填写完整的表单信息')
    return
  }

  deploying.value = true
  deployResult.value = null

  try {
    const res = await api.post('/deploy/hot', {
      clusterId: form.clusterId,
      namespace: form.namespace,
      podName: form.podName,
      containerName: form.containerName,
      fileId: form.fileId,
      className: form.className,
      methodName: form.methodName
    })

    deployResult.value = {
      success: res.data.code === 200,
      message: res.data.message
    }

    ElMessage.success('热部署执行完成')
  } catch (error) {
    deployResult.value = {
      success: false,
      message: error.response?.data?.message || '热部署失败'
    }
    ElMessage.error('热部署失败')
  } finally {
    deploying.value = false
  }
}

onMounted(() => {
  loadClusters()
})
</script>

<style scoped>
.upload-section, .deploy-section, .info-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 16px;
  font-weight: 500;
  color: var(--color-text-primary);
  margin-bottom: 16px;
}

.upload-area {
  margin-bottom: 16px;
}

.upload-area :deep(.el-upload-dragger) {
  background: var(--color-bg-elevated);
  border-color: var(--color-border);
  border-radius: var(--radius-lg);
  padding: 40px;
}

.upload-area :deep(.el-upload-dragger:hover) {
  border-color: var(--color-primary);
}

.upload-icon {
  font-size: 48px;
  color: var(--color-primary);
  margin-bottom: 16px;
}

.upload-text {
  color: var(--color-text-secondary);
  font-size: 14px;
}

.upload-tip {
  color: var(--color-text-muted);
  font-size: 12px;
  margin-top: 8px;
}

.upload-btn {
  width: 100%;
}

.deploy-result {
  margin-top: 16px;
}

.info-content ol {
  color: var(--color-text-secondary);
  padding-left: 20px;
  line-height: 2;
}

.info-content li {
  margin-bottom: 8px;
}

.warning-box {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 16px;
  padding: 12px;
  background: rgba(245, 158, 11, 0.1);
  border-radius: var(--radius-md);
  color: var(--color-warning);
  font-size: 13px;
}
</style>
