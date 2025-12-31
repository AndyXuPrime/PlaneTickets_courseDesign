<template>
  <footer class="footer">
    <div class="footer-top">
      <div class="container">
        <div class="footer-grid">
          <!-- 品牌介绍 -->
          <div class="footer-section brand">
            <h3 class="footer-logo">
              <i class="el-icon-position"></i> 蓝天航空
              <span>BlueSky Airlines</span>
            </h3>
            <p class="description">
              提供全球机票预订服务，覆盖1000+条航线。我们致力于为您提供安全、便捷、舒适的云端出行体验。
            </p>
            <div class="social-icons">
              <i class="el-icon-phone-outline"></i>
              <i class="el-icon-message"></i>
              <i class="el-icon-share"></i>
            </div>
          </div>

          <!-- 快速链接 -->
          <div class="footer-section">
            <h3 class="section-title">快速链接</h3>
            <ul class="footer-links">
              <li><router-link to="/"><i class="el-icon-arrow-right"></i>机票预订</router-link></li>
              <li><router-link to="/#"><i class="el-icon-arrow-right"></i>航班动态</router-link></li>
              <li><router-link to="/profile"><i class="el-icon-arrow-right"></i>用户中心</router-link></li>
              <li><router-link to="/#"><i class="el-icon-arrow-right"></i>企业服务</router-link></li>
            </ul>
          </div>

          <!-- 客户服务 -->
          <div class="footer-section">
            <h3 class="section-title">客户服务</h3>
            <ul class="footer-links">
              <li><router-link :to="{ path: '/service', query: { section: 'faq-booking' } }">帮助中心</router-link></li>
              <li><router-link :to="{ path: '/service', query: { section: 'faq-refund' } }">退改签政策</router-link></li>
              <li><router-link :to="{ path: '/service', query: { section: 'faq-contact' } }">在线客服</router-link></li>
              <li><router-link :to="{ path: '/service', query: { section: 'faq-contact' } }">投诉建议</router-link></li>
            </ul>
          </div>

          <!-- 联系我们 -->
          <div class="footer-section contact">
            <h3 class="section-title">联系我们</h3>
            <div class="contact-item">
              <div class="icon-box"><i class="el-icon-phone"></i></div>
              <div class="text">
                <span class="label">客服热线</span>
                <span class="val">400-123-4567</span>
              </div>
            </div>
            <div class="contact-item">
              <div class="icon-box"><i class="el-icon-message"></i></div>
              <div class="text">
                <span class="label">官方邮箱</span>
                <span class="val">service@bluesky.com</span>
              </div>
            </div>
            <div class="contact-item">
              <div class="icon-box"><i class="el-icon-time"></i></div>
              <div class="text">
                <span class="label">服务时间</span>
                <span class="val">7×24小时 全天候</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 版权信息栏 -->
    <div class="footer-bottom">
      <div class="container">
        <div class="bottom-content">
          <p>© 2025 蓝天航空 版权所有 | 机票预订系统</p>
          <div class="trust-icons">
            <span>隐私政策</span>
            <span class="divider">|</span>
            <span>服务协议</span>
          </div>
        </div>
      </div>
    </div>
  </footer>
</template>

<script>
export default {
  name: 'AppFooter',
};
</script>

<style scoped>
/* 颜色变量 */
:root {
  --footer-bg: #1a2332;
  --footer-bottom-bg: #121926;
  --accent-color: #409eff;
  --text-main: #ffffff;
  --text-muted: #a0a5ba;
}

.footer {
  background-color: #1a2332; /* 航空深蓝 */
  color: #ffffff;
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", Arial, sans-serif;
}

.footer-top {
  padding: 60px 0 40px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.footer-grid {
  display: grid;
  grid-template-columns: 1.5fr 1fr 1fr 1.2fr;
  gap: 40px;
}

/* 品牌部分 */
.footer-logo {
  font-size: 24px;
  color: #409eff;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}
.footer-logo span {
  font-size: 12px;
  color: #606266;
  margin-left: 10px;
  text-transform: uppercase;
  letter-spacing: 1px;
}
.description {
  font-size: 14px;
  color: #a0a5ba;
  line-height: 1.8;
  margin-bottom: 20px;
}
.social-icons i {
  font-size: 20px;
  margin-right: 15px;
  color: #a0a5ba;
  cursor: pointer;
  transition: color 0.3s;
}
.social-icons i:hover { color: #409eff; }

/* 标题样式 */
.section-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 25px;
  position: relative;
  padding-bottom: 10px;
}
.section-title::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: 0;
  width: 30px;
  height: 2px;
  background-color: #409eff;
}

/* 链接样式 */
.footer-links {
  list-style: none;
  padding: 0;
}
.footer-links li {
  margin-bottom: 12px;
}
.footer-links a {
  color: #a0a5ba;
  text-decoration: none;
  font-size: 14px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
}
.footer-links a i {
  font-size: 12px;
  margin-right: 8px;
  opacity: 0;
  transform: translateX(-5px);
  transition: all 0.3s;
}
.footer-links a:hover {
  color: #ffffff;
  padding-left: 5px;
}
.footer-links a:hover i {
  opacity: 1;
  transform: translateX(0);
}

/* 联系方式部分 */
.contact-item {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}
.icon-box {
  width: 36px;
  height: 36px;
  background: rgba(64, 158, 255, 0.1);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  color: #409eff;
}
.contact-item .text {
  display: flex;
  flex-direction: column;
}
.contact-item .label {
  font-size: 12px;
  color: #606266;
}
.contact-item .val {
  font-size: 15px;
  color: #ffffff;
  font-weight: 500;
}

/* 底部版权栏 */
.footer-bottom {
  background-color: #121926;
  padding: 25px 0;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
}
.bottom-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #606266;
  font-size: 13px;
}
.trust-icons span {
  cursor: pointer;
  transition: color 0.3s;
}
.trust-icons span:hover { color: #a0a5ba; }
.divider { margin: 0 10px; cursor: default !important; }

/* 响应式设计 */
@media (max-width: 992px) {
  .footer-grid {
    grid-template-columns: 1fr 1fr;
    gap: 30px;
  }
}
@media (max-width: 576px) {
  .footer-grid {
    grid-template-columns: 1fr;
  }
  .bottom-content {
    flex-direction: column;
    text-align: center;
    gap: 10px;
  }
}
</style>