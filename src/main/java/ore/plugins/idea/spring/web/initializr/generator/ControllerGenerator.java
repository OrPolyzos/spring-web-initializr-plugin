package ore.plugins.idea.spring.web.initializr.generator;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import ore.plugins.idea.base.functionality.TemplateReader;
import ore.plugins.idea.spring.web.initializr.generator.base.SpringInitializrCodeGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ore.plugins.idea.utils.FormatUtils.*;

public class ControllerGenerator extends SpringInitializrCodeGenerator implements TemplateReader {

    private static final String RESOURCE_SERVICE_NAME_TEMPLATE = "%sResourceController";
    private static final String CONTROLLER_ANNOTATION_QN = "org.springframework.stereotype.Controller";
    private static final String MANDATORY_PATH_METHOD_TEMPLATE = "protected String get%s() { return %s; }";
    private static final String GET_MAPPING_QN = "org.springframework.web.bind.annotation.GetMapping";
    private static final String POST_MAPPING_QN = "org.springframework.web.bind.annotation.PostMapping";

    private String packagePath;
    private PsiClass resourceService;

    private String resourceSingular;
    private String resourcePlural;

    private PsiClass resourceForm;
    private PsiClass resourceSearchForm;

    public ControllerGenerator(PsiClass psiClass, String packagePath, PsiClass resourceService) {
        super(psiClass, psiClass.getProject());
        this.packagePath = packagePath;
        this.resourceService = resourceService;
        this.resourceSingular = toFirstLetterLowerCase(Objects.requireNonNull(psiClass.getName()));
        this.resourcePlural = toPlural(resourceSingular);

        // TODO Will be changed when ResourceForm & ResourceSearchForm will be supported
        this.resourceForm = psiClass;
        this.resourceSearchForm = psiClass;
    }

    @Override
    public PsiClass generate() {
        String fullPackagePath = getProjectRootManager().getContentRoots()[0].getPath().concat(DEFAULT_JAVA_SRC_PATH).concat(packagePath.replaceAll("\\.", "/"));
        VirtualFile vfPackage = createFolderIfNotExists(fullPackagePath);
        PsiDirectory pdPackage = getPsiManager().findDirectory(vfPackage);
        return createResourceController(pdPackage);
    }

    private PsiClass createResourceController(PsiDirectory psiDirectory) {
        String resourceControllerName = String.format(RESOURCE_SERVICE_NAME_TEMPLATE, psiClass.getName());
        PsiJavaFile resourceServiceFile = createJavaFileInDirectoryWithPackage(psiDirectory, resourceControllerName, packagePath);

        PsiClass resourceController = getElementFactory().createClass(resourceControllerName);
        addQualifiedAnnotationNameTo(CONTROLLER_ANNOTATION_QN, resourceController);

        String resourceControllerQualifiedName = String.format("spring.web.initializr.base.controller.ResourceController<%s, %s, %s, %s>", psiClass.getQualifiedName(), extractResourceIdQualifiedName(), resourceForm.getQualifiedName(), resourceSearchForm.getQualifiedName());
        addQualifiedExtendsToClass(resourceControllerQualifiedName, resourceController);

        setupConstructor(resourceController);
        setupConstantsAndGetters(resourceController);
        setupServletMethods(resourceController);

        getJavaCodeStyleManager().shortenClassReferences(resourceController);
        resourceServiceFile.add(resourceController);

        return resourceController;
    }

    private void setupServletMethods(PsiClass resourceController) {
        resourceController.add(extractGetResourceViewMethod(resourceController));
        resourceController.add(extractCreateResourceMethod(resourceController));
        resourceController.add(extractDeleteResourceMethod(resourceController));
        resourceController.add(extractGetEditResourceViewMethod(resourceController));
        resourceController.add(extractEditResourceMethod(resourceController));
        resourceController.add(extractSearchByMethod(resourceController));

    }

    @NotNull
    private PsiMethod extractGetResourceViewMethod(PsiClass resourceController) {
        PsiAnnotation annotation = getElementFactory().createAnnotationFromText(String.format("@%s(%s)", GET_MAPPING_QN, "RESOURCE_BASE_URI"), resourceController.getContext());
        PsiMethod psiMethod = getElementFactory().createMethodFromText("public String getResourceView(org.springframework.ui.Model model) { return super.getResourceView(model); }", resourceController.getContext());
        addOverrideTo(psiMethod);
        psiMethod.getModifierList().addAfter(annotation, psiMethod.getModifierList().getAnnotations()[0]);
        return psiMethod;
    }

