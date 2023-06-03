<template>
  <div>
    {{ responseMessages }}
  </div>

  <div>
    <ul>
      <li v-for="error in errors">{{ error.message }}</li>
    </ul>
  </div>

  <div>
    <input type="text" v-model="title" placeholder="title"/>
    <textarea v-model="content" placeholder="content"/>
    <input type="file" @change="handleFileChange">
    <input type="button" :value="buttonLabel" @click="save"/>
  </div>

  <table>
    <thead>
    <tr>
      <th>Id</th>
      <th>Title</th>
      <th>Content</th>
      <th>Image</th>
      <th>Author</th>
      <th>Date</th>
      <th></th>
      <th></th>
      <th></th>
    </tr>
    </thead>
    <tbody>
    <tr v-for="post in posts">
      <td>{{ post.id }}</td>
      <td>{{ post.title }}</td>
      <td>{{ post.content }}</td>
      <td><img :src="getImagePath(post.imageLink)" alt="Article Image"></td>
      <td>{{ post.author.name }}</td>
      <td>{{ post.create_date }}</td>
      <td>
        <button type="button" @click="subscribe(post.author.id)">Subscribe</button>
      </td>
      <td>
        <button type="button" @click="deletePost(post.id)">Delete</button>
      </td>
      <td>
        <button type="button" @click="editPost(post)">Edit</button>
      </td>
    </tr>
    </tbody>
  </table>
</template>

<script>
import axios from "axios";

export default {
  data() {
    return {
      responseMessages: '',
      posts: [],
      errors: [],
      id: '',
      title: '',
      content: '',
      image: null,
      selectedPost: null
    }
  },
  watch: {
    selectedPost(newArt, oldArt) {
      this.id = newArt.id
      this.title = newArt.title
      this.content = newArt.content
    }
  },
  computed: {
    buttonLabel() {
      return this.id ? 'Update' : 'Save';
    }
  },
  methods: {
    getPosts() {
      axios.get("/api/posts")
          .then(response => {
            this.posts = response.data
          })
          .catch(error => {
            if (Array.isArray(error.response.data)) {
              this.errors = error.response.data
            } else {
              this.errors.push(error.response.data)
            }
          })
    },
    deletePost(id) {
      axios.delete(`/api/posts/${id}`)
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
    },
    editPost(post) {
      this.selectedPost = post
    },
    save: function () {
      const formData = new FormData();
      formData.append('title', this.title);
      formData.append('content', this.content);
      formData.append('image', this.image);

      if (this.id) {
        axios.put(`/api/posts/${this.id}`, formData)
            .then(response => {
              let index = this.posts.findIndex(item => item.id === response.data.id)
              this.posts.splice(index, 1, response.data);
            })
            .catch(error => {
              if (Array.isArray(error.response.data)) {
                this.errors = error.response.data
              } else {
                this.errors.push(error.response.data)
              }
            })
      } else {
        axios.post(`/api/posts`, formData)
            .then(response => {
              this.posts.push(response.data)
            })
            .catch(error => {
              if (Array.isArray(error.response.data)) {
                this.errors = error.response.data
              } else {
                this.errors.push(error.response.data)
              }
            })
      }

      this.id = ''
      this.title = ''
      this.content = ''
    },
    subscribe(id) {
      axios.post(`/api/profile/change-subscription/${id}?subscribe=${true}`)
          .then(response => {
            this.$root.push("/friends")
          })
          .catch(error => {
            alert(error.response.data.message)
          })
    },
    handleFileChange(event) {
      // Обработчик события изменения файла
      this.image = event.target.files[0];
    },
    getImagePath(link) {
      return link ? `/img/${link}` : '/img/placeholder.jpg';
    }
  },
  mounted() {
    this.getPosts()
  },
}
</script>

<style scoped>

</style>