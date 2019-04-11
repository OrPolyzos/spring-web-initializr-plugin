package ore.plugins.idea.exception.validation;

public class InvalidNameException extends ValidationException {

    private static final String MESSAGE_TEMPLATE = "Class name '%s' is invalid.";

    public InvalidNameException(String selectedName) {
        super(String.format(MESSAGE_TEMPLATE, selectedName));
    }
}
