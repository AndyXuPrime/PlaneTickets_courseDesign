<template>
  <div class="message-manager">
    <el-row :gutter="24">
      <!-- 左侧：发布面板 -->
      <el-col :span="9">
        <div class="structural-card form-panel">
          <div class="card-header">
            <div class="header-title">
              <i class="el-icon-s-operation" style="color: #0050b2; margin-right: 8px;"></i>
              <span>发布广播</span>
            </div>
            <div class="header-subtitle">BROADCAST CONTROL</div>
          </div>

          <div class="card-body">
            <el-alert
                title="此消息将推送至所有用户终端，请谨慎操作。"
                type="info"
                show-icon
                :closable="false"
                class="structural-alert"
            ></el-alert>

            <el-form :model="form" label-position="top" class="structural-form">
              <el-form-item label="MESSAGE TITLE / 标题">
                <el-input v-model="form.title" placeholder="输入简明扼要的标题..." class="sharp-input"></el-input>
              </el-form-item>

              <el-form-item label="CONTENT DETAILS / 内容详情">
                <el-input
                    type="textarea"
                    :rows="8"
                    v-model="form.content"
                    placeholder="在此输入通知正文..."
                    class="sharp-input"
                    resize="none"
                ></el-input>
              </el-form-item>

              <el-form-item style="margin-top: 30px; margin-bottom: 0;">
                <el-button type="primary" class="structural-btn" @click="onSubmit" :loading="loading" style="width: 100%">
                  <i class="el-icon-s-promotion"></i> 确认发布 / PUBLISH
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </el-col>

      <!-- 右侧：历史记录 -->
      <el-col :span="15">
        <div class="structural-card log-panel">
          <div class="card-header">
            <div class="header-title">
              <i class="el-icon-s-order" style="color: #777b7c; margin-right: 8px;"></i>
              <span>历史日志</span>
            </div>
            <div class="header-subtitle">HISTORY LOGS</div>
          </div>

          <div class="card-body padding-0">
            <el-table
                :data="historyMessages"
                style="width: 100%"
                :header-cell-style="tableHeaderStyle"
                :cell-style="tableCellStyle"
                class="structural-table"
            >
              <el-table-column label="DATE / 时间" width="160">
                <template slot-scope="scope">
                  <span class="mono-text">{{ formatTime(scope.row.createTime) }}</span>
                </template>
              </el-table-column>

              <el-table-column label="INFO / 信息" min-width="200">
                <template slot-scope="scope">
                  <div class="msg-title">{{ scope.row.title }}</div>
                  <div class="msg-content">{{ scope.row.content }}</div>
                </template>
              </el-table-column>

              <el-table-column label="USER / 发布人" width="120" align="center">
                <template slot-scope="scope">
                  <el-tag size="mini" effect="plain" class="publisher-tag">{{ scope.row.publisher }}</el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-col>
    </el-row>
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
        console.error('消息发布失败:', e);
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
      if (!time) return '--';
      const d = new Date(time);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
    },
    // 表格样式配置函数
    tableHeaderStyle() {
      return {
        background: '#f5f7fa',
        color: '#777b7c',
        fontWeight: '600',
        borderBottom: '2px solid #e4e7ed',
        fontSize: '12px',
        letterSpacing: '0.5px'
      };
    },
    tableCellStyle() {
      return {
        borderBottom: '1px solid #ebeef5',
        padding: '12px 0'
      };
    }
  }
};
</script>

<style scoped>
.message-manager {
}

.structural-card {
  background: #feffff;
  border: 1px solid #dcdfe6;
  border-radius: 5px;
  position: relative;
  overflow: hidden;
  height: 100%;
  box-shadow: 0 2px 0 rgba(0,0,0,0.02);
}

.structural-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 3px;
  background: #0050b2;
  z-index: 1;
}

.card-header {
  padding: 20px 24px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
}

.header-title {
  font-size: 16px;
  font-weight: 700;
  color: #24282a;
  display: flex;
  align-items: center;
}

.header-subtitle {
  font-size: 10px;
  color: #b0b4b8;
  letter-spacing: 1px;
  font-weight: 600;
  font-family: 'Helvetica Neue', Arial, sans-serif;
}

.card-body { padding: 24px; }
.card-body.padding-0 { padding: 0; }

.structural-alert {
  background-color: #f0f9eb;
  border: 1px solid #b3e19d;
  color: #67c23a;
  margin-bottom: 24px;
  border-radius: 2px;
}
.structural-alert ::v-deep .el-alert__title { font-size: 12px; }

.structural-form ::v-deep .el-form-item__label {
  color: #777b7c;
  font-size: 12px;
  font-weight: 600;
  line-height: 1.5;
  padding-bottom: 8px;
}

.sharp-input ::v-deep .el-input__inner,
.sharp-input ::v-deep .el-textarea__inner {
  border-radius: 0; /* 直角 */
  border: 1px solid #dcdfe6;
  background-color: #fcfcfc;
  color: #24282a;
  transition: all 0.3s;
}

.sharp-input ::v-deep .el-input__inner:focus,
.sharp-input ::v-deep .el-textarea__inner:focus {
  border-color: #0050b2;
  background-color: #fff;
  box-shadow: inset 3px 0 0 #0050b2;
}

.structural-btn {
  border-radius: 0;
  background-color: #0050b2;
  border-color: #0050b2;
  font-weight: 600;
  letter-spacing: 1px;
  height: 44px;
  transition: all 0.2s;
}
.structural-btn:hover {
  background-color: #003d8a;
  transform: translateY(-1px);
  box-shadow: 0 4px 0 rgba(0,0,0,0.1);
}

.mono-text {
  font-family: 'Consolas', 'Monaco', monospace;
  color: #777b7c;
  font-size: 12px;
  background: #f4f4f5;
  padding: 2px 6px;
  border-radius: 2px;
}

.msg-title {
  font-weight: 600;
  color: #24282a;
  margin-bottom: 4px;
  font-size: 14px;
}

.msg-content {
  color: #606266;
  font-size: 12px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.publisher-tag {
  border-radius: 2px;
  border-color: #dcdfe6;
  color: #606266;
  font-weight: 600;
}

@media screen and (max-width: 1200px) {
  .el-col { width: 100% !important; margin-bottom: 20px; }
}
</style>