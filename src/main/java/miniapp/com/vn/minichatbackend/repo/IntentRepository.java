package miniapp.com.vn.minichatbackend.repo;


import miniapp.com.vn.minichatbackend.entity.Intent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IntentRepository extends JpaRepository<Intent, Integer> {
}
