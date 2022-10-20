package demo;

import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.MessageEvent;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WikimediaChangeHandler implements EventHandler {

    private final Logger log = LoggerFactory.getLogger(WikimediaChangeHandler.class.getSimpleName());
    private final KafkaProducer<String, String> kafkaProducer;
    private final String topic;

    public WikimediaChangeHandler(KafkaProducer<String, String> kafkaProducer, String topic) {
        this.kafkaProducer = kafkaProducer;
        this.topic = topic;
    }

    @Override
    public void onOpen() {
        log.info("Event opening");
    }

    @Override
    public void onClosed() {
        log.info("Event closing");
        kafkaProducer.close();
    }

    @Override
    public void onMessage(String event, MessageEvent messageEvent) {
        log.info(messageEvent.getData());
        // asynchronous
        kafkaProducer.send(new ProducerRecord<>(topic, messageEvent.getData()));

    }

    @Override
    public void onComment(String comment) {
        log.info("Event comment");
    }

    @Override
    public void onError(Throwable t) {
        log.error("Error in Stream Reading", t);
    }
}
