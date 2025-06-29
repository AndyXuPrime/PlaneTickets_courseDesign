<template>
  <div style="text-align: center; padding: 50px;">
    <h1>404 - 页面未找到</h1>
    <p>您访问的页面不存在。</p>
    <router-link to="/">返回首页</router-link>
  </div>
</template>

<script>
export default {
  name: 'NotFoundView',
};
</script>