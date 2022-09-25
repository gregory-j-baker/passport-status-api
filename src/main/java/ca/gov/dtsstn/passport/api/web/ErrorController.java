package ca.gov.dtsstn.passport.api.web;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import ca.gov.dtsstn.passport.api.web.exception.NonUniqueResourceException;
import ca.gov.dtsstn.passport.api.web.exception.ResourceNotFoundException;
import ca.gov.dtsstn.passport.api.web.model.error.BadRequestErrorModel;
import ca.gov.dtsstn.passport.api.web.model.error.BadRequestErrorModel.ValidationErrorModel;
import ca.gov.dtsstn.passport.api.web.model.error.ImmutableBadRequestErrorModel;
import ca.gov.dtsstn.passport.api.web.model.error.ImmutableInternalServerErrorModel;
import ca.gov.dtsstn.passport.api.web.model.error.ImmutableResourceNotFoundErrorModel;
import ca.gov.dtsstn.passport.api.web.model.error.ImmutableUnprocessableEntityErrorModel;
import ca.gov.dtsstn.passport.api.web.model.error.ImmutableValidationErrorModel;
import ca.gov.dtsstn.passport.api.web.model.error.InternalServerErrorModel;
import ca.gov.dtsstn.passport.api.web.model.error.ResourceNotFoundErrorModel;
import ca.gov.dtsstn.passport.api.web.model.error.UnprocessableEntityErrorModel;

/**
 * API global error handler.
 *
 * @author Sébastien Comeau (sebastien.comeau@hrsdc-rhdcc.gc.ca)
 */
@RestControllerAdvice
public class ErrorController {

	@ExceptionHandler({ BindException.class })
	protected ResponseEntity<BadRequestErrorModel> handleBindException(BindException ex) {
		final var details = ex.getAllErrors().stream().map(ObjectError::getDefaultMessage).collect(Collectors.toUnmodifiableList());
		final var validationErrors = ex.getFieldErrors().stream().map(this::toValidationError).collect(Collectors.toUnmodifiableList());
		final var errorModel = ImmutableBadRequestErrorModel.builder().details(details).validationErrors(validationErrors).build();
		return ResponseEntity.badRequest().body(errorModel);
	}

	@ExceptionHandler({ ConversionFailedException.class })
	public ResponseEntity<Object> handleConversionFailedException(ConversionFailedException ex) {
		final var details = List.of("Failed to convert value [" + ex.getValue() + "] to target type " + ex.getTargetType().getName());
		final var error = ImmutableBadRequestErrorModel.builder().details(details).build();
		return ResponseEntity.badRequest().body(error);
	}

	@ExceptionHandler({ NonUniqueResourceException.class })
	public ResponseEntity<UnprocessableEntityErrorModel> handleNonUniqueResourceException(NonUniqueResourceException ex) {
		final var error = ImmutableUnprocessableEntityErrorModel.builder().details(ex.getMessage()).build();
		return ResponseEntity.unprocessableEntity().body(error);
	}

	@ExceptionHandler({ ResourceNotFoundException.class })
	public ResponseEntity<ResourceNotFoundErrorModel> handleResourceNotFoundException(ResourceNotFoundException ex) {
		final var error = ImmutableResourceNotFoundErrorModel.builder().details(ex.getMessage()).build();
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}

	@ExceptionHandler({ Exception.class })
	public ResponseEntity<InternalServerErrorModel> handleGenericException(Exception ex) {
		final var error = ImmutableInternalServerErrorModel.builder().details(ex.getMessage()).build();
		return ResponseEntity.internalServerError().body(error);
	}

	@Nullable
	protected String removeCurlyBraces(@Nullable String string) {
		if (string == null) { return null; }

		final var startIndex = string.indexOf('{');
		final var endIndex = string.lastIndexOf('}');

		if (startIndex == -1 || endIndex == -1 || endIndex+1 != string.length()) {
			return string;
		}

		return string.substring(startIndex+1, endIndex);
	}

	protected ValidationErrorModel toValidationError(FieldError fieldError) {
		Assert.notNull(fieldError, "fieldError is required; it must not be null");
		return ImmutableValidationErrorModel.builder()
			.code(removeCurlyBraces(fieldError.getCode()))
			.field(fieldError.getField())
			.message(fieldError.getDefaultMessage())
			.build();
	}

}
