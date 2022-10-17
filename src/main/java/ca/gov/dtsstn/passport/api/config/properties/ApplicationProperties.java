package ca.gov.dtsstn.passport.api.config.properties;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

/**
 * @author Greg Baker (gregory.j.baker@hrsdc-rhdcc.gc.ca)
 */
@Validated
@ConstructorBinding
@ConfigurationProperties("application")
@EnableConfigurationProperties({ GcNotifyProperties.class, SecurityProperties.class, SwaggerUiProperties.class })
public record ApplicationProperties(
	@NestedConfigurationProperty @NotNull GcNotifyProperties gcnotify,
	@NestedConfigurationProperty @NotNull SecurityProperties security,
	@NestedConfigurationProperty @NotNull SwaggerUiProperties swaggerUi
) {}
