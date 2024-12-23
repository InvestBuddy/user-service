package tech.investbuddy.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String topic, String message) {
        try {
            kafkaTemplate.send(topic, message).get(); // Assurez la livraison avec `.get()`
            log.info("Message envoyé au topic : {} -> {}", topic, message);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi du message Kafka au topic : {}", topic, e);
        }
    }

    //public

}
