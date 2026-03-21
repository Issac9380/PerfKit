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
            <el-input
              v-model="form.clientCert"
              type="textarea"
              :rows="3"
              placeholder="请输入客户端证书 (PEM 格式)"
            />
          </el-form-item>
          <el-form-item label="客户端密钥" prop="clientKey">
            <el-input
              v-model="form.clientKey"
              type="textarea"
              :rows="3"
              placeholder="请输入客户端私钥 (PEM 格式)"
            />
          </el-form-item>
          <el-form-item label="CA 证书" prop="caCert">
            <el-input
              v-model="form.caCert"
              type="textarea"
              :rows="3"
              placeholder="请输入 CA 证书 (PEM 格式，可选)"
            />
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
      width="800px"
    >
      <el-tabs v-model="activeTab">
        <el-tab-pane label="命名空间" name="namespaces">
          <el-select
            v-model="selectedNamespace"
            placeholder="选择命名空间"
            style="width: 100%; margin-bottom: 16px"
            @change="loadPods"
          >
            <el-option
              v-for="ns in namespaces"
              :key="ns"
              :label="ns"
              :value="ns"
            />
          </el-select>

          <el-table :data="pods" v-if="selectedNamespace">
            <el-table-column prop="name" label="Pod 名称" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Connection, FolderOpened, MoreFilled } from '@element-plus/icons-vue'
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
</style>
