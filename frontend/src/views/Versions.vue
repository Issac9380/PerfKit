<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">版本管理</h1>
    </div>

    <el-tabs v-model="activeTab">
      <!-- JDK 版本 -->
      <el-tab-pane label="JDK 版本" name="jdk">
        <div class="version-section">
          <div class="section-header">
            <span class="section-title">JDK 版本列表</span>
            <el-button type="primary" @click="openUploadDialog('jdk')">
              <el-icon><Upload /></el-icon>
              上传 JDK
            </el-button>
          </div>

          <el-table :data="jdkVersions" class="version-table">
            <el-table-column prop="version" label="版本" />
            <el-table-column prop="fileSize" label="文件大小">
              <template #default="{ row }">
                {{ formatSize(row.fileSize) }}
              </template>
            </el-table-column>
            <el-table-column prop="md5" label="MD5" width="300">
              <template #default="{ row }">
                <code class="md5-code">{{ row.md5 }}</code>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="上传时间">
              <template #default="{ row }">
                {{ formatTime(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button size="small" type="danger" @click="deleteVersion('jdk', row.id)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div v-if="jdkVersions.length === 0" class="empty-state">
            <el-icon><Box /></el-icon>
            <p>暂无 JDK 版本</p>
          </div>
        </div>
      </el-tab-pane>

      <!-- Arthas 版本 -->
      <el-tab-pane label="Arthas 版本" name="arthas">
        <div class="version-section">
          <div class="section-header">
            <span class="section-title">Arthas 版本列表</span>
            <el-button type="primary" @click="openUploadDialog('arthas')">
              <el-icon><Upload /></el-icon>
              上传 Arthas
            </el-button>
          </div>

          <el-table :data="arthasVersions" class="version-table">
            <el-table-column prop="version" label="版本" />
            <el-table-column prop="fileSize" label="文件大小">
              <template #default="{ row }">
                {{ formatSize(row.fileSize) }}
              </template>
            </el-table-column>
            <el-table-column prop="md5" label="MD5" width="300">
              <template #default="{ row }">
                <code class="md5-code">{{ row.md5 }}</code>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="上传时间">
              <template #default="{ row }">
                {{ formatTime(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button size="small" type="danger" @click="deleteVersion('arthas', row.id)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div v-if="arthasVersions.length === 0" class="empty-state">
            <el-icon><Box /></el-icon>
            <p>暂无 Arthas 版本</p>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 上传对话框 -->
    <el-dialog v-model="uploadDialogVisible" title="上传版本" width="400px">
      <el-upload
        ref="uploadRef"
        class="upload-demo"
        drag
        :action="uploadUrl"
        :auto-upload="false"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        :limit="1"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">
          拖拽文件到此处 或 <em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            <template v-if="uploadType === 'jdk'">
              支持 .tar.gz, .zip, .jdk 格式，最大 100MB
            </template>
            <template v-else>
              支持 .jar, .zip 格式，最大 100MB
            </template>
          </div>
        </template>
      </el-upload>

      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitUpload">上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, Box, UploadFilled } from '@element-plus/icons-vue'
import api from '@/api'

const activeTab = ref('jdk')
const jdkVersions = ref([])
const arthasVersions = ref([])
const uploadDialogVisible = ref(false)
const uploadType = ref('jdk')
const uploadRef = ref(null)

const uploadUrl = computed(() => {
  return `/api/v1/versions/${uploadType.value}`
})

async function loadJdkVersions() {
  try {
    const res = await api.get('/versions/jdk')
    jdkVersions.value = res.data.data || []
  } catch (error) {
    console.error(error)
  }
}

async function loadArthasVersions() {
  try {
    const res = await api.get('/versions/arthas')
    arthasVersions.value = res.data.data || []
  } catch (error) {
    console.error(error)
  }
}

function openUploadDialog(type) {
  uploadType.value = type
  uploadDialogVisible.value = true
}

function submitUpload() {
  uploadRef.value?.submit()
}

function handleUploadSuccess() {
  ElMessage.success('上传成功')
  uploadDialogVisible.value = false
  if (uploadType.value === 'jdk') {
    loadJdkVersions()
  } else {
    loadArthasVersions()
  }
}

function handleUploadError(error) {
  ElMessage.error('上传失败')
  console.error(error)
}

async function deleteVersion(type, id) {
  try {
    await ElMessageBox.confirm('确定要删除这个版本吗？', '提示', {
      type: 'warning'
    })
    await api.delete(`/versions/${type}/${id}`)
    ElMessage.success('删除成功')
    if (type === 'jdk') {
      loadJdkVersions()
    } else {
      loadArthasVersions()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

function formatSize(bytes) {
  if (!bytes) return '-'
  const units = ['B', 'KB', 'MB', 'GB']
  let size = bytes
  let unitIndex = 0
  while (size >= 1024 && unitIndex < units.length - 1) {
    size /= 1024
    unitIndex++
  }
  return `${size.toFixed(2)} ${units[unitIndex]}`
}

function formatTime(time) {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(() => {
  loadJdkVersions()
  loadArthasVersions()
})
</script>

<style scoped>
.version-section {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: 500;
  color: var(--color-text-primary);
}

.version-table {
  margin-top: 16px;
}

.md5-code {
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--color-text-muted);
  background: var(--color-bg-base);
  padding: 2px 6px;
  border-radius: 4px;
}

.upload-demo {
  text-align: center;
}
</style>
