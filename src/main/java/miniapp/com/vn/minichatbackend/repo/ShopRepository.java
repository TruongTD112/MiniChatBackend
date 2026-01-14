package miniapp.com.vn.minichatbackend.repo;

import miniapp.com.vn.minichatbackend.entity.Owner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopRepository extends JpaRepository<Owner, Integer> {
} 