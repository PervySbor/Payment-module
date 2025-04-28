package payment.module.interfaces;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import payment.module.repository.utils.JpaUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface DAO<T> {

    void save(T obj);

    void delete(T obj);

    void update(T obj);

    T find(Object primaryKey);

    static <X> List<X> executeQuery(String jsql, Map<String, Object> args, Class<X> Xclass){
        EntityManager em = JpaUtils.getEntityManagerFactory().createEntityManager();
        em.getTransaction().begin();
        TypedQuery<X> query= em.createQuery(jsql, Xclass);
        Set<Map.Entry<String, Object>> pairs = args.entrySet();
        for(Map.Entry<String, Object> pair : pairs){
            query.setParameter(pair.getKey(), pair.getValue());
        }
        List<X> result = query.getResultList();
        em.getTransaction().commit();
        em.close();
        return result;
    }

    static int executeUDQuery(String jsql, Map<String, Object> args){
        EntityManager em = JpaUtils.getEntityManagerFactory().createEntityManager();
        em.getTransaction().begin();
        Query query= em.createQuery(jsql);
        Set<Map.Entry<String, Object>> pairs = args.entrySet();
        for(Map.Entry<String, Object> pair : pairs){
            query.setParameter(pair.getKey(), pair.getValue());
        }
        int linesAffected = query.executeUpdate();
        em.getTransaction().commit();
        em.close();
        return linesAffected;
    }
}