package payment.module.repository.utils;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.jpa.HibernatePersistenceProvider;

import java.util.HashMap;
import java.util.Map;

//made for sharing EntityManagerFactory with different threads
//using HikariCP under the hood

public class JpaUtils {
    private static final EntityManagerFactory emf;
    static {
        Map<String,String> props = new HashMap<>();
        //props.put("hibernate.show_sql", "true");
        emf =  new HibernatePersistenceProvider().createContainerEntityManagerFactory(new CustomPersistenceUnitInfo(), props);
    }

    public static EntityManagerFactory getEntityManagerFactory(){
        return emf;
    }
}
