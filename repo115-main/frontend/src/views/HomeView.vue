<script lang="ts">
import { defineComponent } from "vue";

interface Todo {
    id: number;
    title: string;
    description: string;
    priority: 'low' | 'medium' | 'high';
    dueDate: string;
    completed: boolean;
}

interface Assignee {
    id: number;
    prename: string;
    name: string;
    email: string;
}

export default defineComponent({
    name: 'HomeView',
    data() {
        return {
            todoCount: 0,
            assigneeCount: 0,
            loading: false
        }
    },
    async mounted() {
        await this.fetchCounts();
    },
    methods: {
        async fetchCounts(): Promise<void> {
            this.loading = true;
            try {
                // Todos abrufen
                const todosResponse = await fetch('http://localhost:8080/api/v1/todos');
                if (todosResponse.ok) {
                    const todos: Todo[] = await todosResponse.json();
                    this.todoCount = todos.length;
                } else {
                    console.error('Failed to fetch todos for count');
                }

                // Assignees abrufen
                const assigneesResponse = await fetch('http://localhost:8080/api/v1/assignees');
                if (assigneesResponse.ok) {
                    const assignees: Assignee[] = await assigneesResponse.json();
                    this.assigneeCount = assignees.length;
                } else {
                    console.error('Failed to fetch assignees for count');
                }
            } catch (error) {
                console.error('Error fetching counts:', error);
            } finally {
                this.loading = false;
            }
        }
    }
})
</script>

<template>
    <div class="home">
        <div class="welcome-section">
            <h1 class="welcome-title">Willkommen zum Todo-Manager!</h1>
            <p class="welcome-text">Verwalte Todos und Assignees je nach Bedarf.</p>
            <p class="stats">Statistiken:</p>

            <div class="icons-container">
                <div class="icon-item">
                    <div class="icon-content">
                        <div class="icon-wrapper">
                            <img src="https://img.icons8.com/win10/1200/todo-list.jpg" alt="ToDo">
                        </div>
                        <div class="count-display">
                            <span v-if="loading" class="loading-count">...</span>
                            <span v-else class="count-number">{{ todoCount }}</span>
                            <span class="count-label">Todos</span>
                        </div>
                    </div>
                </div>

                <div class="icon-item">
                    <div class="icon-content">
                        <div class="icon-wrapper">
                            <img src="https://upload.wikimedia.org/wikipedia/commons/9/99/Sample_User_Icon.png" alt="Assignee">
                        </div>
                        <div class="count-display">
                            <span v-if="loading" class="loading-count">...</span>
                            <span v-else class="count-number">{{ assigneeCount }}</span>
                            <span class="count-label">Assignees</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.home {
    min-height: 80vh;
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 20px;
}

.welcome-section {
    text-align: center;
    max-width: 900px;
    padding: 60px 40px;
    border-radius: 12px;
    background: #ffffff;
}

.welcome-title {
    font-size: 2.5rem;
    color: #000000;
    margin-bottom: 1rem;
    font-weight: 600;
}

.welcome-text {
    font-size: 1.2rem;
    color: #7f8c8d;
    margin-bottom: 25px;
    line-height: 1.6;
}

.stats {
    font-size: 1.3rem;
    color: #2c3e50;
    margin-bottom: 20px;
    font-weight: 500;
}

.icons-container {
    display: flex;
    justify-content: center;
    gap: 100px;
    margin-top: 40px;
    flex-wrap: wrap;
}

.icon-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 15px;
    transition: transform 0.3s ease;
}

.icon-item:hover {
    transform: translateY(-5px);
}

.icon-content {
    display: flex;
    align-items: center;
    gap: 20px;
}

.icon-wrapper {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 80px;
    height: 80px;
    border-radius: 12px;
    padding: 15px;
}

.icon-wrapper img {
    width: 100%;
    height: 100%;
    object-fit: contain;
    border-radius: 4px;
}

.count-display {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
}

.count-number {
    font-size: 2.5rem;
    font-weight: 700;
    color: #2c3e50;
    line-height: 1;
}

.loading-count {
    font-size: 2.5rem;
    font-weight: 700;
    color: #bdc3c7;
    line-height: 1;
}

.count-label {
    font-size: 1.1rem;
    color: #495057;
    font-weight: 500;
}

/* Responsive Design */
@media (max-width: 768px) {
    .welcome-section {
        padding: 40px 20px;
    }

    .welcome-title {
        font-size: 2rem;
    }

    .icons-container {
        gap: 60px;
    }

    .icon-content {
        gap: 15px;
    }

    .icon-wrapper {
        width: 60px;
        height: 60px;
    }

    .count-number {
        font-size: 2rem;
    }

    .count-label {
        font-size: 1rem;
    }
}

@media (max-width: 480px) {
    .icons-container {
        flex-direction: column;
        gap: 30px;
    }

    .welcome-title {
        font-size: 1.8rem;
    }

    .welcome-text {
        font-size: 1.1rem;
    }

    .icon-content {
        gap: 12px;
    }

    .count-number {
        font-size: 1.8rem;
    }
}
</style>
