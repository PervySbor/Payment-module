package payment.module.repository.entities;

import jakarta.persistence.*;
import payment.module.enums.PaymentStatus;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

import static jakarta.persistence.EnumType.STRING;

@Entity
@Table(name="payments")
public class Payment {

    @Id
    @Column(name="tx_hash")
    private String txHash;

    @Column(name="session_id")
    private UUID sessionId;

    @Column(name="amount_paid")
    private BigDecimal amtPaid;

    @Column(name="created_at")
    private Timestamp createdAt;

    @Column(name="expire_at")
    private Timestamp expireAt;

    @Column(name="status")
    @Enumerated(STRING)
    private PaymentStatus status;

    @Column(name="subscription_name")
    private String subscriptionName;

    public Payment(){}

    public Payment(String txHash, UUID sessionId, BigDecimal amtPaid, Timestamp createdAt, Timestamp expireAt, String subscriptionName){
        this.txHash = txHash;
        this.sessionId = sessionId;
        this.amtPaid = amtPaid;
        this.createdAt = createdAt;
        this.expireAt = expireAt;
        this.subscriptionName = subscriptionName;
        this.status = PaymentStatus.PENDING;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "txHash='" + txHash + '\'' +
                ", sessionId=" + sessionId +
                ", amtPaid=" + amtPaid +
                ", createdAt=" + createdAt +
                ", expireAt=" + expireAt +
                ", status=" + status +
                ", subscriptionName='" + subscriptionName + '\'' +
                '}';
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public BigDecimal getAmtPaid() {
        return amtPaid;
    }

    public void setAmtPaid(BigDecimal amtPaid) {
        this.amtPaid = amtPaid;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(Timestamp expireAt) {
        this.expireAt = expireAt;
    }

    public String getSubscriptionName() {
        return subscriptionName;
    }

    public void setSubscriptionName(String subscriptionName) {
        this.subscriptionName = subscriptionName;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
}
