<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">K8S 集群管理</h1>
        <p class="page-subtitle">管理多集群连接配置</p>
      </div>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon>
        添加集群
      </el-button>
    </div>

    <!-- 集群列表 -->
    <div class="cluster-grid">
      <div
        v-for="cluster in clusters"
        :key="cluster.id"
        class="cluster-card"
      >
        <div class="cluster-header">
          <div class="cluster-info">
            <div class="cluster-icon-wrap">
              <el-icon class="cluster-icon"><Connection /></el-icon>
            </div>
            <span class="cluster-name">{{ cluster.name }}</span>
          </div>
          <div :class="['status-dot', cluster.status === 'ACTIVE' ? 'success' : 'danger']"></div>
        </div>

        <div class="cluster-details">
          <div class="detail-item">
            <span class="label">API Server</span>
            <span class="value">{{ cluster.apiServer }}</span>
          </div>
          <div class="detail-item">
            <span class="label">认证方式</span>
            <el-tag size="small" type="info">{{ getAuthTypeLabel(cluster.authType) }}</el-tag>
          </div>
          <div class="detail-item" v-if="cluster.description">
            <span class="label">描述</span>
            <span class="value">{{ cluster.description }}</span>
          </div>
        </div>

        <div class="cluster-actions">
          <el-button size="default" @click="testConnection(cluster.id)">
            <el-icon><Connection /></el-icon>
            测试连接
          </el-button>
          <el-button size="default" type="primary" @click="openNamespacesDialog(cluster)">
            <el-icon><FolderOpened /></el-icon>
            查看资源
          </el-button>
          <el-dropdown @command="(cmd) => handleCommand(cmd, cluster)">
            <el-button size="default">
              <el-icon><MoreFilled /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="edit">编辑</el-dropdown-item>
                <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="clusters.length === 0" class="empty-state">
        <div class="empty-icon-wrap">
          <el-icon class="empty-icon"><Connection /></el-icon>
        </div>
        <p>暂无集群配置</p>
        <el-button type="primary" @click="openDialog()">添加第一个集群</el-button>
      </div>
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingCluster ? '编辑集群' : '添加集群'"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="集群名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入集群名称" />
        </el-form-item>

        <el-form-item label="API Server" prop="apiServer">
          <el-input v-model="form.apiServer" placeholder="https://kubernetes:6443" />
        </el-form-item>

        <el-form-item label="认证方式" prop="authType">
          <el-select v-model="form.authType" placeholder="选择认证方式" style="width: 100%" @change="handleAuthTypeChange">
            <el-option label="Kubeconfig" value="kubeconfig" />
            <el-option label="Token" value="token" />
            <el-option label="证书" value="certificate" />
          </el-select>
        </el-form-item>

        <!-- Kubeconfig 认证 -->
        <template v-if="form.authType === 'kubeconfig'">
          <el-form-item label="Kubeconfig" prop="config">
            <el-input
              v-model="form.config"
              type="textarea"
              :rows="6"
              placeholder="请粘贴 kubeconfig 内容"
            />
            <div class="form-tip">粘贴完整的 kubeconfig 文件内容（YAML 格式）</div>
          </el-form-item>
        </template>

        <!-- Token 认证 -->
        <template v-else-if="form.authType === 'token'">
          <el-form-item label="Token" prop="config">
            <el-input
              v-model="form.config"
              type="textarea"
              :rows="3"
              placeholder="请输入 Bearer Token"
            />
            <div class="form-tip">在 K8S 中执行: kubectl create token default --duration=87600h 获取</div>
          </el-form-item>
        </template>

        <!-- 证书认证 -->
        <template v-else-if="form.authType === 'certificate'">
          <el-form-item label="客户端证书" prop="clientCert">
            <el-upload
              :auto-upload="false"
              :limit="1"
              accept=".pem,.crt,.cer"
              @change="(file) => handleFileChange(file, 'clientCert')"
            >
              <el-button size="small">
                <el-icon><Upload /></el-icon>
                选择文件
              </el-button>
              <template #tip>
                <div class="upload-tip">支持 .pem, .crt, .cer 格式</div>
              </template>
            </el-upload>
            <div v-if="form.clientCert" class="file-name">
              <el-icon><Document /></el-icon>
              {{ getFileName(form.clientCert) }}
            </div>
          </el-form-item>
          <el-form-item label="客户端密钥" prop="clientKey">
            <el-upload
              :auto-upload="false"
              :limit="1"
              accept=".pem,.key"
              @change="(file) => handleFileChange(file, 'clientKey')"
            >
              <el-button size="small">
                <el-icon><Upload /></el-icon>
                选择文件
              </el-button>
              <template #tip>
                <div class="upload-tip">支持 .pem, .key 格式</div>
              </template>
            </el-upload>
            <div v-if="form.clientKey" class="file-name">
              <el-icon><Document /></el-icon>
              {{ getFileName(form.clientKey) }}
            </div>
          </el-form-item>
          <el-form-item label="CA 证书" prop="caCert">
            <el-upload
              :auto-upload="false"
              :limit="1"
              accept=".pem,.crt,.cer"
              @change="(file) => handleFileChange(file, 'caCert')"
            >
              <el-button size="small">
                <el-icon><Upload /></el-icon>
                选择文件（可选）
              </el-button>
              <template #tip>
                <div class="upload-tip">支持 .pem, .crt, .cer 格式</div>
              </template>
            </el-upload>
            <div v-if="form.caCert" class="file-name">
              <el-icon><Document /></el-icon>
              {{ getFileName(form.caCert) }}
            </div>
          </el-form-item>
        </template>

        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 命名空间/资源对话框 -->
    <el-dialog
      v-model="resourceDialogVisible"
      :title="`${selectedCluster?.name} - 资源浏览`"
      width="900px"
    >
      <el-tabs v-model="activeTab">
        <el-tab-pane label="Pod 列表" name="namespaces">
          <!-- 搜索和筛选 -->
          <div class="pod-filters">
            <el-select
              v-model="selectedNamespace"
              placeholder="选择命名空间"
              style="width: 200px"
              @change="loadPods"
              clearable
            >
              <el-option
                v-for="ns in namespaces"
                :key="ns"
                :label="ns"
                :value="ns"
              />
            </el-select>

            <el-input
              v-model="podSearch"
              placeholder="搜索 Pod 名称..."
              style="width: 250px"
              clearable
              :prefix-icon="Search"
            />

            <span class="pod-count">共 {{ filteredPods.length }} 个 Pod</span>
          </div>

          <!-- Pod 表格（支持分页和选择） -->
          <el-table
            v-if="selectedNamespace"
            :data="paginatedPods"
            @selection-change="handlePodSelection"
            max-height="400"
            style="width: 100%"
          >
            <el-table-column type="selection" width="50" />
            <el-table-column prop="name" label="Pod 名称" min-width="300">
              <template #default="{ row }">
                <div class="pod-name-cell">
                  <el-icon><Box /></el-icon>
                  <span>{{ row.name }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="row.status === 'Running' ? 'success' : 'info'" size="small">
                  {{ row.status || 'Unknown' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button size="small" @click="viewContainers(row)">
                  容器
                </el-button>
                <el-button size="small" type="primary" @click="viewLogs(row)">
                  日志
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div v-if="selectedNamespace && filteredPods.length > pageSize" class="pagination-wrap">
            <el-pagination
              v-model:current-page="currentPage"
              :page-size="pageSize"
              :total="filteredPods.length"
              layout="prev, pager, next"
              background
            />
          </div>

          <!-- 批量操作 -->
          <div v-if="selectedPods.length > 0" class="batch-actions">
            <el-button type="primary" size="small" @click="batchViewLogs">
              批量日志 ({{ selectedPods.length }})
            </el-button>
            <el-button type="success" size="small" @click="batchAnalyze">
              批量分析 ({{ selectedPods.length }})
            </el-button>
            <el-button size="small" @click="clearPodSelection">
              清空选择
            </el-button>
          </div>
        </el-tab-pane>

        <el-tab-pane label="日志" name="logs" v-if="logContent">
          <div class="log-viewer">
            <pre>{{ logContent }}</pre>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Connection, FolderOpened, MoreFilled, Upload, Document } from '@element-plus/icons-vue'
import api from '@/api'

const clusters = ref([])
const dialogVisible = ref(false)
const resourceDialogVisible = ref(false)
const editingCluster = ref(null)
const submitting = ref(false)
const formRef = ref(null)

const selectedCluster = ref(null)
const namespaces = ref([])
const selectedNamespace = ref('')
const pods = ref([])
const containers = ref([])
const logContent = ref('')
const activeTab = ref('namespaces')

// Pod 搜索、分页和批量选择
const podSearch = ref('')
const selectedPods = ref([])
const currentPage = ref(1)
const pageSize = ref(20)

// 搜索过滤后的 Pod 列表
const filteredPods = computed(() => {
  if (!podSearch.value) return pods.value
  const keyword = podSearch.value.toLowerCase()
  return pods.value.filter(pod =>
    pod.name.toLowerCase().includes(keyword)
  )
})

// 分页后的 Pod 列表
const paginatedPods = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredPods.value.slice(start, end)
})

