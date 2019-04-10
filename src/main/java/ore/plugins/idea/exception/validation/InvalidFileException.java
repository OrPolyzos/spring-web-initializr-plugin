package ore.plugins.idea.exception.validation;


import ore.plugins.idea.exception.OrePluginRuntimeException;

public class InvalidFileException extends OrePluginRuntimeException {

    public InvalidFileException(String message) {
        super(message);
    }

    public InvalidFileException() {
    }
}
