package miniapp.com.vn.minichatbackend.repo;


import miniapp.com.vn.minichatbackend.entity.BackOfficeUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BackOfficeUserRepository extends JpaRepository<BackOfficeUser, Integer> {
	Optional<BackOfficeUser> findByEmail(String email);
	boolean existsByEmail(String email);
	boolean existsByEmailAndIdNot(String email, Integer id);
} 