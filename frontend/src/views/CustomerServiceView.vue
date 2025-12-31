<template>
  <div class="customer-service-page">
    <!-- 头部横幅 -->
    <div class="page-hero">
      <div class="hero-content">
        <div class="hero-badge">HELP CENTER</div>
        <h1>客户服务中心</h1>
        <p class="hero-para">专业、贴心、高效，蓝天航空全天候伴您同行</p>
      </div>
    </div>

    <div class="main-container">
      <el-row :gutter="30">
        <!-- 左侧：FAQ 与 服务内容 -->
        <el-col :span="16">
          <!-- 常见问题部分 -->
          <section id="faq-section" class="service-section">
            <div class="section-title-group">
              <svg viewBox="0 0 24 24" class="quotes-icon">
                <path fill="currentColor" d="M4.58341 17.3211C3.55316 16.2274 3 15 3 13.0103C3 9.51086 5.45651 6.37366 9.03059 4.82318L9.92328 6.20079C6.58804 8.00539 5.93618 10.346 5.67564 11.822C6.21263 11.5443 6.91558 11.4466 7.60471 11.5105C9.40908 11.6778 10.8312 13.159 10.8312 15C10.8312 16.933 9.26416 18.5 7.33116 18.5C6.2581 18.5 5.23196 18.0095 4.58341 17.3211ZM14.5834 17.3211C13.5532 16.2274 13 15 13 13.0103C13 9.51086 15.4565 6.37366 19.0306 4.82318L19.9233 6.20079C16.588 8.00539 15.9362 10.346 15.6756 11.822C16.2126 11.5443 16.9156 11.4466 17.6047 11.5105C19.4091 11.6778 20.8312 13.159 20.8312 15C20.8312 16.933 19.2642 18.5 17.3312 18.5C16.2581 18.5 15.232 18.0095 14.5834 17.3211Z"></path>
              </svg>
              <h2>常见问题解答</h2>
            </div>

            <el-collapse v-model="activeFaq" accordion class="custom-collapse">
              <el-collapse-item name="1" ref="faq-booking" title="如何在线预订机票及确认行程？">
                <div class="faq-content">
                  <p class="para">请在首页输入出发地、目的地及日期。确保乘机人证件信息准确无误。支付成功后，系统将在30分钟内完成出票，您可于“个人中心”查看13位票号。</p>
                </div>
              </el-collapse-item>

              <el-collapse-item name="2" ref="faq-refund" title="机票退改签政策及费用说明">
                <div class="faq-content">
                  <p class="para">蓝天航空实行分时段退改政策。起飞前7天外变更费率较低；特价客票（如G/Z舱）通常不支持退票。如遇天气原因导致航班取消，可申请全额退款。</p>
                </div>
              </el-collapse-item>

              <el-collapse-item name="3" ref="faq-membership" title="蓝天云悦会员体系与积分奖励">
                <div class="faq-content">
                  <p class="para">每消费 1 元积 1 分。银卡及以上会员享受额外15%-50%积分奖励、优先登机及机场贵宾室服务。积分可用于兑换免费升舱或精美礼品。</p>
                </div>
              </el-collapse-item>

              <el-collapse-item name="4" ref="faq-others" title="特殊旅客服务（婴儿、轮椅、老人）">
                <div class="faq-content">
                  <p class="para">婴儿旅客需购买出生满14天的婴儿票。轮椅服务请在起飞前48小时申请。对于独自旅行的儿童或老人，我们提供全流程引导陪同服务。</p>
                </div>
              </el-collapse-item>
            </el-collapse>
          </section>

          <!-- 联系方式 -->
          <section id="contact-section" ref="faq-contact" class="service-section">
            <div class="section-title-group">
              <h2>联系我们</h2>
            </div>
            <div class="contact-grid">
              <div class="contact-styled-card">
                <div class="c-header">
                  <strong>24h 服务热线</strong>
                  <p class="para">全国统一客服电话</p>
                </div>
                <div class="c-footer">
                  <span class="c-value">400-123-4567</span>
                  <button class="c-button" @click="callPhone">立即拨打</button>
                </div>
              </div>
              <div class="contact-styled-card">
                <div class="c-header">
                  <strong>官方服务邮箱</strong>
                  <p class="para">投诉、建议及商务合作</p>
                </div>
                <div class="c-footer">
                  <span class="c-value">service@bluesky.com</span>
                  <button class="c-button">发送邮件</button>
                </div>
              </div>
            </div>
          </section>
        </el-col>

        <!-- 右侧：快捷工具 -->
        <el-col :span="8">
          <div class="side-sticky">
            <div class="side-tool-card">
              <div class="card-header">自助快捷服务</div>
              <div class="tool-links">
                <a @click="showBaggagePolicy" class="tool-item">
                  <i class="el-icon-suitcase"></i>
                  <span>行李额度规定</span>
                </a>
                <router-link to="/#" class="tool-item">
                  <i class="el-icon-document-checked"></i>
                  <span>机票验真</span>
                </router-link>
                <router-link to="/#" class="tool-item">
                  <i class="el-icon-position"></i>
                  <span>航班动态查询</span>
                </router-link>
              </div>
            </div>

            <div class="side-tool-card m-t-20">
              <div class="card-header">服务承诺</div>
              <div class="promise-box">
                <div class="p-item"><i class="el-icon-success"></i> 官方直售，价格透明</div>
                <div class="p-item"><i class="el-icon-success"></i> 极速出票，行程无忧</div>
                <div class="p-item"><i class="el-icon-success"></i> 隐私保护，数据加密</div>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 行李政策弹窗 -->
    <el-dialog title="蓝天航空行李运输政策说明" :visible.sync="baggageVisible" width="600px" custom-class="baggage-dialog">
      <div class="baggage-info">
        <h3>1. 随身携带物品（免费额度）</h3>
        <p class="para">每位乘客可携带一件重量不超过 5kg，体积不超过 20×40×55cm 的手提行李。头等舱乘客可携带两件。</p>

        <h3>2. 托运行李限额（成人/儿童）</h3>
        <table class="policy-table">
          <thead>
          <tr><th>舱位等级</th><th>国内航线</th><th>国际航线</th></tr>
          </thead>
          <tbody>
          <tr><td>头等舱</td><td>40 KG</td><td>32 KG × 2件</td></tr>
          <tr><td>公务舱</td><td>30 KG</td><td>32 KG × 2件</td></tr>
          <tr><td>经济舱</td><td>20 KG</td><td>23 KG × 1件</td></tr>
          </tbody>
        </table>

        <h3>3. 液态物品携带限制</h3>
        <p class="para">单件容器容积不超过 100ml，总量不超过 1000ml，需放入透明、可重新密封的塑料袋内。</p>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="baggageVisible = false">我已了解</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'CustomerServiceView',
  data() {
    return {
      activeFaq: '1',
      baggageVisible: false
    };
  },
  watch: {
    '$route.query.section': {
      handler(newSection) {
        this.handleSectionJump(newSection);
      },
      immediate: true,
    },
  },
  methods: {
    showBaggagePolicy() {
      this.baggageVisible = true;
    },
    callPhone() {
      this.$message.info('正在唤起拨号界面...');
    },
    handleSectionJump(sectionId) {
      if (!sectionId) return;
      const sectionMap = {
        'faq-booking': '1', 'faq-refund': '2', 'faq-membership': '3', 'faq-others': '4',
      };
      if (sectionMap[sectionId]) this.activeFaq = sectionMap[sectionId];
      this.$nextTick(() => {
        const element = this.$refs[sectionId];
        if (element) {
          const domElement = element.$el || element;
          domElement.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
      });
    },
  },
};
</script>

