<template>
  <div>
    <el-card shadow="never">
      <div slot="header" class="clearfix">
        <span>全平台用户管理</span>
        <el-input placeholder="搜索手机号" v-model="searchPhone" size="small" style="width: 200px; float: right">
          <el-button slot="append" icon="el-icon-search"></el-button>
        </el-input>
      </div>

      <el-table :data="users" stripe>
        <el-table-column prop="userId" label="ID" width="80"></el-table-column>
        <el-table-column prop="name" label="姓名" width="120"></el-table-column>
        <el-table-column prop="phone" label="手机号" width="150"></el-table-column>
        <el-table-column label="会员等级" width="150">
          <template slot-scope="scope">
            <el-select v-model="scope.row.membershipLevel" size="mini" @change="updateLevel(scope.row)">
              <el-option label="普通" value="普通"></el-option>
              <el-option label="银卡" value="银卡"></el-option>
              <el-option label="金卡" value="金卡"></el-option>
              <el-option label="白金" value="白金"></el-option>
            </el-select>
          </template>
        </el-table-column>
        <el-table-column prop="role" label="角色">
          <template slot-scope="scope">
            <el-tag :type="scope.row.role === 'ROLE_USER' ? 'info' : 'danger'" size="small">
              {{ scope.row.role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <el-button type="warning" size="mini" plain @click="resetPwd(scope.row)">重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
export default {
  data() { return { users: [], searchPhone: '' }; },
  created() { this.fetchUsers(); },
  methods: {
    fetchUsers() {
      // 演示数据，实际需调用 auth-service 接口
      this.users = [
        { userId: 1, name: '张三', phone: '13800138000', membershipLevel: '普通', role: 'ROLE_USER' },
        { userId: 2, name: '李四', phone: '13900139000', membershipLevel: '白金', role: 'ROLE_USER' }
      ];
    },
    updateLevel(row) {
      this.$message.success(`用户 ${row.name} 的等级已调整为 ${row.membershipLevel}`);
    },
    resetPwd(row) {
      this.$confirm(`确定将用户 ${row.name} 的密码重置为 123456 吗？`, '警告').then(() => {
        this.$message.success('密码重置成功');
      });
    }
  }
};
</script>




