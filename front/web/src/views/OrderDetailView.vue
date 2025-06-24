<template>
  <div>
    <h1>订单详情</h1>
    <p>正在查看订单 ID: {{ orderId }}</p>
    <!-- 将来这里会展示单个订单的详细信息 -->
  </div>
</template>

<script>
export default {
  name: 'OrderDetailView',
  props: ['orderId'], // 接收来自路由的 orderId 参数
};
</script>