package payment.module.repository;

import org.junit.Ignore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import payment.module.enums.PaymentStatus;
import payment.module.interfaces.DAO;
import payment.module.repository.entities.Payment;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryTest {
    private Repository repo = new Repository();

    @BeforeEach
    void setUp() {
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentTimestamp);
        cal.add(Calendar.HOUR, +1);
        Timestamp expireAt = new Timestamp(cal.getTimeInMillis());
        repo.savePayment("007", UUID.randomUUID(), "TRIAL", BigDecimal.valueOf(1), currentTimestamp, expireAt);
    }

    @AfterEach
    void tearDown() {
        int linesAffected = DAO.executeUDQuery("DELETE FROM Payment", Map.of());
        System.out.println("deleted " + linesAffected + " lines");
    }

    @Test
    void savePayment() {
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentTimestamp);
        cal.add(Calendar.HOUR, +1);
        Timestamp expireAt = new Timestamp(cal.getTimeInMillis());
        repo.savePayment("001", UUID.randomUUID(), "TRIAL", BigDecimal.valueOf(1), currentTimestamp, expireAt);
        Payment payment = DAO.executeQuery("SELECT p FROM Payment p WHERE p.txHash = :txHash", Map.of("txHash", "001"), Payment.class).getFirst();
        System.out.println(payment);
        assertNotNull(payment);
    }

    @Test
    void checkPaymentStatus() {
        PaymentStatus status = repo.checkPaymentStatus("007");
        assertEquals(PaymentStatus.PENDING, status);
    }

    @Test
    void updatePaymentStatus() {
        repo.updatePaymentStatus("007", PaymentStatus.PAYMENT_RECEIVED);
        Payment payment = DAO.executeQuery("SELECT p FROM Payment p WHERE p.txHash = :txHash", Map.of("txHash", "007"), Payment.class).getFirst();
        assertEquals(PaymentStatus.PAYMENT_RECEIVED, payment.getStatus());
    }
}