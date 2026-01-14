package miniapp.com.vn.minichatbackend.repo;


import miniapp.com.vn.minichatbackend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {

}
