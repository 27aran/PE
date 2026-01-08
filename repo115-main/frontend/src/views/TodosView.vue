<script lang="ts">
import { defineComponent } from 'vue';

interface Assignee {
    id: number;
    prename: string;
    name: string;
    email: string;
}

interface Todo {
    id: number;
    title: string;
    description: string;
    priority: string;
    dueDate: string;
    category: string;
    finished: boolean;
    createdDate: string;
    assigneeList: Assignee[];
    finishedDate?: string;
}

interface NewTodo {
    title: string;
    description: string;
    priority: string;
    dueDate: string;
    category: string;
    assigneeIdList: number[];
}

export default defineComponent({
    name: 'TodoView',
    data() {
        return {
            todos: [] as Todo[],
            assignees: [] as Assignee[],
            loading: false,
            searchQuery: '',
            showDropdown: false,
            sortField: 'title' as 'title' | 'priority' | 'dueDate',
            sortDirection: 'asc' as 'asc' | 'desc',
            showCreateModal: false,
            editingTodoId: null as number | null,
            showCompletedTodos: false,
            detailViewTodo: null as Todo | null,
            newTodo: {
                title: '',
                description: '',
                priority: 'MEDIUM',
                dueDate: '',
                category: '',
                assigneeIdList: [] as number[]
            } as NewTodo,
            mlTimeout: null as ReturnType<typeof setTimeout> | null,
            mlLoading: false
        }
    },
    computed: {
        filteredTodos(): Todo[] {
            let filtered: Todo[] = this.todos.filter(todo => !todo.finished);

            if (this.searchQuery) {
                filtered = filtered.filter(todo =>
                    todo.title.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
                    (todo.description && todo.description.toLowerCase().includes(this.searchQuery.toLowerCase()))
                );
            }

            const sorted = [...filtered].sort((a, b) => {
                let comparison = 0;
                const dir = this.sortDirection === 'asc' ? 1 : -1;

                if (this.sortField === 'title') {
                    comparison = a.title.localeCompare(b.title);
                } else if (this.sortField === 'priority') {
                    const valA = this.priorityValue(a.priority);
                    const valB = this.priorityValue(b.priority);
                    comparison = valA - valB;
                } else if (this.sortField === 'dueDate') {
                    comparison = a.dueDate.localeCompare(b.dueDate);
                }

                return comparison * dir;
            });

            return sorted;
        },
        completedTodos(): Todo[] {
            return this.todos.filter(todo => todo.finished).sort((a, b) => {
                return b.createdDate.localeCompare(a.createdDate);
            });
        },
        daysUntilDue(): number | null {
            if (!this.detailViewTodo || !this.detailViewTodo.dueDate) return null;
            const today = new Date();
            const dueDate = new Date(this.detailViewTodo.dueDate);
            const diffTime = dueDate.getTime() - today.getTime();
            return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        }
    },
    async mounted() {
        await this.fetchAssignees();
        await this.fetchTodos();
    },
    methods: {
        openDetailView(todo: Todo): void {
            this.detailViewTodo = todo;
        },
        closeDetailView(): void {
            this.detailViewTodo = null;
        },
        getPriorityColor(priority: string): string {
            switch (priority) {
                case 'HIGH':
                    return '#dc2626';
                case 'MEDIUM':
                    return '#f59e0b';
                case 'LOW':
                    return '#10b981';
                default:
                    return '#6b7280';
            }
        },
        getStatusText(finished: boolean): string {
            return finished ? 'Erledigt' : 'Ausstehend';
        },
        async fetchTodos(): Promise<void> {
            try {
                const response = await fetch('http://localhost:8080/api/v1/todos');
                if (response.ok) this.todos = await response.json();
            } catch (e) {
                console.error(e);
            }
        },
        async fetchAssignees(): Promise<void> {
            try {
                const response = await fetch('http://localhost:8080/api/v1/assignees');
                if (response.ok) this.assignees = await response.json();
            } catch (e) {
                console.error(e);
            }
        },
        toggleDropdown(): void {
            this.showDropdown = !this.showDropdown;
        },
        sortBy(field: 'title' | 'priority' | 'dueDate', dir: 'asc' | 'desc') {
            this.sortField = field;
            this.sortDirection = dir;
            this.showDropdown = false;
        },
        formatDate(dateString: string) {
            return new Date(dateString).toLocaleDateString('de-DE', {
                year: 'numeric',
                month: 'long',
                day: 'numeric'
            });
        },
        formatDateForCSV(dateString: string | null): string {
            if (!dateString) return '';
            const date = new Date(dateString);
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            return `${year}-${month}-${day}`;
        },
        priorityValue(priority: string): number {
            switch (priority) {
                case 'LOW':
                    return 1;
                case 'MEDIUM':
                    return 2;
                case 'HIGH':
                    return 3;
                default:
                    return 0;
            }
        },
        toggleCompletedVisibility(): void {
            this.showCompletedTodos = !this.showCompletedTodos;
        },
        async toggleComplete(todo: Todo) {
            const newStatus = !todo.finished;
            try {
                const response = await fetch(`http://localhost:8080/api/v1/todos/${todo.id}`, {
                    method: 'PUT',
                    headers: {"Content-Type": "application/json"},
                    body: JSON.stringify({finished: newStatus})
                });
                if (response.ok) {
                    const updatedTodo = await response.json();
                    const index = this.todos.findIndex(t => t.id === todo.id);
                    if (index !== -1) {
                        this.todos[index] = updatedTodo;
                    }
                    if (this.detailViewTodo && this.detailViewTodo.id === todo.id) {
                        this.detailViewTodo = updatedTodo;
                    }
                }
            } catch (e) {
                console.error(e);
                alert("Fehler beim Ändern des Status");
            }
        },
        async deleteTodo(todoId: number) {
            if (!confirm("Möchten Sie dieses Todo wirklich löschen?")) return;
            try {
                await fetch(`http://localhost:8080/api/v1/todos/${todoId}`, {method: 'DELETE'});
                this.todos = this.todos.filter(t => t.id !== todoId);
                if (this.detailViewTodo && this.detailViewTodo.id === todoId) {
                    this.closeDetailView();
                }
            } catch (error) {
                console.error(error);
                alert("Fehler beim Löschen");
            }
        },
        editTodo(todoId: number): void {
            const todoToEdit = this.todos.find(t => t.id === todoId);
            if (!todoToEdit) return;
            this.editingTodoId = todoId;
            this.newTodo = {
                title: todoToEdit.title,
                description: todoToEdit.description,
                priority: todoToEdit.priority,
                dueDate: todoToEdit.dueDate,
                category: todoToEdit.category || '',
                assigneeIdList: todoToEdit.assigneeList.map(a => a.id)
            };
            this.showCreateModal = true;
            this.closeDetailView();
        },
        createTodo(): void {
            this.editingTodoId = null;
            this.newTodo = {
                title: '',
                description: '',
                priority: 'MEDIUM',
                dueDate: '',
                category: '',
                assigneeIdList: []
            };
            this.showCreateModal = true;
        },
        async submitTodo(): Promise<void> {
            if (!this.newTodo.title || !this.newTodo.dueDate) {
                alert('Titel und Fälligkeitsdatum sind Pflichtfelder');
                return;
            }

            this.loading = true;
            try {

                const payload = {
                    title: this.newTodo.title,
                    description: this.newTodo.description,
                    priority: this.newTodo.priority,
                    dueDate: this.newTodo.dueDate,
                    assigneeIdList: this.newTodo.assigneeIdList,
                };

                console.log('Sende Todo an API:', payload);

                let response;
                if (this.editingTodoId) {
                    response = await fetch(`http://localhost:8080/api/v1/todos/${this.editingTodoId}`, {
                        method: 'PUT',
                        headers: {'Content-Type': 'application/json'},
                        body: JSON.stringify(payload)
                    });
                } else {
                    response = await fetch('http://localhost:8080/api/v1/todos', {
                        method: 'POST',
                        headers: {'Content-Type': 'application/json'},
                        body: JSON.stringify(payload)
                    });
                }

                if (response.ok) {
                    const resultTodo = await response.json();
                    if (this.editingTodoId) {
                        const index = this.todos.findIndex(t => t.id === this.editingTodoId);
                        if (index !== -1) {
                            this.todos[index] = resultTodo;
                        }
                    } else {
                        this.todos.push(resultTodo);
                    }
                    this.cancelCreate();
                } else {
                    const txt = await response.text();
                    alert('Fehler: ' + txt);
                }
            } catch (error) {
                console.error('Fehler beim Speichern:', error);
                alert('Netzwerkfehler');
            } finally {
                this.loading = false;
            }
        },

        cancelCreate(): void {
            this.showCreateModal = false;
            this.editingTodoId = null;
            this.newTodo = {
                title: '',
                description: '',
                priority: 'MEDIUM',
                dueDate: '',
                category: '',
                assigneeIdList: []
            };
            if (this.mlTimeout) {
                clearTimeout(this.mlTimeout);
                this.mlTimeout = null;
            }
            this.mlLoading = false;
        },
        getMinDate(): string {
            return new Date().toISOString().split('T')[0] as string;
        },
        toggleAssignee(assigneeId: number): void {
            const index = this.newTodo.assigneeIdList.indexOf(assigneeId);
            if (index > -1) {
                this.newTodo.assigneeIdList.splice(index, 1);
            } else {
                this.newTodo.assigneeIdList.push(assigneeId);
            }
        },
        isAssigneeSelected(assigneeId: number): boolean {
            return this.newTodo.assigneeIdList.includes(assigneeId);
        },
        getAssigneeNames(todo: Todo): string {
            if (!todo.assigneeList || todo.assigneeList.length === 0) return '-';
            return todo.assigneeList.map(a => a.prename + ' ' + a.name).join(', ');
        },
        getAssigneesForCSV(todo: Todo): string {
            if (!todo.assigneeList || todo.assigneeList.length === 0) return '';
            return todo.assigneeList.map(a => `${a.prename} ${a.name}`).join('+');
        },
        exportToCSV(): void {
            if (this.todos.length === 0) {
                alert('Keine Todos zum Exportieren vorhanden');
                return;
            }

            const headers = "id, title, description, finished, assignees, createdDate, dueDate, finishedDate, category, priority";

            const rows = this.todos.map(todo => [
                todo.id ?? '',
                todo.title ?? '',
                todo.description ?? '',
                todo.finished ?? '',
                this.getAssigneesForCSV(todo) ?? '',
                this.formatDateForCSV(todo.createdDate) ?? '',
                this.formatDateForCSV(todo.dueDate) ?? '',
                todo.finished && todo.finishedDate ? this.formatDateForCSV(todo.finishedDate) : '',
                todo.category ?? '',
                todo.priority ?? ''
            ]);

            // Funktion, die nur dann Anführungszeichen setzt, wenn Feld Komma oder Anführungszeichen enthält
            const escapeField = (field: string | number | boolean) => {
                const str = String(field);
                if (str.includes(',') || str.includes('"')) {
                    return `"${str.replace(/"/g, '""')}"`;
                }
                return str;
            };

            // Alle Werte in einer Zeile (A2), sauber escaped
            const valuesCell = rows.flat().map(escapeField).join(",");

            const csvContent = "\uFEFF" + [headers, valuesCell].join("\n");

            const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
            const link = document.createElement("a");
            link.href = URL.createObjectURL(blob);
            link.setAttribute("download", `Todo_Export_${new Date().toISOString().split('T')[0]}.csv`);
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
        },


        async classifyTodo(title: string): Promise<string> {
            const response = await fetch('http://localhost:8080/api/v1/todos/classify', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ title })
            });

            if (!response.ok) {
                throw new Error('Classification failed');
            }

            const result: { category?: string } = await response.json();

            if (typeof result.category === 'string' && result.category.length > 0) {
                return result.category;
            }

            return 'GENERAL';
        },


        getCategoryName(category: string | null | undefined): string {
            if (!category) return 'general';

            const normalized = category.trim().toUpperCase();

            if (normalized === '0') return 'work';
            if (normalized === '1') return 'private';

            return normalized;
        },

        triggerMLClassification(): void {
            if (this.mlTimeout) {
                clearTimeout(this.mlTimeout);
            }

            this.mlLoading = true;

            this.mlTimeout = setTimeout(async () => {
                if (this.newTodo.title.trim().length < 3) {
                    this.newTodo.category = '';
                    this.mlLoading = false;
                    return;
                }

                const category = await this.classifyTodo(this.newTodo.title);
                this.newTodo.category = category;
                this.mlLoading = false;
            }, 600);
        }

    }
})
</script>

