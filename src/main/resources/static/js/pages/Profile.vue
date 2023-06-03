<template>
  <div>
    <h1>Subscribers</h1>
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
      <tr v-for="subscriber in subscribers">
        <td>{{ subscriber.id }}</td>
        <td>{{ subscriber.name }}</td>
        <td><button type="button" @click="acceptSubscriber(subscriber.id)">Accept</button></td>
        <td><button type="button" @click="cancelSubscriber(subscriber.id)">Cancel</button></td>
      </tr>
      </tbody>
    </table>
  </div>
  <br>
  <div>
    <h1>Subscriptions</h1>
    <table>
      <thead>
      <tr>
        <th>Id</th>
        <th>Name</th>
        <th></th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="subscription in subscriptions">
        <td>{{ subscription.id }}</td>
        <td>{{ subscription.name }}</td>
        <td><button type="button" @click="unsubscribe(subscription.id)">Unsubscribe</button></td>
      </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
import axios from "axios";

export default {
  data() {
    return {
      subscribers: [],
      subscriptions: [],
    }
  },
  methods: {
    getSubscribers() {
      axios.get("/api/profile/subscribers")
          .then(response => {
            this.subscribers = response.data
          })
          .catch(error => {
            alert(error.response.data.message)
          })
    },
    getSubscriptions() {
      axios.get("/api/profile/subscriptions")
          .then(response => {
            this.subscriptions = response.data
          })
          .catch(error => {
            alert(error.response.data.message)
          })
    },
    acceptSubscriber(id) {
      axios.post(`/api/profile/change-status/${id}?status=${true}`)
          .then(response => {
            this.$root.push("/friends")
          })
          .catch(error => {
            alert(error.response.data.message)
          })
    },
    cancelSubscriber(id) {
      axios.post(`/api/profile/change-status/${id}?status=${false}`)
          .then(response => {
            this.$root.push("/friends")
          })
          .catch(error => {
            alert(error.response.data.message)
          })
    },
    unsubscribe(id) {
      axios.post(`/api/profile/change-subscription/${id}?subscribe=${false}`)
          .then(response => {
            this.$root.push("/articles")
          })
          .catch(error => {
            alert(error.response.data.message)
          })
    },
  },
  mounted() {
    this.getSubscribers()
    this.getSubscriptions()
  },
}

</script>

<style scoped>

</style>