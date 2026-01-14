package miniapp.com.vn.minichatbackend.repo;


import miniapp.com.vn.minichatbackend.entity.CustomerUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface CustomerUserRepository extends JpaRepository<CustomerUser, Integer> {

    /**
     * Tìm user theo email
     */
    Optional<CustomerUser> findByEmail(String email);

    /**
     * Tìm user theo provider và providerId
     */
    Optional<CustomerUser> findByProviderAndProviderId(String provider, String providerId);

    /**
     * Kiểm tra email đã tồn tại chưa (trừ user hiện tại)
     */
    boolean existsByEmailAndIdNot(String email, Integer id);

    Optional<CustomerUser> findByProviderId(String providerId);
}