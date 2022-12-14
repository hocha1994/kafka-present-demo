package demo.hochnt;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerDemoKeys {

    private static final Logger log = LoggerFactory.getLogger(ProducerDemoKeys.class.getSimpleName());

    public static void main(String[] args) {

        // create Producer Properties
        Properties properties = new Properties();

        // "bootstrap.servers"
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // or "127.0.0.1:9092
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());  // "key.serializer"
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName()); //"value.serializer"

        // create the Producer with value
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);


        log.info("start send record");
        for (int i = 1; i <= 10; i++) {

            String topic = "demo_java";
            String value = "hello kafka " + i;
            String key = "kafka_key_" + i;

            // create producer record (data/message) with key
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);

            // send data - with callbacks
            producer.send(record, new Callback() {// send async
                @Override
                public void onCompletion(RecordMetadata metadata, Exception ex) {
                    // executes every time a record is successfully sent or an exception is thrown
                    log.info("\n\n===========START SHOW METADATA WITH KEY =========");
                    if (ex == null) {
                        // the record was successfully sent
                        log.info("Received new metadata. \n" +
                                "Topic: " + metadata.topic() + "\n" +
                                "Key: " + record.key() + "\n" + // show message key
                                "Partition: " + metadata.partition() + "\n" +
                                "Offset: " + metadata.offset() + "\n" +
                                "Timestamp: " + metadata.timestamp());
                    } else {
                        log.error("Error while producing", ex);
                    }
                    log.info("===========END SHOW METADATA WITH KEY =========\n");
                }
            });

            //            DefaultPartitioner;
            try {
                Thread.sleep(1000); // 1s => next send will higher percent into other partition
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }



        // flush and close the Producer
        producer.flush(); // synchronous - up until data in producer being sent
        log.info("end send record");

        producer.close();
    }
}
