package ore.plugins.idea.spring.web.initializr.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiClass;
import ore.plugins.idea.dialog.InputValueDialog;
import ore.plugins.idea.dialog.PackageInputDialog;
import ore.plugins.idea.spring.web.initializr.action.base.ResourceAction;
import ore.plugins.idea.spring.web.initializr.generator.ControllerGenerator;
import ore.plugins.idea.spring.web.initializr.generator.RepositoryGenerator;
import ore.plugins.idea.spring.web.initializr.generator.ServiceGenerator;

public class SprintInitializrAction extends ResourceAction {

    @Override
    public void safeActionPerformed(AnActionEvent anActionEvent) {
        PsiClass psiClass = extractPsiClass(anActionEvent);
        InputValueDialog packageInputDialog = new PackageInputDialog(psiClass, String.format("%sRepository", psiClass.getName()), "Place in package: (Leave empty for default package))");
        String packagePath = packageInputDialog.getInput();
        // TODO validate class does not exist already
        act(psiClass, packagePath);
    }


    private void act(PsiClass psiClass, String packagePath) {
        WriteCommandAction.runWriteCommandAction(psiClass.getProject(), () -> {
            PsiClass resourceRepositoryClass = new RepositoryGenerator(psiClass, packagePath).generate();
            PsiClass resourceServiceClass = new ServiceGenerator(psiClass, packagePath, resourceRepositoryClass).generate();
            new ControllerGenerator(psiClass, packagePath, resourceServiceClass).generate();
        });
    }


}