function handlePodSelection(selection) {
  selectedPods.value = selection
}

function handlePageChange(page) {
  currentPage.value = page
}

function clearPodSelection() {
  selectedPods.value = []
  currentPage.value = 1
}

// 搜索时重置分页
watch(podSearch, () => {
  currentPage.value = 1
})

const form = reactive({
  name: '',
  apiServer: '',
  authType: 'kubeconfig',
  config: '',
  clientCert: '',
  clientKey: '',
  caCert: '',
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入集群名称', trigger: 'blur' }],
  apiServer: [{ required: true, message: '请输入 API Server', trigger: 'blur' }],
  authType: [{ required: true, message: '请选择认证方式', trigger: 'change' }]
}

function handleAuthTypeChange() {
  // Clear certificate fields when switching auth type
  form.clientCert = ''
  form.clientKey = ''
  form.caCert = ''
}

function handleFileChange(uploadFile, field) {
  const reader = new FileReader()
  reader.onload = (e) => {
    form[field] = e.target.result
  }
  reader.readAsText(uploadFile.raw)
}

function getFileName(content) {
  // Extract filename from base64 or return a default name
  if (content && content.length > 50) {
    return '已选择文件 (证书内容已加载)'
  }
  return content || ''
}

function getAuthTypeLabel(type) {
  const map = {
    kubeconfig: 'Kubeconfig',
    token: 'Token',
    certificate: '证书'
  }
  return map[type] || type
}

async function loadClusters() {
  try {
    const res = await api.get('/clusters')
    clusters.value = res.data.data || []
  } catch (error) {
    console.error(error)
  }
}

function openDialog(cluster = null) {
  editingCluster.value = cluster
  if (cluster) {
    // Parse config based on auth type
    let parsedConfig = {}
    try {
      parsedConfig = JSON.parse(cluster.config || '{}')
    } catch (e) {
      parsedConfig = {}
    }

    Object.assign(form, {
      name: cluster.name,
      apiServer: cluster.apiServer,
      authType: cluster.authType,
      config: parsedConfig.kubeconfig || parsedConfig.token || '',
      clientCert: parsedConfig.clientCert || '',
      clientKey: parsedConfig.clientKey || '',
      caCert: parsedConfig.caCert || '',
      description: cluster.description
    })
  } else {
    Object.assign(form, {
      name: '',
      apiServer: '',
      authType: 'kubeconfig',
      config: '',
      clientCert: '',
      clientKey: '',
      caCert: '',
      description: ''
    })
  }
  dialogVisible.value = true
}

async function submitForm() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        // Build config based on auth type
        let configData = {}
        if (form.authType === 'kubeconfig') {
          configData = { kubeconfig: form.config }
        } else if (form.authType === 'token') {
          configData = { token: form.config }
        } else if (form.authType === 'certificate') {
          configData = {
            clientCert: form.clientCert,
            clientKey: form.clientKey,
            caCert: form.caCert
          }
        }

        const submitData = {
          name: form.name,
          apiServer: form.apiServer,
          authType: form.authType,
          config: JSON.stringify(configData),
          description: form.description
        }

        if (editingCluster.value) {
          await api.put(`/clusters/${editingCluster.value.id}`, submitData)
          ElMessage.success('更新成功')
        } else {
          await api.post('/clusters', submitData)
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        loadClusters()
      } catch (error) {
        console.error(error)
      } finally {
        submitting.value = false
      }
    }
  })
}

