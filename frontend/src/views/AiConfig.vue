<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">AI 配置管理</h1>
        <p class="page-subtitle">配置 AI 大模型用于日志分析和性能诊断</p>
      </div>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon>
        添加配置
      </el-button>
    </div>

    <!-- AI 配置列表 -->
    <div class="card">
      <el-table :data="configs" class="ai-config-table">
        <el-table-column prop="name" label="名称" width="180" />
        <el-table-column prop="provider" label="提供商" width="120">
          <template #default="{ row }">
            <el-tag size="small" :type="getProviderType(row.provider)">
              {{ getProviderLabel(row.provider) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="endpoint" label="Endpoint" min-width="200">
          <template #default="{ row }">
            <span class="endpoint-text">{{ row.endpoint || '默认' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="model" label="模型" width="150" />
        <el-table-column label="默认" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isDefault" type="success" size="small">默认</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
              {{ row.status === 'ACTIVE' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="setDefault(row.id)" :disabled="row.isDefault">
              设为默认
            </el-button>
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteConfig(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="configs.length === 0" class="empty-state">
        <el-icon><Setting /></el-icon>
        <p>暂无 AI 配置</p>
        <el-button type="primary" @click="openDialog()">添加第一个配置</el-button>
      </div>
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingConfig ? '编辑配置' : '添加配置'"
      width="550px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="配置名称" prop="name">
          <el-input v-model="form.name" placeholder="如: OpenAI API" />
        </el-form-item>

        <el-form-item label="提供商" prop="provider">
          <el-select v-model="form.provider" placeholder="选择 AI 提供商" style="width: 100%" @change="handleProviderChange">
            <el-option label="OpenAI" value="openai" />
            <el-option label=" Anthropic (Claude)" value="claude" />
            <el-option label="Azure OpenAI" value="azure" />
            <el-option label="本地模型" value="local" />
          </el-select>
        </el-form-item>

        <el-form-item label="API Key" prop="apiKey">
          <el-input v-model="form.apiKey" type="password" show-password placeholder="输入 API Key" />
        </el-form-item>

        <el-form-item label="Endpoint" prop="endpoint">
          <el-input v-model="form.endpoint" placeholder="留空使用默认地址" />
          <div class="form-tip" v-if="form.provider === 'openai'">
            如: https://api.openai.com/v1
          </div>
          <div class="form-tip" v-if="form.provider === 'claude'">
            如: https://api.anthropic.com/v1
          </div>
          <div class="form-tip" v-if="form.provider === 'azure'">
            如: https://your-resource.openai.azure.com
          </div>
        </el-form-item>

        <el-form-item label="模型" prop="model">
          <el-input v-model="form.model" placeholder="如: gpt-4o, claude-3-5-sonnet-20241022" />
          <div class="form-tip" v-if="form.provider === 'openai'">
            常用: gpt-4o, gpt-4-turbo, gpt-3.5-turbo
          </div>
          <div class="form-tip" v-if="form.provider === 'claude'">
            常用: claude-3-5-sonnet-20241022, claude-3-opus-20240229
          </div>
        </el-form-item>

        <el-form-item label="设为默认">
          <el-switch v-model="form.isDefault" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Setting } from '@element-plus/icons-vue'
import api from '@/api'

const configs = ref([])
const dialogVisible = ref(false)
const editingConfig = ref(null)
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({
  name: '',
  provider: 'openai',
  apiKey: '',
  endpoint: '',
  model: '',
  isDefault: false
})

const rules = {
  name: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
  provider: [{ required: true, message: '请选择 AI 提供商', trigger: 'change' }],
  apiKey: [{ required: true, message: '请输入 API Key', trigger: 'blur' }],
  model: [{ required: true, message: '请输入模型名称', trigger: 'blur' }]
}

function getProviderType(provider) {
  const types = {
    openai: 'success',
    claude: 'warning',
    azure: 'primary',
    local: 'info'
  }
  return types[provider] || ''
}

function getProviderLabel(provider) {
  const labels = {
    openai: 'OpenAI',
    claude: 'Claude',
    azure: 'Azure',
    local: '本地模型'
  }
  return labels[provider] || provider
}

function handleProviderChange() {
  // 根据提供商设置默认模型
  if (form.provider === 'openai' && !form.model) {
    form.model = 'gpt-4o'
  } else if (form.provider === 'claude' && !form.model) {
    form.model = 'claude-3-5-sonnet-20241022'
  }
}

async function loadConfigs() {
  try {
    const res = await api.get('/ai/config')
    configs.value = res.data.data || []
  } catch (error) {
    console.error(error)
  }
}

function openDialog(config = null) {
  editingConfig.value = config
  if (config) {
    Object.assign(form, {
      name: config.name,
      provider: config.provider,
      apiKey: config.apiKey,
      endpoint: config.endpoint,
      model: config.model,
      isDefault: config.isDefault
    })
  } else {
    Object.assign(form, {
      name: '',
      provider: 'openai',
      apiKey: '',
      endpoint: '',
      model: 'gpt-4o',
      isDefault: configs.value.length === 0
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
        const submitData = {
          name: form.name,
          provider: form.provider,
          apiKey: form.apiKey,
          endpoint: form.endpoint || null,
          model: form.model,
          isDefault: form.isDefault
        }

        if (editingConfig.value) {
          await api.put(`/ai/config/${editingConfig.value.id}`, submitData)
          ElMessage.success('更新成功')
        } else {
          await api.post('/ai/config', submitData)
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        loadConfigs()
      } catch (error) {
        console.error(error)
      } finally {
        submitting.value = false
      }
    }
  })
}

async function setDefault(id) {
  try {
    await api.put(`/ai/config/${id}/default`)
    ElMessage.success('已设为默认')
    loadConfigs()
  } catch (error) {
    console.error(error)
  }
}

async function deleteConfig(id) {
  try {
    await ElMessageBox.confirm('确定要删除这个 AI 配置吗？', '提示', {
      type: 'warning'
    })
    await api.delete(`/ai/config/${id}`)
    ElMessage.success('删除成功')
    loadConfigs()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

onMounted(() => {
  loadConfigs()
})
</script>

<style scoped>
.ai-config-table {
  border-radius: 12px;
  overflow: hidden;
}

.endpoint-text {
  font-family: 'JetBrains Mono', monospace;
  font-size: 13px;
  color: #64748B;
}

.form-tip {
  font-size: 12px;
  color: #94A3B8;
  margin-top: 4px;
}

.empty-state {
  text-align: center;
  padding: 48px 20px;
}

.empty-state .el-icon {
  font-size: 48px;
  color: #CBD5E1;
  margin-bottom: 12px;
}

.empty-state p {
  color: #94A3B8;
  margin-bottom: 16px;
}
</style>
