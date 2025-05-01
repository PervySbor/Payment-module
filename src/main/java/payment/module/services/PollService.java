package payment.module.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import payment.module.enums.PaymentStatus;
import payment.module.exceptions.ParsingUserRequestException;
import payment.module.exceptions.TransactionNotFoundException;
import payment.module.repository.Repository;
import payment.module.util.JsonManager;
import payment.module.util.LogManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class PollService {
    private final Repository repository;

    public PollService(Repository repository){
        this.repository = repository;
    }

    public Map<String,String> checkPayment(String txHash){//expected json {txHash : <tx_hash>}
        Map<String, String> result = new HashMap<>();
        try {

            PaymentStatus status = repository.checkPaymentStatus(txHash);
            String jsonResponse = "";
            int statusCode = 500;
            switch (status) {
                case PaymentStatus.SUBSCRIPTION_CREATED:
                    jsonResponse = JsonManager.serialize(Map.of("message", "Payment completed"));
                    statusCode = 200;
                    break;
                case PaymentStatus.FAILED:
                    jsonResponse = JsonManager.serialize(Map.of("message", "Payment failed"));
                    statusCode = 408;
                    break;
                case PaymentStatus.PENDING: case PaymentStatus.PAYMENT_RECEIVED:
                    jsonResponse = JsonManager.serialize(Map.of("message", "Pending"));
                    statusCode = 202;
                    break;
            }
            result.put("json", jsonResponse);
            result.put("statusCode", String.valueOf(statusCode));


        } catch(TransactionNotFoundException ex){
            result.put("error", "404");
            result.put("message", "transaction not found");
        } catch (JsonProcessingException e) {
            LogManager.logException(e, Level.WARNING);
            result.put("error", "422");
            result.put("message", "failed to form the answer");
        }
        return result;
    }
}
