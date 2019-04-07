package ore.plugins.idea.base.functionality;

import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import ore.plugins.idea.base.domain.PsiHelperField;

import java.util.List;
import java.util.stream.Collectors;

public interface ConstructorCreator {

    String CONSTRUCTOR_TEMPLATE = "%s(%s){%s}";
    String CONSTRUCTOR_ARGUMENT_TEMPLATE = "%s %s";
    String CONSTRUCTOR_ASSIGNMENT_TEMPLATE = "this.%s = %s;";

    default PsiMethod extractConstructorForClass(PsiClass psiClass, List<PsiField> ctrArgs, List<PsiField> ctrArgsToAssign, List<String> superArgs) {
        String constructorArgsPart = ctrArgs.stream()
                .map(psiField -> {
                    PsiHelperField psiHelperField = new PsiHelperField(psiField);
                    return String.format(CONSTRUCTOR_ARGUMENT_TEMPLATE, psiHelperField.getFieldType(), psiHelperField.getLowerCaseFieldName());
                })
                .collect(Collectors.joining(", "));

        ctrArgsToAssign.forEach(psiClass::add);
        String superPart = !superArgs.isEmpty() ? String.format("super(%s);\n", String.join(", ", superArgs)) : "";
        String constructorArgsAssignPart = ctrArgsToAssign.stream()
                .map(psiField -> {
                    PsiHelperField psiHelperField = new PsiHelperField(psiField);
                    return String.format(CONSTRUCTOR_ASSIGNMENT_TEMPLATE, psiHelperField.getLowerCaseFieldName(), psiHelperField.getLowerCaseFieldName());
                })
                .collect(Collectors.joining(""));
        String bodyPart = superPart.concat(constructorArgsAssignPart);

        String constructor = String.format(CONSTRUCTOR_TEMPLATE, psiClass.getName(), constructorArgsPart, bodyPart);
        return JavaPsiFacade.getElementFactory(psiClass.getProject()).createMethodFromText(constructor, psiClass);
    }
}
