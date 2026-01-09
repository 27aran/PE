package de.unistuttgart.iste.ese.api.Controllers;

import de.unistuttgart.iste.ese.api.Entities.Assignee;
import de.unistuttgart.iste.ese.api.Services.AssigneeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/assignees")
@CrossOrigin(origins = "http://localhost:3000")
public class AssigneeController {

    private final AssigneeService assigneeService;

    public AssigneeController(AssigneeService assigneeService) {
        this.assigneeService = assigneeService;
    }

    @PostMapping
    public ResponseEntity<?> createAssignee(@RequestBody Assignee assignee) {
        try {
            // defensive trimming / null check before validation
            if (assignee == null || assignee.getEmail() == null) {
                return ResponseEntity.badRequest().body("Assignee validation failed");
            }
            String emailTrimmed = assignee.getEmail().trim();
            assignee.setEmail(emailTrimmed);

            // ENFORCE uni-stuttgart.de domain (allow subdomains like iste.uni-stuttgart.de)
            if (!emailTrimmed.toLowerCase().matches("^[^@\\s]+@(?:[a-z0-9-]+\\.)*uni-stuttgart\\.de$")) {
                return ResponseEntity.badRequest().body("Assignee validation failed");
            }

            // existing validation (still performed for local-part rules, empty prename/name, etc.)
            if (!assigneeService.validate(assignee)) {
                return ResponseEntity.badRequest().body("Assignee validation failed");
            }
            Assignee savedAssignee = assigneeService.createAssignee(assignee);
            return ResponseEntity.status(201).body(savedAssignee);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body("Error creating assignee: " + e.getMessage());
        }
    }

    @GetMapping
    public List<Assignee> getAllAssignees() {
        return assigneeService.getAllAssignees();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Assignee> getAssigneeById(@PathVariable long id){
        try {
            Assignee assignee = assigneeService.getAssigneeById(id);
            return ResponseEntity.ok(assignee);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Assignee> updateAssignee(@PathVariable long id, @RequestBody Assignee assigneeDetails) {
        if (!assigneeService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        if (!assigneeService.validate(assigneeDetails)) {
            return ResponseEntity.badRequest().build();
        }
        assigneeDetails.setId(id); // ID setzen
        Assignee updated = assigneeService.updateAssignee(assigneeDetails);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignee(@PathVariable long id) {
        if (!assigneeService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        assigneeService.deleteAssignee(id);
        return ResponseEntity.ok().build();
    }
}
