package de.unistuttgart.iste.ese.api.Repositories;

import de.unistuttgart.iste.ese.api.Entities.ToDo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ToDoRepository extends JpaRepository<ToDo, Long> {
}
