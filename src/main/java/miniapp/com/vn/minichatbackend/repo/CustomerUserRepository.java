package miniapp.com.vn.minichatbackend.repo;

import miniapp.com.vn.minichatbackend.entity.CustomerUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerUserRepository extends JpaRepository<CustomerUser, Long> {

    Optional<CustomerUser> findByProviderAndProviderId(String provider, String providerId);
}

