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
    <tr v-for="article in articles">
      <td>{{ article.id }}</td>
      <td>{{ article.title }}</td>
      <td>{{ article.content }}</td>
      <td><img :src="getImagePath(article.imageLink)" alt="Article Image"></td>
      <td>{{ article.author.name }}</td>
      <td>{{ article.create_date }}</td>
      <td>
        <button type="button" @click="subscribe(article.author.id)">Subscribe</button>
      </td>
      <td>
        <button type="button" @click="deleteArticle(article.id)">Delete</button>
      </td>
      <td>
        <button type="button" @click="editArticle(article)">Edit</button>
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
      articles: [],
      errors: [],
      id: '',
      title: '',
      content: '',
      image: null,
      selectedArticle: null
    }
  },
  watch: {
    selectedArticle(newArt, oldArt) {
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
    getArticles() {
      axios.get("/api/articles")
          .then(response => {
            this.articles = response.data
          })
          .catch(error => {
            if (Array.isArray(error.response.data)) {
              this.errors = error.response.data
            } else {
              this.errors.push(error.response.data)
            }
          })
    },
    deleteArticle(id) {
      axios.delete(`/api/articles/${id}`)
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
    editArticle(article) {
      this.selectedArticle = article
    },
    save: function () {
      const formData = new FormData();
      formData.append('title', this.title);
      formData.append('content', this.content);
      formData.append('image', this.image);

      if (this.id) {
        axios.put(`/api/articles/${this.id}`, formData)
            .then(response => {
              let index = this.articles.findIndex(item => item.id === response.data.id)
              this.articles.splice(index, 1, response.data);
            })
            .catch(error => {
              if (Array.isArray(error.response.data)) {
                this.errors = error.response.data
              } else {
                this.errors.push(error.response.data)
              }
            })
      } else {
        axios.post(`/api/articles`, formData)
            .then(response => {
              this.articles.push(response.data)
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
    this.getArticles()
  },
}
</script>

<style scoped>

</style>