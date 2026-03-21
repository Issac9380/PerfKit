<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">命令模板管理</h1>
        <p class="page-subtitle">管理性能分析命令模板</p>
      </div>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon>
        添加模板
      </el-button>
    </div>

    <!-- 模板列表 -->
    <div class="card">
      <el-table :data="templates" class="template-table">
        <el-table-column prop="name" label="名称" width="180" />
        <el-table-column prop="commandType" label="类型" width="120">
          <template #default="{ row }">
            <el-tag size="small" :type="getTypeColor(row.commandType)">
              {{ row.commandType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="template" label="模板命令">
          <template #default="{ row }">
            <code class="template-code">{{ row.template }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" width="200" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteTemplate(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="templates.length === 0" class="empty-state">
        <el-icon><Document /></el-icon>
        <p>暂无命令模板</p>
      </div>
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingTemplate ? '编辑模板' : '添加模板'"
      width="550px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="如: JVM堆内存" />
        </el-form-item>

        <el-form-item label="类型" prop="commandType">
          <el-select v-model="form.commandType" placeholder="选择类型" style="width: 100%">
            <el-option label="jmap" value="jmap" />
            <el-option label="jstack" value="jstack" />
            <el-option label="jstat" value="jstat" />
            <el-option label=" Arthas" value="arthas" />
          </el-select>
        </el-form-item>

        <el-form-item label="模板" prop="template">
          <el-input
            v-model="form.template"
            type="textarea"
            :rows="3"
            placeholder="如: jmap -heap {pid}"
          />
          <div class="form-tip">
            使用 {pid}、{className}、{methodName} 作为参数占位符
          </div>
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="2"
            placeholder="描述此命令的用途"
          />
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
import { Plus, Document } from '@element-plus/icons-vue'
import api from '@/api'

const templates = ref([])
const dialogVisible = ref(false)
const editingTemplate = ref(null)
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({
  name: '',
  commandType: 'jmap',
  template: '',
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  commandType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  template: [{ required: true, message: '请输入模板命令', trigger: 'blur' }]
}

function getTypeColor(type) {
  const colors = {
    jmap: 'primary',
    jstack: 'success',
    jstat: 'warning',
    arthas: 'info'
  }
  return colors[type] || ''
}

async function loadTemplates() {
  try {
    const res = await api.get('/analysis/templates')
    templates.value = res.data.data || []
  } catch (error) {
    console.error(error)
  }
}

function openDialog(template = null) {
  editingTemplate.value = template
  if (template) {
    Object.assign(form, template)
  } else {
    Object.assign(form, {
      name: '',
      commandType: 'jmap',
      template: '',
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
        if (editingTemplate.value) {
          await api.put(`/analysis/templates/${editingTemplate.value.id}`, form)
          ElMessage.success('更新成功')
        } else {
          await api.post('/analysis/templates', form)
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        loadTemplates()
      } catch (error) {
        console.error(error)
      } finally {
        submitting.value = false
      }
    }
  })
}

async function deleteTemplate(id) {
  try {
    await ElMessageBox.confirm('确定要删除这个模板吗？', '提示', {
      type: 'warning'
    })
    await api.delete(`/analysis/templates/${id}`)
    ElMessage.success('删除成功')
    loadTemplates()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

onMounted(() => {
  loadTemplates()
})
</script>

<style scoped>
.template-table {
  border-radius: 12px;
  overflow: hidden;
}

.template-code {
  font-family: 'JetBrains Mono', monospace;
  font-size: 13px;
  color: #2563EB;
  background: #F1F5F9;
  padding: 4px 8px;
  border-radius: 6px;
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
}
</style>
