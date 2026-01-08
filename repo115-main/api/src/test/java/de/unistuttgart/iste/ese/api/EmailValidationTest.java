package de.unistuttgart.iste.ese.api;

import de.unistuttgart.iste.ese.api.Entities.Assignee;
import de.unistuttgart.iste.ese.api.Services.AssigneeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles(profiles = "test")
public class EmailValidationTest {

    @Autowired
    private AssigneeService assigneeService;

    @Test
    public void testVariousEmailFormats() {
        // Valid emails that should be accepted
        String[] validEmails = {
                "test@uni-stuttgart.de",
                "test.name@uni-stuttgart.de",
                "test_name@uni-stuttgart.de",
                "test-name@uni-stuttgart.de",
                "test+tag@uni-stuttgart.de",
                "t@uni-stuttgart.de",
                "123@uni-stuttgart.de",
                "test@iste.uni-stuttgart.de",
                "test.name@iste.uni-stuttgart.de",
                "test_name@sec.uni-stuttgart.de",
                "a.b@uni-stuttgart.de",
                "first.last@uni-stuttgart.de",
                "test!name@uni-stuttgart.de",
                "test#name@uni-stuttgart.de",
                "test'name@uni-stuttgart.de",
                "test$name@uni-stuttgart.de",
                "test&name@uni-stuttgart.de",
                "test*name@uni-stuttgart.de",
                "test=name@uni-stuttgart.de",
                "test?name@uni-stuttgart.de",
                "test^name@uni-stuttgart.de",
                "test`name@uni-stuttgart.de",
                "test{name@uni-stuttgart.de",
                "test|name@uni-stuttgart.de",
                "test}name@uni-stuttgart.de",
                "test~name@uni-stuttgart.de"
        };

        for (String email : validEmails) {
            Assignee assignee = new Assignee();
            assignee.setPrename("Test");
            assignee.setName("User");
            assignee.setEmail(email);

            boolean result = assigneeService.validate(assignee);
            System.out.println("Email: " + email + " -> " + (result ? "VALID" : "INVALID"));
            assertTrue(result, "Expected email to be valid: " + email);
        }

        // Invalid emails that should be rejected
        String[] invalidEmails = {
                "@uni-stuttgart.de",
                "test..name@uni-stuttgart.de",
                "test....@uni-stuttgart.de",
                ".test@uni-stuttgart.de",
                "test.@uni-stuttgart.de",
                "test@gmail.com",
                "test@isteuni-stuttgart.de"
        };

        for (String email : invalidEmails) {
            Assignee assignee = new Assignee();
            assignee.setPrename("Test");
            assignee.setName("User");
            assignee.setEmail(email);

            boolean result = assigneeService.validate(assignee);
            System.out.println("Email: " + email + " -> " + (result ? "VALID" : "INVALID"));
            assertFalse(result, "Expected email to be invalid: " + email);
        }
    }
}
