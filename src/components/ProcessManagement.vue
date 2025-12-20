<template>
  <div class="process-management-container">
    <h2>产品管理</h2>
    <el-card shadow="hover" class="content-card">
      <!-- 新增产品和批量删除按钮 -->
      <div class="button-group">
        <el-button type="primary" @click="showAddProductDialog = true" class="add-product-btn">
          新增产品
        </el-button>
        <el-button 
          type="danger" 
          @click="handleBatchDeleteProducts" 
          :disabled="selectedProducts.length === 0"
          :loading="batchDeleting.value"
        >
          批量删除选中产品
        </el-button>
        <el-button 
          type="success" 
          @click="showImportDialog = true"
        >
          一键导入产品
        </el-button>
        <el-button 
          type="info" 
          @click="openHistoryDialog"
        >
          导入历史记录
        </el-button>
      </div>
      
      <!-- 产品表格 -->
      <el-table
        :data="products"
        style="width: 100%"
        @selection-change="handleSelectionChange"
        stripe
        max-height="600px"
        :header-cell-style="{ backgroundColor: '#f5f7fa' }"
        :loading="loading"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="productName" label="品名" min-width="200" />
        <el-table-column prop="productType" label="图号" min-width="150" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(scope.row)"
              :loading="deletingIds.includes(scope.row.id)"
            >
              删除
            </el-button>
            <el-button
              type="primary"
              size="small"
              @click="handleViewProcesses(scope.row)"
            >
              查看工序
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页组件 -->
      <div class="pagination-container">
        <el-pagination
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
          background
          prev-text="上一页"
          next-text="下一页"
          jump-text="跳至"
          page-sizes-text="每页条数"
          total-text="共"
        />
      </div>
    </el-card>

    <!-- 新增产品弹窗 -->
    <el-dialog
      v-model="showAddProductDialog"
      title="新增产品"
      width="1000px"
      :before-close="handleCloseAddProduct"
    >
      <el-form :model="newProduct" label-width="80px" size="small">
        <el-form-item label="品名">
          <el-input v-model="newProduct.productName" placeholder="请输入品名" />
        </el-form-item>
        <el-form-item label="图号">
          <el-input v-model="newProduct.productType" placeholder="请输入图号" />
        </el-form-item>

        <h4>工序信息</h4>
        <el-table
          :data="newProduct.processes"
          style="width: 100%"
          stripe
          class="new-product-processes-table"
        >
          <el-table-column prop="order" label="顺序" width="120">
            <template #default="scope">
              <el-input-number
                v-model="scope.row.order"
                :min="1"
                @change="updateProcessOrder"
                style="width: 100%"
              />
            </template>
          </el-table-column>
          <el-table-column prop="processID" label="工序号" min-width="150">
            <template #default="scope">
              <el-input 
                v-model="scope.row.processID" 
                placeholder="请输入工序号" 
                style="width: 100%"
              />
            </template>
          </el-table-column>
          <el-table-column prop="process" label="加工工序" min-width="200">
            <template #default="scope">
              <el-input 
                v-model="scope.row.process" 
                placeholder="请输入加工工序" 
                style="width: 100%"
              />
            </template>
          </el-table-column>
          <el-table-column prop="debugTime" label="调机时间" min-width="120">
            <template #default="scope">
              <el-input-number 
                v-model="scope.row.debugTime" 
                :min="0" 
                :step="0.5" 
                :precision="1" 
                style="width: 100%"
              />
            </template>
          </el-table-column>
          <el-table-column prop="workPoints" label="单件工分" min-width="120">
            <template #default="scope">
              <el-input-number 
                v-model="scope.row.workPoints" 
                :min="0" 
                :step="0.1" 
                :precision="1" 
                style="width: 100%"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="scope">
              <el-button type="danger" size="small" @click="removeProcess(scope.$index)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-button type="success" @click="addProcess" class="add-process-btn">
          添加工序
        </el-button>

        <div class="form-actions">
          <el-button type="primary" @click="handleSaveProduct">保存</el-button>
          <el-button @click="handleCloseAddProduct">取消</el-button>
        </div>
      </el-form>
    </el-dialog>

    <!-- 导入历史记录弹窗 -->
    <el-dialog
      v-model="showHistoryDialog"
      title="导入历史记录"
      width="1000px"
    >
      <el-table
        :data="importHistories"
        style="width: 100%"
        stripe
        :loading="loadingHistories"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="fileName" label="文件名" min-width="200" />
        <el-table-column prop="importTime" label="导入时间" width="200" :formatter="(row) => new Date(row.importTime).toLocaleString()" />
        <el-table-column prop="totalCount" label="总记录数" width="100" />
        <el-table-column prop="successCount" label="成功数" width="100" />
        <el-table-column prop="errorCount" label="失败数" width="100" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag 
              :type="scope.row.status === 'SUCCESS' ? 'success' : scope.row.status === 'FAILED' ? 'danger' : 'warning'"
            >
              {{ scope.row.status === 'SUCCESS' ? '成功' : scope.row.status === 'FAILED' ? '失败' : '部分成功' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="结果消息" min-width="200" />
        <el-table-column prop="operator" label="操作人" width="100" />
      </el-table>
    </el-dialog>

    <!-- 导入产品弹窗 -->
    <el-dialog
      v-model="showImportDialog"
      title="一键导入产品"
      width="1000px"
      :before-close="handleCloseImport"
    >
      <!-- 文件上传区域 -->
      <div class="import-content">
        <el-upload
          ref="uploadRef"
          class="upload-demo"
          drag
          action=""
          :auto-upload="false"
          :on-change="handleFileChange"
          :limit="1"
          accept=".xlsx,.xls"
        >
          <i class="el-icon-upload"></i>
          <div class="el-upload__text">
            将文件拖到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              仅支持 .xlsx 和 .xls 格式文件，且不超过 10MB
            </div>
          </template>
        </el-upload>
        
        <!-- 数据预览区域 -->
        <div v-if="previewData.length > 0" class="preview-section">
          <h4>数据预览</h4>
          <el-table
            :data="previewData"
            style="width: 100%"
            stripe
            max-height="400"
          >
            <el-table-column prop="rowNumber" label="行号" width="80" />
            <el-table-column prop="productType" label="图号" min-width="150" />
            <el-table-column prop="productName" label="品名" min-width="200" />
            <el-table-column prop="processID" label="工序号" min-width="120" />
            <el-table-column prop="process" label="加工工序" min-width="200" />
            <el-table-column prop="process_order" label="加工次序" width="100" />
            <el-table-column prop="debugTime" label="准备时间(分钟)" width="120" />
            <el-table-column prop="workPoints" label="定额工时(分钟)" width="120" />
            <el-table-column prop="errors" label="错误信息" min-width="200" type="danger">
              <template #default="scope">
                <el-tag v-if="scope.row.errors && scope.row.errors.length > 0" type="danger" size="small">
                  {{ scope.row.errors.join(', ') }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          
          <!-- 错误统计 -->
          <div v-if="hasErrors()" class="error-summary">
            <el-alert
              :title="'发现 ' + getErrorCount() + ' 条错误记录'"
              type="error"
              :closable="false"
            />
          </div>
        </div>
      </div>
      
      <!-- 导入操作按钮 -->
      <div class="form-actions">
          <el-button type="primary" @click="handleImport" :loading="importing" :disabled="importing">
            {{ importing ? '导入中...' : '开始导入' }}
          </el-button>
          <el-button type="danger" @click="cancelImport" :disabled="!importing">
            取消导入
          </el-button>
          <el-button @click="handleCloseImport" :disabled="importing">关闭</el-button>
        </div>
        
        <!-- 导入进度条 -->
        <div v-if="importing" class="import-progress">
          <el-progress 
            :percentage="importProgress" 
            status="active"
            :stroke-width="20"
          ></el-progress>
          <p>正在导入... {{ importProgress }}%</p>
        </div>
    </el-dialog>

    <!-- 查看工序弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="工序信息"
      width="800px"
      :before-close="handleClose"
    >
      <div v-if="currentProduct">
        <h3>{{ currentProduct.productName }} ({{ currentProduct.productType }})</h3>
        
        <!-- 工序操作栏 -->
        <div class="process-actions">
          <el-button type="primary" @click="addEmptyProcess">新增工序</el-button>
          <el-button type="success" @click="saveNewProcesses" :disabled="!hasNewProcesses()">保存新增</el-button>
          <el-button type="danger" @click="handleDeleteSelectedProcesses" :disabled="selectedProcesses.length === 0">
            删除选中
          </el-button>
        </div>

        <!-- 工序列表 -->
        <el-table
          :data="currentProduct.processes"
          style="width: 100%"
          @selection-change="handleProcessSelectionChange"
          stripe
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="processID" label="工序号" min-width="120">
            <template #default="scope">
              <el-input
                v-if="editingProcesses.includes(scope.row.id) || !scope.row.id"
                v-model="scope.row.processID"
                placeholder="请输入工序号"
                :disabled="!!scope.row.id && !editingProcesses.includes(scope.row.id)"
              />
              <span v-else>{{ scope.row.processID }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="process" label="加工工序" min-width="200">
            <template #default="scope">
              <el-input
                v-if="editingProcesses.includes(scope.row.id) || !scope.row.id"
                v-model="scope.row.process"
                placeholder="请输入加工工序"
                :disabled="!!scope.row.id && !editingProcesses.includes(scope.row.id)"
              />
              <span v-else>{{ scope.row.process }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="debugTime" label="调机时间" width="100">
            <template #default="scope">
              <el-input-number
                v-if="editingProcesses.includes(scope.row.id) || !scope.row.id"
                v-model="scope.row.debugTime"
                :min="0"
                :step="0.5"
                :precision="1"
                :disabled="!!scope.row.id && !editingProcesses.includes(scope.row.id)"
              />
              <span v-else>{{ scope.row.debugTime }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="workPoints" label="单件工分" width="100">
            <template #default="scope">
              <el-input-number
                v-if="editingProcesses.includes(scope.row.id) || !scope.row.id"
                v-model="scope.row.workPoints"
                :min="0"
                :step="0.5"
                :precision="1"
                :disabled="!!scope.row.id && !editingProcesses.includes(scope.row.id)"
              />
              <span v-else>{{ scope.row.workPoints }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="order" label="顺序" width="80">
            <template #default="scope">
              <el-input-number
                v-if="editingProcesses.includes(scope.row.id) || !scope.row.id"
                v-model="scope.row.order"
                :min="1"
                :disabled="!!scope.row.id && !editingProcesses.includes(scope.row.id)"
              />
              <span v-else>{{ scope.row.order }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <template v-if="scope.row.id">
                <el-button
                  v-if="!editingProcesses.includes(scope.row.id)"
                  type="primary"
                  size="small"
                  @click="handleEditProcess(scope.row)"
                >
                  编辑
                </el-button>
                <el-button
                  v-else
                  type="success"
                  size="small"
                  @click="handleCancelEdit(scope.row)"
                >
                  完成
                </el-button>
              </template>
              <el-button
                v-else
                type="danger"
                size="small"
                @click="removeEmptyProcess(scope.$index)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

export default {
  name: 'ProcessManagement',
  setup() {
    const products = ref([])
    const selectedProducts = ref([])
    const deletingIds = ref([])
    const dialogVisible = ref(false)
    const currentProduct = ref(null)
    const selectedProcesses = ref([])
    const editingProcesses = ref([])
    const batchDeleting = ref(false)
    const showAddProductDialog = ref(false)
    
    // 分页相关变量
    const pageSize = ref(10)
    const currentPage = ref(1)
    const total = ref(0)
    const loading = ref(false)
    const newProduct = ref({
      productName: '',
      productType: '',
      processes: []
    })
    
    // 导入功能相关变量
    const showImportDialog = ref(false)
    const uploadedFile = ref(null)
    const previewData = ref([])
    const importing = ref(false)
    const importProgress = ref(0)
    const uploadXhr = ref(null)
    const uploadRef = ref(null)
    
    // 导入历史记录相关变量
    const showHistoryDialog = ref(false)
    const importHistories = ref([])
    const loadingHistories = ref(false)

    // 加载产品列表
    const loadProducts = async () => {
      try {
        loading.value = true
        // 前端页码从1开始，后端从0开始，需要转换
        const page = currentPage.value - 1
        const response = await fetch(`/api/parts?page=${page}&size=${pageSize.value}`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        if (!response.ok) {
          throw new Error('Failed to fetch products')
        }
        const data = await response.json()
        if (data.success) {
          products.value = data.products
          total.value = data.total
        }
      } catch (error) {
        console.error('Error loading products:', error)
        ElMessage.error('加载产品列表失败')
      } finally {
        loading.value = false
      }
    }
    
    // 处理分页变化
    const handlePageChange = (val) => {
      currentPage.value = val
      loadProducts()
    }
    
    // 处理页大小变化
    const handleSizeChange = (val) => {
      pageSize.value = val
      currentPage.value = 1
      loadProducts()
    }

    // 处理选择变化
    const handleSelectionChange = (selection) => {
      selectedProducts.value = selection
    }

    // 删除产品
    const handleDelete = async (product) => {
      try {
        await ElMessageBox.confirm(
          `确定要删除产品 "${product.productName}" 吗？此操作将同时删除该产品的所有工序。`,
          '确认删除',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        
        deletingIds.value.push(product.id)
        
        const response = await fetch(`/api/parts/${product.id}`, {
          method: 'DELETE',
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        
        if (!response.ok) {
          throw new Error('Failed to delete product')
        }
        
        ElMessage.success('产品删除成功')
        loadProducts() // 重新加载产品列表
      } catch (error) {
        if (error !== 'cancel') {
          console.error('Error deleting product:', error)
          ElMessage.error('删除产品失败')
        }
      } finally {
        deletingIds.value = deletingIds.value.filter(id => id !== product.id)
      }
    }

    // 批量删除产品
    const handleBatchDeleteProducts = async () => {
      try {
        await ElMessageBox.confirm(
          `确定要删除选中的 ${selectedProducts.value.length} 个产品吗？此操作将同时删除这些产品的所有工序。`,
          '确认批量删除',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        
        batchDeleting.value = true
        
        // 循环删除每个选中的产品
        for (const product of selectedProducts.value) {
          deletingIds.value.push(product.id)
          
          const response = await fetch(`/api/parts/${product.id}`, {
            method: 'DELETE',
            headers: {
              'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
          })
          
          if (!response.ok) {
            throw new Error(`Failed to delete product ${product.id}`)
          }
          
          deletingIds.value = deletingIds.value.filter(id => id !== product.id)
        }
        
        ElMessage.success('批量删除产品成功')
        selectedProducts.value = [] // 清空选中列表
        loadProducts() // 重新加载产品列表
      } catch (error) {
        if (error !== 'cancel') {
          console.error('Error batch deleting products:', error)
          ElMessage.error('批量删除产品失败')
        }
      } finally {
        batchDeleting.value = false
        deletingIds.value = [] // 清空删除中的ID列表
      }
    }

    // 查看工序
    const handleViewProcesses = (product) => {
      currentProduct.value = product
      dialogVisible.value = true
    }

    // 关闭弹窗
    const handleClose = () => {
      dialogVisible.value = false
      setTimeout(() => {
        currentProduct.value = null
        selectedProcesses.value = []
        editingProcesses.value = []
      }, 300)
    }

    // 处理工序选择变化
    const handleProcessSelectionChange = (selection) => {
      selectedProcesses.value = selection
    }

    // 编辑工序
    const handleEditProcess = (process) => {
      editingProcesses.value.push(process.id)
    }

    // 取消编辑
    const handleCancelEdit = (process) => {
      editingProcesses.value = editingProcesses.value.filter(id => id !== process.id)
    }

    // 更新工序
    const handleUpdateProcess = async (process) => {
      try {
        const response = await fetch(`/api/processes/${process.id}`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          },
          body: JSON.stringify({
            processID: process.processID,
            process: process.process,
            debugTime: process.debugTime,
            workPoints: process.workPoints,
            order: process.order
          })
        })
        
        if (!response.ok) {
          throw new Error('Failed to update process')
        }
        
        ElMessage.success('工序更新成功')
      } catch (error) {
        console.error('Error updating process:', error)
        ElMessage.error('更新工序失败')
      }
    }

    // 添加空的工序行
    const addEmptyProcess = () => {
      if (!currentProduct.value.processes) {
        currentProduct.value.processes = []
      }
      // 计算新工序的默认顺序
      const maxOrder = currentProduct.value.processes.reduce((max, process) => {
        return Math.max(max, process.order || 0)
      }, 0)
      // 添加空工序行
      currentProduct.value.processes.push({
        processID: '',
        process: '',
        debugTime: 0,
        workPoints: 0,
        order: maxOrder + 1
      })
    }

    // 移除空的工序行
    const removeEmptyProcess = (index) => {
      currentProduct.value.processes.splice(index, 1)
    }

    // 检查是否有新工序需要保存
    const hasNewProcesses = () => {
      if (!currentProduct.value.processes) return false
      return currentProduct.value.processes.some(process => !process.id)
    }

    // 保存所有新增的工序
    const saveNewProcesses = async () => {
      try {
        // 验证所有新工序
        const newProcesses = currentProduct.value.processes.filter(process => !process.id)
        
        for (const process of newProcesses) {
          if (!process.processID || !process.process) {
            ElMessage.warning('所有新增工序的工序号和加工工序不能为空')
            return
          }
        }

        // 批量保存新工序
        const savedProcesses = []
        for (const process of newProcesses) {
          const response = await fetch('/api/workpoints', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
              'Authorization': `Bearer ${localStorage.getItem('token')}`
            },
            body: JSON.stringify({
              ...process,
              partId: currentProduct.value.id,
              productName: currentProduct.value.productName,
              productType: currentProduct.value.productType
            })
          })
          
          if (!response.ok) {
            throw new Error('Failed to add process')
          }
          
          const data = await response.json()
          savedProcesses.push(data.process)
        }

        // 更新本地数据
        currentProduct.value.processes = currentProduct.value.processes
          .filter(process => process.id) // 移除所有未保存的空行
          .concat(savedProcesses) // 添加保存成功的工序

        ElMessage.success(`成功保存 ${savedProcesses.length} 道工序`)
      } catch (error) {
        console.error('Error saving new processes:', error)
        ElMessage.error('保存新增工序失败')
      }
    }

    // 新增产品相关方法
    const addProcess = () => {
      newProduct.value.processes.push({
        processID: '',
        process: '',
        debugTime: 0,
        workPoints: 0,
        order: newProduct.value.processes.length + 1
      })
    }

    const removeProcess = (index) => {
      newProduct.value.processes.splice(index, 1)
      // 更新顺序
      newProduct.value.processes.forEach((process, idx) => {
        process.order = idx + 1
      })
    }

    const updateProcessOrder = () => {
      // 可以添加排序逻辑，如果需要的话
    }

    const handleCloseAddProduct = () => {
      showAddProductDialog.value = false
      setTimeout(() => {
        newProduct.value = {
          productName: '',
          productType: '',
          processes: []
        }
      }, 300)
    }

    const handleSaveProduct = async () => {
      try {
        // 验证产品信息
        if (!newProduct.value.productName || !newProduct.value.productType) {
          ElMessage.warning('品名和图号不能为空')
          return
        }

        // 验证工序信息
        if (newProduct.value.processes.length === 0) {
          ElMessage.warning('请至少添加一个工序')
          return
        }

        for (const process of newProduct.value.processes) {
          if (!process.processID || !process.process) {
            ElMessage.warning('所有工序的工序号和加工工序不能为空')
            return
          }
        }

        // 创建产品
        const productResponse = await fetch('/api/parts', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          },
          body: JSON.stringify({
            productName: newProduct.value.productName,
            productType: newProduct.value.productType
          })
        })

        if (!productResponse.ok) {
          throw new Error('Failed to create product')
        }

        const productData = await productResponse.json()
        if (!productData.success) {
          throw new Error(productData.message || '产品创建失败')
        }

        const productId = productData.part.id

        // 创建工序
        for (const process of newProduct.value.processes) {
          const processResponse = await fetch('/api/workpoints', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
              'Authorization': `Bearer ${localStorage.getItem('token')}`
            },
            body: JSON.stringify({
              productName: newProduct.value.productName,
              productType: newProduct.value.productType,
              partId: productId,
              processID: process.processID,
              process: process.process,
              debugTime: process.debugTime,
              workPoints: process.workPoints,
              order: process.order
            })
          })

          if (!processResponse.ok) {
            throw new Error('Failed to create process')
          }

          const processData = await processResponse.json()
          if (!processData.success) {
            throw new Error(processData.message || '工序创建失败')
          }
        }

        ElMessage.success('产品创建成功')
        handleCloseAddProduct()
        loadProducts() // 重新加载产品列表
      } catch (error) {
        console.error('Error saving product:', error)
        ElMessage.error(error.message || '保存产品失败')
      }
    }

    // 删除选中工序
    const handleDeleteSelectedProcesses = async () => {
      try {
        if (selectedProcesses.value.length === 0) {
          ElMessage.warning('请选择要删除的工序')
          return
        }
        
        await ElMessageBox.confirm(
          '确定要删除选中的工序吗？',
          '确认删除',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        
        // 获取要删除的工序ID列表
        const processIds = selectedProcesses.value.map(p => p.id)
        
        // 逐个删除工序
        for (const processId of processIds) {
          const response = await fetch(`/api/processes/${processId}`, {
            method: 'DELETE',
            headers: {
              'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
          })
          
          if (!response.ok) {
            throw new Error('Failed to delete process')
          }
        }
        
        // 更新本地数据
        currentProduct.value.processes = currentProduct.value.processes.filter(
          p => !processIds.includes(p.id)
        )
        
        selectedProcesses.value = []
        ElMessage.success('工序删除成功')
      } catch (error) {
        if (error !== 'cancel') {
          console.error('Error deleting processes:', error)
          ElMessage.error('删除工序失败')
        }
      }
    }
    
    // 导入功能相关函数
    
    // 处理文件上传变化
    const handleFileChange = (file) => {
      uploadedFile.value = file.raw
      previewData.value = []
      
      // 检查文件类型
      const validTypes = ['application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 'application/vnd.ms-excel']
      if (!validTypes.includes(file.raw.type)) {
        ElMessage.error('请上传正确的Excel文件格式 (.xlsx 或 .xls)')
        uploadRef.value.clearFiles()
        return
      }
      
      // 检查文件大小（限制为10MB）
      const maxSize = 10 * 1024 * 1024 // 10MB
      if (file.raw.size > maxSize) {
        ElMessage.error('文件大小不能超过10MB')
        uploadRef.value.clearFiles()
        return
      }
      
      // 读取文件内容
      const reader = new FileReader()
      reader.onload = async () => {
        try {
          // 发送文件到后端进行预览
          const formData = new FormData()
          formData.append('file', file.raw)
          
          const response = await fetch('/api/parts/preview-import', {
            method: 'POST',
            headers: {
              'Authorization': `Bearer ${localStorage.getItem('token')}`
            },
            body: formData
          })
          
          if (!response.ok) {
            throw new Error(`预览失败: ${response.status} ${response.statusText}`)
          }
          
          const data = await response.json()
          if (data.success) {
            previewData.value = data.previewData || []
            
            // 检查是否有错误记录
            if (hasErrors()) {
              ElMessage.warning(`发现 ${getErrorCount()} 条错误记录，请检查后再导入`)
            } else {
              ElMessage.success('文件预览成功，没有发现错误记录')
            }
          } else {
            ElMessage.error(`预览失败: ${data.message || '未知错误'}`)
          }
        } catch (error) {
          console.error('Error previewing file:', error)
          ElMessage.error(`文件预览失败: ${error.message}`)
          previewData.value = []
        }
      }
      
      reader.onerror = (error) => {
        console.error('FileReader error:', error)
        ElMessage.error('文件读取失败: ' + error.target.error.message)
        if (uploadRef.value) {
          uploadRef.value.clearFiles()
        }
      }
      
      reader.onabort = () => {
        ElMessage.warning('文件读取已取消')
        if (uploadRef.value) {
          uploadRef.value.clearFiles()
        }
      }
      
      // 读取文件
      reader.readAsArrayBuffer(file.raw)
    }
    
    // 检查是否有错误记录
    const hasErrors = () => {
      return previewData.value.some(item => item.errors && item.errors.length > 0)
    }
    
    // 获取错误记录数量
    const getErrorCount = () => {
      return previewData.value.filter(item => item.errors && item.errors.length > 0).length
    }
    
    // 处理导入操作
    const handleImport = () => {
      if (!uploadedFile.value) {
        ElMessage.warning('请先上传文件')
        return
      }
      
      // 如果有错误记录，提示用户是否继续导入
      if (hasErrors()) {
        ElMessageBox.confirm(`发现 ${getErrorCount()} 条错误记录，继续导入将只导入正确的数据，是否继续？`, '导入确认', {
          confirmButtonText: '继续导入',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          performImport()
        }).catch(() => {
          // 用户取消导入
        })
      } else {
        // 没有错误记录，直接询问是否导入
        ElMessageBox.confirm('确定要导入这些数据吗？', '导入确认', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          performImport()
        }).catch(() => {
          // 用户取消导入
        })
      }
    }
    
    // 执行导入操作
    const performImport = () => {
      // 创建XMLHttpRequest对象以支持进度显示
      const xhr = new XMLHttpRequest()
      uploadXhr.value = xhr
      importProgress.value = 0
      importing.value = true
      
      // 配置请求
      xhr.open('POST', '/api/parts/import')
      xhr.setRequestHeader('Authorization', `Bearer ${localStorage.getItem('token')}`)
      
      // 监听上传进度
      xhr.upload.onprogress = (event) => {
        if (event.lengthComputable) {
          importProgress.value = Math.round((event.loaded / event.total) * 100)
        }
      }
      
      // 监听请求完成
      xhr.onload = () => {
        importing.value = false
        uploadXhr.value = null
        
        try {
          if (xhr.status === 200) {
            const data = JSON.parse(xhr.responseText)
            if (data.success) {
              // 根据导入结果显示不同的成功消息
              if (data.successCount === (data.successCount + data.errorCount)) {
                ElMessage.success(`数据导入成功！共导入 ${data.successCount} 条记录`)
              } else {
                ElMessage.success(`数据导入完成！成功导入 ${data.successCount} 条记录，失败 ${data.errorCount} 条记录`)
              }
              
              handleCloseImport()
              loadProducts() // 刷新数据
              // 加载最新的导入历史记录
              loadImportHistories()
            } else {
              ElMessage.error(`数据导入失败: ${data.message || '未知错误'}`)
            }
          } else {
            ElMessage.error(`导入请求失败: ${xhr.status} ${xhr.statusText}`)
          }
        } catch (error) {
          console.error('Error parsing import response:', error)
          ElMessage.error('导入响应解析失败，请检查网络连接')
        }
      }
      
      // 监听请求错误
      xhr.onerror = (error) => {
        console.error('Import request error:', error)
        importing.value = false
        uploadXhr.value = null
        ElMessage.error('网络错误，导入失败')
      }
      
      // 监听请求中断
      xhr.onabort = () => {
        importing.value = false
        uploadXhr.value = null
        ElMessage.warning('导入已取消')
      }
      
      // 发送请求
      const formData = new FormData()
      formData.append('file', uploadedFile.value)
      xhr.send(formData)
    }
    
    // 取消导入操作
    const cancelImport = () => {
      if (uploadXhr.value) {
        uploadXhr.value.abort()
      }
    }
    
    // 关闭导入对话框
    const handleCloseImport = () => {
      showImportDialog.value = false
      uploadedFile.value = null
      previewData.value = []
      importing.value = false
      if (uploadRef.value) {
        uploadRef.value.clearFiles()
      }
    }

    // 获取导入历史记录
    const loadImportHistories = async () => {
      try {
        loadingHistories.value = true
        const response = await fetch('/api/import-history', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        
        if (!response.ok) {
          throw new Error('获取导入历史记录失败')
        }
        
        const data = await response.json()
        importHistories.value = data
      } catch (error) {
        console.error('Error loading import histories:', error)
        ElMessage.error('获取导入历史记录失败: ' + error.message)
      } finally {
        loadingHistories.value = false
      }
    }
    
    // 打开导入历史记录对话框
    const openHistoryDialog = () => {
      showHistoryDialog.value = true
      loadImportHistories()
    }
    
    // 页面加载时获取数据
    onMounted(() => {
      loadProducts()
    })

    return {
      products,
      selectedProducts,
      deletingIds,
      dialogVisible,
      currentProduct,
      selectedProcesses,
      editingProcesses,
      batchDeleting,
      showAddProductDialog,
      // 分页相关
      pageSize,
      currentPage,
      total,
      loading,
      handleSizeChange,
      handlePageChange,
      newProduct,
      handleSelectionChange,
      handleDelete,
      handleBatchDeleteProducts,
      handleViewProcesses,
      handleClose,
      handleProcessSelectionChange,
      handleEditProcess,
      handleCancelEdit,
      handleUpdateProcess,
      handleDeleteSelectedProcesses,
      addProcess,
      removeProcess,
      updateProcessOrder,
      handleCloseAddProduct,
      handleSaveProduct,
      addEmptyProcess,
      removeEmptyProcess,
      saveNewProcesses,
      hasNewProcesses,
      // 导入功能相关
      showImportDialog,
      uploadedFile,
      previewData,
      importing,
      importProgress,
      uploadXhr,
      uploadRef,
      handleFileChange,
      hasErrors,
      getErrorCount,
      handleImport,
      cancelImport,
      handleCloseImport,
      // 导入历史记录相关
      showHistoryDialog,
      importHistories,
      loadingHistories,
      openHistoryDialog,
      loadImportHistories
    }
  }
}
</script>

<style scoped>
.process-management-container {
  height: 100%;
  padding: 20px;
}

.content-card {
  margin-top: 20px;
}

.process-actions {
  margin-bottom: 15px;
  display: flex;
  gap: 10px;
}

.add-process-card {
  margin-bottom: 20px;
}

.form-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
  justify-content: flex-end;
}

.button-group {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
  align-items: center;
}

.add-product-btn {
}

.new-product-processes-table {
  margin-bottom: 15px;
}

.add-process-btn {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
}
</style>