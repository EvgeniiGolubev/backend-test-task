<template>
  <div>
    <div>
      {{ responseMessages }}
    </div>

    <div>
      <ul>
        <li v-for="error in errors">{{ error.message }}</li>
      </ul>
    </div>

    <form @submit.prevent="register">
      <div>
        <label for="email">Email</label>
        <input type="text" id="email" v-model="email"/>
      </div>
      <div>
        <label for="name">Username</label>
        <input type="text" id="name" v-model="name"/>
      </div>
      <div>
        <label for="password">Password</label>
        <input type="password" id="password" v-model="password"/>
      </div>
      <div>
        <label for="confirm">Confirm the password</label>
        <input type="password" id="confirm" v-model="confirm_password"/>
      </div>
      <button type="submit">Sign up</button>
    </form>
  </div>
</template>

<script>
import axios from "axios"

export default {
  data() {
    return {
      email: '',
      name: '',
      password: '',
      confirm_password: '',
      errors: [],
      responseMessages: '',
    }
  },
  methods: {
    register() {
      this.errors = []
      this.responseMessages = ''

      const user = {
        email: this.email,
        name: this.name,
        password: this.password,
        confirm_password: this.confirm_password,
      }

      axios.post('/api/auth/register', user)
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