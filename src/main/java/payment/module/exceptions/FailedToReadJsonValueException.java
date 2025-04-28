package payment.module.exceptions;

public class FailedToReadJsonValueException extends Exception {
    public FailedToReadJsonValueException(String message) {
        super(message);
    }
}
