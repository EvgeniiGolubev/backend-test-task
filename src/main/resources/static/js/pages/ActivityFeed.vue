<template>
  <div>
    <ul>
      <li v-for="error in errors">{{ error.message }}</li>
    </ul>
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
    </tr>
    </thead>
    <tbody>
    <tr v-for="post in articles">
      <td>{{ post.id }}</td>
      <td>{{ post.title }}</td>
      <td>{{ post.content }}</td>
      <td><img :src="getImagePath(post.imageLink)" alt="Article Image"></td>
      <td>{{ post.author.name }}</td>
      <td>{{ post.create_date }}</td>
    </tr>
    </tbody>
  </table>
  <div>
    <input type="number" v-model="currentPage" placeholder="Current page"/>
    <input type="number" v-model="pageSize" placeholder="Page size"/>
    <label for="sort-select">Выберите сортировку:</label>
    <select id="sort-select" v-model="sortType">
      <option value="asc">По возрастанию</option>
      <option value="desc">По убыванию</option>
    </select>
    <button type="button" @click="handlePageChange">Submit</button>
  </div>
</template>

<script>
import axios from "axios";

export default {
  data() {
    return {
      articles: [],
      errors: [],
      currentPage: 0,
      pageSize: 5,
      sortType: 'desc'
    }
  },
  methods: {
    getActivityFeed() {
      axios.get("/api/activity-feed",
          { params: {
                  pageSize: this.pageSize,
                  page: this.currentPage,
                  sortType: this.sortType
                }})
          .then(response => {
            this.articles = response.data.content
          })
          .catch(error => {
            if (Array.isArray(error.response.data)) {
              this.errors = error.response.data
            } else {
              this.errors.push(error.response.data)
            }
          })
    },
    getImagePath(link) {
      return link ? `/img/${link}` : '/img/placeholder.jpg';
    },
    handlePageChange() {
      this.getActivityFeed()
    }
  },
  mounted() {
    this.getActivityFeed()
  },
}
</script>

<style scoped>

</style>