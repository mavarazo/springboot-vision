package io.github.mavarazo.vision.shared.persistence.entity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MessageType {
    KAFKA(Values.KAFKA);

    private final String value;

    public static class Values {
        public static final String KAFKA = "KAFKA";
    }
}
