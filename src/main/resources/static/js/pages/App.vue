<script>

</script>

<template>
  <a href="/logout" style="margin: 5px">Logout</a>
  <router-link to="/login" style="margin: 5px">Login</router-link>
  <router-link to="/register" style="margin: 5px">Register</router-link>
  <router-link to="/main" style="margin: 5px">Main</router-link>
  <router-link to="/articles" style="margin: 5px">Articles</router-link>
  <router-link to="/profile" style="margin: 5px">Profile</router-link>
  <router-link to="/friends" style="margin: 5px">Friends</router-link>
  <router-link to="/activity" style="margin: 5px">Activity feed</router-link>
  <router-view></router-view>
</template>

<style scoped>

</style>