package miniapp.com.vn.minichatbackend.repo;

import miniapp.com.vn.minichatbackend.entity.BackOfficeUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BackOfficeUserRepository extends JpaRepository<BackOfficeUser, Long> {

    Optional<BackOfficeUser> findByProviderId(String providerId);
}

