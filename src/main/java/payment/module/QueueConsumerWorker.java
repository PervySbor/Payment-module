package payment.module;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import payment.module.enums.PaymentStatus;
import payment.module.exceptions.ParsingUserRequestException;
import payment.module.repository.Repository;
import payment.module.util.JsonManager;
import payment.module.util.LogManager;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

public class QueueConsumerWorker implements Runnable{
    private final Repository repository;
    private final ConsumerRecord<String,String> record;
    private final ConcurrentMap<TopicPartition, OffsetAndMetadata> offsetsToCommit;

    public QueueConsumerWorker(Repository repository, ConsumerRecord<String,String> record, ConcurrentMap<TopicPartition, OffsetAndMetadata> offsetsToCommit){
        this.repository = repository;
        this.record = record;
        this.offsetsToCommit = offsetsToCommit;
    }

    //receiving Map.of("txHash", txHash, "expireAt", expireAt, "scheduledAt", scheduledAt)
    @Override
    public void run() {
        try {
            List<String> data = JsonManager.unwrapPairs(List.of("txHash", "expireAt", "scheduledAt"), record.value());

            String txHash = data.get(0);
            Timestamp expireAt = new Timestamp(Long.parseLong(data.get(1)));
            Timestamp scheduledAt = new Timestamp(Long.parseLong(data.get(2)));
            Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());

            if(scheduledAt.compareTo(currentTimestamp) > 0){ //not enough time passed to process it again
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
            } else { //expired & payment still not received
                this.repository.updatePaymentStatus(txHash, PaymentStatus.FAILED);
            }

            TopicPartition tp = new TopicPartition(record.topic(), record.partition());

            offsetsToCommit.put(tp, new OffsetAndMetadata(record.offset() + 1));

        } catch (ParsingUserRequestException e) {
            LogManager.logException(e, Level.SEVERE);
        }
    }
}