<style scoped>
.customer-service-page {
  background-color: #fcfcfc;
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}

.page-hero {
  background: #213c7c;
  height: 240px;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: white;
}

.hero-badge {
  font-size: 12px;
  letter-spacing: 3px;
  color: #409eff;
  margin-bottom: 10px;
}

.hero-content h1 { font-size: 32px; margin: 0; font-weight: 700; }
.hero-para { font-size: 15px; color: #999; margin-top: 10px; }

.main-container {
  max-width: 1100px;
  margin: -40px auto 60px;
  padding: 0 20px;
}

.section-title-group {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.quotes-icon {
  width: 24px;
  height: 24px;
  margin-right: 10px;
  color: #409eff;
}

.section-title-group h2 { font-size: 20px; color: #ffffff; margin: 0; font-weight: 600; }

.service-section {
  margin-bottom: 40px;
}

.contact-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 25px;
}

.contact-styled-card {
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  border: 1px solid #f0f0f0;
  transition: transform 0.3s;
}

.contact-styled-card:hover { transform: translateY(-5px); }

.c-header strong { font-size: 18px; color: #1a1a1a; display: block; margin-bottom: 5px; }
.para { font-size: 14px; color: #666; line-height: 1.6; margin: 0; }

.c-footer {
  margin-top: 25px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.c-value { font-size: 16px; font-weight: 700; color: #409eff; }
.c-button {
  padding: 8px 16px;
  border-radius: 20px;
  border: none;
  background: #1a1a1a;
  color: white;
  font-size: 12px;
  cursor: pointer;
  transition: background 0.3s;
}

.c-button:hover { background: #409eff; }

.custom-collapse { border: none; }
::v-deep .el-collapse-item__header {
  font-size: 15px;
  font-weight: 600;
  padding: 15px 0;
  color: #333;
}

.faq-content { padding: 0 10px 20px; }

.side-tool-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.03);
}

.card-header { font-size: 16px; font-weight: 700; border-bottom: 1px solid #eee; padding-bottom: 10px; margin-bottom: 15px; }

.tool-item {
  display: flex;
  align-items: center;
  padding: 12px;
  color: #444;
  text-decoration: none;
  cursor: pointer;
  border-radius: 8px;
  transition: background 0.2s;
}

.tool-item:hover { background: #f5f9ff; color: #409eff; }
.tool-item i { font-size: 18px; margin-right: 12px; }

.promise-box { padding: 5px 0; }
.p-item { font-size: 13px; color: #666; margin-bottom: 12px; display: flex; align-items: center; }
.p-item i { color: #67c23a; margin-right: 8px; font-size: 16px; }

.policy-table {
  width: 100%;
  border-collapse: collapse;
  margin: 15px 0;
}

.policy-table th, .policy-table td {
  border: 1px solid #eee;
  padding: 12px;
  text-align: left;
  font-size: 14px;
}

.policy-table th { background: #f9f9f9; }
.m-t-20 { margin-top: 20px; }

.baggage-info h3 {
  font-size: 16px;
  margin: 20px 0 10px;
  color: #1a1a1a;
}

@media (max-width: 768px) {
  .contact-grid { grid-template-columns: 1fr; }
}
</style>