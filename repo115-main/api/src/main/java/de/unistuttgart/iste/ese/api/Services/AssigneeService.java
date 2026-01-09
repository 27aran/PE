package de.unistuttgart.iste.ese.api.Services;

import de.unistuttgart.iste.ese.api.Entities.Assignee;
import de.unistuttgart.iste.ese.api.Entities.ToDo;
import de.unistuttgart.iste.ese.api.Repositories.AssigneeRepository;
import de.unistuttgart.iste.ese.api.Repositories.ToDoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.regex.Pattern;
import java.util.List;

@Service
@Transactional
public class AssigneeService {

    private final AssigneeRepository assigneeRepository;
    private final ToDoRepository toDoRepository;

    public AssigneeService(AssigneeRepository assigneeRepository, ToDoRepository toDoRepository) {
        this.assigneeRepository = assigneeRepository;
        this.toDoRepository = toDoRepository;
    }

    public Assignee createAssignee(Assignee assignee) {
        assignee.setId(0);
        return assigneeRepository.save(assignee);
    }

    public List<Assignee> getAllAssignees() {
        return assigneeRepository.findAll();
    }

    public Assignee getAssigneeById(long id) {
        return assigneeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignee nicht gefunden mit ID: " + id));
    }

    // standard "Dot-Atom" structure for the local part to ban consecutive/start/end
    // dots; restrict domain to uni-stuttgart.de (including subdomains like iste.uni-stuttgart.de)
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-zA-Z0-9-]+\\.)*uni-stuttgart\\.de$",
            Pattern.CASE_INSENSITIVE);

    public boolean validate(Assignee assignee) {
        if (assignee.getName() == null || assignee.getPrename() == null || assignee.getEmail() == null) {
            return false;
        }
        if (assignee.getName().trim().isEmpty() || assignee.getPrename().trim().isEmpty()) {
            return false;
        }
        String email = assignee.getEmail().trim();
        if (email.isEmpty()) {
            return false;
        }
        // Update assignee with trimmed values for consistency
        assignee.setName(assignee.getName().trim());
        assignee.setPrename(assignee.getPrename().trim());
        assignee.setEmail(email);

        return EMAIL_PATTERN.matcher(email).matches();
    }

    public boolean existsById(long id) {
        return assigneeRepository.existsById(id);
    }

    public Assignee updateAssignee(Assignee assignee) {
        return assigneeRepository.save(assignee);
    }

    public void deleteAssignee(long id) {

        Assignee assigneeToDelete = assigneeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignee nicht gefunden"));

        List<ToDo> allToDos = toDoRepository.findAll();
        for (ToDo todo : allToDos) {
            if (todo.getAssigneeList().contains(assigneeToDelete)) {
                todo.getAssigneeList().remove(assigneeToDelete);
                toDoRepository.save(todo);
            }
        }
        assigneeRepository.delete(assigneeToDelete);
    }
}
