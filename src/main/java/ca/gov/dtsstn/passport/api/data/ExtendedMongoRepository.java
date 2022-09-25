package ca.gov.dtsstn.passport.api.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * @author Greg Baker (gregory.j.baker@hrsdc-rhdcc.gc.ca)
 */
@NoRepositoryBean
public interface ExtendedMongoRepository<T, ID> extends MongoRepository<T, ID> { // NOSONAR

	<S extends T> Page<S> findAllCaseInsensitive(S probe, Pageable pageable);

}
