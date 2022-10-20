package demo.hochnt.object.serializer;

import demo.hochnt.ProducerDemo;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class DriverProducer {
    private static final Logger log = LoggerFactory.getLogger(ProducerDemo.class.getSimpleName());

    public static void main(String[] args) {

        // create Producer Properties
        Properties properties = new Properties();

        // "bootstrap.servers"
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // or "127.0.0.1:9092
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());  // "key.serializer"
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, DriverLocationSerializer.class.getName()); //"value.serializer"

        // create the Producer with value (no key)
        KafkaProducer<String, DriverLocation> producer = new KafkaProducer<String, DriverLocation>(properties);
        // create producer record (data/message)
        DriverLocation driverLocation = new DriverLocation();
        driverLocation.setDriverId(1);
        driverLocation.setLatitude(1000);
        driverLocation.setLongitude(2000);
        ProducerRecord<String, DriverLocation> record = new ProducerRecord<>("demo_java", driverLocation);

        log.info("start send record");
        // send data
        producer.send(record); // send async - doesn't wait until it's send -> go to the next line -> need to flush

        // flush and close the Producer
        producer.flush(); // synchronous - up until data in producer being sent
        log.info("end send record");

        producer.close();
    }
}

