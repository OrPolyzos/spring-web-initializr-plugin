package ore.plugins.idea.base.functionality;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import ore.plugins.idea.exception.CancelException;
import ore.plugins.idea.exception.OrePluginRuntimeException;
import ore.plugins.idea.exception.validation.InvalidFileException;
import ore.plugins.idea.exception.validation.ValidationException;
import org.slf4j.Logger;

public interface ExceptionResolver {

    default void safeExecute(Runnable runnable, AnActionEvent anActionEvent, Logger logger) {
        try {
            runnable.run();
        } catch (InvalidFileException invalidFileException) {
            anActionEvent.getPresentation().setEnabled(false);
        } catch (ValidationException validationException) {
            Messages.showErrorDialog(validationException.getMessage(), "Error");
        } catch (CancelException ignored) {
        } catch (OrePluginRuntimeException exception) {
            logger.error(exception.getMessage());
        }
    }

}
