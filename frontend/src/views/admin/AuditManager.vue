<template>
  <el-card header="待审核航司管理员申请">
    <el-table :data="list" stripe empty-text="暂无待审核申请">
      <el-table-column prop="name" label="姓名"></el-table-column>
      <el-table-column prop="phone" label="手机号"></el-table-column>
      <el-table-column prop="airlineCode" label="申请航司代码">
        <template slot-scope="scope">
          <el-tag type="warning">{{ scope.row.airlineCode }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template slot-scope="scope">
          <el-button type="success" size="mini" @click="doAudit(scope.row, 'ACTIVE')">批准入驻</el-button>
          <el-button type="danger" size="mini" @click="doAudit(scope.row, 'REJECTED')">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
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
    }
  }
};
</script>