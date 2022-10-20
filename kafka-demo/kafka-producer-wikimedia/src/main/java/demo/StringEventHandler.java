package demo;

import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.MessageEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class StringEventHandler implements EventHandler {

    private final Logger log = LoggerFactory.getLogger(StringEventHandler.class.getSimpleName());
    private static int count = 0;

    @Override
    public void onOpen() throws Exception {
        log.info("Event opening");
    }

    @Override
    public void onClosed() throws Exception {
        log.info("Event closing");
    }

    @Override
    public void onMessage(String event, MessageEvent messageEvent) throws Exception {
        System.out.println("Current thread - " + Thread.currentThread().getId());
        TimeUnit.SECONDS.sleep(1);
        log.info(messageEvent.getData());
        System.out.println("==================================");
        // asynchronous
//        kafkaProducer.send(new ProducerRecord<>(topic, messageEvent.getData()));

    }

    @Override
    public void onComment(String comment) throws Exception {
        log.info("Event comment");
    }

    @Override
    public void onError(Throwable t) {
        log.error("Error in Stream Reading", t);
    }
}
