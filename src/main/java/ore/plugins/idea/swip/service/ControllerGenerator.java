package ore.plugins.idea.swip.service;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import ore.plugins.idea.swip.model.SwipRequest;
import ore.plugins.idea.swip.service.base.SwipJavaCodeGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ore.plugins.idea.lib.utils.FormatUtils.*;

public class ControllerGenerator extends SwipJavaCodeGenerator {

    private static final String RESOURCE_SERVICE_NAME_TEMPLATE = "%sResourceController";
    private static final String CONTROLLER_ANNOTATION_QN = "org.springframework.stereotype.Controller";
    private static final String MANDATORY_PATH_METHOD_TEMPLATE = "protected String get%s() { return %s; }";
    private static final String GET_MAPPING_QN = "org.springframework.web.bind.annotation.GetMapping";
    private static final String POST_MAPPING_QN = "org.springframework.web.bind.annotation.PostMapping";

    private PsiClass resourceService;

    private String resourceSingular;
    private String resourcePlural;

    public ControllerGenerator(SwipRequest swipRequest, PsiClass resourceService) {
        super(swipRequest);
        this.resourceService = resourceService;
        this.resourceSingular = toFirstLetterLowerCase(Objects.requireNonNull(swipRequest.getResourceClass().getName()));
        this.resourcePlural = toPlural(resourceSingular);
    }

    @Override
    public PsiClass generateJavaClass() {
        String fullPackagePath = getProjectRootManager().getContentRoots()[0].getPath().concat(DEFAULT_JAVA_SRC_PATH).concat(swipRequest.getResourceControllerPackage().replaceAll("\\.", "/"));
        VirtualFile vfPackage = createFolderIfNotExists(fullPackagePath);
        PsiDirectory pdPackage = getPsiManager().findDirectory(vfPackage);
        return createResourceController(pdPackage);
    }

    private PsiClass createResourceController(PsiDirectory psiDirectory) {
        String resourceControllerName = String.format(RESOURCE_SERVICE_NAME_TEMPLATE, swipRequest.getResourceClass().getName());
        PsiJavaFile resourceControllerFile = createJavaFileInDirectoryWithPackage(psiDirectory, resourceControllerName, swipRequest.getResourceControllerPackage());

        PsiClass resourceController = getElementFactory().createClass(resourceControllerName);
        addQualifiedAnnotationNameTo(CONTROLLER_ANNOTATION_QN, resourceController);

        String resourceControllerQualifiedName = String.format("ore.spring.web.initializr.controller.ResourceController<%s, %s, %s, %s>",
                swipRequest.getResourceClass().getQualifiedName(),
                swipRequest.getResourceIdQualifiedName(),
                swipRequest.getResourceFormClass().getQualifiedName(),
                swipRequest.getResourceSearchFormClass().getQualifiedName());
        addQualifiedExtendsToClass(resourceControllerQualifiedName, resourceController);

        setupConstructor(resourceController);
        setupConstantsAndGetters(resourceController);
        setupServletMethods(resourceController);
        setupConvertMethods(resourceController);

        getJavaCodeStyleManager().shortenClassReferences(resourceController);
        resourceControllerFile.add(resourceController);

        return resourceController;
    }

