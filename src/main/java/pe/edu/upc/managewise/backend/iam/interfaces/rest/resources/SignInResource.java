//package pe.edu.upc.managewise.backend.iam.interfaces.rest.resources;

//public record SignInResource(String username, String password,  String recaptchaToken) {
//}

package pe.edu.upc.managewise.backend.iam.interfaces.rest.resources;

public record SignInResource(
        String email,
        String password
) {
}
