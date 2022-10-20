package demo.hochnt;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

public class ConsumerDemoInGroup {

    private static final Logger log = LoggerFactory.getLogger(ConsumerDemoInGroup.class.getSimpleName());

    public static void main(String[] args) {

        String groupId = "my_third_application";
        String topic = "demo_java";

        // create Producer Properties
        Properties properties = new Properties();

        // "bootstrap.servers"
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // or "127.0.0.1:9092
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());  // "key.deserializer"
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName()); //"value.deserializer"
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // create the Consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

        // get a reference to the cur thread - shutdown hook is running in different thread
        final Thread mainThread = Thread.currentThread();
        // adding shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                log.info("Detected shutdown, exit by calling consumer.wakeup()...");
                consumer.wakeup(); // will throw exception when consumer try to poll

                // join main thread => allow execution of the code in the main thread
                try {
                    mainThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            log.info("start receive record");
            // subscribe consumer to our topic(s)
            consumer.subscribe(List.of(topic)); // can subscribes multiple topics

            // poll for new data
            while (true) {
                // log.info("Polling");
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000)); // poll per one second

                for (ConsumerRecord<String, String> record : records) {
                    log.info("Key: " + record.key() + ", Value: " + record.value());
                    log.info("Partition: " + record.partition() + ", Offset: " + record.offset());
                }

            }
        } catch (WakeupException ex) {
            log.info("Exception of wakeup"); // expected exception
        } catch (Exception exception) {
            log.error("Unexpected exception");
        } finally {
            consumer.close(); // close consumer + connection to kafka also
            log.info("Consumer closed");
        }
    }
}
