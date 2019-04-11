package ore.plugins.idea.spring.web.initializr.generator.base;

import com.intellij.psi.PsiMember;
import ore.plugins.idea.spring.web.initializr.model.SpringWebInitializrRequest;

public abstract class SpringWebInitializrCodeGenerator extends CodeGenerator {

    protected SpringWebInitializrRequest springWebInitializrRequest;

    public SpringWebInitializrCodeGenerator(SpringWebInitializrRequest springWebInitializrRequest) {
        super(springWebInitializrRequest.getResourceClass(), springWebInitializrRequest.getResourceClass().getProject());
        this.springWebInitializrRequest = springWebInitializrRequest;
    }

    protected void addAutowiredTo(PsiMember psiMember) {
        addQualifiedAnnotationNameTo("org.springframework.beans.factory.annotation.Autowired", psiMember);
    }
}
