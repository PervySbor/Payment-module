package payment.module.repository;

import payment.module.enums.PaymentStatus;
import payment.module.exceptions.FatalException;
import payment.module.repository.DAOs.PaymentDao;
import payment.module.repository.entities.Payment;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

public class Repository {

    private final PaymentDao paymentDao = new PaymentDao();

    public void savePayment(String txHash, UUID sessionId,String subscriptionName, BigDecimal amtPaid, Timestamp createdAt, Timestamp expireAt){
        Payment payment = new Payment(txHash, sessionId,amtPaid, createdAt, expireAt, subscriptionName);
        paymentDao.save(payment);
    }

    public PaymentStatus checkPaymentStatus(String txHash){
        Payment payment = paymentDao.find(txHash);
        if(payment == null){
            throw new FatalException("searching for the status of non existing payment");
        }
        return payment.getStatus();
    }


    public void updatePaymentStatus(String txHash, PaymentStatus status){
        Payment payment = paymentDao.find(txHash);
        payment.setStatus(status);
        paymentDao.update(payment);
    }
}
