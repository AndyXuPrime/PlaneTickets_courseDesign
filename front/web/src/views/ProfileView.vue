<template>
  <div class="profile-page" v-if="user">
    <!-- 1. 特色服务展示区 (移到顶部) -->
    <div class="features-grid">
      <div class="feature-card" v-for="feature in features" :key="feature.title">
        <img :src="feature.image" :alt="feature.title" class="feature-image" @error="onImageError">
        <h3 class="feature-title">{{ feature.title }}</h3>
      </div>
    </div>

    <!-- 2. 信息折叠面板 -->
    <div class="info-accordion">
      <el-collapse v-model="activeAccordion" accordion>
        <el-collapse-item name="1" title="清风航空会员卡 电子卡和实体卡">
          <div class="collapse-content">
            <p>您的电子会员卡已在App中激活。实体卡片将在您完成首次飞行后的2-4周内寄出。您可以使用任一卡片累积里程和享受权益。</p>
          </div>
        </el-collapse-item>
        <el-collapse-item name="2" title="关于会员号/主卡的说明">
          <div class="collapse-content">
            <p>您的会员号是您在我们这里的唯一身份标识。如果您同时拥有我们的联名信用卡，系统会自动将您的会员账户设置为主卡，所有消费累积的里程将统一计入此账户。</p>
          </div>
        </el-collapse-item>
        <el-collapse-item name="3" title="如果您居住在中国大陆">
          <div class="collapse-content">
            <p>我们为居住在中国大陆的会员提供了本地化的客户服务和专属优惠活动。请确保您在个人资料中填写的地址是最新且准确的。</p>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>

    <!-- 3. 帮助与支持区 -->
    <div class="help-section">
      <h3>您需要帮助吗？</h3>
      <button class="btn-consult" @click="goToHelpCenter">咨询</button>
    </div>
  </div>
</template>

<script>
import { store } from '../store';

export default {
  name: 'ProfileView',
  data() {
    return {
      features: [
        { title: '以家庭为单位累积里程', image: 'https://images.unsplash.com/photo-1544025162-d76694265947?w=500&q=80' },
        { title: '清风航空高级会员服务', image: 'https://images.unsplash.com/photo-1582693432722-2d96013b1379?w=500&q=80' },
        { title: '使用联名信用卡获取奖励', image: 'https://images.unsplash.com/photo-1556742502-ec7c0e9f34b1?w=500&q=80' },
        { title: '各地区的会员优惠', image: 'https://images.unsplash.com/photo-1498837167922-ddd27525d352?w=500&q=80' },
      ],
      activeAccordion: '1',
    };
  },
  computed: {
    user() {
      return store.user;
    }
  },
  created() {
    if (!store.isLoggedIn) {
      this.$message.error('请先登录以访问会员中心！');
      this.$router.push('/');
    }
  },
  methods: {
    onImageError(event) {
      const imgElement = event.target;
      const placeholder = document.createElement('div');
      placeholder.className = 'image-placeholder';
      const icon = document.createElement('i');
      icon.className = 'fas fa-image';
      placeholder.appendChild(icon);
      imgElement.parentNode.replaceChild(placeholder, imgElement);
    },
    goToHelpCenter() {
      // 点击“咨询”按钮，跳转到客户服务页面
      this.$router.push('/service');
    }
  }
};
</script>

<style scoped>
/* 定义主色调 */
:root {
  --theme-primary: #0033a0; /* 深蓝色 */
  --theme-secondary: #00a0e9; /* 天蓝色/青色 */
  --theme-accent: #e87722; /* 橙色，用于按钮 */
  --theme-bg-light: #f5f7fa; /* 浅灰色背景 */
  --theme-text-dark: #2c3e50; /* 深色文字 */
  --theme-text-light: #5a6a7a; /* 浅色文字 */
}

.profile-page {
  max-width: 1100px; /* 调整最大宽度以适应4列布局 */
  margin: 20px auto;
  padding: 20px;
}

/* 特色服务网格 */
.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 50px; /* 增加与下方内容的间距 */
}
.feature-card {
  background-color: var(--theme-bg-light);
  border-radius: 12px;
  overflow: hidden;
  text-align: center;
  transition: all 0.3s ease;
  border: 1px solid #e0e0e0;
}
.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.08);
}
.feature-image {
  width: 100%;
  height: 160px;
  object-fit: cover;
  display: block;
}
.feature-title {
  font-size: 1rem;
  font-weight: 600;
  padding: 20px 15px;
  margin: 0;
  color: var(--theme-text-dark);
}

/* 图片加载失败占位符 */
::v-deep .image-placeholder {
  width: 100%;
  height: 160px;
  background-color: #e9ecef;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #adb5bd;
  font-size: 48px;
}

/* 折叠面板 (Accordion) */
.info-accordion {
  margin-bottom: 50px;
}
/* 覆盖 Element UI 的默认样式 */
::v-deep .el-collapse {
  border-top: none;
  border-bottom: none;
}
::v-deep .el-collapse-item {
  margin-bottom: 12px;
}
::v-deep .el-collapse-item__header {
  background-color: var(--theme-bg-light);
  padding: 15px 25px; /* 调整内边距 */
  font-size: 1rem;
  font-weight: 500;
  color: var(--theme-text-dark);
  border-radius: 8px;
  border: 1px solid #e0e0e0;
  transition: background-color 0.3s;
}
::v-deep .el-collapse-item__header:hover {
  background-color: #e9eff5;
}
::v-deep .el-collapse-item__header.is-active {
  border-bottom-left-radius: 0;
  border-bottom-right-radius: 0;
  border-bottom-color: transparent;
}
::v-deep .el-collapse-item__wrap {
  border-bottom: none;
  border-left: 1px solid #e0e0e0;
  border-right: 1px solid #e0e0e0;
  border-bottom: 1px solid #e0e0e0;
  border-bottom-left-radius: 8px;
  border-bottom-right-radius: 8px;
}
.collapse-content {
  padding: 20px 25px;
  font-size: 0.95rem;
  line-height: 1.7;
  color: var(--theme-text-light);
}

/* 帮助区域 */
.help-section {
  text-align: left; /* 文本左对齐 */
  padding: 0; /* 移除背景和内边距 */
  background-color: transparent;
}
.help-section h3 {
  font-size: 1.5rem; /* 字体变大 */
  margin-bottom: 20px;
  color: var(--theme-text-dark);
  font-weight: 600;
}
.btn-consult {
  background-color: var(--theme-accent, #e87722); /* 使用主题色变量 */
  color: white;
  border: none;
  padding: 12px 40px;
  font-size: 1rem;
  font-weight: bold;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}
.btn-consult:hover {
  background-color: #c45d00;
  box-shadow: 0 4px 12px rgba(232, 119, 34, 0.3);
}
</style>