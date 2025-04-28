package payment.module.util;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.logging.Logger;

//encapsulates interactions with KafkaProducer
public class KafkaProducerManager {
    private static final Logger logger = Logger.getLogger("myLogger");
    private static KafkaProducer<String, String> producer;

    //WARNING doesn't need to be synchronized, as it's called only once during Context initialization
    public static void init(String bootServersString, String clientId){
        if(producer == null){
            Properties props = new Properties();
            props.setProperty("bootstrap.servers", bootServersString);
            props.setProperty("client.id", clientId);
            props.setProperty("enable.idempotence", "true");
            props.setProperty("key.serializer", StringSerializer.class.getCanonicalName());
            props.setProperty("value.serializer", StringSerializer.class.getCanonicalName());
            producer = new KafkaProducer<String, String>(props);
        }
    }

    //WARNING doesn't need to be synchronized, as it's called only once during Context destruction
    public static void destroy(){
        if(producer != null){
            producer.close();
            producer = null;
        }
    }


    public static void send(String topic, Integer partition, String key, String value) {
        if (producer != null) {
            producer.send(new ProducerRecord<>(topic, partition, key, value));
        }
        else{
            logger.severe("Haven't created KafkaProducer yet");
            throw new IllegalStateException("KafkaProducer not initialized yet");
        }
    }
}
