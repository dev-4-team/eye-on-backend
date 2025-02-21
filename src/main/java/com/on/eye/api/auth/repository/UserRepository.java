package com.on.eye.api.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.on.eye.api.auth.model.entity.OauthInfo;
import com.on.eye.api.auth.model.entity.User;
import com.on.eye.api.auth.model.enums.AccountState;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByOauthInfo(OauthInfo oAuthInfo);

    Optional<User> findByProfileEmailAndAccountState(
            String profileEmail, AccountState accountState);
}
