package payment.module.models;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import payment.module.enums.LogType;

@JsonPropertyOrder(value={"service_name", "log_type", "log_message"})
public class LogMessageWrapper {

    private final LogMessage logMessage;
    private final LogType logType;
    private final String serviceName = "IDENTITY_SERVICE";


    public LogMessageWrapper(LogMessage logMessage, LogType logType){
        this.logMessage = logMessage;
        this.logType = logType;
    }

    @JsonGetter(value = "log_message")
    public LogMessage getLogMessage() {
        return logMessage;
    }

    @JsonGetter(value = "log_type")
    public String getLogType() {
        return this.logType.name();
    }
    @JsonGetter(value = "service_name")
    public String getServiceName(){
        return this.serviceName;
    }
}