async function testConnection(id) {
  try {
    const res = await api.post(`/clusters/${id}/test`)
    if (res.data.data) {
      ElMessage.success('连接成功')
    } else {
      ElMessage.warning('连接失败')
    }
  } catch (error) {
    ElMessage.error('连接失败')
  }
}

async function openNamespacesDialog(cluster) {
  selectedCluster.value = cluster
  resourceDialogVisible.value = true
  activeTab.value = 'namespaces'
  selectedNamespace.value = ''
  pods.value = []
  logContent.value = ''

  try {
    const res = await api.get(`/clusters/${cluster.id}/namespaces`)
    namespaces.value = res.data.data || []
  } catch (error) {
    ElMessage.error('获取命名空间失败')
  }
}

async function loadPods() {
  if (!selectedNamespace.value) return

  // 清空之前的选择
  selectedPods.value = []
  currentPage.value = 1

  try {
    const res = await api.get(`/clusters/${selectedCluster.value.id}/pods?namespace=${selectedNamespace.value}`)
    pods.value = res.data.data || []
  } catch (error) {
    ElMessage.error('获取 Pod 列表失败')
  }
}

async function viewContainers(pod) {
  try {
    const res = await api.get(
      `/clusters/${selectedCluster.value.id}/pods/${pod.name}/containers?namespace=${selectedNamespace.value}`
    )
    containers.value = res.data.data || []
    ElMessage.info(`容器: ${containers.value.join(', ')}`)
  } catch (error) {
    ElMessage.error('获取容器失败')
  }
}

