package net.proselyte.personservice.exception;

public class PersonException extends RuntimeException {

    public PersonException(String message) {
        super(message);
    }

    public PersonException(String message, Object... args) {
        super(String.format(message, args));
    }
}
