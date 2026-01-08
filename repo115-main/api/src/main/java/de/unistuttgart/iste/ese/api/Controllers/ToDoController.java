package de.unistuttgart.iste.ese.api.Controllers;

import de.unistuttgart.iste.ese.api.Entities.ToDo;
import de.unistuttgart.iste.ese.api.Services.ToDoService;
import de.unistuttgart.iste.ese.api.TodoModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/todos")
@CrossOrigin(origins = "http://localhost:3000")
public class ToDoController {

    private static final Log LOG = LogFactory.getLog(ToDoController.class);

    private final ToDoService toDoService;
    private final TodoModel todoModel;

    public ToDoController(ToDoService toDoService) {
        this.toDoService = toDoService;
        String modelPath = "model.pmml";
        this.todoModel = new TodoModel(modelPath);
        LOG.info("ToDoController initialized with model path: " + modelPath);
    }

    @PostMapping
    public ResponseEntity<?> createTodo(@RequestBody Map<String, Object> requestData) {
        try {
            LOG.info("Creating new todo with data: " + requestData);

            boolean hasCategoryInRequest = requestData.get("category") != null &&
                !((String) requestData.get("category")).trim().isEmpty();

            if (!hasCategoryInRequest) {
                String title = (String) requestData.get("title");
                String predictedCategory = "GENERAL"; // Default

                if (title != null && !title.trim().isEmpty()) {
                    try {
                        predictedCategory = todoModel.predictClass(title);
                        LOG.info("KI-Klassifikation erfolgreich: '" + title + "' -> " + predictedCategory);
                    } catch (Exception e) {
                        LOG.error("Fehler bei KI-Klassifikation, verwende GENERAL: " + e.getMessage(), e);
                        predictedCategory = "GENERAL";
                    }
                    requestData.put("category", predictedCategory);
                } else {
                    LOG.info("Kein Titel vorhanden, setze Kategorie auf GENERAL");
                    requestData.put("category", "GENERAL");
                }
            } else {
                LOG.info("Kategorie wurde manuell gesetzt: " + requestData.get("category"));
            }

            ToDo createdTodo = toDoService.createTodoWithAssignees(requestData);
            LOG.info("Todo erfolgreich erstellt mit ID: " + createdTodo.getId() + " und Kategorie: " + createdTodo.getCategory());
            return ResponseEntity.status(201).body(createdTodo);
        } catch (IllegalArgumentException e) {
            LOG.error("Validation error: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            LOG.error("Error creating todo: " + e.getMessage(), e);
            return ResponseEntity.status(500).body("Fehler beim Erstellen des ToDos: " + e.getMessage());
        }
    }

    @GetMapping
    public List<ToDo> getAllTodos() {
        LOG.info("Fetching all todos");
        return toDoService.getAllToDos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ToDo> getTodoById(@PathVariable Long id) {
        LOG.info("Fetching todo with id: " + id);
        ToDo todo = toDoService.getTodoById(id);
        if (todo != null) {
            return ResponseEntity.ok(todo);
        } else {
            LOG.warn("Todo not found with id: " + id);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/finish")
    public ResponseEntity<ToDo> markTodoAsFinished(@PathVariable Long id) {
        LOG.info("Marking todo as finished: " + id);
        ToDo todo = toDoService.markTodoAsFinished(id);
        if (todo != null) {
            return ResponseEntity.ok(todo);
        } else {
            LOG.warn("Todo not found for finishing: " + id);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTodo(@PathVariable Long id, @RequestBody Map<String, Object> requestData) {
        try {
            LOG.info("Updating todo " + id + " with data: " + requestData);

            if (!toDoService.existsById(id)) {
                LOG.warn("Todo not found for update: " + id);
                return ResponseEntity.notFound().build();
            }

            if (!toDoService.validateUpdateRequest(requestData)) {
                LOG.warn("Validation failed for update request: " + requestData);
                return ResponseEntity.badRequest().body("Validierung fehlgeschlagen");
            }

            if (requestData.get("title") != null &&
                (requestData.get("category") == null || ((String) requestData.get("category")).trim().isEmpty())) {
                String title = (String) requestData.get("title");
                try {
                    String predictedCategory = todoModel.predictClass(title);
                    LOG.info("KI-Klassifikation für Update erfolgreich: '" + title + "' -> " + predictedCategory);
                    requestData.put("category", predictedCategory);
                } catch (Exception e) {
                    LOG.error("Fehler bei KI-Klassifikation beim Update: " + e.getMessage(), e);
                    requestData.put("category", "GENERAL");
                }
            }

            ToDo updated = toDoService.updateTodoWithAssignees(id, requestData);
            LOG.info("Todo erfolgreich aktualisiert: ID=" + id + ", Kategorie=" + updated.getCategory());
            return ResponseEntity.ok(updated);

        } catch (Exception e) {
            LOG.error("Error updating todo: " + e.getMessage(), e);
            return ResponseEntity.status(500).body("Fehler beim Aktualisieren des ToDos: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(@PathVariable Long id) {
        LOG.info("Deleting todo: " + id);
        if (!toDoService.existsById(id)) {
            LOG.warn("Todo not found for deletion: " + id);
            return ResponseEntity.notFound().build();
        }
        toDoService.deleteTodo(id);
        LOG.info("Todo deleted: " + id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/classify")
    public ResponseEntity<?> classifyTodo(@RequestBody Map<String, String> request) {
        try {
            String title = request.get("title");
            if (title == null || title.trim().isEmpty()) {
                LOG.warn("Empty title for classification");
                return ResponseEntity.badRequest().body("Titel darf nicht leer sein");
            }

            LOG.info("Classifying title: '" + title + "'");
            String predictedCategory = "GENERAL";

            try {
                predictedCategory = todoModel.predictClass(title);
                LOG.info("Classification result: '" + title + "' -> " + predictedCategory);
            } catch (Exception e) {
                LOG.error("Fehler bei der Klassifikation: " + e.getMessage(), e);
                predictedCategory = "GENERAL";
            }

            return ResponseEntity.ok(Map.of(
                "category", predictedCategory,
                "title", title
            ));
        } catch (Exception e) {
            LOG.error("Error during classification: " + e.getMessage(), e);
            return ResponseEntity.status(500).body("Fehler bei der Klassifikation: " + e.getMessage());
        }
    }
}
