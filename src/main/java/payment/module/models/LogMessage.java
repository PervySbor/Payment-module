package payment.module.models;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.logging.Level;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


@JsonPropertyOrder(value={"level", "timestamp", "sender", "message", "trace"})
public class LogMessage {
    private String sender;
    private String message;
    private Timestamp timestamp;
    private String level;

    private List<String> trace;

    public LogMessage(String message, String sender, Timestamp timestamp, Level level){
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
        this.level = level.toString();
        this.trace = new ArrayList<>();
    }

    public void addTraceElement(String element){
        trace.add(element);
    }

    @JsonGetter
    public String getMessage(){
        return this.message;
    }

    @JsonGetter
    public Timestamp getTimestamp(){
        return this.timestamp;
    }

    @JsonGetter
    public String getLevel(){
        return this.level;
    }

    @JsonGetter
    public List<String> getTrace(){
        return this.trace;
    }

    @JsonGetter
    public String getSender(){
        return this.sender;
    }
}
