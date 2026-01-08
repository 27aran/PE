package de.unistuttgart.iste.ese.api.Repositories;

import de.unistuttgart.iste.ese.api.Entities.Assignee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssigneeRepository extends JpaRepository<Assignee, Long> {
}
