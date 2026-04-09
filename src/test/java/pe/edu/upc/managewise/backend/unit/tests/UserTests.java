package pe.edu.upc.managewise.backend.unit.tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.edu.upc.managewise.backend.iam.domain.model.aggregates.User;
import pe.edu.upc.managewise.backend.iam.domain.model.commands.UpdateUserCommand;

import static org.assertj.core.api.Assertions.assertThat;

public class UserTests {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("omar_luquillas@hotmail.com", "123456");
    }

    @Test
    void createUser_ShouldSetUsernameAndPassword() {
        assertThat(user.getEmail()).isEqualTo("omar_luquillas@hotmail.com");
        assertThat(user.getPassword()).isEqualTo("123456");
    }

    @Test
    void updateUserDetails_ShouldUpdateUsernameAndPassword() {
        UpdateUserCommand command = new UpdateUserCommand("Omar", "Luquillas", "imagen.png",10000.00);
        user.updateUserDetails(command);

        assertThat(user.getEmail()).isEqualTo("omar_luquillas@hotmail.com");
        assertThat(user.getPassword()).isEqualTo("123456");
        assertThat(user.getName()).isEqualTo("Omar");
        assertThat(user.getLastName()).isEqualTo("Luquillas");
        assertThat(user.getImageUrl()).isEqualTo("imagen.png");
        assertThat(user.getSalary()).isEqualTo(10000.00);
    }
}
