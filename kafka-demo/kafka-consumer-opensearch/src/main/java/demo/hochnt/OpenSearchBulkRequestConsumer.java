package demo.hochnt;

import com.google.gson.JsonParser;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.bulk.BulkResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.indices.CreateIndexRequest;
import org.opensearch.client.indices.GetIndexRequest;
import org.opensearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class OpenSearchBulkRequestConsumer {

    public static void main(String[] args) throws IOException, InterruptedException {
        Logger log = LoggerFactory.getLogger(OpenSearchBulkRequestConsumer.class.getSimpleName());

        // crate open search client
        RestHighLevelClient openSearchClient = createOpenSearchClient();

        // create kafka client
        String boostrapServers = "127.0.0.1:9092";
        String groupId = "consumer-opensearch-demo";

        // create consumer configs
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, boostrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest"); // only read the latest data by default
        properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(properties);

        // create the index on OpenSearch if it doesn't exist already
        try (openSearchClient; kafkaConsumer) {
            boolean indexExists = openSearchClient.indices()
                    .exists(new GetIndexRequest("wikimedia"), RequestOptions.DEFAULT);
            if (!indexExists) {
                CreateIndexRequest createIndexRequest = new CreateIndexRequest("wikimedia");
                openSearchClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
                log.info("Index not exist, created");
            } else {
                log.info("Index exist");
            }

            kafkaConsumer.subscribe(Collections.singleton("wikimedia.recentchange"));
            // consuming data
            while (true) {
                ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(5000));
                int recordCount = records.count();
                log.info("Received: " + recordCount + " record(s)");

                // send record to elastic search - openSearch
                BulkRequest bulkRequest = new BulkRequest();

                for (var record : records) {
                    try {
                        // Extract id from metadata
                        String id2 = extractId(record.value());
                        IndexRequest indexRequest = new IndexRequest("wikimedia")
                                .source(record.value(), XContentType.JSON)
                                .id(id2); //send json data to openSearch with id
                        log.info("add meta id to bulk request: {}", id2);
                        //IndexResponse response = openSearchClient.index(indexRequest, RequestOptions.DEFAULT);
                        // add index request to bulk instead of send each indexRq to open search
                        bulkRequest.add(indexRequest);
                    } catch (Exception e) {
                        // ignore exception related to data during send consumer record to openSearch
                    }
                }
                // check and send bulk items to openSearch
                if (bulkRequest.numberOfActions() > 0) {
                    BulkResponse bulkResponse = openSearchClient.bulk(bulkRequest, RequestOptions.DEFAULT);
                    log.info("Inserted: " + bulkResponse.getItems().length + " bulk records");
                    Thread.sleep(1000);
                }

                // auto-commit-false: commit offset after the batch is consumed
                // at-least-once cuz only after we've successfully processed the entire batch
                kafkaConsumer.commitSync();
                log.info("offsets committed");
            }
        }

    }

    private static String extractId(String recordJson) {
        return JsonParser.parseString(recordJson)
                .getAsJsonObject()
                .get("meta")
                .getAsJsonObject()
                .get("id")
                .getAsString();
    }

    public static RestHighLevelClient createOpenSearchClient() {
        String connString = "http://localhost:9200";

        // we build a URI from the connection string
        RestHighLevelClient restHighLevelClient;
        URI connUri = URI.create(connString);
        // extract login information if it exists
        String userInfo = connUri.getUserInfo();

        if (userInfo == null) {
            // REST client without security
            restHighLevelClient = new RestHighLevelClient(RestClient.builder(new HttpHost(connUri.getHost(), connUri.getPort(), "http")));
        } else {
            // REST client with security
            String[] auth = userInfo.split(":");

            CredentialsProvider cp = new BasicCredentialsProvider();
            cp.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(auth[0], auth[1]));

            restHighLevelClient = new RestHighLevelClient(
                    RestClient.builder(new HttpHost(connUri.getHost(), connUri.getPort(), connUri.getScheme()))
                            .setHttpClientConfigCallback(
                                    httpAsyncClientBuilder -> httpAsyncClientBuilder.setDefaultCredentialsProvider(cp)
                                            .setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())));
        }

        return restHighLevelClient;
    }
}
