package demo.hochnt;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerDemo {

    private static final Logger log = LoggerFactory.getLogger(ProducerDemo.class.getSimpleName());

    public static void main(String[] args) {

        // kafka-topics --bootstrap-server localhost:9092 --topic demo_java --create --partitions 3 --replication-factor 1

        // create Producer Properties
        Properties properties = new Properties();

        // "bootstrap.servers"
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // or "127.0.0.1:9092
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());  // "key.serializer"
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName()); //"value.serializer"

        // create the Producer with value (no key)
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);
        // create producer record (data/message)
        ProducerRecord<String, String> record = new ProducerRecord<>("demo_java", "hello kafka");

        log.info("start send record");
        // send data
        producer.send(record); // send async - doesn't wait until it's send -> go to the next line -> need to flush

        // flush and close the Producer
        producer.flush(); // synchronous - up until data in producer being sent
        log.info("end send record");

        producer.close();
    }
}
