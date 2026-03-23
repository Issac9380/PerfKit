<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">版本管理</h1>
        <p class="page-subtitle">管理 JDK 和 Arthas 版本</p>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="custom-tabs">
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
            <el-table-column prop="version" label="版本" width="100" />
            <el-table-column prop="downloadUrl" label="下载链接" min-width="200">
              <template #default="{ row }">
                <a v-if="row.downloadUrl" :href="row.downloadUrl" target="_blank" class="download-link">
                  <el-icon><Link /></el-icon>
                  官方下载
                </a>
                <span v-else class="no-link">-</span>
              </template>
            </el-table-column>
            <el-table-column prop="recommendedArthasVersion" label="配套 Arthas" width="120">
              <template #default="{ row }">
                <el-tag v-if="row.recommendedArthasVersion" type="success" size="small">
                  {{ row.recommendedArthasVersion }}
                </el-tag>
                <span v-else class="no-link">-</span>
              </template>
            </el-table-column>
            <el-table-column prop="fileSize" label="文件大小" width="100">
              <template #default="{ row }">
                {{ formatSize(row.fileSize) }}
              </template>
            </el-table-column>
            <el-table-column prop="md5" label="MD5" width="200">
              <template #default="{ row }">
                <code class="md5-code">{{ row.md5 || '-' }}</code>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="上传时间" width="180">
              <template #default="{ row }">
                {{ formatTime(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button size="small" type="danger" @click="deleteVersion('jdk', row.id)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div v-if="jdkVersions.length === 0" class="empty-state">
            <div class="empty-icon-wrap">
              <el-icon class="empty-icon"><Box /></el-icon>
            </div>
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
            <el-table-column prop="version" label="版本" width="100" />
            <el-table-column prop="downloadUrl" label="下载链接" min-width="200">
              <template #default="{ row }">
                <a v-if="row.downloadUrl" :href="row.downloadUrl" target="_blank" class="download-link">
                  <el-icon><Link /></el-icon>
                  官方下载
                </a>
                <span v-else class="no-link">-</span>
              </template>
            </el-table-column>
            <el-table-column prop="compatibleJdkVersions" label="兼容 JDK" width="150">
              <template #default="{ row }">
                <el-tag v-if="row.compatibleJdkVersions" type="info" size="small">
                  JDK {{ row.compatibleJdkVersions }}
                </el-tag>
                <span v-else class="no-link">-</span>
              </template>
            </el-table-column>
            <el-table-column prop="fileSize" label="文件大小" width="100">
              <template #default="{ row }">
                {{ formatSize(row.fileSize) }}
              </template>
            </el-table-column>
            <el-table-column prop="md5" label="MD5" width="200">
              <template #default="{ row }">
                <code class="md5-code">{{ row.md5 || '-' }}</code>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="上传时间" width="180">
              <template #default="{ row }">
                {{ formatTime(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button size="small" type="danger" @click="deleteVersion('arthas', row.id)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div v-if="arthasVersions.length === 0" class="empty-state">
            <div class="empty-icon-wrap">
              <el-icon class="empty-icon"><Box /></el-icon>
            </div>
            <p>暂无 Arthas 版本</p>
          </div>
        </div>
      </el-tab-pane>

      <!-- 版本映射 -->
      <el-tab-pane label="版本映射" name="mappings">
        <div class="version-section">
          <div class="section-header">
            <span class="section-title">JDK / Arthas 版本映射关系</span>
            <el-tag type="info">推荐组合</el-tag>
          </div>

          <el-table :data="versionMappings" class="version-table">
            <el-table-column prop="jdkVersion" label="JDK 版本" width="120">
              <template #default="{ row }">
                <el-tag type="warning">JDK {{ row.jdkVersion }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="arthasVersion" label="Arthas 版本" width="150">
              <template #default="{ row }">
                <el-tag type="success">Arthas {{ row.arthasVersion }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="说明" />
            <el-table-column label="推荐" width="80">
              <template #default="{ row }">
                <el-tag v-if="row.recommended" type="danger" size="small">推荐</el-tag>
              </template>
            </el-table-column>
          </el-table>

          <div class="mapping-tips">
            <el-icon><InfoFilled /></el-icon>
            <span>以上是官方推荐的 JDK/Arthas 版本组合，生产环境建议使用推荐版本</span>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 上传对话框 -->
    <el-dialog v-model="uploadDialogVisible" title="上传版本" width="450px">
      <el-form label-width="100px">
        <el-form-item label="选择文件">
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
            <div class="upload-content">
              <el-icon class="upload-icon"><UploadFilled /></el-icon>
              <div class="upload-text">
                拖拽文件到此处 或 <em>点击上传</em>
              </div>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                <template v-if="uploadType === 'jdk'">
                  支持 .tar.gz, .zip, .jdk 格式，最大 100MB<br/>
                  建议从 Oracle 或 AdoptOpenJDK 官网下载
                </template>
                <template v-else>
                  支持 .jar, .zip 格式，最大 100MB<br/>
                  建议从 Arthas GitHub Releases 下载
                </template>
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <el-divider />

        <el-form-item label="或使用链接">
          <div v-if="uploadType === 'jdk'" class="quick-links">
            <el-link href="https://adoptium.net/" target="_blank">Adoptium (推荐)</el-link>
            <el-link href="https://www.oracle.com/java/technologies/downloads/" target="_blank">Oracle JDK</el-link>
          </div>
          <div v-else class="quick-links">
            <el-link href="https://github.com/alibaba/arthas/releases" target="_blank">Arthas GitHub</el-link>
            <el-link href="https://arthas.aliyun.com/" target="_blank">Arthas 官网</el-link>
          </div>
        </el-form-item>
      </el-form>

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
import { Upload, Box, UploadFilled, Link, InfoFilled } from '@element-plus/icons-vue'
import api from '@/api'

const activeTab = ref('jdk')
const jdkVersions = ref([])
const arthasVersions = ref([])
const versionMappings = ref([])
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

async function loadVersionMappings() {
  try {
    const res = await api.get('/versions/mappings')
    versionMappings.value = res.data.data || []
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
  loadVersionMappings()
})
</script>

<style scoped>
.version-section {
  background: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-radius: 16px;
  padding: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1E293B;
}

.version-table {
  margin-top: 16px;
  border-radius: 12px;
  overflow: hidden;
}

.download-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #2563EB;
  font-size: 13px;
  text-decoration: none;
}

.download-link:hover {
  text-decoration: underline;
}

.no-link {
  color: #94A3B8;
  font-size: 13px;
}

.md5-code {
  font-family: 'JetBrains Mono', monospace;
  font-size: 12px;
  color: #64748B;
  background: #F1F5F9;
  padding: 4px 8px;
  border-radius: 6px;
}

.upload-demo {
  text-align: center;
}

.upload-content {
  padding: 20px;
}

.upload-icon {
  font-size: 48px;
  color: #2563EB;
  margin-bottom: 12px;
}

.upload-text {
  color: #64748B;
  font-size: 14px;
}

.upload-text em {
  color: #2563EB;
  font-style: normal;
}

.empty-state {
  text-align: center;
  padding: 48px 20px;
  background: #F8FAFC;
  border: 1px dashed #E2E8F0;
  border-radius: 12px;
  margin-top: 16px;
}

.empty-icon-wrap {
  width: 64px;
  height: 64px;
  background: #FFFFFF;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.empty-icon {
  font-size: 28px;
  color: #94A3B8;
}

.empty-state p {
  color: #94A3B8;
}

.mapping-tips {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 16px;
  padding: 12px 16px;
  background: #F0F9FF;
  border-radius: 8px;
  color: #0369A1;
  font-size: 13px;
}

.quick-links {
  display: flex;
  gap: 16px;
}
</style>
