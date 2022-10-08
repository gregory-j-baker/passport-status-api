package ca.gov.dtsstn.passport.api.web;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.Assert;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import ca.gov.dtsstn.passport.api.web.exception.NonUniqueResourceException;
import ca.gov.dtsstn.passport.api.web.exception.ResourceNotFoundException;
import ca.gov.dtsstn.passport.api.web.model.error.BadRequestErrorModel;
import ca.gov.dtsstn.passport.api.web.model.error.BadRequestErrorModel.FieldValidationErrorModel;
import ca.gov.dtsstn.passport.api.web.model.error.ImmutableBadRequestErrorModel;
import ca.gov.dtsstn.passport.api.web.model.error.ImmutableFieldValidationErrorModel;
import ca.gov.dtsstn.passport.api.web.model.error.ImmutableInternalServerErrorModel;
import ca.gov.dtsstn.passport.api.web.model.error.ImmutableResourceNotFoundErrorModel;
import ca.gov.dtsstn.passport.api.web.model.error.ImmutableUnprocessableEntityErrorModel;
import ca.gov.dtsstn.passport.api.web.model.error.InternalServerErrorModel;
import ca.gov.dtsstn.passport.api.web.model.error.ResourceNotFoundErrorModel;
import ca.gov.dtsstn.passport.api.web.model.error.UnprocessableEntityErrorModel;

/**
 * API global error handler.
 *
 * @author Sébastien Comeau (sebastien.comeau@hrsdc-rhdcc.gc.ca)
 * @author Greg Baker (gregory.j.baker@hrsdc-rhdcc.gc.ca)
 */
@RestControllerAdvice
public class ApiErrorHandler {

	private static final Logger log = LoggerFactory.getLogger(ApiErrorHandler.class);

	@ExceptionHandler({ BindException.class })
	protected ResponseEntity<BadRequestErrorModel> handleBindException(BindException ex) {
		final var details = ex.getAllErrors().stream().map(ObjectError::getDefaultMessage).toList();
		final var validationErrors = ex.getFieldErrors().stream().map(this::toValidationError).toList();
		final var errorModel = ImmutableBadRequestErrorModel.builder().details(details).fieldValidationErrors(validationErrors).build();
		return ResponseEntity.badRequest().body(errorModel);
	}

	@ExceptionHandler({ ConversionFailedException.class })
	public ResponseEntity<BadRequestErrorModel> handleConversionFailedException(ConversionFailedException ex) {
		final var details = List.of("Failed to convert value [" + ex.getValue() + "] to target type " + ex.getTargetType().getName());
		final var error = ImmutableBadRequestErrorModel.builder().details(details).build();
		return ResponseEntity.badRequest().body(error);
	}

	@ExceptionHandler({ HttpMessageNotReadableException.class })
	public ResponseEntity<BadRequestErrorModel> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
		final var error = ImmutableBadRequestErrorModel.builder().message(ex.getMessage()).build(); // NOSONAR
		return ResponseEntity.badRequest().body(error);
	}

	@ExceptionHandler({ NonUniqueResourceException.class })
	public ResponseEntity<UnprocessableEntityErrorModel> handleNonUniqueResourceException(NonUniqueResourceException ex) {
		final var error = ImmutableUnprocessableEntityErrorModel.builder().details(ex.getMessage()).build(); // NOSONAR
		return ResponseEntity.unprocessableEntity().body(error);
	}

	@ExceptionHandler({ ResourceNotFoundException.class })
	public ResponseEntity<ResourceNotFoundErrorModel> handleResourceNotFoundException(ResourceNotFoundException ex) {
		final var error = ImmutableResourceNotFoundErrorModel.builder().details(ex.getMessage()).build(); // NOSONAR
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}

	@ExceptionHandler({ Exception.class })
	public ResponseEntity<InternalServerErrorModel> handleGenericException(Exception ex) {
		final var correlationId = generateCorrelationId();
		log.error("[correlationId: {}] Request processing failed; nested exception is {}: {}", correlationId, ex.getClass().getName(), ex.getMessage(), ex);
		final var error = ImmutableInternalServerErrorModel.builder().details(ex.getMessage()).correlationId(correlationId).build();
		return ResponseEntity.internalServerError().body(error);
	}

	protected String generateCorrelationId() {
		return UUID.randomUUID().toString();
	}

	protected FieldValidationErrorModel toValidationError(FieldError fieldError) {
		Assert.notNull(fieldError, "fieldError is required; it must not be null");
		return ImmutableFieldValidationErrorModel.builder()
			.code(fieldError.getCode())              // NOSONAR
			.field(fieldError.getField())
			.message(fieldError.getDefaultMessage()) // NOSONAR
			.build();
	}

}
