package payment.module;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import payment.module.repository.Repository;
import payment.module.util.LogManager;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class QueueMainConsumer implements Runnable {
    private KafkaConsumer<String, String> consumer;
    private final ExecutorService es;
    private final Repository repository;
    private final ConcurrentMap<TopicPartition, OffsetAndMetadata> offsetsToCommit = new ConcurrentHashMap<>();
    private final ConcurrentMap<TopicPartition, Long> positionToRollback = new ConcurrentHashMap<>();

    public QueueMainConsumer(int maxAmtOfThreads, String bootServers, String clientId, String consumerGroupName, List<String> topics, Repository repository){
        this.es = Executors.newFixedThreadPool(maxAmtOfThreads);
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", bootServers);
        props.setProperty("client.id", clientId);
        props.setProperty("group.id", consumerGroupName);
        props.setProperty("enable.auto.commit", "false");
        props.setProperty("key.deserializer", StringDeserializer.class.getCanonicalName());
        props.setProperty("value.deserializer", StringDeserializer.class.getCanonicalName());

        this.consumer = new KafkaConsumer<String,String>(props);
        this.consumer.subscribe(topics);

        this.repository = repository;
    }

    //1. stop MainConsumerRunner's thread
    //2. call mainConsumerRunner.destroy()
    public void destroy(){
        if(this.consumer != null){
            this.consumer.close();
            this.consumer = null;
        }
    }

    public void run() {
        do {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                Runnable consumerWorker = new QueueConsumerWorker(repository, record, offsetsToCommit, positionToRollback);
                this.es.execute(consumerWorker);
            }

            if(!positionToRollback.isEmpty()){
                for(TopicPartition tp : positionToRollback.keySet()) {
                    consumer.seek(tp, positionToRollback.get(tp));
                }
                positionToRollback.clear();
            }

            if (!offsetsToCommit.isEmpty()) {
                Map<TopicPartition, OffsetAndMetadata> toCommit =
                        new HashMap<>(offsetsToCommit);
                consumer.commitAsync(toCommit, (offsets, exception) -> {
                    if (exception != null) {
                        LogManager.logException(exception, Level.WARNING);
                    }
                });
                offsetsToCommit.clear();
            }
        } while (!Thread.currentThread().isInterrupted());
        es.close();
        consumer.close();
    }
}
