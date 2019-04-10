package ore.plugins.idea.exception.validation;


import ore.plugins.idea.exception.OrePluginRuntimeException;

public class InvalidStructureException extends OrePluginRuntimeException {

    public InvalidStructureException(String message) {
        super(message);
    }

    public InvalidStructureException() {
    }
}
