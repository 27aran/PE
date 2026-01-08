package de.unistuttgart.iste.ese.api.Entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import jakarta.persistence.*;

@Entity
@Table(name = "assignees")
public class Assignee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;
    private String prename;
    private String email;

    public Assignee() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrename() {
        return prename;
    }

    public void setPrename(String prename) {
        this.prename = prename;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
