package payment.module;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import payment.module.enums.PaymentStatus;
import payment.module.exceptions.ParsingUserRequestException;
import payment.module.repository.Repository;
import payment.module.util.ConfigReader;
import payment.module.util.JsonManager;
import payment.module.util.KafkaProducerManager;
import payment.module.util.LogManager;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

public class QueueConsumerWorker implements Runnable{
    private final Repository repository;
    private final ConsumerRecord<String,String> record;
    private final ConcurrentMap<TopicPartition, Long> offsetsToCommit;
    private final ConcurrentMap<TopicPartition, Long> positionToRollback;
    private final String createSubTopicName;
    private final int createSubPartition;
    private final String createSubKey;

    public QueueConsumerWorker(Repository repository, ConsumerRecord<String,String> record, ConcurrentMap<TopicPartition,
            Long> offsetsToCommit, ConcurrentMap<TopicPartition, Long> positionToRollback){
        this.repository = repository;
        this.record = record;
        this.offsetsToCommit = offsetsToCommit;
        this.positionToRollback = positionToRollback;
        this.createSubTopicName = ConfigReader.getStringValue("CREATE_SUB_TOPIC");
        this.createSubPartition = Integer.parseInt(ConfigReader.getStringValue("CREATE_SUB_PARTITION"));
        this.createSubKey = ConfigReader.getStringValue("CREATE_SUB_KEY");
    }

    //receiving Map.of("txHash", txHash, "expireAt", expireAt, "scheduledAt", scheduledAt)
    @Override
    public void run() {
        TopicPartition tp = new TopicPartition(record.topic(), record.partition());
        try {
            List<String> data = JsonManager.unwrapPairs(List.of("tx_hash", "expire_at", "scheduled_at", "session_id", "subscription_name"), record.value());

            String txHash = data.get(0);
            Timestamp expireAt = new Timestamp(Long.parseLong(data.get(1)));
            Timestamp scheduledAt = new Timestamp(Long.parseLong(data.get(2)));
            Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
            String sessionId = data.get(3);//no need to convert to UUID here
            String subscriptionName = data.get(4);
            String request = "";

            if(scheduledAt.compareTo(currentTimestamp) > 0){ //not enough time passed to process it again
                //rolling back to the position before the writing we've just skipped
                //assigning position to rollback to only if other threads haven't found an earlier position to rollback to
                positionToRollback.compute(tp, (topic, currentOffset) ->
                     (currentOffset == null || currentOffset > record.offset())? record.offset(): currentOffset);
                return;
            } else if (expireAt.compareTo(currentTimestamp) > 0){ //not expired yet
                /* omitted calls to BlockchainManager
                *  BlockchainManager.checkConfirmations(...)
                * if(not enough)
                * { push back to Kafka}
                *  check visio doc for more details
                * */
                this.repository.updatePaymentStatus(txHash, PaymentStatus.PAYMENT_RECEIVED); //payment_received

                //writing message to subscriptions_topic for identity module to create the subscription
                try {
                    request = JsonManager.serialize(Map.of("session_id", sessionId, "subscription_type", subscriptionName, "tx_hash", txHash));
                } catch (JsonProcessingException e) {
                    System.out.println("it's impossible to get exception here with the specified parameters");
                }
                KafkaProducerManager.send(createSubTopicName, createSubPartition, createSubKey, request);

            } else { //expired & payment still not received
                this.repository.updatePaymentStatus(txHash, PaymentStatus.FAILED);
            }

            //assigning offset only if other threads haven't processed earlier writings
            offsetsToCommit.compute(tp, (topic, currentOffset) ->
                    (currentOffset == null || currentOffset < record.offset() + 1)? record.offset()+1: currentOffset);

        } catch (ParsingUserRequestException e) {
            LogManager.logException(e, Level.SEVERE);
        }
    }
}
