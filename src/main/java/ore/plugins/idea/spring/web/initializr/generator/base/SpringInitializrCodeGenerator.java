package ore.plugins.idea.spring.web.initializr.generator.base;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMember;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class SpringInitializrCodeGenerator extends CodeGenerator {

    public SpringInitializrCodeGenerator(PsiClass psiClass, Project project) {
        super(psiClass, project);
    }

    @NotNull
    protected String extractResourceIdQualifiedName() {
        return Objects.requireNonNull(psiClass.getImplementsList()).getReferencedTypes()[1].getParameters()[0].getCanonicalText();
    }

    protected void addAutowiredTo(PsiMember psiMember) {
        addQualifiedAnnotationNameTo("org.springframework.beans.factory.annotation.Autowired", psiMember);
    }
}
