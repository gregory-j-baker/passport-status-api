package ca.gov.dtsstn.passport.api.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author Greg Baker (gregory.j.baker@hrsdc-rhdcc.gc.ca)
 * @since 0.0.0
 */
@ExtendWith({ MockitoExtension.class })
class UuidGeneratorTests {

	@Mock UuidGenerator.ValueGenerator valueGenerator;

	UuidGenerator uuidGenerator;

	final UUID id = UUID.randomUUID();

	@BeforeEach
	void setUp() {
		this.uuidGenerator = new UuidGenerator(valueGenerator);
	}

	@Test
	void testGenerate_withId() {
		final SharedSessionContractImplementor sharedSessionContractImplementor = mock(SharedSessionContractImplementor.class, Mockito.RETURNS_DEEP_STUBS);
		when(sharedSessionContractImplementor.getEntityPersister(any(), any()).getIdentifier(any(), any(SharedSessionContractImplementor.class))).thenReturn(id.toString());
		assertThat(uuidGenerator.generate(sharedSessionContractImplementor, new Object())).asString().isEqualTo(id.toString());
	}

	@Test
	void testGenerate_withNullId() {
		final SharedSessionContractImplementor sharedSessionContractImplementor = mock(SharedSessionContractImplementor.class, Mockito.RETURNS_DEEP_STUBS);
		when(sharedSessionContractImplementor.getEntityPersister(any(), any()).getIdentifier(any(), any(SharedSessionContractImplementor.class))).thenReturn(null);
		when(valueGenerator.generateUuid()).thenReturn(id);
		assertThat(uuidGenerator.generate(sharedSessionContractImplementor, new Object())).asString().isEqualTo(id.toString());
	}

}
