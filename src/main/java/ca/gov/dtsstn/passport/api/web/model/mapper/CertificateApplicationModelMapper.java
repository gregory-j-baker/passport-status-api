package ca.gov.dtsstn.passport.api.web.model.mapper;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import ca.gov.dtsstn.passport.api.service.domain.PassportStatus;
import ca.gov.dtsstn.passport.api.service.domain.PassportStatus.Status;
import ca.gov.dtsstn.passport.api.web.model.CertificateApplicationApplicantModel;
import ca.gov.dtsstn.passport.api.web.model.CertificateApplicationIdentificationModel;
import ca.gov.dtsstn.passport.api.web.model.CertificateApplicationStatusModel;
import ca.gov.dtsstn.passport.api.web.model.CreateCertificateApplicationRequestModel;
import ca.gov.dtsstn.passport.api.web.model.GetCertificateApplicationRepresentationModel;
import ca.gov.dtsstn.passport.api.web.model.ImmutableCertificateApplicationIdentificationModel;

/**
 * @author Greg Baker (gregory.j.baker@hrsdc-rhdcc.gc.ca)
 */
@Mapper
public interface CertificateApplicationModelMapper {

	// TODO :: GjB :: remove this once actual code → status mappings are known
	final Map<String, PassportStatus.Status> statusMap = Map.of(
		"-1", PassportStatus.Status.UNKNOWN,
		"1", PassportStatus.Status.APPROVED,
		"2", PassportStatus.Status.IN_EXAMINATION,
		"3", PassportStatus.Status.REJECTED
	);

	@Nullable
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdBy", ignore = true)
	@Mapping(target = "createdDate", ignore = true)
	@Mapping(target = "lastModifiedBy", ignore = true)
	@Mapping(target = "lastModifiedDate", ignore = true)
	@Mapping(target = "applicationRegisterSid", source = "certificateApplication.certificateApplicationIdentifications", qualifiedByName = { "findApplicationRegisterSid" })
	@Mapping(target = "dateOfBirth", source = "certificateApplication.certificateApplicationApplicant.birthDate.date")
	@Mapping(target = "email", source = "certificateApplication.certificateApplicationApplicant.personContactInformation.contactEmailId")
	@Mapping(target = "fileNumber", source = "certificateApplication.certificateApplicationIdentifications", qualifiedByName = { "findFileNumber" })
	@Mapping(target = "firstName", source = "certificateApplication.certificateApplicationApplicant.personName.personGivenNames", qualifiedByName = { "getFirstElement" })
	@Mapping(target = "lastName", source = "certificateApplication.certificateApplicationApplicant.personName.personSurname")
	@Mapping(target = "status", source = "certificateApplication.certificateApplicationStatus", qualifiedByName = { "toStatus" })
	@Mapping(target = "statusDate", source = "certificateApplication.certificateApplicationStatus.statusDate.date")
	PassportStatus toDomain(@Nullable CreateCertificateApplicationRequestModel createCertificateApplicationRequest);

	@Nullable
	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(target = "add", ignore = true) // fixes a weird vscode/eclipse & mapstruct bug quirk/bug 💩
	@Mapping(target = "certificateApplication.certificateApplicationApplicant.birthDate.date", source = "dateOfBirth")
	@Mapping(target = "certificateApplication.certificateApplicationApplicant.personContactInformation.contactEmailId", source = "email")
	@Mapping(target = "certificateApplication.certificateApplicationIdentifications", source = "passportStatus", qualifiedByName = { "getCertificateApplicationIdentifications" })
	@Mapping(target = "certificateApplication.certificateApplicationApplicant.personName.personGivenNames", source = "passportStatus", qualifiedByName = { "getPersonGivenNames" })
	@Mapping(target = "certificateApplication.certificateApplicationApplicant.personName.personSurname", source = "lastName")
	@Mapping(target = "certificateApplication.certificateApplicationStatus.statusCode", source = "status", qualifiedByName = { "toStatus" })
	@Mapping(target = "certificateApplication.certificateApplicationStatus.statusDate.date", source = "statusDate")
	GetCertificateApplicationRepresentationModel toModel(@Nullable PassportStatus passportStatus);

	/**
	 * Map an ISO 8601 compliant date string to a {@link LocalDate}.
	 * Throws a {@link DateTimeParseException} if the string is invalid.
	 */
	@Nullable
	default LocalDate toLocalDate(@Nullable String date) {
		if (date == null) { return null; }
		return LocalDate.parse(date);
	}

	/**
	 * Map a {@link CertificateApplicationApplicantModel} to a {@link PassportStatus.Status}. Returns {@code null} if
	 * {@code certificateApplicationStatusModel} is null or the status cannot be found.
	 */
	@Nullable
	@Named("toStatus")
	default PassportStatus.Status toStatus(@Nullable CertificateApplicationStatusModel certificateApplicationStatus) {
		return Optional.ofNullable(certificateApplicationStatus)
			.map(CertificateApplicationStatusModel::getStatusCode)
			.map(statusMap::get)
			.orElse(null);
	}

