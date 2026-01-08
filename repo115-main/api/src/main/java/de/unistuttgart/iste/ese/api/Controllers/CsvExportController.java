package de.unistuttgart.iste.ese.api.Controllers;

import de.unistuttgart.iste.ese.api.Entities.Assignee;
import de.unistuttgart.iste.ese.api.Entities.ToDo;
import de.unistuttgart.iste.ese.api.Services.ToDoService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/csv-downloads")
public class CsvExportController {

    private final ToDoService toDoService;

    public CsvExportController(ToDoService toDoService) {
        this.toDoService = toDoService;
    }

    @GetMapping("/todos")
    public ResponseEntity<String> getTodosCsv() {
        List<ToDo> todos = toDoService.getAllToDos();

        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.RFC4180.builder()
                .setHeader("id", "title", "description", "finished", "assignees", "createdDate", "dueDate",
                        "finishedDate", "category", "priority")
                .build();

        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            for (ToDo todo : todos) {
                String assigneeString = "";
                if (todo.getAssigneeList() != null && !todo.getAssigneeList().isEmpty()) {
                    assigneeString = todo.getAssigneeList().stream()
                            .map(a -> a.getPrename() + " " + a.getName())
                            .collect(Collectors.joining("+"));
                }

                printer.printRecord(
                        todo.getId(),
                        todo.getTitle(),
                        todo.getDescription(),
                        String.valueOf(todo.isFinished()).toLowerCase(), // "true"/"false"
                        assigneeString,
                        todo.getCreatedDate(),
                        todo.getDueDate(),
                        todo.getFinishedDate() != null ? todo.getFinishedDate() : "",
                        todo.getCategory(),
                        todo.getPriority());
            }
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"todos.csv\"")
                .body(writer.toString());
    }
}
