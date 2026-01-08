<template>
    <div class="page">
        <h1>Assignees</h1>
        <p>Verwalten Sie Ihre Assignees</p>

        <div class="controls">
            <input
                v-model="searchQuery"
                class="search"
                type="text"
                placeholder="Search assignees..."
            >
            <button class="create-btn" @click="showCreateModal = true">
                Assignee erstellen
            </button>
        </div>

        <div v-if="showCreateModal" class="modal-overlay">
            <div class="modal">
                <h2>{{ editingAssigneeId ? 'Assignee bearbeiten' : 'Neuen Assignee erstellen' }}</h2>
                <form @submit.prevent="submitAssignee">
                    <div class="form-group">
                        <label for="prename">Vorname:</label>
                        <input
                            id="prename"
                            v-model="newAssignee.prename"
                            type="text"
                            required
                            class="form-input"
                            placeholder="Max"
                        >
                    </div>
                    <div class="form-group">
                        <label for="name">Nachname:</label>
                        <input
                            id="name"
                            v-model="newAssignee.name"
                            type="text"
                            required
                            class="form-input"
                            placeholder="Mustermann"
                        >
                    </div>
                    <div class="form-group">
                        <label for="email">E-Mail:</label>
                        <input
                            id="email"
                            v-model="newAssignee.email"
                            type="email"
                            required
                            class="form-input"
                            placeholder="max.mustermann@uni-stuttgart.de"
                        >
                        <small class="email-hint">Email muss von der Domain uni-stuttgart.de sein</small>
                    </div>
                    <div class="modal-actions">
                        <button type="button" @click="cancelCreate" class="btn-cancel">
                            Abbrechen
                        </button>
                        <button type="submit" class="btn-submit" :disabled="loading">
                            {{
                                loading
                                    ? (editingAssigneeId ? 'Speichert...' : 'Erstellt...')
                                    : (editingAssigneeId ? 'Speichern' : 'Erstellen')
                            }}
                        </button>
                    </div>
                </form>
            </div>
        </div>

        <div class="assignee-list">
            <div v-if="loading && !showCreateModal" class="loading">Loading...</div>

            <div v-else-if="filteredAssignees.length === 0" class="no-assignees">
                Keine Assignees verfügbar
            </div>

            <div v-else class="assignees">
                <div
                    v-for="assignee in filteredAssignees"
                    :key="assignee.id"
                    class="assignee-item"
                >
                    <div class="assignee-content">
                        <h3>{{ assignee.prename }} {{ assignee.name }}</h3>
                        <p>{{ assignee.email }}</p>
                        <div class="assignee-meta">
                            <span class="assignee-id">ID: {{ assignee.id }}</span>
                        </div>
                    </div>
                    <div class="assignee-actions">
                        <button @click="editAssignee(assignee.id)">
                            Bearbeiten
                        </button>
                        <button @click="deleteAssignee(assignee.id)" class="delete">
                            Löschen
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

interface Assignee {
    id: number;
    prename: string;
    name: string;
    email: string;
}

interface NewAssignee {
    prename: string;
    name: string;
    email: string;
}

export default defineComponent({
    name: 'AssigneeView',
    data() {
        return {
            assignees: [] as Assignee[],
            loading: false,
            searchQuery: '',
            showCreateModal: false,
            editingAssigneeId: null as number | null,
            newAssignee: {
                prename: '',
                name: '',
                email: ''
            } as NewAssignee
        }
    },
    computed: {
        filteredAssignees(): Assignee[] {
            if (!this.searchQuery) {
                return this.assignees;
            }
            const query = this.searchQuery.toLowerCase();
            return this.assignees.filter(assignee =>
                assignee.prename.toLowerCase().includes(query) ||
                assignee.name.toLowerCase().includes(query) ||
                assignee.email.toLowerCase().includes(query)
            );
        }
    },
    async mounted() {
        await this.fetchAssignees();
    },
    methods: {
        async fetchAssignees(): Promise<void> {
            this.loading = true;
            try {
                const response = await fetch('http://localhost:8080/api/v1/assignees');
                if (response.ok) {
                    this.assignees = await response.json();
                } else {
                    console.error('Fehler beim Laden:', response.status);
                }
            } catch (e: unknown) {
                console.error('Error fetching assignees:', e);

                if (e instanceof Error) {
                    console.error('Error details:', e.message, e.name);
                    alert(`Netzwerkfehler: ${e.message || 'Backend nicht erreichbar'}\n\nStelle sicher, dass:\n1. Spring Boot Backend läuft\n2. Port 8080 verfügbar ist\n3. CORS korrekt konfiguriert ist`);
                } else {
                    console.error('Unknown error:', e);
                    alert('Netzwerkfehler: Backend nicht erreichbar');
                }
            } finally {
                this.loading = false;
            }

        },

        async submitAssignee(): Promise<void> {
            if (!this.newAssignee.prename.trim() || !this.newAssignee.name.trim() || !this.newAssignee.email.trim()) {
                alert('Bitte alle Felder ausfüllen');
                return;
            }

            if (!this.newAssignee.email.toLowerCase().endsWith('uni-stuttgart.de')) {
                alert('E-Mail muss von uni-stuttgart.de Domain sein');
                return;
            }

            this.loading = true;

            try {
                const payload = {
                    prename: this.newAssignee.prename,
                    name: this.newAssignee.name,
                    email: this.newAssignee.email,
                };

                const headers = { 'Content-Type': 'application/json' }; // Korrekter Header-Name

                let response;

                if (this.editingAssigneeId) {
                    response = await fetch(`http://localhost:8080/api/v1/assignees/${this.editingAssigneeId}`, {
                        method: 'PUT',
                        headers: headers, // Korrigiert
                        body: JSON.stringify(payload)
                    });
                } else {
                    response = await fetch('http://localhost:8080/api/v1/assignees', {
                        method: 'POST',
                        headers: headers,
                        body: JSON.stringify(payload) // Konsistente Nutzung von payload
                    });
                }


                if (response.ok) {
                    const resultAssignee = await response.json() as Assignee; // Typisieren des Ergebnisses

                    if (this.editingAssigneeId) {
                        const index = this.assignees.findIndex(a => a.id === this.editingAssigneeId);
                        if (index !== -1) {
                            this.assignees[index] = resultAssignee;
                        }
                    } else {
                        this.assignees.push(resultAssignee);
                    }

                    this.cancelCreate();
                } else {
                    const errorText = await response.text();
                    alert(`Fehler vom Server: ${errorText}`);
                }
            } catch (error) {
                console.error('Error:', error);
                alert('Netzwerkfehler beim Speichern. Läuft das Backend?');
            } finally {
                this.loading = false;
            }
        },

        editAssignee(assigneeId: number): void {
            const assigneeToEdit = this.assignees.find(t => t.id === assigneeId);
            if (!assigneeToEdit) return;

            this.editingAssigneeId = assigneeId;

            this.newAssignee = {
                prename: assigneeToEdit.prename,
                name: assigneeToEdit.name,
                email: assigneeToEdit.email
            };

            this.showCreateModal = true;
        },


        cancelCreate(): void {
            this.showCreateModal = false;
            this.editingAssigneeId = null;
            this.newAssignee = { prename: '', name: '', email: '' };
        },

        async deleteAssignee(assigneeId: number): Promise<void> {
            if (!confirm('Wirklich löschen?')) return;

            try {
                const response = await fetch(`http://localhost:8080/api/v1/assignees/${assigneeId}`, {
                    method: 'DELETE'
                });
                if (response.ok) {
                    this.assignees = this.assignees.filter(a => a.id !== assigneeId);
                } else {
                    alert('Fehler beim Löschen');
                }
            } catch (e: unknown) {
                if (e instanceof Error) {
                    alert(`Netzwerkfehler beim Löschen: ${e.message}`);
                } else {
                    alert('Netzwerkfehler beim Löschen');
                }
            }

        }
    }
})
</script>

