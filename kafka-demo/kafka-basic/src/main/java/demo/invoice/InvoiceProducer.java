package demo.invoice;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.Random;
import java.util.stream.IntStream;

public class InvoiceProducer {

    public static void main(String[] args) {

        final var random = new Random();
        final var props = new Properties();
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "producer");
//        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092,localhost:9093");
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        try (var producer = new KafkaProducer<String, Invoice>(props)) {
            IntStream.range(0, 100)
                    .parallel()
                    .forEach(i -> {
                        final var invoice = new Invoice()
                                .setInvoiceNumber(String.format("%05d", i))
                                .setStoreId(i % 5 + "")
                                .setCreated(System.currentTimeMillis())
                                .setValid(random.nextBoolean());
                        producer.send(new ProducerRecord<>("invoice-topic", invoice));
                    });
        }
    }

    // kafka-topics --bootstrap-server localhost:9092 --partitions 2 --topic invoice-topic --create

}
