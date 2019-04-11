package ore.plugins.idea.spring.web.initializr.generator;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import ore.plugins.idea.spring.web.initializr.generator.base.SpringWebInitializrCodeGenerator;
import ore.plugins.idea.spring.web.initializr.model.SpringWebInitializrRequest;

public class ResourcePersistableGenerator extends SpringWebInitializrCodeGenerator {

    public ResourcePersistableGenerator(SpringWebInitializrRequest springWebInitializrRequest) {
        super(springWebInitializrRequest);
    }

    @Override
    public PsiClass generate() {
        String resourcePersistableQualifiedName = String.format("spring.web.initializr.base.domain.ResourcePersistable<%s>", springWebInitializrRequest.getResourceIdQualifiedName());
        addQualifiedImplementsToClass(resourcePersistableQualifiedName, springWebInitializrRequest.getResourceClass());
        springWebInitializrRequest.getResourceClass().add(extractGetIdMethod());
        getJavaCodeStyleManager().shortenClassReferences(springWebInitializrRequest.getResourceClass());
        return springWebInitializrRequest.getResourceClass();
    }

    private PsiMethod extractGetIdMethod() {
        PsiMethod psiMethod = getElementFactory().createMethodFromText(String.format("public %s getResourcePersistableId() { return this.%s; }", springWebInitializrRequest.getResourceIdQualifiedName(), springWebInitializrRequest.getResourceIdPsiField().getNameIdentifier().getText()), springWebInitializrRequest.getResourceClass());
        addOverrideTo(psiMethod);
        return psiMethod;
    }
}
