package ore.plugins.idea.base.domain;

import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;

import java.util.Objects;

import static ore.plugins.idea.utils.FormatUtils.toLowerCaseFirstLetterString;
import static ore.plugins.idea.utils.FormatUtils.toFirstLetterUpperCase;

public class PsiHelperField {

    private static final String GETTER_TEMPLATE = "%s get%s(){return %s;}";
    private static final String SETTER_TEMPLATE = "void set%s(%s %s){this.%s = %s;}";

    private String upperCaseFieldName;
    private String lowerCaseFieldName;
    private String fieldType;

    public PsiHelperField(PsiField psiField) {
        String psiFieldName = Objects.requireNonNull(psiField.getName());
        this.upperCaseFieldName = toFirstLetterUpperCase(psiFieldName);
        this.lowerCaseFieldName = toLowerCaseFirstLetterString(psiFieldName);
        this.fieldType = psiField.getType().getCanonicalText();
    }

    public String getUpperCaseFieldName() {
        return upperCaseFieldName;
    }

    public String getLowerCaseFieldName() {
        return lowerCaseFieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public PsiMethod getGetterMethod(PsiClass psiClass) {
        String getterMethod = String.format(GETTER_TEMPLATE, fieldType, upperCaseFieldName, lowerCaseFieldName);
        return JavaPsiFacade.getElementFactory(psiClass.getProject()).createMethodFromText(getterMethod, psiClass);
    }

    public PsiMethod getSetterMethod(PsiClass psiClass) {
        String setterMethod = String.format(SETTER_TEMPLATE, upperCaseFieldName, fieldType, lowerCaseFieldName, lowerCaseFieldName, lowerCaseFieldName);
        return JavaPsiFacade.getElementFactory(psiClass.getProject()).createMethodFromText(setterMethod, psiClass);
    }
}
