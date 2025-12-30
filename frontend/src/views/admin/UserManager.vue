<template>
  <div>
    <el-card shadow="never">
      <div slot="header" class="clearfix">
        <span>全平台用户管理</span>
        <el-input
            placeholder="搜索手机号"
            v-model="searchPhone"
            size="small"
            style="width: 200px; float: right"
            clearable
            @clear="fetchUsers"
        >
          <el-button slot="append" icon="el-icon-search" @click="fetchUsers"></el-button>
        </el-input>
      </div>

      <el-table :data="users" stripe v-loading="loading">
        <el-table-column prop="userId" label="ID" width="80"></el-table-column>
        <el-table-column prop="name" label="姓名" width="120"></el-table-column>
        <el-table-column prop="phone" label="手机号" width="150"></el-table-column>
        <el-table-column prop="idCard" label="身份证号" width="180"></el-table-column>

        <el-table-column label="会员等级" width="150">
          <template slot-scope="scope">
            <el-select
                v-model="scope.row.membershipLevel"
                size="mini"
                @change="updateLevel(scope.row)"
                :disabled="scope.row.role !== 'ROLE_USER'"
            >
              <el-option label="普通" value="普通"></el-option>
              <el-option label="银卡" value="银卡"></el-option>
              <el-option label="金卡" value="金卡"></el-option>
              <el-option label="白金" value="白金"></el-option>
            </el-select>
          </template>
        </el-table-column>

        <el-table-column prop="role" label="角色">
          <template slot-scope="scope">
            <el-tag :type="getRoleTagType(scope.row.role)" size="small">
              {{ formatRole(scope.row.role) }}
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
import api from '@/api';

export default {
  data() {
    return {
      users: [],
      searchPhone: '',
      loading: false
    };
  },
  created() {
    this.fetchUsers();
  },
  methods: {
    async fetchUsers() {
      this.loading = true;
      try {
        const res = await api.getAllUsers(this.searchPhone);
        if (res.code === 200) {
          this.users = res.data;
        }
      } catch (e) {
        this.$message.error('加载用户列表失败');
      } finally {
        this.loading = false;
      }
    },
    async updateLevel(row) {
      try {
        await api.updateUserMembership(row.userId, row.membershipLevel);
        this.$message.success(`用户 ${row.name} 的等级已更新`);
      } catch (e) {
        this.$message.error('更新失败');
        this.fetchUsers(); // 回滚状态
      }
    },
    resetPwd(row) {
      this.$confirm(`确定将用户 ${row.name} 的密码重置为 123456 吗？`, '高危操作', {
        type: 'warning'
      }).then(async () => {
        try {
          await api.resetUserPassword(row.userId);
          this.$message.success('密码重置成功');
        } catch (e) {
          this.$message.error('重置失败');
        }
      }).catch(() => {});
    },
    getRoleTagType(role) {
      if (role === 'ROLE_PLATFORM_ADMIN') return 'danger';
      if (role === 'ROLE_AIRLINE_ADMIN') return 'warning';
      return 'info';
    },
    formatRole(role) {
      const map = {
        'ROLE_USER': '普通用户',
        'ROLE_PLATFORM_ADMIN': '平台管理员',
        'ROLE_AIRLINE_ADMIN': '航司管理员'
      };
      return map[role] || role;
    }
  }
};
</script>