package miniapp.com.vn.minichatbackend.repo;

import miniapp.com.vn.minichatbackend.entity.CustomerUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerUserRepository extends JpaRepository<CustomerUser, Long> {
}

