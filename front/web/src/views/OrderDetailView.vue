<template>
  <div>
    <h1>订单详情</h1>
    <p>正在查看订单 ID: {{ orderId }}</p>
  </div>
</template>

<script>
export default {
  name: 'OrderDetailView',
  props: ['orderId'], 
};
</script>