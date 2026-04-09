//package pe.edu.upc.managewise.backend.iam.interfaces.rest.resources;

//import java.util.List;

//public record SignUpResource(String username, String password, List<String> roles, String recaptchaToken) {
//}


package pe.edu.upc.managewise.backend.iam.interfaces.rest.resources;

import pe.edu.upc.managewise.backend.iam.domain.model.valueobjects.Roles;

import java.util.List;

public record SignUpResource(
        String email,
        String password,
        List<Roles> roles,
        String name,
        String lastName
) {
}
