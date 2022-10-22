package ca.gov.dtsstn.passport.api.web.model;

import java.io.Serializable;
import java.time.LocalDate;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Pattern;

import org.immutables.value.Value.Immutable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @author Greg Baker (gregory.j.baker@hrsdc-rhdcc.gc.ca)
 */
@Immutable
@Schema(name = "ElectronicServiceCreateRequest")
@JsonDeserialize(as = ImmutableCreateElectronicServiceRequestModel.class)
public interface CreateElectronicServiceRequestModel extends Serializable {

	@Nullable
	@DateTimeFormat(iso = ISO.DATE)
	@NotNull(message = "dateOfBirth is required; it must not be null")
	@PastOrPresent(message = "dateOfBirth must be a date in the past")
	@Schema(description = "The date of birth of the passport applicant in ISO-8601 format.", example = "2000-01-01", required = true)
	LocalDate getDateOfBirth();

	@Nullable
	@Email(message = "email must be a valid email address")
	@NotNull(message = "email is required; it must not be null")
	@Pattern(message = "email must be a valid email address", regexp = "[^@]+@[^@]+\\.[^@]+") // prevents user@localhost style emails
	@Schema(description = "The email address of the user submitting the electronic service request.", example = "user@example.com", required = true)
	String getEmail();

	@Nullable
	@NotBlank(message = "firstName is required; it must not be null or blank")
	@Schema(description = "The first name of the passport applicant.", example = "John", required = true)
	String getFirstName();

	@Nullable
	@NotBlank(message = "lastName is required; it must not be null or blank")
	@Schema(description = "The last name of the passport applicant.", example = "Doe", required = true)
	String getLastName();

}