    @NotNull
    private PsiMethod extractCreateResourceMethod(PsiClass resourceController) {
        PsiAnnotation annotation = getElementFactory().createAnnotationFromText(String.format("@%s(%s)", POST_MAPPING_QN, "RESOURCE_BASE_URI"), resourceController.getContext());
        PsiMethod psiMethod = getElementFactory()
                .createMethodFromText(String.format("public String createResource(" +
                                "@javax.validation.Valid @org.springframework.web.bind.annotation.ModelAttribute(RESOURCE_FORM_HOLDER) %s resourceForm, " +
                                "org.springframework.validation.BindingResult bindingResult, " +
                                "org.springframework.ui.Model model, " +
                                "org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) { " +
                                "return super.createResource(resourceForm, bindingResult, model, redirectAttributes); " +
                                "}", resourceForm.getQualifiedName())
                        , resourceController.getContext());
        addOverrideTo(psiMethod);
        psiMethod.getModifierList().addAfter(annotation, psiMethod.getModifierList().getAnnotations()[0]);
        return psiMethod;
    }

    @NotNull
    private PsiElement extractDeleteResourceMethod(PsiClass resourceController) {
        PsiAnnotation annotation = getElementFactory().createAnnotationFromText(String.format("@%s(\"%s/{resourceId}/delete\")", POST_MAPPING_QN, resourcePlural), resourceController.getContext());
        PsiMethod psiMethod = getElementFactory()
                .createMethodFromText(String.format("public String deleteResource(" +
                        "@org.springframework.web.bind.annotation.PathVariable(\"resourceId\") %s resourceId, " +
                        "org.springframework.ui.Model model, " +
                        "org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {" +
                        "return super.deleteResource(resourceId, model, redirectAttributes); }", extractResourceIdQualifiedName()), resourceController.getContext());
        addOverrideTo(psiMethod);
        psiMethod.getModifierList().addAfter(annotation, psiMethod.getModifierList().getAnnotations()[0]);
        return psiMethod;
    }

    @NotNull
    private PsiElement extractGetEditResourceViewMethod(PsiClass resourceController) {
        PsiAnnotation annotation = getElementFactory().createAnnotationFromText(String.format("@%s(\"%s/{resourceId}/edit\")", GET_MAPPING_QN, resourcePlural), resourceController.getContext());
        PsiMethod psiMethod = getElementFactory()
                .createMethodFromText(String.format("public String getEditResourceView(" +
                        "@org.springframework.web.bind.annotation.PathVariable(\"resourceId\") %s resourceId, " +
                        "org.springframework.ui.Model model, " +
                        "org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {" +
                        "return super.getEditResourceView(resourceId, model, redirectAttributes); }", extractResourceIdQualifiedName()), resourceController.getContext());
        addOverrideTo(psiMethod);
        psiMethod.getModifierList().addAfter(annotation, psiMethod.getModifierList().getAnnotations()[0]);
        return psiMethod;
    }

    @NotNull
    private PsiElement extractEditResourceMethod(PsiClass resourceController) {
        PsiAnnotation annotation = getElementFactory().createAnnotationFromText(String.format("@%s(\"%s/{resourceId}/edit\")", POST_MAPPING_QN, resourcePlural), resourceController.getContext());
        PsiMethod psiMethod = getElementFactory()
                .createMethodFromText(String.format("public String editResource(" +
                        "@org.springframework.web.bind.annotation.PathVariable(\"resourceId\") %s resourceId, " +
                        "@javax.validation.Valid @org.springframework.web.bind.annotation.ModelAttribute(RESOURCE_FORM_HOLDER) %s resourceForm, " +
                        "org.springframework.validation.BindingResult bindingResult, " +
                        "org.springframework.ui.Model model, " +
                        "org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes, " +
                        "javax.servlet.http.HttpServletRequest httpServletRequest) {" +
                        "return super.editResource(resourceId, resourceForm, bindingResult, model, redirectAttributes, httpServletRequest); }", extractResourceIdQualifiedName(), resourceForm.getQualifiedName()), resourceController.getContext());
        addOverrideTo(psiMethod);
        psiMethod.getModifierList().addAfter(annotation, psiMethod.getModifierList().getAnnotations()[0]);
        return psiMethod;
    }

