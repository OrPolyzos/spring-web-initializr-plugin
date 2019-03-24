package ore.plugins.idea.spring.web.initializr.action.base;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiClass;
import ore.plugins.idea.action.OrePluginAction;
import ore.plugins.idea.exception.validation.InvalidFileException;
import org.jetbrains.annotations.NotNull;
import spring.web.initializr.base.domain.ResourcePersistable;

import java.util.Arrays;

public abstract class ResourceAction extends OrePluginAction {

    @Override
    public void update(@NotNull AnActionEvent anActionEvent) {
        safeExecute(() -> {
            super.update(anActionEvent);
            PsiClass psiClass = extractPsiClass(anActionEvent);
            // TODO Currently only supporting access from ResourcePersistable
            if (psiClass.getImplementsList() == null || Arrays.stream(psiClass.getImplementsList().getReferencedTypes())
                    .noneMatch(refType -> refType.getName().equals(ResourcePersistable.class.getSimpleName()))) {
                throw new InvalidFileException();
            }
        }, anActionEvent, LOGGER);
    }

}
