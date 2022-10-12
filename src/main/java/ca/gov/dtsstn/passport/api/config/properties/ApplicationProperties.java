package ca.gov.dtsstn.passport.api.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @author Greg Baker (gregory.j.baker@hrsdc-rhdcc.gc.ca)
 */
@Validated
@ConstructorBinding
@ConfigurationProperties("application")
@EnableConfigurationProperties({ SecurityProperties.class, SwaggerUiProperties.class })
public record ApplicationProperties(
	SecurityProperties security,
	SwaggerUiProperties swaggerUi
) {}
