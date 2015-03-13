package com.nwt.spade.repository;

import com.nwt.spade.domain.PersistentToken;
import com.nwt.spade.domain.SpringUser;

import org.joda.time.LocalDate;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Spring Data MongoDB repository for the PersistentToken entity.
 */
public interface PersistentTokenRepository extends MongoRepository<PersistentToken, String> {

    List<PersistentToken> findByUser(SpringUser user);

    List<PersistentToken> findByTokenDateBefore(LocalDate localDate);

}
