package de.unistuttgart.iste.ese.api.Entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "todos")
public class ToDo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private boolean finished;
    private String priority;
    private String title;
    private String description;
    private String category;
    private LocalDate dueDate;
    private LocalDate finishedDate;
    private LocalDate createdDate;


    @ManyToMany(cascade = {CascadeType.MERGE})
    @JoinTable(
        name = "todo_assignee",
        joinColumns = @JoinColumn(name = "todo_id"),
        inverseJoinColumns = @JoinColumn(name = "assignee_id")
    )
    @JsonProperty("assigneeList")
    private List<Assignee> assigneeList;

    public ToDo() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty("assigneeList")
    public List<Assignee> getAssigneeList() {
        return assigneeList;
    }

    public void setAssigneeList(List<Assignee> assigneeList) {
        this.assigneeList = assigneeList;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getFinishedDate() {
        return finishedDate;
    }

    public void setFinishedDate(LocalDate finishedDate) {
        this.finishedDate = finishedDate;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }


}
