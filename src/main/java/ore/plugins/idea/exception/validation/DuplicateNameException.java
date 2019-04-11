package ore.plugins.idea.exception.validation;

public class DuplicateNameException extends ValidationException {

    private static final String MESSAGE_TEMPLATE = "Class name '%s' already exists";

    public DuplicateNameException(String selectedName) {
        super(String.format(MESSAGE_TEMPLATE, selectedName));
    }
}
