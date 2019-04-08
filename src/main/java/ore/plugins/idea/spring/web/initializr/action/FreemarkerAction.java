package ore.plugins.idea.spring.web.initializr.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import ore.plugins.idea.exception.validation.InvalidStructureException;
import ore.plugins.idea.spring.web.initializr.action.base.ResourceAction;

import java.util.Arrays;

public class FreemarkerAction extends ResourceAction {

    @Override
    public void safeActionPerformed(AnActionEvent anActionEvent) {
        PsiClass psiClass = extractPsiClass(anActionEvent);
        // TODO Currently only supporting "src/main/java/resources"

    }


    private VirtualFile getVirtualFolder(VirtualFile virtualFolder, String folderName) {
        return Arrays.stream(virtualFolder.getChildren()).filter(child -> child.getName().equals(folderName)).findFirst().orElseThrow(InvalidStructureException::new);
    }
}
