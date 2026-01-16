package miniapp.com.vn.minichatbackend.repo;

import miniapp.com.vn.minichatbackend.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
}

