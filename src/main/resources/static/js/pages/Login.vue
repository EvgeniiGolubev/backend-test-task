<template>
  <h1>LOGIN</h1>
  <div>
    {{ responseMessages }}
  </div>

  <div>
    <ul>
      <li v-for="error in errors">{{ error.message }}</li>
    </ul>
  </div>
  <div>
    <form @submit.prevent="submitForm">
      <div>
        <label for="email">Email</label>
        <input autocomplete="email" placeholder="email" type="text" id="email" v-model="email"/>
      </div>
      <div>
        <label for="password">Password</label>
        <input autocomplete="password" placeholder="password" type="password" id="password" v-model="password"/>
      </div>
      <button type="submit">Sign in</button>
    </form>
  </div>
</template>

<script>
import axios from "axios";

export default {
  name: "Login",
  data() {
    return {
      email: '',
      password: '',
      errors: [],
      responseMessages: '',
    }
  },
  methods: {
    submitForm() {
      this.errors = []
      this.responseMessages = ''

      const user = {
        email: this.email,
        password: this.password
      }
      axios.post('/api/auth/login', user)
          .then(response => {
            this.responseMessages = response.data.message
          })
          .catch(error => {
            if (Array.isArray(error.response.data)) {
              this.errors = error.response.data
            } else {
              this.errors.push(error.response.data)
            }
          })
    }
  }
}
</script>

<style scoped>

</style>