    @NotNull
    private PsiElement extractSearchByMethod(PsiClass resourceController) {
        PsiAnnotation annotation = getElementFactory().createAnnotationFromText(String.format("@%s(\"%s/search\")", POST_MAPPING_QN, resourcePlural), resourceController.getContext());
        PsiMethod psiMethod = getElementFactory()
                .createMethodFromText(String.format("public String searchBy(" +
                        "@javax.validation.Valid @org.springframework.web.bind.annotation.ModelAttribute(RESOURCE_SEARCH_FORM_HOLDER) %s resourceSearchForm, " +
                        "org.springframework.validation.BindingResult bindingResult, " +
                        "org.springframework.ui.Model model, " +
                        "org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {" +
                        "return super.searchBy(resourceSearchForm, bindingResult, model, redirectAttributes); }", resourceSearchForm.getQualifiedName()), resourceController.getContext());
        addOverrideTo(psiMethod);
        psiMethod.getModifierList().addAfter(annotation, psiMethod.getModifierList().getAnnotations()[0]);
        return psiMethod;
    }

    private void setupConstructor(PsiClass resourceController) {
        PsiField resourceServiceElement = getElementFactory().createField(toFirstLetterLowerCase(Objects.requireNonNull(this.resourceService.getName())), getElementFactory().createType(this.resourceService));
        PsiUtil.setModifierProperty(resourceServiceElement, PsiModifier.PRIVATE, true);
        PsiUtil.setModifierProperty(resourceServiceElement, PsiModifier.FINAL, true);

        List<PsiField> constructorArguments = Collections.singletonList(resourceServiceElement);
        List<String> superArguments =
                Stream.of(String.format("%s.class", psiClass.getQualifiedName()),
                        String.format("%s.class", resourceForm.getQualifiedName()),
                        String.format("%s.class", resourceSearchForm.getQualifiedName()),
                        // TODO Will be changed when ResourceForm & ResourceSearchForm will be supported
                        "resource -> resource",
                        "resource -> resource",
                        resourceServiceElement.getNameIdentifier().getText())
                        .collect(Collectors.toList());

        PsiMethod constructor = extractConstructorForClass(resourceController, constructorArguments, constructorArguments, superArguments);
        PsiUtil.setModifierProperty(constructor, PsiModifier.PUBLIC, true);
        addAutowiredTo(constructor);

        resourceController.add(constructor);
    }

    private void setupConstantsAndGetters(PsiClass resourceController) {
        addConstantAndGetter(resourceController, "resourceBaseUri", String.format("/%s", resourcePlural));
        addConstantAndGetter(resourceController, "resourceViewPath", String.format("/%s/%s", resourceSingular, resourcePlural));
        addConstantAndGetter(resourceController, "editResourceViewPath", String.format("/%s/edit-%s", resourceSingular, resourceSingular));
        addConstantAndGetter(resourceController, "resourceFormHolder", String.format("%sForm", resourceSingular));
        addConstantAndGetter(resourceController, "resourceSearchFormHolder", String.format("%sSearchForm", resourceSingular));
        addConstantAndGetter(resourceController, "resourceListHolder", String.format("%sList", resourceSingular));
    }

    private void addConstantAndGetter(PsiClass resourceController, String name, String value) {
        PsiField resourceField = getElementFactory().createFieldFromText(String.format("private static final String %s = \"%s\";", camelCaseToUpperCaseWithUnderScore(name), value), resourceController.getContext());
        resourceController.add(resourceField);

        PsiMethod resourceFieldGetter = getElementFactory().createMethodFromText(String.format(MANDATORY_PATH_METHOD_TEMPLATE, toFirstLetterUpperCase(name), camelCaseToUpperCaseWithUnderScore(name)), resourceController.getContext());
        addOverrideTo(resourceFieldGetter);
        resourceController.add(resourceFieldGetter);
    }

}