<template>
    <div class="page">
        <h1>Todos</h1>
        <p>Verwalten Sie Ihre Aufgaben und Deadlines</p>

        <div class="controls">
            <input
                v-model="searchQuery"
                class="search"
                type="text"
                placeholder="Search todos..."
            >
            <div class="filter-dropdown">
                <button class="filter-btn" @click="toggleDropdown">
                    Filter ▼
                </button>
                <div v-if="showDropdown" class="dropdown-menu">
                    <div @click="sortBy('title', 'asc')">Nach Titel (aufsteigend)</div>
                    <div @click="sortBy('title', 'desc')">Nach Titel (absteigend)</div>
                    <div @click="sortBy('priority', 'asc')">Nach Priorität (aufsteigend)</div>
                    <div @click="sortBy('priority', 'desc')">Nach Priorität (absteigend)</div>
                    <div @click="sortBy('dueDate', 'asc')">Nach dueDate (aufsteigend)</div>
                    <div @click="sortBy('dueDate', 'desc')">Nach dueDate (absteigend)</div>
                </div>
            </div>
            <button class="create-btn" @click="createTodo">
                Todo erstellen
            </button>
            <button class="csv-btn" @click="exportToCSV">
                CSV Export
            </button>
        </div>

        <!-- Create/Edit Modal -->
        <div v-if="showCreateModal" class="modal-overlay">
            <div class="modal">
                <h2>{{ editingTodoId ? 'Todo bearbeiten' : 'Neues Todo erstellen' }}</h2>
                <form @submit.prevent="submitTodo">
                    <div class="form-group">
                        <label for="title">Titel *:</label>
                        <input
                            id="title"
                            v-model="newTodo.title"
                            @input="triggerMLClassification"
                            type="text"
                            required
                            class="form-input"
                            placeholder="Todo Titel eingeben"
                        >
                        <div v-if="mlLoading" class="ml-hint">
                            🔍 Kategorie wird automatisch bestimmt...
                        </div>
                        <div v-else-if="newTodo.category && newTodo.category !== 'Wird klassifiziert...'" class="ml-hint">
                            ✅ Automatisch erkannt: {{ newTodo.category }}
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="description">Beschreibung:</label>
                        <textarea
                            id="description"
                            v-model="newTodo.description"
                            class="form-input"
                            rows="3"
                            placeholder="Optionale Beschreibung"
                        ></textarea>
                    </div>
                    <div class="form-group">
                        <label for="priority">Priorität *:</label>
                        <select
                            id="priority"
                            v-model="newTodo.priority"
                            class="form-input"
                            required
                        >
                            <option value="LOW">Niedrig</option>
                            <option value="MEDIUM">Mittel</option>
                            <option value="HIGH">Hoch</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="dueDate">Fällig am *:</label>
                        <input
                            id="dueDate"
                            v-model="newTodo.dueDate"
                            type="date"
                            required
                            class="form-input"
                            :min="getMinDate()"
                        >
                    </div>
                    <div class="form-group">
                        <label>Assignees:</label>
                        <div class="assignee-selection">
                            <div
                                v-for="assignee in assignees"
                                :key="assignee.id"
                                class="assignee-option"
                                :class="{ selected: isAssigneeSelected(assignee.id) }"
                                @click="toggleAssignee(assignee.id)"
                            >
                                <span class="assignee-name">{{ assignee.prename }} {{ assignee.name }}</span>
                                <span class="assignee-email">{{ assignee.email }}</span>
                            </div>
                            <div v-if="assignees.length === 0" class="no-assignees">
                                Keine Assignees verfügbar
                            </div>
                        </div>
                    </div>
                    <div class="modal-actions">
                        <button type="button" @click="cancelCreate" class="btn-cancel">
                            Abbrechen
                        </button>
                        <button type="submit" class="btn-submit" :disabled="loading">
                            {{ loading ? 'Speichert...' : (editingTodoId ? 'Speichern' : 'Erstellen') }}
                        </button>
                    </div>
                </form>
            </div>
        </div>

        <div class="todo-list">
            <div v-if="loading && !showCreateModal" class="loading">Loading...</div>
            <div v-else-if="filteredTodos.length === 0" class="no-todos">
                {{ todos.filter(t => !t.finished).length === 0 ? 'Keine offenen Todos verfügbar' : 'Keine aktiven Todos gefunden, die der Suche entsprechen.' }}
            </div>
            <div v-else class="todos-container">
                <div class="todos-grid">
                    <div
                        v-for="todo in filteredTodos"
                        :key="todo.id"
                        class="todo-card"
                        :class="{
                            'high-priority': todo.priority === 'HIGH',
                            'medium-priority': todo.priority === 'MEDIUM',
                            'low-priority': todo.priority === 'LOW'
                        }"
                        @click="openDetailView(todo)"
                    >
                        <div class="todo-card-header">
                            <h3 class="todo-title">{{ todo.title }}</h3>
                            <span class="priority-badge" :class="todo.priority.toLowerCase()">
                                {{ todo.priority }}
                            </span>
                        </div>
                        <div class="todo-card-meta">
                            <div class="meta-item">
                                <span class="meta-label">📅 Fällig: {{ formatDate(todo.dueDate) }}</span>
                                <span v-if="todo.category && todo.category !== 'GENERAL'" class="category-badge">
                                    Kategorie: {{ todo.category }}
                                </span>
                            </div>
                        </div>
                        <div class="todo-card-actions">
                            <button @click.stop="toggleComplete(todo)" class="action-btn" :class="{ complete: !todo.finished, undo: todo.finished }">
                                {{ todo.finished ? '↩' : '✓' }}
                            </button>
                            <button @click.stop="editTodo(todo.id)" class="action-btn edit-btn">
                                ✎
                            </button>
                            <button @click.stop="deleteTodo(todo.id)" class="action-btn delete-btn">
                                🗑
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Completed Todos -->
            <div class="completed-section">
                <button
                    class="toggle-completed-btn"
                    @click="toggleCompletedVisibility"
                    v-if="completedTodos.length > 0"
                >
                    Erledigte Todos ({{ completedTodos.length }}) {{ showCompletedTodos ? '▲' : '▼' }}
                </button>
                <div v-if="showCompletedTodos" class="completed-todos-grid">
                    <div
                        v-for="todo in completedTodos"
                        :key="todo.id"
                        class="todo-card completed"
                        :class="{
                            'high-priority': todo.priority === 'HIGH',
                            'medium-priority': todo.priority === 'MEDIUM',
                            'low-priority': todo.priority === 'LOW'
                        }"
                        @click="openDetailView(todo)"
                    >
                        <div class="todo-card-header">
                            <h3 class="todo-title">{{ todo.title }}</h3>
                            <span class="status-badge completed">✅ Erledigt</span>
                        </div>
                        <p class="todo-description" v-if="todo.description">
                            {{ todo.description.length > 80 ? todo.description.substring(0, 80) + '...' : todo.description }}
                        </p>
                        <div class="todo-card-meta">
                            <div class="meta-item">
                                <span class="meta-label">📅 Fällig war:</span>
                                <span class="meta-value">{{ formatDate(todo.dueDate) }}</span>
                            </div>
                        </div>
                        <div class="todo-card-actions">
                            <button @click.stop="toggleComplete(todo)" class="action-btn undo-btn">
                                ↩ Wiedereröffnen
                            </button>
                            <button @click.stop="deleteTodo(todo.id)" class="action-btn delete-btn">
                                🗑 Löschen
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Detailansicht Modal -->
        <div v-if="detailViewTodo" class="detail-modal-overlay" @click.self="closeDetailView">
            <div class="detail-modal">
                <div class="detail-modal-header">
                    <h2>{{ detailViewTodo.title }}</h2>
                    <button class="close-btn" @click="closeDetailView">✕</button>
                </div>
                <div class="detail-modal-content">
                    <div class="detail-section">
                        <h3>Beschreibung</h3>
                        <p class="detail-description">{{ detailViewTodo.description || 'Keine Beschreibung vorhanden' }}</p>
                    </div>
                    <div class="detail-grid">
                        <div class="detail-item">
                            <h4>Priorität</h4>
                            <span class="priority-detail" :style="{ color: getPriorityColor(detailViewTodo.priority) }">
                        {{ detailViewTodo.priority }}
                    </span>
                        </div>
                        <div class="detail-item">
                            <h4>Status</h4>
                            <span class="status-detail" :class="{ completed: detailViewTodo.finished }">
                        {{ getStatusText(detailViewTodo.finished) }}
                    </span>
                        </div>
                        <div class="detail-item">
                            <h4>Kategorie</h4>
                            <span class="category-detail">
                        {{ getCategoryName(detailViewTodo.category) || 'Keine Kategorie' }}
                    </span>
                        </div>
                        <div class="detail-item">
                            <h4>Fällig am</h4>
                            <span class="due-date-detail">
                        {{ formatDate(detailViewTodo.dueDate) }}
                        </span>
                        </div>
                        <div class="detail-item">
                            <h4>Erstellt am</h4>
                            <span class="created-date-detail">
                        {{ formatDate(detailViewTodo.createdDate) }}
                    </span>
                        </div>
                    </div>
                    <div class="detail-section">
                        <h3>Zugewiesene Personen</h3>
                        <div v-if="detailViewTodo.assigneeList && detailViewTodo.assigneeList.length > 0" class="assignees-list">
                            <div
                                v-for="assignee in detailViewTodo.assigneeList"
                                :key="assignee.id"
                                class="assignee-detail"
                            >
                                <div class="assignee-info">
                                    <strong>{{ assignee.prename }} {{ assignee.name }}</strong>
                                    <small>{{ assignee.email }}</small>
                                </div>
                            </div>
                        </div>
                        <div v-else class="no-assignees-detail">
                            Keine Personen zugewiesen
                        </div>
                    </div>
                </div>
                <div class="detail-modal-actions">
                    <button @click="toggleComplete(detailViewTodo)" class="action-btn status">
                        {{ detailViewTodo.finished ? 'Als unerledigt markieren' : 'Als erledigt markieren' }}
                    </button>
                    <button @click="editTodo(detailViewTodo.id)" class="action-btn edit">
                        Bearbeiten
                    </button>
                    <button @click="deleteTodo(detailViewTodo.id)" class="action-btn delete">
                        Löschen
                    </button>
                </div>
            </div>
        </div>
    </div>

</template>

<style src="src/assets/TodosViewStyle.css"></style>
