import { createApp } from 'vue'
import App from 'pages/App.vue'
import router from 'router/router'

const app = createApp(App)
app.use(router)
app.mount('#app')