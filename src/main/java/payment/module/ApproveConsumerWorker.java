package payment.module;

import com.sun.jdi.InternalException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import payment.module.enums.PaymentStatus;
import payment.module.exceptions.ParsingUserRequestException;
import payment.module.repository.Repository;
import payment.module.util.JsonManager;
import payment.module.util.LogManager;

import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

public class ApproveConsumerWorker implements Runnable{
    private final Repository repository;
    private final ConsumerRecord<String,String> record;
    private final ConcurrentMap<TopicPartition, OffsetAndMetadata> offsetsToCommit;

    public ApproveConsumerWorker(Repository repository, ConsumerRecord<String,String> record, ConcurrentMap<TopicPartition, OffsetAndMetadata> offsetsToCommit){
        this.repository = repository;
        this.record = record;
        this.offsetsToCommit = offsetsToCommit;
    }

    //receiving Map.of("txHash", txHash, "expireAt", expireAt, "scheduledAt", scheduledAt)
    @Override
    public void run() {
        try {
            List<String> data = JsonManager.unwrapPairs(List.of("code", "status", "message"), record.value());

            //Other fields are transmitted solely for debug purposes
            int code = Integer.parseInt(data.get(0));
            String status = data.get(1);
            String message = data.get(2);
            String txHash;
            //String txHash = data.get(3);

            switch(code){
                case 200:
                    txHash = JsonManager.unwrapPairs(List.of("tx_hash"), record.value()).getFirst();
                    this.repository.updatePaymentStatus(txHash, PaymentStatus.SUBSCRIPTION_CREATED);
                    break;
                case 500:
                    LogManager.logException(new InternalException(message), Level.WARNING);
                    break;
                default:
                    txHash = JsonManager.unwrapPairs(List.of("tx_hash"), record.value()).getFirst();
                    this.repository.updatePaymentStatus(txHash, PaymentStatus.FAILED);
                    break;
            }

            TopicPartition tp = new TopicPartition(record.topic(), record.partition());

            offsetsToCommit.put(tp, new OffsetAndMetadata(record.offset() + 1));

        } catch (ParsingUserRequestException e) {
            LogManager.logException(e, Level.SEVERE);
        }
    }
}
