package io.github.mavarazo.vision.shared.kafka;

import io.github.mavarazo.vision.shared.kafka.interceptor.ConsumerLoggingInterceptor;
import io.github.mavarazo.vision.shared.kafka.interceptor.ProducerLoggingInterceptor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.kafka.autoconfigure.DefaultKafkaProducerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.Map;

@AutoConfiguration
@EnableKafka
public class SharedKafkaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ConsumerLoggingInterceptor consumerLoggingInterceptor() {
        return new ConsumerLoggingInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean(name = "kafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerContainerFactory(
            final ConsumerFactory<Object, Object> consumerFactory,
            final ConsumerLoggingInterceptor interceptor) {

        final ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);

        factory.setRecordInterceptor(interceptor);

        final ContainerProperties containerProps = factory.getContainerProperties();
        containerProps.setObservationEnabled(true);

        return factory;
    }

    @Bean
    public DefaultKafkaProducerFactoryCustomizer producerLoggingCustomizer() {
        return producerFactory -> {
            producerFactory.updateConfigs(Map.of(
                    ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, ProducerLoggingInterceptor.class.getName()
            ));
        };
    }

    @Bean
    public static BeanPostProcessor kafkaTemplateObservationPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
                if (bean instanceof final KafkaTemplate<?, ?> template) {
                    template.setObservationEnabled(true);
                }
                return bean;
            }
        };
    }
}
