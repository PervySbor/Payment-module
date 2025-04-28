package payment.module.repository.DAOs;

import jakarta.persistence.EntityManager;
import payment.module.interfaces.DAO;
import payment.module.repository.entities.Payment;
import payment.module.repository.utils.JpaUtils;

public class PaymentDao implements DAO<Payment> {

    @Override
    public void save(Payment obj) {
        EntityManager em = JpaUtils.getEntityManagerFactory().createEntityManager();
        em.getTransaction().begin();
        em.persist(obj);
        em.getTransaction().commit();
        em.close();
    }

    @Override
    public void delete(Payment obj) {
        throw new UnsupportedOperationException("deletion is not supported");
    }

    @Override
    public void update(Payment obj) {
        EntityManager em = JpaUtils.getEntityManagerFactory().createEntityManager();
        em.getTransaction().begin();
        em.merge(obj);
        em.getTransaction().commit();
        em.close();
    }

    @Override
    public Payment find(Object primaryKey) {
        EntityManager em = JpaUtils.getEntityManagerFactory().createEntityManager();
        Payment payment = em.find(Payment.class, primaryKey); //in this case primary key - txHash
        em.close();
        return payment;
    }
}
