import { createRouter, createWebHashHistory  } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import TodosView from '../views/TodosView.vue'
import AssigneesView from "@/views/AssigneesView.vue";

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/todos',
      name: 'todos',
      component: TodosView
    },
      {
          path: '/assignees',
          name: 'assignees',
          component: AssigneesView
      }
  ]
})

export default router
