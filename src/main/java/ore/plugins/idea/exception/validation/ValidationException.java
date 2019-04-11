package ore.plugins.idea.exception.validation;

import ore.plugins.idea.exception.OrePluginRuntimeException;

public class ValidationException extends OrePluginRuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
