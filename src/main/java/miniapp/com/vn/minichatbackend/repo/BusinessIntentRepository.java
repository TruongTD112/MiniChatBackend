package miniapp.com.vn.minichatbackend.repo;

import miniapp.com.vn.minichatbackend.entity.BusinessIntent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessIntentRepository extends JpaRepository<BusinessIntent, Long> {
}

