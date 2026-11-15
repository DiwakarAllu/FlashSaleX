package com.flashsalex.user.repository;

import java.util.Optional;
import com.flashsalex.user.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserAccount, Long> {

	Optional<UserAccount> findByEmail(String email);

	boolean existsByEmail(String email);
}
