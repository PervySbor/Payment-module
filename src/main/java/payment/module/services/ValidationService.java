package payment.module.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import payment.module.exceptions.ParsingUserRequestException;
import payment.module.repository.Repository;
import payment.module.util.JsonManager;
import payment.module.util.KafkaProducerManager;
import payment.module.util.LogManager;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.logging.Level;

public class ValidationService {
    private final Repository repository;
    private final int confirmWaitingPeriod;
    private final String queueTopicName;
    private final String queueKey;
    private final int queuePartition;
    private final int checkDelay; //minutes before another check

    public ValidationService(Repository repository,int confirmWaitingPeriod, String queueTopicName, String queueKey, int queuePartition, int checkDelay){
        this.repository = repository;
        this.confirmWaitingPeriod = confirmWaitingPeriod;
        this.queueTopicName = queueTopicName;
        this.queueKey = queueKey;
        this.queuePartition = queuePartition;
        this.checkDelay = checkDelay;
    }

    public Map<String,String> validate(String json){//expected json {session_id : <session_id>, subscription_name : <subscription_name>, tx_hash : <tx_hash>}

        Map<String,String> result = new HashMap<>();

        try {
            List<String> values = JsonManager.unwrapPairs(List.of("session_id", "subscription_name", "tx_hash"), json);
            UUID sessionId = UUID.fromString(values.get(0));
            String subscriptionName = values.get(1);
            String txHash = values.get(2);
            /* omitted calls to the BlockchainManager
            * BlockchainManager.getMagValue(txHash);
            * BigDecimal priceDifference = BigDecimal.parse...(ConfigReader.getStringValue("PRICE_DIFFERENCE"));
            * ...{
            * BlockchainManager.refund(messageValue)
            * }
            * see visio doc for more details
            * */
            Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
            Calendar cal = Calendar.getInstance();
            cal.setTime(currentTimestamp);
            cal.add(Calendar.MINUTE, this.confirmWaitingPeriod);
            Timestamp expireAt = new Timestamp(cal.getTimeInMillis());

            repository.savePayment(txHash, sessionId, subscriptionName, BigDecimal.valueOf(0), currentTimestamp, expireAt);

            cal.setTime(currentTimestamp);
            cal.add(Calendar.SECOND, this.checkDelay);
            Timestamp scheduledAt = new Timestamp(cal.getTimeInMillis());

            String resultJson = JsonManager.serialize(Map.of("txHash", txHash, "expireAt", expireAt, "scheduledAt", scheduledAt));

            KafkaProducerManager.send(queueTopicName, queuePartition, queueKey, resultJson);

            String jsonResponse = JsonManager.serialize(Map.of("message", "successfully accepted payment"));
            result.put("json", jsonResponse);
            result.put("statusCode", "202");

        } catch (ParsingUserRequestException | JsonProcessingException e) {
            LogManager.logException(e, Level.WARNING);
        }
        return result;
    }
}
