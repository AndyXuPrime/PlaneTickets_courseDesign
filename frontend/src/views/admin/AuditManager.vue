<template>
  <div class="audit-page">
    <el-card shadow="never" class="structural-card" :body-style="{ padding: '0' }">
      <div slot="header" class="card-header">
        <div class="header-left">
          <div class="deco-block"></div>
          <span class="title">待审核航司管理员申请</span>
        </div>
        <div class="header-right">
          <span class="sub-text">PENDING APPROVALS</span>
          <el-tag size="mini" effect="dark" color="#003ea1" style="border:none; border-radius:0;">
            {{ list.length }} REQUESTS
          </el-tag>
        </div>
      </div>

      <el-table
          :data="list"
          stripe
          empty-text="暂无待审核申请"
          class="structural-table"
          :header-cell-style="headerCellStyle"
      >
        <el-table-column label="APPLICANT / 申请人" min-width="140">
          <template slot-scope="scope">
            <div class="applicant-info">
              <i class="el-icon-user-solid" style="color: #003ea1;"></i>
              <span class="name-text">{{ scope.row.name }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="CONTACT / 手机号" min-width="140">
          <template slot-scope="scope">
            <span class="mono-text">{{ scope.row.phone }}</span>
          </template>
        </el-table-column>

        <el-table-column label="AIRLINE CODE / 航司代码" min-width="160">
          <template slot-scope="scope">
            <div class="code-wrapper">
              <span class="code-label">CODE:</span>
              <span class="code-value">{{ scope.row.airlineCode }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="ACTIONS / 操作  " width="240" align="right">
          <template slot-scope="scope">
            <el-button
                class="struct-btn btn-approve"
                size="mini"
                icon="el-icon-check"
                @click="doAudit(scope.row, 'ACTIVE')"
            >批准</el-button>
            <el-button
                class="struct-btn btn-reject"
                size="mini"
                icon="el-icon-close"
                @click="doAudit(scope.row, 'REJECTED')"
            >驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import api from '@/api';
export default {
  data() { return { list: [] }; },
  created() { this.fetchList(); },
  methods: {
    async fetchList() {
      const res = await api.getPendingAdmins();
      if (res.code === 200) this.list = res.data;
    },
    async doAudit(row, status) {
      const res = await api.auditUser(row.userId, status);
      if (res.code === 200) {
        this.$message.success('操作成功');
        this.fetchList();
      }
    },
    // 表头样式配置
    headerCellStyle() {
      return {
        backgroundColor: '#f4f6f9',
        color: '#606266',
        fontWeight: '600',
        borderBottom: '2px solid #003ea1',
        fontSize: '12px',
        letterSpacing: '0.5px'
      };
    }
  }
};
</script>

<style scoped>
.audit-page {
  padding: 20px;
}

.structural-card {
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  position: relative;
  overflow: visible;
}

.structural-card::before {
  content: '';
  position: absolute;
  top: -1px;
  left: -1px;
  right: -1px;
  height: 4px;
  background: linear-gradient(90deg, #003ea1 0%, #0cadc6 100%);
  z-index: 1;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
}

.header-left {
  display: flex;
  align-items: center;
}

.deco-block {
  width: 4px;
  height: 24px;
  background-color: #003ea1;
  margin-right: 12px;
}

.title {
  font-size: 18px;
  font-weight: 700;
  color: #2c3e50;
  letter-spacing: 0.5px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.sub-text {
  font-size: 12px;
  color: #909399;
  font-weight: 600;
  letter-spacing: 1px;
}

.applicant-info {
  display: flex;
  align-items: center;
  gap: 8px;
}
.name-text {
  font-weight: 600;
  color: #303133;
}

.mono-text {
  font-family: 'Consolas', 'Monaco', monospace;
  color: #606266;
  background: #f4f4f5;
  padding: 2px 6px;
  border-radius: 2px;
}

.code-wrapper {
  display: inline-flex;
  align-items: center;
  border: 1px solid #003ea1;
  background-color: #f0f7ff;
  height: 28px;
}
.code-label {
  background-color: #003ea1;
  color: #fff;
  font-size: 10px;
  padding: 0 6px;
  height: 100%;
  display: flex;
  align-items: center;
  font-weight: bold;
}
.code-value {
  color: #003ea1;
  font-weight: 700;
  padding: 0 10px;
  font-family: 'Arial Black', sans-serif;
}

.struct-btn {
  border-radius: 0;
  font-weight: 600;
  border: none;
  transition: all 0.3s;
  padding: 7px 15px;
}

.btn-approve {
  background: linear-gradient(135deg, #003ea1 0%, #0cadc6 100%);
  color: white;
}
.btn-approve:hover {
  opacity: 0.9;
  transform: translateY(-1px);
  box-shadow: 0 4px 10px rgba(12, 173, 198, 0.3);
}

.btn-reject {
  background-color: #ea0040;
  color: white;
  margin-left: 10px;
}
.btn-reject:hover {
  background-color: #c90035;
  transform: translateY(-1px);
  box-shadow: 0 4px 10px rgba(234, 0, 64, 0.3);
}

.structural-table ::v-deep .el-table__body tr:hover > td {
  background-color: #f0f9fb !important;
}
</style>