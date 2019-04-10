package ore.plugins.idea.spring.web.initializr.generator;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import ore.plugins.idea.spring.web.initializr.generator.base.SpringInitializrCodeGenerator;
import ore.plugins.idea.spring.web.initializr.model.SpringWebInitializrRequest;

import java.util.Arrays;
import java.util.Objects;

public class ResourcePersistableGenerator extends SpringInitializrCodeGenerator {

    public ResourcePersistableGenerator(SpringWebInitializrRequest springWebInitializrRequest) {
        super(springWebInitializrRequest);
    }

    @Override
    public PsiClass generate() {
        String resourcePersistableQualifiedName = String.format("spring.web.initializr.base.domain.ResourcePersistable<%s>", springWebInitializrRequest.getResourceIdQualifiedName());
        addQualifiedImplementsToClass(resourcePersistableQualifiedName, springWebInitializrRequest.getResourceClass());
        if (Arrays.stream(springWebInitializrRequest.getResourceClass().getMethods()).noneMatch(e -> Objects.requireNonNull(e.getNameIdentifier()).getText().equals("getId")
                && Objects.requireNonNull(e.getReturnType()).equals(springWebInitializrRequest.getResourceIdPsiField().getType()))) {
            springWebInitializrRequest.getResourceClass().add(extractGetIdMethod());
        }
        getJavaCodeStyleManager().shortenClassReferences(springWebInitializrRequest.getResourceClass());
        return springWebInitializrRequest.getResourceClass();
    }

    private PsiMethod extractGetIdMethod() {
        PsiMethod psiMethod = getElementFactory().createMethodFromText(String.format("public %s getId() { return this.%s; }", springWebInitializrRequest.getResourceIdQualifiedName(), springWebInitializrRequest.getResourceIdPsiField().getNameIdentifier().getText()), springWebInitializrRequest.getResourceClass());
        addOverrideTo(psiMethod);
        return psiMethod;
    }
}
