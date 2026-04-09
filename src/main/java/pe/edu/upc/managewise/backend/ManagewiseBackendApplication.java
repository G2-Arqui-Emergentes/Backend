package pe.edu.upc.managewise.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class ManagewiseBackendApplication {

	/*Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        System.setProperty("recaptcha.secret", dotenv.get("RECAPTCHA_SECRET_KEY"));*/
//
	public static void main(String[] args) {
		System.out.println("🔍 Working dir: " + System.getProperty("user.dir"));
		System.out.println("📄 .env exists? " + new File(".env").exists());


		//Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

		//String secret = dotenv.get("RECAPTCHA_SECRET_KEY");
		//String verifyUrl = dotenv.get("RECAPTCHA_VERIFY_URL");

		//if (secret == null || verifyUrl == null) {
		//	System.err.println("❌ Variables de entorno RECAPTCHA no encontradas.");
		//	System.exit(1);
		//}

		//System.setProperty("recaptcha.secret", secret);
		//System.setProperty("recaptcha.verify.url", verifyUrl);
		SpringApplication.run(ManagewiseBackendApplication.class, args);
	}

}
