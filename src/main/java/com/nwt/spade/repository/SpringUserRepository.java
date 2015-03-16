package com.nwt.spade.repository;

import com.nwt.spade.domain.SpringUser;

import org.joda.time.DateTime;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Spring Data MongoDB repository for the User entity.
 */
public interface SpringUserRepository extends MongoRepository<SpringUser, String> {

    SpringUser findOneByActivationKey(String activationKey);

    List<SpringUser> findAllByActivatedIsFalseAndCreatedDateBefore(DateTime dateTime);

    SpringUser findOneByLogin(String login);

    SpringUser findOneByEmail(String email);

}
