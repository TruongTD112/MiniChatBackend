package miniapp.com.vn.minichatbackend.repo;

import miniapp.com.vn.minichatbackend.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessRepository extends JpaRepository<Business, Long> {
}

