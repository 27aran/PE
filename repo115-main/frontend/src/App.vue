<template>
    <div id="app">
        <nav class="navbar">
            <div class="nav-container">
                <div class="nav-left">
                    <div class="WebsiteLogo">
                        <img src="https://cdn3d.iconscout.com/3d/premium/thumb/todo-list-3d-icon-png-download-8168038.png" alt="WebsiteLogo">
                    </div>
                    <div class="nav-brand">
                        <h2>ToDo-Manager</h2>
                    </div>
                </div>
                <ul class="nav-menu" :class="{ 'nav-menu-active': isMenuOpen }">
                    <li class="nav-item">
                        <router-link to="/" class="nav-link" exact-active-class="nav-link-active">
                            Home
                        </router-link>
                    </li>
                    <li class="nav-item">
                        <router-link to="/todos" class="nav-link" exact-active-class="nav-link-active"> <!-- Korrigiert: /todos -->
                            Todos
                        </router-link>
                    </li>
                    <li class="nav-item">
                        <router-link to="/assignees" class="nav-link" exact-active-class="nav-link-active"> <!-- Korrigiert: /assignees -->
                            Assignees
                        </router-link>
                    </li>
                </ul>

                <!-- Mobile menu button -->
                <div class="nav-toggle" @click="toggleMenu">
                    <span class="bar"></span>
                    <span class="bar"></span>
                    <span class="bar"></span>
                </div>
            </div>
        </nav>

        <!-- Main Content -->
        <main class="main-content">
            <router-view />
        </main>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
    name: 'App',
    data() {
        return {
            isMenuOpen: false as boolean
        }
    },
    methods: {
        toggleMenu(): void {
            this.isMenuOpen = !this.isMenuOpen
        }
    }
})
</script>

<style>
* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

body {
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
    background-color: #ffffff;
}

#app {
    min-height: 100vh;
}

.main-content {
    padding: 20px;
    max-width: 1200px;
    margin: 0 auto;
}

/* Navigation Styles */
.navbar {
    background-color: #2c3e50;
    padding: 0 20px;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    position: sticky;
    top: 0;
    z-index: 1000;
}

.nav-container {
    display: flex;
    justify-content: space-between;
    align-items: center;
    max-width: 1200px;
    margin: 0 auto;
    height: 80px;
    position: relative;
}

/* Linke Seite: Logo + Titel */
.nav-left {
    display: flex;
    align-items: center;
    gap: 15px;
    position: absolute;
    left: -70px;
    margin-bottom: 5px;
}

.WebsiteLogo img {
    width: 55px;
    margin-top: 8px;
}

.nav-brand h2 {
    color: #ffffff;
    font-size: 25px;
    font-weight: 600;
    margin: 0;
    white-space: nowrap;
}

/* Mittlere Navigation */
.nav-menu {
    display: flex;
    list-style: none;
    gap: 50px;
    margin: 0;
    padding: 0;
    position: absolute;
    left: 50%;
    transform: translateX(-50%);
}

.nav-item {
    position: static;
}

.nav-link {
    color: #ecf0f1;
    text-decoration: none;
    padding: 8px 16px;
    border-radius: 4px;
    transition: all 0.3s ease;
    font-weight: 500;
    display: block;
}

.nav-link:hover {
    background-color: #34495e;
    color: #42b883;
}

.nav-link-active {
    background-color: #42b883;
    color: white;
}

/* Mobile Styles */
.nav-toggle {
    display: none;
    flex-direction: column;
    cursor: pointer;
    position: absolute;
    right: 0;
}

.bar {
    width: 25px;
    height: 3px;
    background-color: #ecf0f1;
    margin: 3px 0;
    transition: 0.3s;
}

/* Responsive Design */
@media screen and (max-width: 768px) {
    .nav-container {
        justify-content: space-between;
        padding: 0 15px;
    }

    .nav-left {
        position: static;
        transform: none;
    }

    .nav-menu {
        position: fixed;
        left: -100%;
        top: 80px;
        flex-direction: column;
        background-color: #2c3e50;
        width: 100%;
        text-align: center;
        transition: 0.3s;
        box-shadow: 0 10px 27px rgba(0,0,0,0.05);
        padding: 20px 0;
        gap: 0;
        transform: translateX(-50%);
        height: auto;
    }

    .nav-menu-active {
        left: 50%;
        transform: translateX(-50%);
    }

    .nav-item {
        margin: 15px 0;
    }

    .nav-toggle {
        display: flex;
        position: static;
    }
}

/* Für sehr kleine Bildschirme */
@media screen and (max-width: 480px) {
    .nav-left {
        gap: 10px;
    }

    .nav-brand h2 {
        font-size: 20px;
    }

    .WebsiteLogo img {
        width: 32px;
        height: 32px;
    }
}
</style>
