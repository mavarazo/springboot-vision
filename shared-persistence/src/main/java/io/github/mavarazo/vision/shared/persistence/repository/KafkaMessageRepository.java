package io.github.mavarazo.vision.shared.persistence.repository;

import io.github.mavarazo.vision.shared.persistence.entity.KafkaMessage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface KafkaMessageRepository extends CrudRepository<KafkaMessage, UUID> {

    List<KafkaMessage> findTop50ByCreatedAtBeforeOrderByCreatedAtAsc(LocalDateTime dateTime);
}
