package ca.gov.dtsstn.passport.api.web;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ca.gov.dtsstn.passport.api.service.NotificationService;
import ca.gov.dtsstn.passport.api.service.PassportStatusService;
import ca.gov.dtsstn.passport.api.web.exception.NonUniqueResourceException;
import ca.gov.dtsstn.passport.api.web.model.CreateElectronicServiceRequestModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Greg Baker (gregory.j.baker@hrsdc-rhdcc.gc.ca)
 */
@RestController
@ApiResponses.InternalServerError
@RequestMapping({ "/api/v1/esrf-requests" })
@Tag(name = "esrf-requests", description = "Endpoint to create ESRF requests.")
public class ElectronicServiceRequestController {

	private static final Logger log = LoggerFactory.getLogger(ElectronicServiceRequestController.class);

	private final NotificationService notificationService;

	private final PassportStatusService passportStatusService;

	public ElectronicServiceRequestController(NotificationService notificationService, PassportStatusService passportStatusService) {
		log.info("Creating 'electronicServiceRequestController' bean");

		Assert.notNull(notificationService, "notificationService is required; it must not be null");
		Assert.notNull(passportStatusService, "passportStatusService is required; it must not be null");

		this.notificationService = notificationService;
		this.passportStatusService = passportStatusService;
	}

	@PostMapping({ "" })
	@ApiResponses.BadRequestError
	@ResponseStatus(HttpStatus.ACCEPTED)
	@Operation(summary = "Create a new electronic service request.", operationId = "esrf-create")
	@ApiResponse(responseCode = "202", description = "The request has been accepted for processing.")
	public void create(@RequestBody @Validated CreateElectronicServiceRequestModel createElectronicServiceRequest) {
		log.trace("New electronic service request posted for: [{}]", createElectronicServiceRequest);

		final var dateOfBirth = LocalDate.parse(createElectronicServiceRequest.getClient().getPersonBirthDate().getDate());
		final var email = createElectronicServiceRequest.getClient().getPersonContactInformation().getContactEmailId();
		final var firstName = createElectronicServiceRequest.getClient().getPersonName().getPersonGivenNames().get(0);
		final var lastName = createElectronicServiceRequest.getClient().getPersonName().getPersonSurname();

		final var passportStatuses = passportStatusService.emailSearch(dateOfBirth, email, firstName, lastName);
		log.debug("Found {} file numbers for email address [{}]", passportStatuses.size(), email);

		if (passportStatuses.size() > 1) {
			log.warn("Search query returned non-unique file numbers: {}", createElectronicServiceRequest);
			throw new NonUniqueResourceException("Search query returned non-unique file numbers");
		}

		passportStatuses.stream().findFirst().ifPresent(notificationService::sendFileNumberNotification);
	}

}
