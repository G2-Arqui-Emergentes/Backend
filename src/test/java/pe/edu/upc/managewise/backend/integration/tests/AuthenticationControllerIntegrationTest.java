package pe.edu.upc.managewise.backend.integration.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.upc.managewise.backend.iam.domain.model.valueobjects.Roles;
import pe.edu.upc.managewise.backend.iam.interfaces.rest.resources.SignInResource;
import pe.edu.upc.managewise.backend.iam.interfaces.rest.resources.SignUpResource;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void signUp_CreatesUser_WhenValidRequest() throws Exception {
        SignUpResource signUpResource = new SignUpResource(
                "omar_luquillas@hotmail.com",
                "123456",
                List.of(Roles.ROLE_MEMBER),
                "Omar",
                "Luquillas"
        );

        mockMvc.perform(post("/api/v1/authentication/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpResource)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userName").value("testuser4"));
    }

    @Test
    void signIn_ReturnsAuthenticatedUser_WhenCredentialsAreValid() throws Exception {
        // Registrar el usuario
        SignUpResource signUpResource = new SignUpResource(
                "omar_luquillas@hotmail.com",
                "123456",
                List.of(Roles.ROLE_MEMBER),
                "Omar",
                "Luquillas"
        );
        mockMvc.perform(post("/api/v1/authentication/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpResource)))
                .andExpect(status().isCreated());

        // Iniciar sesión
        SignInResource signInResource = new SignInResource(
                "loginuser5",
                "loginpassword"
        );
        mockMvc.perform(post("/api/v1/authentication/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signInResource)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("loginuser5"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }
}
