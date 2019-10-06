package ore.plugins.idea.swip.service.base;

import com.intellij.psi.PsiMember;
import ore.plugins.idea.lib.service.JavaCodeGenerator;
import ore.plugins.idea.swip.model.SwipRequest;

public abstract class SwipJavaCodeGenerator extends JavaCodeGenerator {

    protected SwipRequest swipRequest;

    public SwipJavaCodeGenerator(SwipRequest swipRequest) {
        super(swipRequest.getResourcePersistableClass());
        this.swipRequest = swipRequest;
    }

    protected void addAutowiredTo(PsiMember psiMember) {
        addQualifiedAnnotationNameTo("org.springframework.beans.factory.annotation.Autowired", psiMember);
    }
}
