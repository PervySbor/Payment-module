package payment.module;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import payment.module.repository.Repository;
import payment.module.repository.utils.MyHikariDataSource;
import payment.module.util.ConfigReader;
import payment.module.util.KafkaProducerManager;
import payment.module.services.ValidationService;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent ev){
        ServletContext ctx = ev.getServletContext();

        Repository repo = new Repository();

        int confirmWaitingPeriod =Integer.parseInt( ConfigReader.getStringValue("CONFIRM_WAITING_PERIOD_MINUTES"));
        String queueTopicName = ConfigReader.getStringValue("QUEUE_TOPIC");
        String queueKey = ConfigReader.getStringValue("QUEUE_KEY");
        int queuePartitionNum = Integer.parseInt(ConfigReader.getStringValue("QUEUE_PARTITION_NUM"));
        int checkDelay =Integer.parseInt( ConfigReader.getStringValue("CHECK_DELAY_SECONDS"));
        ValidationService vs = new ValidationService(repo, confirmWaitingPeriod, queueTopicName, queueKey, queuePartitionNum, checkDelay);

        ctx.setAttribute("validationService", vs);

        List<String> bootServers = ConfigReader.getListValue("KAFKA_BROKERS");
        String bootServersString = String.join(",", bootServers);
        String clientId = ConfigReader.getStringValue("CONTAINER_NAME");

        KafkaProducerManager.init(bootServersString, clientId + "-producer");

        int maxAmtOfThreads = Integer.parseInt(ConfigReader.getStringValue("QUEUE_CONSUMER_THREADS_AMT"));
        String consumerGroupName = ConfigReader.getStringValue("QUEUE_CONSUMER_GROUP_NAME");

        MainConsumerRunner mainConsumerRunner = new MainConsumerRunner(maxAmtOfThreads, bootServersString,
                consumerGroupName, clientId + "-consumer", List.of(queueTopicName), repo);
        ctx.setAttribute("mainConsumerRunner", mainConsumerRunner);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        ctx.setAttribute("executorService", executorService);
        executorService.execute(mainConsumerRunner);
    }

    @Override
    public void contextDestroyed(ServletContextEvent ev){
        ServletContext ctx = ev.getServletContext();
        ExecutorService executorService = (ExecutorService) ctx.getAttribute("executorService");
        executorService.close();

        MainConsumerRunner mainConsumerRunner = (MainConsumerRunner) ctx.getAttribute("mainConsumerRunner");
        mainConsumerRunner.destroy();

        KafkaProducerManager.destroy();
        MyHikariDataSource.destroy();
    }
}
