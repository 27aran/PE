package de.unistuttgart.iste.ese.api.Services;

import de.unistuttgart.iste.ese.api.Entities.Assignee;
import de.unistuttgart.iste.ese.api.Entities.ToDo;
import de.unistuttgart.iste.ese.api.Repositories.AssigneeRepository;
import de.unistuttgart.iste.ese.api.Repositories.ToDoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ToDoService {

    private final ToDoRepository toDoRepository;
    private final AssigneeRepository assigneeRepository;

    public ToDoService(ToDoRepository toDoRepository, AssigneeRepository assigneeRepository) {
        this.toDoRepository = toDoRepository;
        this.assigneeRepository = assigneeRepository;
    }

    public ToDo createTodoWithAssignees(Map<String, Object> requestData) {

        ToDo toDo = new ToDo();
        toDo.setTitle((String) requestData.get("title"));
        toDo.setDescription((String) requestData.get("description"));
        toDo.setPriority((String) requestData.get("priority"));

        // 1. Datum parsen absichern
        if (requestData.get("dueDate") == null) {
            throw new IllegalArgumentException("dueDate ist erforderlich");
        }
        try {
            toDo.setDueDate(LocalDate.parse((String) requestData.get("dueDate")));
        } catch (DateTimeParseException | ClassCastException e) {
            throw new IllegalArgumentException("Ungültiges Datumsformat"); // Führt zu 400
        }

        // 2. Assignee-Liste parsen absichern
        List<Long> assigneeIdList = null;
        if (requestData.get("assigneeIdList") != null) {
            try {
                List<?> rawList = (List<?>) requestData.get("assigneeIdList");
                assigneeIdList = rawList.stream()
                        .map(obj -> Long.valueOf(obj.toString())) // Sicherer Cast (NumberFormatException)
                        .collect(Collectors.toList());
            } catch (Exception e) {
                throw new IllegalArgumentException("Ungültiges Format der Assignee-IDs"); // Führt zu 400
            }
        }

        if (!validateWithAssigneeIds(toDo, assigneeIdList)) {
            throw new IllegalArgumentException("Validierung fehlgeschlagen");
        }

        if (requestData.get("category") != null) {
            String category = requestData.get("category").toString();
            if (!category.equalsIgnoreCase("work") && !category.equalsIgnoreCase("private")
                    && !category.equalsIgnoreCase("general")) {
                throw new IllegalArgumentException("Ungültige Kategorie");
            }
            toDo.setCategory(category);
        } else {
            toDo.setCategory("GENERAL");
        }

        if (requestData.get("finished") != null) {
            toDo.setFinished((Boolean) requestData.get("finished"));
        }

        return createTodoWithAssigneesInternal(toDo, assigneeIdList);
    }

    public List<ToDo> getAllToDos() {
        return toDoRepository.findAll();
    }

    public ToDo getTodoById(Long id) {
        return toDoRepository.findById(id).orElse(null);
    }

    public ToDo markTodoAsFinished(Long id) {
        return toDoRepository.findById(id).map(todo -> {
            todo.setFinished(true);
            todo.setFinishedDate(LocalDate.now());
            return toDoRepository.save(todo);
        }).orElse(null);
    }

    public boolean existsById(Long id) {
        return toDoRepository.existsById(id);
    }

    public void deleteTodo(Long id) {
        toDoRepository.deleteById(id);
    }

    public ToDo updateTodoWithAssignees(Long id, Map<String, Object> requestData) {
        ToDo existingToDo = toDoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ToDo nicht gefunden: " + id));

        if (requestData.get("title") != null) {
            existingToDo.setTitle((String) requestData.get("title"));
        }
        if (requestData.get("description") != null) {
            existingToDo.setDescription((String) requestData.get("description"));
        }
        if (requestData.get("priority") != null) {
            existingToDo.setPriority((String) requestData.get("priority"));
        }
        if (requestData.get("finished") != null) {
            boolean finished = (Boolean) requestData.get("finished");
            existingToDo.setFinished(finished);
            if (finished && existingToDo.getFinishedDate() == null) {
                existingToDo.setFinishedDate(LocalDate.now());
            } else if (!finished) {
                existingToDo.setFinishedDate(null);
            }
        }
        if (requestData.get("dueDate") != null) {
            LocalDate dueDate = LocalDate.parse((String) requestData.get("dueDate"));
            if (!dueDate.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("dueDate muss in der Zukunft liegen");
            }
            existingToDo.setDueDate(dueDate);
        }

        // Assignee Liste sicher verarbeiten
        if (requestData.get("assigneeIdList") != null) {
            List<?> rawList = (List<?>) requestData.get("assigneeIdList");
            List<Long> assigneeIdList = rawList.stream()
                    .map(obj -> Long.valueOf(obj.toString()))
                    .collect(Collectors.toList());

            List<Assignee> assignees = new ArrayList<>();
            for (Long assigneeId : assigneeIdList) {
                Assignee assignee = assigneeRepository.findById(assigneeId)
                        .orElseThrow(() -> new RuntimeException("Assignee nicht gefunden: " + assigneeId));
                assignees.add(assignee);
            }
            existingToDo.setAssigneeList(assignees);
        } else if (requestData.containsKey("assigneeIdList")) {
            // Wenn der Key da ist aber null, Liste leeren (optional, je nach Anforderung)
            existingToDo.setAssigneeList(new ArrayList<>());
        }

        if (requestData.get("category") != null) {
            String category = requestData.get("category").toString();
            if (!category.equalsIgnoreCase("work") && !category.equalsIgnoreCase("private")
                    && !category.equalsIgnoreCase("general")) {
                throw new IllegalArgumentException("Ungültige Kategorie");
            }
            existingToDo.setCategory(category);
        }
        return toDoRepository.save(existingToDo);
    }

    public boolean validateUpdateRequest(Map<String, Object> requestData) {
        if (requestData.get("title") != null) {
            String title = (String) requestData.get("title");
            if (title.trim().isEmpty()) {
                return false;
            }
        }

        if (requestData.get("dueDate") != null) {
            try {
                LocalDate dueDate = LocalDate.parse((String) requestData.get("dueDate"));
                if (!dueDate.isAfter(LocalDate.now())) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }

        if (requestData.get("priority") != null) {
            String priority = (String) requestData.get("priority");
            if (!priority.equals("LOW") && !priority.equals("MEDIUM") && !priority.equals("HIGH")) {
                return false;
            }
        }

        if (requestData.get("assigneeIdList") != null) {
            try {
                List<?> rawList = (List<?>) requestData.get("assigneeIdList");
                List<Integer> assigneeIdList = rawList.stream()
                        .map(obj -> Integer.valueOf(obj.toString()))
                        .collect(Collectors.toList());

                Set<Integer> uniqueIds = new HashSet<>(assigneeIdList);
                if (uniqueIds.size() != assigneeIdList.size()) {
                    return false;
                }
                for (Integer assigneeId : assigneeIdList) {
                    if (!assigneeRepository.existsById(assigneeId.longValue())) {
                        return false;
                    }
                }
            } catch (Exception e) {
                return false;
            }
        }

        if (requestData.get("category") != null) {
            String category = requestData.get("category").toString();
            if (!category.equalsIgnoreCase("work") && !category.equalsIgnoreCase("private")
                    && !category.equalsIgnoreCase("general")) {
                return false;
            }
        }

        return true;
    }

    private boolean validateWithAssigneeIds(ToDo toDo, List<Long> assigneeIdList) {

        if (toDo.getTitle() == null || toDo.getTitle().trim().isEmpty()) {
            return false;
        }

        if (toDo.getDueDate() == null || !toDo.getDueDate().isAfter(LocalDate.now())) {
            return false;
        }

        if (toDo.getPriority() == null ||
                !(toDo.getPriority().equals("LOW") ||
                        toDo.getPriority().equals("MEDIUM") ||
                        toDo.getPriority().equals("HIGH"))) {
            return false;
        }

        if (assigneeIdList != null) {
            Set<Long> uniqueIds = new HashSet<>(assigneeIdList);
            if (uniqueIds.size() != assigneeIdList.size()) {
                return false;
            }

            for (Long assigneeId : assigneeIdList) {
                if (!assigneeRepository.existsById(assigneeId)) {
                    return false;
                }
            }
        }
        return true;
    }

    private ToDo createTodoWithAssigneesInternal(ToDo toDo, List<Long> assigneeIdList) {

        toDo.setCreatedDate(LocalDate.now());

        if (toDo.isFinished()) {
            if (toDo.getFinishedDate() == null) {
                toDo.setFinishedDate(LocalDate.now());
            }
        }

        if (assigneeIdList != null && !assigneeIdList.isEmpty()) {
            List<Assignee> assignees = new ArrayList<>();
            for (Long assigneeId : assigneeIdList) {
                Assignee assignee = assigneeRepository.findById(assigneeId)
                        .orElseThrow(() -> new RuntimeException("Assignee nicht gefunden: " + assigneeId));
                assignees.add(assignee);
            }
            toDo.setAssigneeList(assignees);
        } else {
            toDo.setAssigneeList(new ArrayList<>());
        }

        return toDoRepository.save(toDo);
    }
}