async function viewLogs(pod) {
  try {
    const containerRes = await api.get(
      `/clusters/${selectedCluster.value.id}/pods/${pod.name}/containers?namespace=${selectedNamespace.value}`
    )
    const containerList = containerRes.data.data || []
    if (containerList.length === 0) {
      ElMessage.warning('没有容器')
      return
    }

    const res = await api.get(
      `/clusters/${selectedCluster.value.id}/pods/${pod.name}/logs?namespace=${selectedNamespace.value}&container=${containerList[0]}`
    )
    logContent.value = res.data.data || '无日志'
    activeTab.value = 'logs'
  } catch (error) {
    ElMessage.error('获取日志失败')
  }
}

// 批量获取日志
async function batchViewLogs() {
  if (selectedPods.value.length === 0) return

  try {
    const podNames = selectedPods.value.map(p => p.name).join(',')
    const res = await api.get(
      `/clusters/${selectedCluster.value.id}/pods/batch/logs?namespace=${selectedNamespace.value}&pods=${podNames}`
    )
    logContent.value = res.data.data || '无日志'
    activeTab.value = 'logs'
  } catch (error) {
    ElMessage.error('批量获取日志失败')
  }
}

// 批量分析（跳转到性能分析页面）
function batchAnalyze() {
  if (selectedPods.value.length === 0) return

  const podNames = selectedPods.value.map(p => p.name)
  // 跳转到性能分析页面，并传递选中的 Pod 信息
  ElMessage.info(`已选择 ${podNames.length} 个 Pod，请选择分析命令`)
  // 这里可以导航到分析页面并预填选中的 Pod
}

async function handleCommand(cmd, cluster) {
  if (cmd === 'edit') {
    openDialog(cluster)
  } else if (cmd === 'delete') {
    try {
      await ElMessageBox.confirm('确定要删除这个集群吗？', '提示', {
        type: 'warning'
      })
      await api.delete(`/clusters/${cluster.id}`)
      ElMessage.success('删除成功')
      loadClusters()
    } catch (error) {
      if (error !== 'cancel') {
        console.error(error)
      }
    }
  }
}

onMounted(() => {
  loadClusters()
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.cluster-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 20px;
}

.cluster-card {
  background: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-radius: 16px;
  padding: 24px;
  transition: all 0.25s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.cluster-card:hover {
  border-color: #3B82F6;
  box-shadow: 0 8px 24px rgba(37, 99, 235, 0.12);
  transform: translateY(-2px);
}

.cluster-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #E2E8F0;
}

.cluster-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.cluster-icon-wrap {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #2563EB 0%, #3B82F6 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cluster-icon {
  font-size: 22px;
  color: #FFFFFF;
}

.cluster-name {
  font-size: 18px;
  font-weight: 600;
  color: #1E293B;
}

.cluster-details {
  margin-bottom: 20px;
}

.detail-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 12px;
  font-size: 14px;
}

.detail-item .label {
  color: #94A3B8;
  width: 80px;
  flex-shrink: 0;
  font-weight: 500;
}

.detail-item .value {
  color: #64748B;
  word-break: break-all;
}

.cluster-actions {
  display: flex;
  gap: 8px;
}

.log-viewer {
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: 16px;
  max-height: 400px;
  overflow: auto;
}

.log-viewer pre {
  font-family: 'JetBrains Mono', monospace;
  font-size: 12px;
  color: #64748B;
  white-space: pre-wrap;
  word-break: break-all;
  margin: 0;
}

.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px 20px;
  background: #FFFFFF;
  border: 1px dashed #E2E8F0;
  border-radius: 16px;
}

.empty-icon-wrap {
  width: 80px;
  height: 80px;
  background: #F1F5F9;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

.empty-icon {
  font-size: 36px;
  color: #94A3B8;
}

.empty-state p {
  color: #94A3B8;
  margin-bottom: 16px;
}

.form-tip {
  font-size: 12px;
  color: #94A3B8;
  margin-top: 4px;
  line-height: 1.5;
}

.upload-tip {
  font-size: 12px;
  color: #94A3B8;
  margin-top: 4px;
}

.file-name {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  padding: 6px 10px;
  background: #F1F5F9;
  border-radius: 6px;
  font-size: 13px;
  color: #10B981;
}

.file-name .el-icon {
  font-size: 14px;
}

/* Pod 筛选和批量操作样式 */
.pod-filters {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: #F8FAFC;
  border-radius: 8px;
}

.pod-filters .el-input {
  max-width: 300px;
}

.pod-count {
  font-size: 13px;
  color: #64748B;
  white-space: nowrap;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #E2E8F0;
}

.batch-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 16px;
  padding: 12px 16px;
  background: #EFF6FF;
  border-radius: 8px;
  border: 1px solid #BFDBFE;
}

.batch-actions .el-button {
  font-weight: 500;
}
</style>
