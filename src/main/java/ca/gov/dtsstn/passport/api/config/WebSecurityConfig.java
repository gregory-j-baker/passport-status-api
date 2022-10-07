package ca.gov.dtsstn.passport.api.config;

import static org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest.to;
import static org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest.toAnyEndpoint;
import static org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest.toLinks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import ca.gov.dtsstn.passport.api.web.AuthenticationErrorHandler;
import ca.gov.dtsstn.passport.api.web.ChangelogEndpoint;

/**
 * @author Greg Baker <gregory.j.baker@hrsdc-rhdcc.gc.ca>
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

	private static final Logger log = LoggerFactory.getLogger(WebSecurityConfig.class);

	/**
	 * CORS configuration bean.
	 */
	@ConfigurationProperties("application.security.cors")
	@Bean CorsConfiguration corsConfiguration() {
		log.info("Creating 'corsConfiguration' bean");
		return new CorsConfiguration();
	}

	@Bean CorsConfigurationSource corsConfigurationSource() {
		log.info("Creating 'corsConfigurationSource' bean");
		final var corsConfiguration = corsConfiguration();
		final var corsConfigurationSource = new UrlBasedCorsConfigurationSource();
		corsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
		return corsConfigurationSource;
	}

	@Bean SecurityFilterChain securityFilterChain(AuthenticationErrorHandler authenticationErrorController, Environment environment, HttpSecurity http) throws Exception {
		final var contentSecurityPolicy = environment.getProperty("application.security.content-security-policy");

		http // general security configuration
			.csrf().disable()
			.cors().and()
			.exceptionHandling()
				.accessDeniedHandler(authenticationErrorController).and()
			.headers()
				.cacheControl().disable()
				.contentSecurityPolicy(contentSecurityPolicy).and()
				.frameOptions().sameOrigin()
				.referrerPolicy(ReferrerPolicy.NO_REFERRER).and().and()
			.oauth2ResourceServer()
				.jwt().and()
				.authenticationEntryPoint(authenticationErrorController).and()
			.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS);

		http // public resources
			.authorizeRequests()
				.antMatchers(HttpMethod.OPTIONS).permitAll()
				.requestMatchers(toLinks()).permitAll()
				.requestMatchers(to(ChangelogEndpoint.class)).permitAll()
				.requestMatchers(to(HealthEndpoint.class)).permitAll()
				.requestMatchers(to(InfoEndpoint.class)).permitAll();

		http // protected resources
			.authorizeRequests()
				.requestMatchers(toAnyEndpoint()).authenticated();

		return http.build();
	}

}
