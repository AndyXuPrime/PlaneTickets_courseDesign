<template>
    <footer class="footer">
        <div class="container">
            <div class="footer-content">
                <!-- 关于我们 -->
                <div class="footer-section">
                    <h3>关于我们</h3>
                    <p>蓝天航空提供全球机票预订服务，覆盖1000+条航线，为您的出行提供便捷服务。</p>
                </div>

                <!-- 快速链接 -->
                <div class="footer-section">
                    <h3>快速链接</h3>
                    <ul class="footer-links">
                        <li><router-link to="/">机票预订</router-link></li>
                        <li><router-link to="/#">航班动态</router-link></li> <!-- 假设未来有航班动态功能 -->
                        <li><router-link to="/profile">会员中心</router-link></li>
                        <li><router-link to="/#">企业服务</router-link></li>
                    </ul>
                </div>

                <!-- 客户服务 (关键修改部分) -->
                <div class="footer-section">
                    <h3>客户服务</h3>
                    <ul class="footer-links">
                        <!-- 跳转到客户服务页，并带上查询参数，值为对应FAQ的ID -->
                        <li><router-link
                                :to="{ path: '/service', query: { section: 'faq-booking' } }">帮助中心</router-link></li>
                        <li><router-link
                                :to="{ path: '/service', query: { section: 'faq-refund' } }">退改签政策</router-link></li>
                        <li><router-link
                                :to="{ path: '/service', query: { section: 'faq-contact' } }">联系客服</router-link></li>
                        <li><router-link
                                :to="{ path: '/service', query: { section: 'faq-contact' } }">投诉建议</router-link></li>
                    </ul>
                </div>

                <!-- 联系我们 -->
                <div class="footer-section">
                    <h3>联系我们</h3>
                    <p>客服电话: 400-123-4567</p>
                    <p>邮箱: service@bluesky.com</p>
                    <p>工作时间: 7×24小时</p>
                </div>
            </div>
            <div class="copyright">
                © 2025 蓝天航空 版权所有 | 机票预订系统
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
/* 确保p标签有合适的间距 */
.footer-section p {
    margin-bottom: 10px;
    color: #bdbdbd;
    line-height: 1.7;
}
</style>