    private void setupConvertMethods(PsiClass resourceController) {
        PsiMethod resourceFormToResourceMethod = getElementFactory().createMethodFromText(String.format("protected %s resourceFormToResource(%s %s) { return %s; }",
                swipRequest.getResourceClass().getQualifiedName(),
                swipRequest.getResourceFormClass().getQualifiedName(),
                toFirstLetterLowerCase(Objects.requireNonNull(swipRequest.getResourceFormClass().getName())),
                toFirstLetterLowerCase(Objects.requireNonNull(swipRequest.getResourceClass().getName()))), resourceController.getContext());
        addOverrideTo(resourceFormToResourceMethod);
        resourceController.add(resourceFormToResourceMethod);

        PsiMethod resourceToResourceFormMethod = getElementFactory().createMethodFromText(String.format("protected %s resourceToResourceForm(%s %s) { return %s; }",
                swipRequest.getResourceFormClass().getQualifiedName(),
                swipRequest.getResourceClass().getQualifiedName(),
                toFirstLetterLowerCase(Objects.requireNonNull(swipRequest.getResourceClass().getName())),
                toFirstLetterLowerCase(Objects.requireNonNull(swipRequest.getResourceFormClass().getName()))), resourceController.getContext());
        addOverrideTo(resourceToResourceFormMethod);
        resourceController.add(resourceToResourceFormMethod);
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
                                "}", swipRequest.getResourceFormClass().getQualifiedName())
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
                        "return super.deleteResource(resourceId, model, redirectAttributes); }", swipRequest.getResourceIdQualifiedName()), resourceController.getContext());
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
                        "return super.getEditResourceView(resourceId, model, redirectAttributes); }", swipRequest.getResourceIdQualifiedName()), resourceController.getContext());
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
                                "return super.editResource(resourceId, resourceForm, bindingResult, model, redirectAttributes, httpServletRequest); }",
                        swipRequest.getResourceIdQualifiedName(), swipRequest.getResourceFormClass().getQualifiedName()),
                        resourceController.getContext());
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
                                "return super.searchBy(resourceSearchForm, bindingResult, model, redirectAttributes); }",
                        swipRequest.getResourceSearchFormClass().getQualifiedName()), resourceController.getContext());
        addOverrideTo(psiMethod);
        psiMethod.getModifierList().addAfter(annotation, psiMethod.getModifierList().getAnnotations()[0]);
        return psiMethod;
    }

    private void setupConstructor(PsiClass resourceController) {
        String qualifiedServiceName = resourceService.getName();
        if (swipRequest.getResourceServicePackage().length() > 0) {
            qualifiedServiceName = swipRequest.getResourceServicePackage().concat(".").concat(Objects.requireNonNull(qualifiedServiceName));
        }
        PsiField resourceServiceElement = getElementFactory().createFieldFromText(String.format("private final %s %s;", qualifiedServiceName,
                toFirstLetterLowerCase(Objects.requireNonNull(resourceService.getName()))), resourceService.getContext());


        List<PsiField> constructorArguments = Collections.singletonList(resourceServiceElement);
        List<String> superArguments = constructorArguments
                .stream()
                .map(e -> e.getNameIdentifier().getText())
                .collect(Collectors.toList());

        PsiMethod constructor = extractConstructorForClass(resourceController, constructorArguments, constructorArguments, superArguments);
        PsiUtil.setModifierProperty(constructor, PsiModifier.PUBLIC, true);
        addAutowiredTo(constructor);

        resourceController.add(constructor);
    }

    private void setupConstantsAndGetters(PsiClass resourceController) {
        addConstantAndGetter(resourceController, "resourceBaseUri", getResourceBaseUri());
        addConstantAndGetter(resourceController, "resourceViewPath", getResourceViewPath());
        addConstantAndGetter(resourceController, "editResourceViewPath", getEditResourceViewPath());
        addConstantAndGetter(resourceController, "resourceFormHolder", getResourceFormHolder());
        addConstantAndGetter(resourceController, "resourceSearchFormHolder", getResourceSearchFormHolder());
        addConstantAndGetter(resourceController, "resourceListHolder", getResourceListHolder());
    }

    String getResourceListHolder() {
        return String.format("%sList", resourceSingular);
    }

    String getResourceSearchFormHolder() {
        return String.format("%sSearchForm", resourceSingular);
    }

    String getResourceFormHolder() {
        return String.format("%sForm", resourceSingular);
    }

    String getEditResourceViewPath() {
        return String.format("/%s/edit-%s", resourceSingular, resourceSingular);
    }

    String getResourceViewPath() {
        return String.format("/%s/%s", resourceSingular, resourcePlural);
    }

    String getResourceBaseUri() {
        return String.format("/%s", resourcePlural);
    }

    private void addConstantAndGetter(PsiClass resourceController, String name, String value) {
        PsiField resourceField = getElementFactory().createFieldFromText(String.format("private static final String %s = \"%s\";", camelCaseToUpperCaseWithUnderScore(name), value), resourceController.getContext());
        resourceController.add(resourceField);

        PsiMethod resourceFieldGetter = getElementFactory().createMethodFromText(String.format(MANDATORY_PATH_METHOD_TEMPLATE, toFirstLetterUpperCase(name), camelCaseToUpperCaseWithUnderScore(name)), resourceController.getContext());
        addOverrideTo(resourceFieldGetter);
        resourceController.add(resourceFieldGetter);
    }

}
