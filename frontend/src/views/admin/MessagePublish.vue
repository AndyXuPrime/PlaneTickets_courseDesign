<template>
  <div class="message-manager">
    <!-- 发布消息表单 -->
    <el-card shadow="never">
      <div slot="header">
        <span>发布系统广播</span>
        <el-tag size="small" type="info" style="margin-left: 10px">全员可见</el-tag>
      </div>

      <el-form :model="form" label-width="80px" style="max-width: 600px;">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="请输入通知标题 (如: 系统维护通知)"></el-input>
        </el-form-item>

        <el-form-item label="内容" required>
          <el-input
              type="textarea"
              :rows="6"
              v-model="form.content"
              placeholder="请输入通知详情..."
          ></el-input>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" icon="el-icon-s-promotion" @click="onSubmit" :loading="loading">立即发布</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 历史消息列表 -->
    <el-card shadow="never" style="margin-top: 20px;">
      <div slot="header"><span>历史消息记录</span></div>
      <el-table :data="historyMessages" stripe size="small">
        <el-table-column prop="title" label="标题" width="200"></el-table-column>
        <el-table-column prop="content" label="内容" show-overflow-tooltip></el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180">
          <template slot-scope="scope">{{ formatTime(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column prop="publisher" label="发布人" width="100"></el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import api from '@/api';

export default {
  data() {
    return {
      form: { title: '', content: '' },
      loading: false,
      historyMessages: []
    };
  },
  created() {
    this.fetchHistory();
  },
  methods: {
    async onSubmit() {
      if (!this.form.title || !this.form.content) {
        return this.$message.warning('请填写完整的标题和内容');
      }
      this.loading = true;
      try {
        await api.publishMessage(this.form);
        this.$message.success('广播发布成功');
        this.form.title = '';
        this.form.content = '';
        this.fetchHistory();
      } catch (e) {
        // 拦截器已处理错误提示
      } finally {
        this.loading = false;
      }
    },
    async fetchHistory() {
      try {
        const res = await api.getSystemMessages();
        if (res.code === 200) {
          this.historyMessages = res.data;
        }
      } catch (e) {
        console.error('获取历史消息失败:', e);
      }
    },
    formatTime(time) {
      return time ? new Date(time).toLocaleString() : '';
    }
  }
};
</script>

<style scoped>
.message-manager { padding: 20px; }
</style>