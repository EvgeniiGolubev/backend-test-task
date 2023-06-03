import { createRouter, createWebHistory } from 'vue-router'
import Login from "pages/Login.vue";
import Register from "pages/Register.vue";
import Main from "pages/Main.vue";
import Posts from "pages/Posts.vue";
import Profile from "pages/Profile.vue";
import Friends from "pages/Friends.vue";
import ActivityFeed from "pages/ActivityFeed.vue";

const router = createRouter({
    history: createWebHistory(),
    routes: [
        { path: '/', name: 'Main', component: Main },
        { path: '/login', name: 'Login', component: Login },
        { path: '/register', name: 'Register', component: Register },
        { path: '/posts', name: 'Posts', component: Posts },
        { path: '/profile', name: 'Profile', component: Profile },
        { path: '/friends', name: 'Friends', component: Friends },
        { path: '/activity', name: 'ActivityFeed', component: ActivityFeed },
    ]
})

export default router