package payment.module.util;


import payment.module.enums.LogType;
import payment.module.models.LogMessage;
import payment.module.models.LogMessageWrapper;

import java.sql.Timestamp;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogManager {
    private static final Logger logger = Logger.getLogger("myLogger");
    private static final String logTopicName;
    private static final String key;
    private static final int partitionNumber;
    private static final String containerName;

    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);
        logTopicName = ConfigReader.getStringValue("LOG_TOPIC");
        key = ConfigReader.getStringValue("LOG_KEY");
        partitionNumber = Integer.parseInt(ConfigReader.getStringValue("LOG_PARTITION_NUM"));
        containerName = ConfigReader.getStringValue("CONTAINER_NAME");
    }

    public static String logException(Exception exception, Level level){
        String jsonLogMessage = "";
        StackTraceElement[] elements = exception.getStackTrace();

        logger.log(Level.INFO, "Currently logging exception: ", exception);

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        LogMessage logMessage = new LogMessage(exception.getMessage(), containerName, timestamp, level);

        for(StackTraceElement element : elements){
            String message = element.toString();
            logMessage.addTraceElement(message);
        }
        LogMessageWrapper wrappedLogMessage = new LogMessageWrapper(logMessage, LogType.ERROR);

        try {
            jsonLogMessage = JsonManager.serialize(wrappedLogMessage);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e){
            logger.log(Level.SEVERE, "Failed to serialize LogMessage: ", e);
        }

        KafkaProducerManager.send(logTopicName, partitionNumber, key, jsonLogMessage);
        return jsonLogMessage;
    }

}
