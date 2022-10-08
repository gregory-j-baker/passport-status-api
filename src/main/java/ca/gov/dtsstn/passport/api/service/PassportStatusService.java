package ca.gov.dtsstn.passport.api.service;

import java.util.Optional;

import org.mapstruct.factory.Mappers;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import ca.gov.dtsstn.passport.api.data.PassportStatusRepository;
import ca.gov.dtsstn.passport.api.service.domain.PassportStatus;
import ca.gov.dtsstn.passport.api.service.event.ImmutablePassportStatusCreatedEvent;
import ca.gov.dtsstn.passport.api.service.event.ImmutablePassportStatusDeletedEvent;
import ca.gov.dtsstn.passport.api.service.event.ImmutablePassportStatusReadEvent;
import ca.gov.dtsstn.passport.api.service.event.ImmutablePassportStatusUpdatedEvent;
import ca.gov.dtsstn.passport.api.service.mapper.PassportStatusMapper;

/**
 * Service class to handle {@link PassportStatus} interactions.
 *
 * @author Greg Baker (gregory.j.baker@hrsdc-rhdcc.gc.ca)
 */
@Service
public class PassportStatusService {

	private final ApplicationEventPublisher eventPublisher;

	private final JmsTemplate jmsTemplate;

	private final PassportStatusMapper mapper = Mappers.getMapper(PassportStatusMapper.class);

	private final PassportStatusRepository repository;

	public PassportStatusService(ApplicationEventPublisher eventPublisher, JmsTemplate jmsTemplate, PassportStatusRepository repository) {
		Assert.notNull(eventPublisher, "eventPublisher is required; it must not be null");
		Assert.notNull(jmsTemplate, "jmsTemplate is required; it must not be null");
		Assert.notNull(repository, "repository is required; it must not be null");
		this.eventPublisher = eventPublisher;
		this.jmsTemplate = jmsTemplate;
		this.repository = repository;
	}

	public PassportStatus create(PassportStatus passportStatus) {
		Assert.notNull(passportStatus, "passportStatus is required; it must not be null");                  // NOSONAR
		Assert.isNull(passportStatus.getId(), "passportStatus.id must be null when creating new instance"); // NOSONAR
		final var createdPassportStatus = mapper.fromDocument(repository.save(mapper.toDocument(passportStatus)));
		eventPublisher.publishEvent(ImmutablePassportStatusCreatedEvent.of(createdPassportStatus));
		return createdPassportStatus;
	}

	public void queueCreation(PassportStatus passportStatus) {
		Assert.notNull(passportStatus, "passportStatus is required; it must not be null");                  // NOSONAR
		Assert.isNull(passportStatus.getId(), "passportStatus.id must be null when creating new instance"); // NOSONAR
		jmsTemplate.convertAndSend("passport-statuses", passportStatus);
	}

	public Optional<PassportStatus> read(String id) {
		Assert.hasText(id, "id is required; it must not be null or blank");
		final var passportStatus = repository.findById(id).map(mapper::fromDocument);
		passportStatus.map(ImmutablePassportStatusReadEvent::of).ifPresent(eventPublisher::publishEvent);
		return passportStatus;
	}

	public PassportStatus update(PassportStatus passportStatus) {
		Assert.notNull(passportStatus, "passportStatus is required; it must not be null");
		Assert.notNull(passportStatus.getId(), "passportStatus.id must not be null when updating existing instance");
		final var originalPassportStatus = repository.findById(passportStatus.getId()).orElseThrow(); // NOSONAR
		final var updatedPassportStatus = mapper.fromDocument(repository.save(mapper.update(passportStatus, originalPassportStatus)));
		eventPublisher.publishEvent(ImmutablePassportStatusUpdatedEvent.of(mapper.fromDocument(originalPassportStatus), updatedPassportStatus));
		return updatedPassportStatus;
	}

	public void delete(String id) {
		repository.findById(id)
			.map(mapper::fromDocument)
			.ifPresent(passportStatus -> {
				repository.deleteById(id);
				eventPublisher.publishEvent(ImmutablePassportStatusDeletedEvent.of(passportStatus));
			});
	}

	public Page<PassportStatus> readAll(Pageable pageable) {
		Assert.notNull(pageable, "pageable is required; it must not be null");
		final var passportStatuses = repository.findAll(pageable).map(mapper::fromDocument);
		passportStatuses.map(ImmutablePassportStatusReadEvent::of).forEach(eventPublisher::publishEvent);
		return passportStatuses;
	}

	public Page<PassportStatus> search(PassportStatus passportStatusProbe, Pageable pageable) {
		Assert.notNull(passportStatusProbe, "passportStatusProbe is required; it must not be null");
		Assert.notNull(pageable, "pageable is required; it must not be null");
		final var searchablePassportStatusProbe = mapper.toSearchableDocument(passportStatusProbe);
		final var passportStatuses = repository.findAllCaseInsensitive(searchablePassportStatusProbe, pageable).map(mapper::fromDocument);
		passportStatuses.map(ImmutablePassportStatusReadEvent::of).forEach(eventPublisher::publishEvent);
		return passportStatuses;
	}

}
