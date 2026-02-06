package miniapp.com.vn.minichatbackend.repo;

import miniapp.com.vn.minichatbackend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByBusinessId(Long businessId);

    List<Product> findByBusinessIdIn(List<Long> businessIds);
}

