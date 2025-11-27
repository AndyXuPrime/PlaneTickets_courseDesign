<template>
  <div class="customer-service-page">
    <div class="page-header">
      <h1>客户服务中心</h1>
      <p>我们随时为您提供帮助。您可以在下方找到常见问题的答案或直接联系我们。</p>
    </div>

    <div class="card">
      <div class="card-body">
        <!-- 常见问题 (FAQ) 部分 -->
        <section id="faq-section" class="service-section">
          <h2><i class="el-icon-question"></i> 常见问题 (FAQ)</h2>
          <!-- 给每个 el-collapse-item 添加 ref，方便滚动定位 -->
          <el-collapse v-model="activeFaq" accordion>
            <el-collapse-item title="如何预订机票？" name="1" ref="faq-booking">
              <!-- ... 内容不变 ... -->
            </el-collapse-item>
            <el-collapse-item title="机票的退改签政策是怎样的？" name="2" ref="faq-refund">
              <!-- ... 内容不变 ... -->
            </el-collapse-item>
            <el-collapse-item title="会员等级有什么用？如何获得折扣？" name="3" ref="faq-membership">
              <!-- ... 内容不变 ... -->
            </el-collapse-item>
            <el-collapse-item title="如何为他人购买机票？" name="4" ref="faq-others">
              <!-- ... 内容不变 ... -->
            </el-collapse-item>
          </el-collapse>
        </section>

        <!-- 联系方式和快速链接部分 -->
        <section id="contact-section" ref="faq-contact" class="service-section contact-grid">
          <!-- ... 内容不变 ... -->
        </section>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CustomerServiceView',
  data() {
    return {
      activeFaq: '1', // 默认展开第一个
    };
  },
  watch: {
    // 监听路由变化，特别是查询参数的变化
    '$route.query.section': {
      handler(newSection) {
        this.handleSectionJump(newSection);
      },
      immediate: true, // 确保组件首次加载时也能触发
    },
  },
  methods: {
    handleSectionJump(sectionId) {
      if (!sectionId) return;

      // 映射查询参数到FAQ的name属性
      const sectionMap = {
        'faq-booking': '1',
        'faq-refund': '2',
        'faq-membership': '3',
        'faq-others': '4',
      };

      if (sectionMap[sectionId]) {
        // 展开对应的折叠项
        this.activeFaq = sectionMap[sectionId];
      }

      // 使用 nextTick 确保DOM已经更新（折叠项已展开）
      this.$nextTick(() => {
        // 获取目标元素的引用
        const element = this.$refs[sectionId];
        if (element) {
          // 如果是Vue组件实例（如el-collapse-item），需要获取其DOM元素
          const domElement = element.$el || element;
          // 平滑滚动到该元素的位置
          domElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
      });
    },
  },
};
</script>
<style scoped>
.customer-service-page {
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  padding: 20px 0;
  text-align: center;
  margin-bottom: 20px;
}

.page-header h1 {
  font-size: 32px;
  font-weight: 600;
  color: var(--dark);
  margin-bottom: 10px;
}

.page-header p {
  font-size: 16px;
  color: var(--gray);
}

.service-section {
  margin-bottom: 40px;
}

.service-section:last-child {
  margin-bottom: 0;
}

.service-section h2 {
  font-size: 22px;
  font-weight: 600;
  color: var(--dark);
  border-bottom: 2px solid var(--primary);
  padding-bottom: 10px;
  margin-bottom: 20px;
  display: inline-block;
}

.service-section h2 i {
  margin-right: 8px;
}

/* Element UI Collapse 样式覆盖 */
::v-deep .el-collapse-item__header {
  font-size: 16px;
  font-weight: 500;
}

::v-deep .el-collapse-item__content {
  padding-bottom: 20px;
  font-size: 15px;
  line-height: 1.8;
  color: #555;
}

.el-collapse-item li {
  margin-bottom: 8px;
}

.contact-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
}

.contact-item {
  display: flex;
  justify-content: space-between;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.contact-item strong {
  color: var(--dark);
}

.contact-item span {
  color: var(--gray);
}

.quick-links ul {
  list-style: none;
  padding: 0;
}

.quick-links li a {
  display: block;
  padding: 15px;
  background-color: #f9f9f9;
  margin-bottom: 10px;
  border-radius: 4px;
  text-decoration: none;
  color: var(--primary);
  font-weight: 500;
  transition: all 0.3s ease;
}

.quick-links li a:hover {
  background-color: var(--primary);
  color: white;
  transform: translateX(5px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .contact-grid {
    grid-template-columns: 1fr;
  }
}
</style>