package com.flashsalex.user.repository;

import java.util.List;
import java.util.Optional;
import com.flashsalex.user.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {

	List<UserAddress> findAllByUser_IdOrderByIdAsc(Long userId);

	Optional<UserAddress> findByIdAndUser_Id(Long id, Long userId);
}
