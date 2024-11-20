package Back.whats_your_ETF;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class WhatsYourEtfApplication {

	public static void main(String[] args) {
		SpringApplication.run(WhatsYourEtfApplication.class, args);
	}

}
