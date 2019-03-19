package ore.plugins.idea.exception.validation;

import com.intellij.psi.PsiClass;
import ore.plugins.idea.exception.OrePluginRuntimeException;

public class ValidationException extends OrePluginRuntimeException {

    private PsiClass psiClass;

    public ValidationException(PsiClass psiClass, String message) {
        super(message);
        this.psiClass = psiClass;
    }

    public PsiClass getPsiClass() {
        return psiClass;
    }
}
