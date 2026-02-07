package miniapp.com.vn.minichatbackend.repo;

import miniapp.com.vn.minichatbackend.document.MessageDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MessageRepository extends MongoRepository<MessageDocument, String> {

    Optional<MessageDocument> findByExternalMessageIdAndPlatform(String externalMessageId, String platform);

    Page<MessageDocument> findByConversationIdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);
}
