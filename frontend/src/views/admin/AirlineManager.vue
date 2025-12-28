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
              <!-- 正在加载时的占位 -->
              <div slot="placeholder" class="image-slot">
                <i class="el-icon-loading"></i>
              </div>
            </el-image>
          </template>
        </el-table-column>

        <!-- 其他列保持不变 -->
        <el-table-column prop="airlineCode" label="代码" width="80" align="center"></el-table-column>
        <el-table-column prop="airlineName" label="航司名称" min-width="150"></el-table-column>
        <el-table-column prop="country" label="国家" width="120"></el-table-column>
        <el-table-column prop="contactPhone" label="联系电话" width="150"></el-table-column>

        <el-table-column label="操作" width="150" align="center">
          <template slot-scope="scope">
            <el-upload
                action="http://localhost:8080/api/files/upload"
                :show-file-list="false"
                :on-success="(res) => handleLogoSuccess(res, scope.row)"
                :headers="uploadHeaders">
              <el-button type="text" size="small" icon="el-icon-upload">更换Logo</el-button>
            </el-upload>
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
      airlines: [],
      loading: false,
      showAddDialog: false,
      uploadHeaders: { Authorization: 'Bearer ' + localStorage.getItem('authToken') }
    };
  },
  created() { this.fetchAirlines(); },
  methods: {
    // 【新增】校验 URL 是否有效
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
  font-size: 24px;
}
</style>