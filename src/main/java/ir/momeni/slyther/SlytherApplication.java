package ir.momeni.slyther;

import ir.momeni.slyther.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;


/**
 * Application entry point for the Slyther service.
 * <p>
 * {@link SpringBootApplication} enables component scanning, auto-configuration,
 * and property support. {@link ConfigurationPropertiesScan} registers classes
 * annotated with {@code @ConfigurationProperties} (e.g., {@link AppProperties})
 * so their typed configuration is bound from application properties.
 */
@SpringBootApplication
@ConfigurationPropertiesScan(basePackageClasses = AppProperties.class)
public class SlytherApplication {


	/**
	 * Boots the Spring context and starts the embedded server.
	 *
	 * @param args CLI arguments passed to the application
	 */
	public static void main(String[] args) {
		SpringApplication.run(SlytherApplication.class, args);
	}
}