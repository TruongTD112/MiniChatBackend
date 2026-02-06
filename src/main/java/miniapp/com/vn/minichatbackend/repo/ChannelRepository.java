package miniapp.com.vn.minichatbackend.repo;

import miniapp.com.vn.minichatbackend.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {
    /**
     * Tìm tất cả channels theo businessId và platform
     * @param businessId ID của Business
     * @param platform Platform (FACEBOOK, ZALO, etc.) - null để lấy tất cả
     * @return Danh sách channels
     */
    List<Channel> findByBusinessIdAndPlatform(Long businessId, String platform);
    List<Channel> findByBusinessId(Long businessId);

    /** Tìm channel theo business và page (dùng cho reconnect cùng Page) */
    Optional<Channel> findByBusinessIdAndChannelId(Long businessId, String channelId);
}

