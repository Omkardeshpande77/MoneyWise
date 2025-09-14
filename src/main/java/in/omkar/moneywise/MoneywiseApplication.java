package in.omkar.moneywise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MoneywiseApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoneywiseApplication.class, args);
	}

}