<style scoped>
.page {
    max-width: 800px;
    margin: 0 auto;
    padding: 20px;
    font-family: Arial, sans-serif;
}

.controls {
    display: flex;
    gap: 15px;
    align-items: center;
    margin: 30px 0;
    flex-wrap: wrap;
}

.search {
    flex: 1;
    min-width: 200px;
    padding: 10px;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 16px;
}

.create-btn {
    padding: 15px 20px;
    background: #007bff;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 16px;
    transition: background-color 0.2s;
}

.create-btn:hover {
    background: #0056b3;
}

/* Modal Styles */
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
}

.modal {
    background: white;
    padding: 30px;
    border-radius: 8px;
    width: 90%;
    max-width: 500px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.modal h2 {
    margin-top: 0;
    margin-bottom: 20px;
    color: #333;
    font-size: 1.5rem;
}

.form-group {
    margin-bottom: 20px;
}

.form-group label {
    display: block;
    margin-bottom: 5px;
    font-weight: 500;
    color: #333;
}

.form-input {
    width: 100%;
    padding: 10px;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 16px;
    box-sizing: border-box;
    transition: border-color 0.2s;
}

.form-input:focus {
    outline: none;
    border-color: #007bff;
}

.email-hint {
    color: #666;
    font-size: 12px;
    margin-top: 5px;
    display: block;
}

.modal-actions {
    display: flex;
    gap: 10px;
    justify-content: flex-end;
    margin-top: 25px;
}

.btn-cancel {
    padding: 10px 20px;
    background: #6c757d;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    transition: background-color 0.2s;
}

.btn-cancel:hover {
    background: #545b62;
}

.btn-submit {
    padding: 10px 20px;
    background: #28a745;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    transition: background-color 0.2s;
}

.btn-submit:hover:not(:disabled) {
    background: #218838;
}

.btn-submit:disabled {
    background: #6c757d;
    cursor: not-allowed;
}

/* Assignee List Styles */
.assignee-list {
    margin-top: 30px;
}

.loading, .no-assignees {
    text-align: center;
    padding: 40px;
    color: #666;
    font-size: 1.1rem;
}

.assignee-item {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    padding: 20px;
    border: 1px solid #ddd;
    border-radius: 8px;
    margin-bottom: 15px;
    background: white;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.assignee-content h3 {
    margin: 0 0 10px 0;
    color: #333;
    font-size: 1.2rem;
}

.assignee-content p {
    margin: 0 0 10px 0;
    color: #666;
}

.assignee-meta {
    font-size: 12px;
    color: #888;
}

.assignee-actions {
    display: flex;
    gap: 10px;
}

.assignee-actions button {
    padding: 8px 15px;
    border: 1px solid #ddd;
    border-radius: 4px;
    background: white;
    cursor: pointer;
    font-size: 14px;
    transition: all 0.2s;
}

.assignee-actions button:hover {
    background: #f8f9fa;
}

.assignee-actions button.delete {
    color: #dc3545;
    border-color: #dc3545;
}

.assignee-actions button.delete:hover {
    background: #dc3545;
    color: white;
}

/* Responsive Design */
@media (max-width: 768px) {
    .controls {
        flex-direction: column;
        align-items: stretch;
    }

    .search {
        min-width: auto;
    }

    .assignee-item {
        flex-direction: column;
        gap: 15px;
    }

    .assignee-actions {
        align-self: flex-end;
    }

    .modal {
        margin: 20px;
        width: calc(100% - 40px);
    }
}
</style>
