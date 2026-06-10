<template>
  <div class="home-page">
    <div class="hero">
      <h1>欢迎来到教学机构课程管理系统</h1>
      <p>专业课程，优质教学，助力成长</p>
      <el-button type="primary" size="large" @click="$router.push('/courses')">
        浏览全部课程
      </el-button>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.home-page {
  padding: 20px 0;
}

.hero {
  text-align: center;
  padding: 80px 20px;
  background: linear-gradient(135deg, #409eff 0%, #53a8ff 100%);
  border-radius: 8px;
  color: #fff;
}

.hero h1 {
  font-size: 32px;
  margin-bottom: 16px;
}

.hero p {
  font-size: 18px;
  margin-bottom: 32px;
  opacity: 0.9;
}
</style>
