package ir.momeni.slyther;

import ir.momeni.slyther.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackageClasses = AppProperties.class)
public class SlytherApplication {
	public static void main(String[] args) {
		SpringApplication.run(SlytherApplication.class, args);
	}
}