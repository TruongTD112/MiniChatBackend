package miniapp.com.vn.minichatbackend.repo;

import miniapp.com.vn.minichatbackend.entity.BackOfficeBusiness;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BackOfficeBusinessRepository extends JpaRepository<BackOfficeBusiness, Long> {

    List<BackOfficeBusiness> findByBackOfficeUserId(Long backOfficeUserId);

    Optional<BackOfficeBusiness> findByBackOfficeUserIdAndBusinessId(Long backOfficeUserId, Long businessId);

    List<BackOfficeBusiness> findByBusinessId(Long businessId);
}

