package miniapp.com.vn.minichatbackend.repo;

import miniapp.com.vn.minichatbackend.entity.BackOfficeBusiness;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BackOfficeBusinessRepository extends JpaRepository<BackOfficeBusiness, Long> {
}

