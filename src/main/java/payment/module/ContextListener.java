package payment.module;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import payment.module.repository.Repository;
import payment.module.repository.utils.MyHikariDataSource;
import payment.module.util.ConfigReader;
import payment.module.util.KafkaProducerManager;
import payment.module.validation.ValidationService;

import java.util.List;

public class ContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent ev){
        Repository repo = new Repository();
        //ev.getServletContext().setAttribute("repository", repo);
        int confirmWaitingPeriod =Integer.parseInt( ConfigReader.getStringValue("CONFIRM_WAITING_PERIOD_MINUTES"));
        String queueTopicName = ConfigReader.getStringValue("QUEUE_TOPIC");
        String queueKey = ConfigReader.getStringValue("QUEUE_KEY");
        int queuePartitionNum = Integer.parseInt(ConfigReader.getStringValue("QUEUE_PARTITION_NUM"));
        int checkDelay =Integer.parseInt( ConfigReader.getStringValue("CHECK_DELAY_SECONDS"));
        ValidationService vs = new ValidationService(repo, confirmWaitingPeriod, queueTopicName, queueKey, queuePartitionNum, checkDelay);
        ev.getServletContext().setAttribute("validationService", vs);

        List<String> bootServers = ConfigReader.getListValue("KAFKA_BROKERS");
        String bootServersString = String.join(",", bootServers);
        String clientId = ConfigReader.getStringValue("CONTAINER_NAME");

        KafkaProducerManager.init(bootServersString, clientId + "-producer");
    }

    @Override
    public void contextDestroyed(ServletContextEvent ev){
        KafkaProducerManager.destroy();
        MyHikariDataSource.destroy();
    }
}
