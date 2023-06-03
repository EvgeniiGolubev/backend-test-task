<template>
  <div>
    {{ responseMessages }}
    <ul>
      <li v-for="error in errors">{{ error.message }}</li>
    </ul>
  </div>
  <h1>Friends</h1>
  <div>
    <h3>Messages</h3>
    <div>
      <div v-for="message in messages">
        <div>From: {{ message.sender }}</div>
        <div>To: {{ message.receiver }}</div>
        <div>Date: {{ message.timestamp }}</div>
        <div>Content: {{ message.content }}</div>
        <hr/>
      </div>
    </div>
  </div>
  <table>
    <thead>
    <tr>
      <th>Id</th>
      <th>Name</th>
      <th></th>
      <th></th>
    </tr>
    </thead>
    <tbody>
    <tr v-for="friend in friends">
      <td>{{ friend.id }}</td>
      <td>{{ friend.name }}</td>
      <td>
        <div>
          <textarea v-model="content" placeholder="content"/>
          <button type="button" @click="send(friend.id)">Send message</button>
        </div>
      </td>
      <td>
        <button type="button" @click="getMessage(friend.id)">Get messages</button>
      </td>
    </tr>
    </tbody>
  </table>
  <br>

</template>

<script>
import axios from "axios";

export default {
  data() {
    return {
      friends: [],
      content: '',
      messages: [],
      responseMessages: '',
      errors: [],
    }
  },
  methods: {
    getFriends() {
      axios.get("/api/profile/friends")
          .then(response => {
            this.friends = response.data
          })
          .catch(error => {
            alert(error.response.data.message)
          })
    },
    send(id) {
      this.errors = []

      axios.post(`/api/messages/send/${id}`, {content: this.content})
          .then(response => {

          })
          .catch(error => {
            if (Array.isArray(error.response.data)) {
              this.errors = error.response.data
            } else {
              this.errors.push(error.response.data)
            }
          })
    },
    getMessage(id) {
      axios.get(`/api/messages/history/${id}`)
          .then(response => {
            this.messages = response.data
          })
          .catch(error => {
            if (Array.isArray(error.response.data)) {
              this.errors = error.response.data
            } else {
              this.errors.push(error.response.data)
            }
          })
    }
  },
  mounted() {
    this.getFriends()
  },
}
</script>

<style scoped>

</style>