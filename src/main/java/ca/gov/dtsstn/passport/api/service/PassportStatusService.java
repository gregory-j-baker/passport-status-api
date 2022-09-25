package ca.gov.dtsstn.passport.api.service;

import java.util.Optional;

import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import ca.gov.dtsstn.passport.api.data.PassportStatusRepository;
import ca.gov.dtsstn.passport.api.service.domain.PassportStatus;
import ca.gov.dtsstn.passport.api.service.mapper.PassportStatusMapper;

/**
 * Service class to handle {@link PassportStatus} interactions.
 *
 * @author Greg Baker (gregory.j.baker@hrsdc-rhdcc.gc.ca)
 */
@Service
public class PassportStatusService {

	private final PassportStatusMapper passportStatusMapper = Mappers.getMapper(PassportStatusMapper.class);

	private final PassportStatusRepository passportStatusRepository;

	public PassportStatusService(PassportStatusRepository passportStatusRepository) {
		Assert.notNull(passportStatusRepository, "passportStatusRepository is required; it must not be null");
		this.passportStatusRepository = passportStatusRepository;
	}

	public PassportStatus create(PassportStatus passportStatus) {
		Assert.notNull(passportStatus, "passportStatus is required; it must not be null");
		Assert.isNull(passportStatus.getId(), "passportStatus.id must be null when creating new instance");
		return passportStatusMapper.fromDocument(passportStatusRepository.save(passportStatusMapper.toDocument(passportStatus)));
	}

	public Optional<PassportStatus> read(String id) {
		Assert.hasText(id, "id is required; it must not be null or blank");
		return passportStatusRepository.findById(id).map(passportStatusMapper::fromDocument);
	}

	public PassportStatus update(PassportStatus passportStatus) {
		Assert.notNull(passportStatus, "passportStatus is required; it must not be null");
		Assert.notNull(passportStatus.getId(), "passportStatus.id must not be null when updating existing instance");
		final var target = passportStatusRepository.findById(passportStatus.getId()).orElseThrow(); // NOSONAR
		return passportStatusMapper.fromDocument(passportStatusRepository.save(passportStatusMapper.update(passportStatus, target)));
	}

	public void delete(String id) {
		passportStatusRepository.deleteById(id);
	}

	public Page<PassportStatus> readAll(Pageable pageable) {
		Assert.notNull(pageable, "pageable is required; it must not be null");
		return passportStatusRepository.findAll(pageable).map(passportStatusMapper::fromDocument);
	}

	public Page<PassportStatus> search(PassportStatus passportStatusProbe, Pageable pageable) {
		Assert.notNull(passportStatusProbe, "passportStatusProbe is required; it must not be null");
		Assert.notNull(pageable, "pageable is required; it must not be null");
		final var searchablePassportStatusProbe = passportStatusMapper.toSearchableDocument(passportStatusProbe);
		return passportStatusRepository.findAllCaseInsensitive(searchablePassportStatusProbe, pageable).map(passportStatusMapper::fromDocument);
	}

}
