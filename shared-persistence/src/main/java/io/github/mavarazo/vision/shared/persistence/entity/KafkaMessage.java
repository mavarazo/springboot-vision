package io.github.mavarazo.vision.shared.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue(value = MessageType.Values.KAFKA)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
public class KafkaMessage extends Message {

    @Column
    private String destination;

    @Column(name = "message_key")
    private String key;

    @Column(name = "message_value", columnDefinition = "BLOB")
    @Lob
    private byte[] payload;

    @Column(name = "message_name")
    private String name;

    @Column
    private String message;
}
