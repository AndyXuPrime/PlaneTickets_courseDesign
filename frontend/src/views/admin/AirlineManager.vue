<template>
  <div class="airline-manager">
    <el-card shadow="never">
      <div slot="header" class="header-wrapper">
        <span><i class="el-icon-office-building"></i> 合作航空公司管理</span>
        <el-button type="primary" size="small" icon="el-icon-plus" @click="showAddDialog = true">新增航司</el-button>
      </div>

      <el-table :data="airlines" border stripe v-loading="loading">
        <el-table-column label="航司 Logo" width="120" align="center">
          <template slot-scope="scope">
            <el-image
                style="width: 60px; height: 60px; border-radius: 4px; border: 1px solid #eee"
                :src="isValidUrl(scope.row.logoUrl) ? scope.row.logoUrl : ''"
                fit="contain">
              <div slot="error" class="image-slot">
                <i class="el-icon-picture-outline"></i>
              </div>
              <div slot="placeholder" class="image-slot">
                <i class="el-icon-loading"></i>
              </div>
            </el-image>
          </template>
        </el-table-column>

        <el-table-column prop="airlineCode" label="代码" width="80" align="center">
          <template slot-scope="scope">
            <el-tag effect="plain">{{ scope.row.airlineCode }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="airlineName" label="航司名称" min-width="150"></el-table-column>
        <el-table-column prop="country" label="国家" width="120"></el-table-column>
        <el-table-column prop="contactPhone" label="联系电话" width="150"></el-table-column>

        <el-table-column label="操作" width="150" align="center">
          <template slot-scope="scope">
            <el-upload
                action="http://localhost:8080/api/files/upload?bizType=airline"
                :show-file-list="false"
                :on-success="(res) => handleLogoSuccess(res, scope.row)"
                :headers="uploadHeaders">
              <el-button type="text" size="small" icon="el-icon-upload">更换Logo</el-button>
            </el-upload>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增弹窗 -->
    <el-dialog title="入驻新航司" :visible.sync="showAddDialog" width="500px" :close-on-click-modal="false">
      <el-form :model="form" label-width="100px" ref="addForm">
        <el-form-item label="二字码" required>
          <el-input v-model="form.airlineCode" placeholder="如: CA, MU" maxlength="2" show-word-limit></el-input>
        </el-form-item>
        <el-form-item label="航司名称" required>
          <el-input v-model="form.airlineName" placeholder="如: 中国国际航空"></el-input>
        </el-form-item>
        <el-form-item label="国家">
          <el-input v-model="form.country" placeholder="默认为中国"></el-input>
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.contactPhone"></el-input>
        </el-form-item>
        <el-form-item label="官网">
          <el-input v-model="form.website" placeholder="www.example.com"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="submitAdd" :loading="submitting">确认入驻</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import api from '@/api';

export default {
  data() {
    return {
      airlines: [],
      loading: false,
      showAddDialog: false,
      submitting: false,
      form: {
        airlineCode: '',
        airlineName: '',
        country: '中国',
        contactPhone: '',
        website: ''
      },
      uploadHeaders: { Authorization: 'Bearer ' + localStorage.getItem('authToken') }
    };
  },
  created() { this.fetchAirlines(); },
  methods: {
    isValidUrl(url) {
      return url && url !== 'null' && url !== '';
    },
    async fetchAirlines() {
      this.loading = true;
      try {
        const res = await api.getAllAirlines();
        if (res.code === 200) this.airlines = res.data;
      } finally { this.loading = false; }
    },
    async handleLogoSuccess(res, row) {
      if (res.code === 200) {
        const newLogoUrl = res.data;
        await api.updateAirlineLogo(row.airlineCode, newLogoUrl);
        row.logoUrl = newLogoUrl;
        this.$message.success('Logo 更新成功');
      }
    },
    async submitAdd() {
      // 1. 基础校验
      if (!this.form.airlineCode || !this.form.airlineName) {
        return this.$message.warning('请填写二字码和航司名称');
      }

      this.submitting = true;
      try {
        // 2. 调用 API (确保 api/index.js 中有 createAirline)
        const res = await api.createAirline(this.form);

        if (res.code === 200) {
          this.$message.success('航司入驻成功');
          this.showAddDialog = false;
          this.fetchAirlines(); // 刷新列表
          // 重置表单
          this.form = { airlineCode: '', airlineName: '', country: '中国', contactPhone: '', website: '' };
        }
      } catch (e) {
        // 3. 错误处理 (如果后端返回 400/403，这里会捕获)
        console.error(e);
        // 注意：这里不需要手动设置 errorMessage，因为 axios 拦截器通常会弹出 Message.error
      } finally {
        this.submitting = false;
      }
    }
  }
};
</script>

<style scoped>
.header-wrapper { display: flex; justify-content: space-between; align-items: center; }
.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 20px;
}
</style>