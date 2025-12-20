<template>
  <div class="workpoint-approval-container">
    <!-- 将标题移到整体界面左上角 -->
    <div class="page-title-container">
      <h2 class="page-title">工序工分核准</h2>
    </div>
    
    <el-card class="content-card">
      <!-- 移除原来的card-header -->
      
      <!-- 搜索区域 -->
      <div class="search-container">
        <div class="search-item">
          <el-input
            v-model="searchProductName"
            placeholder="按品名搜索"
            class="search-input"
            @input="handleSearchInput"
            clearable
          >
            <template #append>
              <el-button @click="handleSearch">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>
        </div>
        <div class="search-item">
          <el-input
            v-model="searchProductType"
            placeholder="按图号搜索"
            class="search-input"
            @input="handleSearchInput"
            clearable
          >
            <template #append>
              <el-button @click="handleSearch">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>
        </div>
      </div>
      
      <!-- 表格区域 -->
        <div class="table-container">
          <el-table 
            :data="pagedParts" 
            :row-key="getRowKey"
            border 
            style="width: 100%"
            :loading="loading"
          >
          <!-- 零件行 -->
          <el-table-column prop="productName" label="品名" width="150">
            <template #default="scope">
              <span class="part-name">{{ scope.row.productName }}</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="productType" label="图号" width="200"></el-table-column>
          
          <el-table-column label="工序号" width="120">
            <template #default="scope">
              <span v-if="scope.row.processes"></span>
              <span v-else>{{ scope.row.processID }}</span>
            </template>
          </el-table-column>
          
          <el-table-column label="工序" width="200">
            <template #default="scope">
              <span v-if="scope.row.processes"></span>
              <span v-else>{{ scope.row.process }}</span>
            </template>
          </el-table-column>
          
          <el-table-column label="调机时间(分钟)" width="130">
            <template #default="scope">
              <div v-if="!scope.row.processes" class="editable-cell">
                <span 
                  v-if="!editingCells.includes(scope.row.id + '-debugTime')" 
                  @dblclick="startEdit(scope.row.id, 'debugTime')"
                  class="cell-value"
                >
                  {{ scope.row.debugTime }}
                </span>
                <el-input 
                  v-else 
                  v-model.number="scope.row.debugTime" 
                  @blur="saveEdit(scope.row.id, 'debugTime')"
                  @keyup.enter="saveEdit(scope.row.id, 'debugTime')"
                  type="number"
                  min="0"
                  size="small"
                  class="edit-input"
                />
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="工分" width="160">
            <template #default="scope">
              <div v-if="!scope.row.processes" class="editable-cell">
                <span 
                  v-if="!editingCells.includes(scope.row.id + '-workPoints')" 
                  @dblclick="startEdit(scope.row.id, 'workPoints')"
                  class="cell-value"
                >
                  {{ scope.row.workPoints }}
                </span>
                <el-input 
                  v-else 
                  v-model.number="scope.row.workPoints" 
                  @blur="saveEdit(scope.row.id, 'workPoints')"
                  @keyup.enter="saveEdit(scope.row.id, 'workPoints')"
                  type="number"
                  min="0"
                  step="0.1"
                  size="small"
                  class="edit-input"
                />
              </div>
            </template>
          </el-table-column>
          
          <!-- 主表格中的操作列 -->
          <el-table-column label="操作" width="200" align="center">
            <template #default="scope">
              <div class="approval-actions">
                <el-button 
                  v-if="!scope.row.isApproved" 
                  type="success" 
                  size="small" 
                  @click="approvePart(scope.row.id)"
                >
                  核准
                </el-button>
                <el-button 
                  v-else 
                  type="danger" 
                  size="small" 
                  @click="unapprovePart(scope.row.id)"
                >
                  取消核准
                </el-button>
              </div>
            </template>
          </el-table-column>
          
          <!-- 工序子表格 -->
          <el-table-column type="expand" width="50">
            <template #default="scope">
              <div class="expanded-processes">
                <el-table 
                  :data="scope.row.processes" 
                  border 
                  style="width: 100%"
                  show-header="true"
                >
                  <el-table-column label="工序号" width="110">
                    <template #default="subScope">
                      <div class="process-value">{{ subScope.row.processID }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="工序" width="180">
                    <template #default="subScope">
                      <div class="process-value">{{ subScope.row.process }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="调机时间(分钟)" width="160">
                  <template #default="subScope">
                    <div class="editable-cell">
                      <span 
                        v-if="!editingCells.includes(subScope.row.id + '-debugTime')" 
                        @dblclick="startEdit(subScope.row.id, 'debugTime')"
                        class="cell-value"
                      >
                        {{ subScope.row.debugTime }}
                      </span>
                      <el-input 
                        v-else 
                        v-model.number="subScope.row.debugTime" 
                        @blur="saveEdit(subScope.row.id, 'debugTime')"
                        @keyup.enter="saveEdit(subScope.row.id, 'debugTime')"
                        type="number"
                        min="0"
                        size="small"
                        class="edit-input"
                      />
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="工分" width="160" header-align=center>
                  <template #default="subScope">
                    <div class="editable-cell">
                      <span 
                        v-if="!editingCells.includes(subScope.row.id + '-workPoints')" 
                        @dblclick="startEdit(subScope.row.id, 'workPoints')"
                        class="cell-value"
                      >
                        {{ subScope.row.workPoints }}
                      </span>
                      <el-input 
                        v-else 
                        v-model.number="subScope.row.workPoints" 
                        @blur="saveEdit(subScope.row.id, 'workPoints')"
                        @keyup.enter="saveEdit(subScope.row.id, 'workPoints')"
                        type="number"
                        min="0"
                        step="0.1"
                        size="small"
                        class="edit-input"
                      />
                    </div>
                  </template>
                </el-table-column>
                <el-table-column width="200" align="center">
                  <template #default>
                    <!-- 子表格中不再显示核准按钮 -->
                  </template>
                </el-table-column>
              </el-table>
              </div>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页组件 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="pagination.currentPage"
            v-model:page-size="pagination.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="pagination.total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            prev-text="上一页"
            next-text="下一页"
            jump-text="跳至"
            page-sizes-text="每页条数"
            total-text="共"
            background
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search } from '@element-plus/icons-vue';

export default {
  name: 'WorkPointApproval',
  components: {
    Search
  },
  data() {
      return {
        parts: [], // 零件列表（包含工序）
        filteredParts: [], // 过滤后的零件列表
        editingCells: [], // 当前正在编辑的单元格
        originalValues: {}, // 编辑前的原始值，用于取消编辑时恢复
        loading: false, // 加载状态
        
        // 搜索相关
        searchProductName: '', // 按品名搜索关键词
        searchProductType: '', // 按图号搜索关键词
        
        // 分页相关
        pagination: {
          currentPage: 1, // 当前页码
          pageSize: 10, // 每页记录数
          total: 0 // 总记录数
        }
      }
  },
  mounted() {
    this.fetchParts(); // 加载零件数据
    
    // 添加点击外部区域退出编辑模式的事件监听
    this.documentClickHandler = (e) => {
      // 检查点击是否发生在编辑输入框外部
      if (!e.target.closest('.edit-input') && this.editingCells.length > 0) {
        // 克隆当前编辑单元格列表，避免在循环中修改原数组
        const editingCellsCopy = [...this.editingCells];
        editingCellsCopy.forEach(cellKey => {
          const [id, field] = cellKey.split('-');
          this.saveEdit(id, field);
        });
      }
    };
    
    document.addEventListener('click', this.documentClickHandler);
  },
  
  beforeUnmount() {
    // 移除事件监听器，避免内存泄漏
    document.removeEventListener('click', this.documentClickHandler);
  },
  computed: {
    // 分页后的零件数据
    pagedParts() {
      const start = (this.pagination.currentPage - 1) * this.pagination.pageSize;
      const end = start + this.pagination.pageSize;
      return this.filteredParts.slice(start, end);
    }
  },
  methods: {
    // 获取零件列表（包含工序）
    fetchParts() {
      this.loading = true;
      // 获取本地存储的token
      const token = localStorage.getItem('token');
      
      fetch('/api/parts/with-processes?page=0&size=1000', {
        headers: {
          'Authorization': token ? `Bearer ${token}` : ''
        }
      })
        .then(response => response.json())
        .then(data => {
          if (data.success) {
            this.parts = data.parts;
            this.filteredParts = [...this.parts]; // 初始时显示所有数据
            // 更新总记录数和重置分页
            this.pagination.total = this.filteredParts.length;
            this.pagination.currentPage = 1;
            console.log('获取到的零件数据:', data.parts);
            if (data.parts.length > 0) {
              console.log('第一个零件的数据结构:', data.parts[0]);
              if (data.parts[0].processes && data.parts[0].processes.length > 0) {
                console.log('工序的数据结构:', data.parts[0].processes[0]);
              }
            }
          } else {
            ElMessage.error('获取数据失败: ' + data.message);
          }
        })
        .catch(error => {
          console.error('获取数据失败:', error);
          ElMessage.error('获取数据失败，请重试');
        })
        .finally(() => {
          this.loading = false;
        });
    },
    
    // 搜索输入处理
    handleSearchInput() {
      // 实时搜索
      this.performSearch();
    },
    
    // 执行搜索
    handleSearch() {
      this.performSearch(); // 重新获取数据
    },
    
    // 执行搜索逻辑
    performSearch() {
      const nameKeyword = (this.searchProductName || '').toLowerCase().trim();
      const typeKeyword = (this.searchProductType || '').toLowerCase().trim();
      
      // 如果两个搜索框都为空，则显示所有数据
      if (!nameKeyword && !typeKeyword) {
        this.filteredParts = [...this.parts];
      } else {
        this.filteredParts = this.parts.filter(part => {
          // 直接检查零件本身的品名和图号
          const partName = (part.productName || '').toLowerCase();
          const partType = (part.productType || '').toLowerCase();
          const matchesName = nameKeyword ? partName.includes(nameKeyword) : true;
          const matchesType = typeKeyword ? partType.includes(typeKeyword) : true;
          return matchesName && matchesType;
        });
      }
      
      // 更新总记录数和重置分页
      this.pagination.total = this.filteredParts.length;
      this.pagination.currentPage = 1;
    },
    
    // 分页相关方法
    handleSizeChange(newSize) {
      this.pagination.pageSize = newSize;
      this.pagination.currentPage = 1; // 切换每页条数时回到第一页
    },
    
    handleCurrentChange(newPage) {
      this.pagination.currentPage = newPage;
    },
    
    // 开始编辑单元格
    startEdit(processId, field) {
      const key = processId + '-' + field;
      this.editingCells.push(key);
      
      // 保存原始值
      const process = this.findProcessById(processId);
      if (process) {
        this.originalValues[key] = process[field];
      }
    },
    
    // 保存编辑
    saveEdit(processId, field) {
      const key = processId + '-' + field;
      const process = this.findProcessById(processId);
      
      if (process) {
        // 验证输入值
        if (process[field] === null || process[field] === '') {
          // 恢复原始值
          process[field] = this.originalValues[key];
          ElMessage.warning('值不能为空');
        } else {
          // 保存到服务器
          this.updateProcess(processId, { [field]: process[field] });
        }
      }
      
      // 移除编辑状态
      const index = this.editingCells.indexOf(key);
      if (index > -1) {
        this.editingCells.splice(index, 1);
      }
      
      // 清除原始值
      delete this.originalValues[key];
    },
    
    // 根据ID查找工序
    findProcessById(processId) {
      for (const part of this.parts) {
        if (part.id === processId) {
          return part;
        }
        if (part.processes) {
          const process = part.processes.find(p => p.id === processId);
          if (process) return process;
        }
      }
      return null;
    },
    
    // 更新工序数据
    updateProcess(processId, data) {
      // 获取本地存储的token
      const token = localStorage.getItem('token');
      
      fetch(`/api/processes/${processId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': token ? `Bearer ${token}` : ''
        },
        body: JSON.stringify(data)
      })
      .then(response => response.json())
      .then(result => {
        if (result.success) {
          ElMessage.success('更新成功');
        } else {
          ElMessage.error('更新失败: ' + result.message);
          // 刷新数据以确保一致性
          this.fetchParts();
        }
      })
      .catch(error => {
        console.error('更新失败:', error);
        ElMessage.error('更新失败，请重试');
        // 刷新数据以确保一致性
        this.fetchParts();
      });
    },
    
    // 核准零件
    approvePart(partId) {
      ElMessageBox.confirm('确定要核准该零件吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 获取本地存储的token
        const token = localStorage.getItem('token');
        
        fetch(`/api/parts/${partId}/approve`, {
          method: 'POST',
          headers: {
            'Authorization': token ? `Bearer ${token}` : ''
          }
        })
        .then(response => response.json())
        .then(result => {
          if (result.success) {
            // 更新本地数据
            const part = this.parts.find(p => p.id === partId);
            if (part) {
              part.isApproved = 1;
              part.approvedBy = result.part.approvedBy;
              part.approvedAt = result.part.approvedAt;
            }
            ElMessage.success('核准成功');
          } else {
            ElMessage.error('核准失败: ' + result.message);
          }
        })
        .catch(error => {
          console.error('核准失败:', error);
          ElMessage.error('核准失败，请重试');
        });
      }).catch(() => {
        // 取消操作
      });
    },
    
    // 取消核准零件
    unapprovePart(partId) {
      ElMessageBox.confirm('确定要取消核准该零件吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 获取本地存储的token
        const token = localStorage.getItem('token');
        
        fetch(`/api/parts/${partId}/unapprove`, {
          method: 'POST',
          headers: {
            'Authorization': token ? `Bearer ${token}` : ''
          }
        })
        .then(response => response.json())
        .then(result => {
          if (result.success) {
            // 更新本地数据
            const part = this.parts.find(p => p.id === partId);
            if (part) {
              part.isApproved = 0;
              part.approvedBy = null;
              part.approvedAt = null;
            }
            ElMessage.success('取消核准成功');
          } else {
            ElMessage.error('取消核准失败: ' + result.message);
          }
        })
        .catch(error => {
          console.error('取消核准失败:', error);
          ElMessage.error('取消核准失败，请重试');
        });
      }).catch(() => {
        // 取消操作
      });
    },
    
    // 获取行键值
    getRowKey(row) {
      return row.id;
    }
  }
}
</script>

<style scoped>
.workpoint-approval-container {
  padding: 20px;
  min-height: 100vh;
  background-color: #f5f7fa;
}

/* 页面标题样式 */
.page-title-container {
  margin-bottom: 20px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.content-card {
  margin-bottom: 20px;
  width: 100%;
}

/* 移除原来的card-header样式 */
.card-header {
  display: none;
}

/* 搜索区域样式 */
.search-container {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.search-item {
  /* 恢复默认样式 */
}

.search-input {
  /* 恢复默认样式 */
}

/* 表格容器样式 */
.table-container {
  max-height: 600px;
  overflow-y: auto;
  margin-bottom: 20px;
  width: 100%;
}

/* 分页组件样式 */
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* 调整表格列间距和宽度 */
:deep(.el-table__header-wrapper th.el-table__cell) {
  padding: 12px 20px;
}

:deep(.el-table__body-wrapper td.el-table__cell) {
  padding: 12px 20px;
}

/* 调整表格列宽度 */
:deep(.el-table__column[width="200"]) {
  width: 220px !important;
}

:deep(.el-table__column[width="150"]) {
  width: 180px !important;
}

:deep(.el-table__column[width="120"]) {
  width: 140px !important;
}

:deep(.el-table__column[width="130"]) {
  width: 160px !important;
}

:deep(.el-table__column[width="100"]) {
  width: 130px !important;
}

:deep(.el-table__body-wrapper tr) {
  transition: all 0.3s ease;
}

:deep(.el-table__body-wrapper tr:hover) {
  background-color: #f5f7fa !important;
}

/* 零件行样式 */
:deep(.el-table__body-wrapper tr:nth-child(odd) .el-table__cell) {
  background-color: #fafafa;
}

/* 核准状态样式 */
:deep(.el-table__body-wrapper tr[data-row-key="approved"] .el-table__cell) {
  background-color: rgba(147, 235, 234, 0.3);
}

/* 零件名称样式 */
.part-name {
  font-weight: 600;
  color: #303133;
}

/* 可编辑单元格样式 */
.editable-cell {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
}

.cell-value {
  display: block;
  width: 100%;
  padding: 12px 8px;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.3s ease;
  text-align: center;
  box-sizing: border-box;
}

.cell-value:hover {
  background-color: #f5f7fa;
  color: #409EFF;
}

.edit-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  outline: none;
  transition: border-color 0.2s;
  box-sizing: border-box;
  text-align: center;
}

.edit-input:focus {
  border-color: #409EFF;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

/* 按钮样式 */
.approval-actions {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.expanded-processes {
  margin-left: 350px; /* 与主表的工序号标题对齐（品名200px + 图号150px） */
  margin-top: 10px;
  margin-bottom: 10px;
}

/* 工序信息样式 */
.process-info {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.process-label {
  font-size: 12px;
  color: #606266;
  margin-bottom: 4px;
}

.process-value {
  font-size: 14px;
  color: #303133;
}

:deep(.el-button) {
  transition: all 0.3s ease;
}

:deep(.el-button--success:hover) {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(103, 194, 58, 0.4);
}

:deep(.el-button--danger:hover) {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(245, 108, 108, 0.4);
}
</style>