	/**
	 * Map a {@link PassportStatus.Status} to a {@link CertificateApplicationStatusModel}. Returns {@code null} if
	 * {@code passportStatus} is null or the status cannot be found.
	 */
	@Nullable
	@Named("toStatus")
	default String toStatus(@Nullable PassportStatus.Status passportStatus) {
		final Function<PassportStatus.Status, String> toStatusCode = status -> statusMap.entrySet().stream()
			.filter(entry -> entry.getValue().equals(status))
			.map(Entry<String, Status>::getKey)
			.findFirst().orElse(null);

		return Optional.ofNullable(passportStatus)
			.map(toStatusCode)
			.orElse(null);
	}

	/**
	 * Finds the application registration SID element within {@code certificateApplicationIdentifications}. Returns null
	 * if not found or {@code certificateApplicationIdentifications} is null.
	 */
	@Nullable
	@Named("findApplicationRegisterSid")
	default String findApplicationRegisterSid(@Nullable Iterable<CertificateApplicationIdentificationModel> certificateApplicationIdentifications) {
		return findCertificateApplicationIdentification(certificateApplicationIdentifications, CertificateApplicationIdentificationModel.APPLICATION_REGISTER_SID_CATEGORY_TEXT);
	}

	@Nullable
	@Named("getCertificateApplicationIdentifications")
	default List<CertificateApplicationIdentificationModel> getCertificateApplicationIdentifications(@Nullable PassportStatus passportStatus) {
		if (passportStatus == null) { return null; }

		final CertificateApplicationIdentificationModel applicationRegisterSid = Optional.ofNullable(passportStatus.getApplicationRegisterSid())
			.map(xxx -> ImmutableCertificateApplicationIdentificationModel.builder()
				.identificationCategoryText(CertificateApplicationIdentificationModel.APPLICATION_REGISTER_SID_CATEGORY_TEXT)
				.identificationId(passportStatus.getApplicationRegisterSid())
				.build())
			.orElse(null);

		final CertificateApplicationIdentificationModel fileNumber = Optional.ofNullable(passportStatus.getFileNumber())
			.map(x -> ImmutableCertificateApplicationIdentificationModel.builder()
				.identificationCategoryText(CertificateApplicationIdentificationModel.FILE_NUMBER_CATEGORY_TEXT)
				.identificationId(passportStatus.getFileNumber())
				.build())
			.orElse(null);

		return Stream.of(applicationRegisterSid, fileNumber).filter(Objects::nonNull).toList();
	}

	@Nullable
	@Named("getPersonGivenNames")
	default List<String> getPersonGivenNames(@Nullable PassportStatus passportStatus) {
		return Optional.ofNullable(passportStatus)
			.map(PassportStatus::getFirstName)
			.map(List::of)
			.orElse(List.of());
	}

	/**
	 * Finds the {@code CertificateApplicationIdentificationModel} that has a matching {@code identificationCategoryText}
	 * value. Returns {@code null} if {@code certificateApplicationIdentifications} is null.
	 */
	@Nullable
	default String findCertificateApplicationIdentification(@Nullable Iterable<CertificateApplicationIdentificationModel> certificateApplicationIdentifications, String identificationCategoryText) {
		Assert.hasText(identificationCategoryText, "identificationCategoryText is required; it must not be null or blank");
		return stream(certificateApplicationIdentifications)
			.filter(hasIdentificationCategoryText(identificationCategoryText))
			.map(CertificateApplicationIdentificationModel::getIdentificationId)
			.findFirst().orElse(null);
	}

	/**
	 * Finds the file number element within {@code certificateApplicationIdentifications}. Returns null if not found or
	 * {@code certificateApplicationIdentifications} is null.
	 */
	@Nullable
	@Named("findFileNumber")
	default String findFileNumber(@Nullable Iterable<CertificateApplicationIdentificationModel> certificateApplicationIdentifications) {
		return findCertificateApplicationIdentification(certificateApplicationIdentifications, CertificateApplicationIdentificationModel.FILE_NUMBER_CATEGORY_TEXT);
	}

	/**
	 * Maps an {@code Iterable} to a single element by returning the first element found. Returns {@code null} if
	 * {@code iterable} is empty or {@code null}.
	 */
	@Nullable
	@Named("getFirstElement")
	default <T> T getFirstElement(@Nullable Iterable<T> iterable) {
		return stream(iterable).findFirst().orElse(null);
	}

	/**
	 * A predicate that operates over {@code CertificateApplicationIdentificationModel}, returning {@code true} if
	 * {@code CertificateApplicationIdentificationModel.identificationCategoryText} equals {@code identificationCategoryText}.
	 * Returns {@code false} if {@code certificateApplicationIdentificationModel} is {@code null}.
	 */
	default Predicate<CertificateApplicationIdentificationModel> hasIdentificationCategoryText(String identificationCategoryText) {
		Assert.hasText(identificationCategoryText, "identificationCategoryText is required; it must not be null or blank");
		return certificateApplicationIdentification -> Optional.ofNullable(certificateApplicationIdentification)
			.map(CertificateApplicationIdentificationModel::getIdentificationCategoryText)
			.map(identificationCategoryText::equals)
			.orElse(false);
	}

	/**
	 * Maps an {@link Iterable} to a {@link Stream}. Returns {@code Stream.empty()} if {@code iterable} is null.
	 */
	default <T> Stream<T> stream(@Nullable Iterable<T> iterable) {
		return Optional.ofNullable(iterable)
			.map(Iterable::spliterator)
			.map(spliterator -> StreamSupport.stream(spliterator, false))
			.orElse(Stream.empty());
	}

}
