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

public class ResourcePersistableControllerGenerator extends SwipJavaCodeGenerator {


    private static final String SWI_RESOURCE_PERSISTABLE_CONTROLLER_TEMPLATE = "ore.spring.web.initializr.controller.ResourcePersistableController<%s, %s, %s, %s>";
    private static final String CONTROLLER_ANNOTATION_QN = "org.springframework.stereotype.Controller";
    private static final String GET_MAPPING_QN = "org.springframework.web.bind.annotation.GetMapping";
    private static final String POST_MAPPING_QN = "org.springframework.web.bind.annotation.PostMapping";

    private static final String NAME_TEMPLATE = "%sResourcePersistableController";
    private static final String MANDATORY_PATH_METHOD_TEMPLATE = "protected String get%s() { return %s; }";

    private PsiClass resourcePersistableService;

    private String resourcePersistableSingular;
    private String resourcePersistablePlural;

    public ResourcePersistableControllerGenerator(SwipRequest swipRequest, PsiClass resourcePersistableService) {
        super(swipRequest);
        this.resourcePersistableService = resourcePersistableService;
        this.resourcePersistableSingular = toFirstLetterLowerCase(Objects.requireNonNull(swipRequest.getResourcePersistableClass().getName()));
        this.resourcePersistablePlural = toPlural(resourcePersistableSingular);
    }

    @Override
    public PsiClass generateJavaClass() {
        String fullPackagePath = getProjectRootManager().getContentRoots()[0].getPath().concat(DEFAULT_JAVA_SRC_PATH).concat(swipRequest.getResourcePersistableControllerPackage().replaceAll("\\.", "/"));
        VirtualFile vfPackage = createFolderIfNotExists(fullPackagePath);
        PsiDirectory pdPackage = getPsiManager().findDirectory(vfPackage);
        return createResourcePersistableController(pdPackage);
    }

    private PsiClass createResourcePersistableController(PsiDirectory psiDirectory) {
        String resourcePersistableControllerName = String.format(NAME_TEMPLATE, swipRequest.getResourcePersistableClass().getName());
        PsiJavaFile resourcePersistableControllerFile = createJavaFileInDirectoryWithPackage(psiDirectory, resourcePersistableControllerName, swipRequest.getResourcePersistableControllerPackage());

        PsiClass resourcePersistableController = getElementFactory().createClass(resourcePersistableControllerName);
        addQualifiedAnnotationNameTo(CONTROLLER_ANNOTATION_QN, resourcePersistableController);

        String resourcePersistableControllerQualifiedName =
                String.format(SWI_RESOURCE_PERSISTABLE_CONTROLLER_TEMPLATE,
                        swipRequest.getResourcePersistableClass().getQualifiedName(),
                        swipRequest.getResourcePersistableIdQualifiedName(),
                        swipRequest.getResourcePersistableFormClass().getQualifiedName(),
                        swipRequest.getResourcePersistableSearchFormClass().getQualifiedName());
        addQualifiedExtendsToClass(resourcePersistableControllerQualifiedName, resourcePersistableController);

        setupConstructor(resourcePersistableController);
        setupConstantsAndGetters(resourcePersistableController);
        setupServletMethods(resourcePersistableController);
        setupConvertMethods(resourcePersistableController);

        getJavaCodeStyleManager().shortenClassReferences(resourcePersistableController);
        resourcePersistableControllerFile.add(resourcePersistableController);

        return resourcePersistableController;
    }

    private void setupConvertMethods(PsiClass resourcePersistableController) {
        PsiMethod resourcePersistableFormToResourcePersistableMethod = getElementFactory().createMethodFromText(String.format("protected %s resourcePersistableFormToResourcePersistable(%s %s) { return %s; }",
                swipRequest.getResourcePersistableClass().getQualifiedName(),
                swipRequest.getResourcePersistableFormClass().getQualifiedName(),
                toFirstLetterLowerCase(Objects.requireNonNull(swipRequest.getResourcePersistableFormClass().getName())),
                toFirstLetterLowerCase(Objects.requireNonNull(swipRequest.getResourcePersistableClass().getName()))), resourcePersistableController.getContext());
        addOverrideTo(resourcePersistableFormToResourcePersistableMethod);
        resourcePersistableController.add(resourcePersistableFormToResourcePersistableMethod);

        PsiMethod resourcePersistableToResourcePersistableFormMethod = getElementFactory().createMethodFromText(String.format("protected %s resourcePersistableToResourcePersistableForm(%s %s) { return %s; }",
                swipRequest.getResourcePersistableFormClass().getQualifiedName(),
                swipRequest.getResourcePersistableClass().getQualifiedName(),
                toFirstLetterLowerCase(Objects.requireNonNull(swipRequest.getResourcePersistableClass().getName())),
                toFirstLetterLowerCase(Objects.requireNonNull(swipRequest.getResourcePersistableFormClass().getName()))), resourcePersistableController.getContext());
        addOverrideTo(resourcePersistableToResourcePersistableFormMethod);
        resourcePersistableController.add(resourcePersistableToResourcePersistableFormMethod);
    }

    private void setupServletMethods(PsiClass resourcePersistableController) {
        resourcePersistableController.add(extractGetResourcePersistableBaseViewMethod(resourcePersistableController));
        resourcePersistableController.add(extractCreateResourcePersistableMethod(resourcePersistableController));
        resourcePersistableController.add(extractDeleteResourcePersistableMethod(resourcePersistableController));
        resourcePersistableController.add(extractGetResourcePersistableEditViewMethod(resourcePersistableController));
        resourcePersistableController.add(extractEditResourcePersistableMethod(resourcePersistableController));
        resourcePersistableController.add(extractSearchResourcePersistablesByMethod(resourcePersistableController));

    }

    @NotNull
    private PsiMethod extractGetResourcePersistableBaseViewMethod(PsiClass resourcePersistableController) {
        PsiAnnotation annotation = getElementFactory().createAnnotationFromText(String.format("@%s(%s)", GET_MAPPING_QN, "RESOURCE_PERSISTABLE_BASE_URI"), resourcePersistableController.getContext());
        PsiMethod psiMethod = getElementFactory().createMethodFromText("public String getResourcePersistableBaseView(org.springframework.ui.Model model) { return super.getResourcePersistableBaseView(model); }", resourcePersistableController.getContext());
        addOverrideTo(psiMethod);
        psiMethod.getModifierList().addAfter(annotation, psiMethod.getModifierList().getAnnotations()[0]);
        return psiMethod;
    }

    @NotNull
    private PsiMethod extractCreateResourcePersistableMethod(PsiClass resourcePersistableController) {
        PsiAnnotation annotation = getElementFactory().createAnnotationFromText(String.format("@%s(%s)", POST_MAPPING_QN, "RESOURCE_PERSISTABLE_BASE_URI"), resourcePersistableController.getContext());
        PsiMethod psiMethod = getElementFactory()
                .createMethodFromText(String.format("public String createResourcePersistable(" +
                                "@javax.validation.Valid @org.springframework.web.bind.annotation.ModelAttribute(RESOURCE_PERSISTABLE_FORM_HOLDER) %s resourcePersistableForm, " +
                                "org.springframework.validation.BindingResult bindingResult, " +
                                "org.springframework.ui.Model model, " +
                                "org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) { " +
                                "return super.createResourcePersistable(resourcePersistableForm, bindingResult, model, redirectAttributes); " +
                                "}", swipRequest.getResourcePersistableFormClass().getQualifiedName())
                        , resourcePersistableController.getContext());
        addOverrideTo(psiMethod);
        psiMethod.getModifierList().addAfter(annotation, psiMethod.getModifierList().getAnnotations()[0]);
        return psiMethod;
    }

    @NotNull
    private PsiElement extractDeleteResourcePersistableMethod(PsiClass resourcePersistableController) {
        PsiAnnotation annotation = getElementFactory().createAnnotationFromText(String.format("@%s(\"%s/{resourcePersistableId}/delete\")", POST_MAPPING_QN, resourcePersistablePlural), resourcePersistableController.getContext());
        PsiMethod psiMethod = getElementFactory()
                .createMethodFromText(String.format("public String deleteResourcePersistable(" +
                        "@org.springframework.web.bind.annotation.PathVariable(\"resourcePersistableId\") %s resourcePersistableId, " +
                        "org.springframework.ui.Model model) {" +
                        "return super.deleteResourcePersistable(resourcePersistableId, model); }", swipRequest.getResourcePersistableIdQualifiedName()), resourcePersistableController.getContext());
        addOverrideTo(psiMethod);
        psiMethod.getModifierList().addAfter(annotation, psiMethod.getModifierList().getAnnotations()[0]);
        return psiMethod;
    }

    @NotNull
    private PsiElement extractGetResourcePersistableEditViewMethod(PsiClass resourcePersistableController) {
        PsiAnnotation annotation = getElementFactory().createAnnotationFromText(String.format("@%s(\"%s/{resourcePersistableId}/edit\")", GET_MAPPING_QN, resourcePersistablePlural), resourcePersistableController.getContext());
        PsiMethod psiMethod = getElementFactory()
                .createMethodFromText(String.format("public String getResourcePersistableEditView(" +
                        "@org.springframework.web.bind.annotation.PathVariable(\"resourcePersistableId\") %s resourcePersistableId, " +
                        "org.springframework.ui.Model model) {" +
                        "return super.getResourcePersistableEditView(resourcePersistableId, model); }", swipRequest.getResourcePersistableIdQualifiedName()), resourcePersistableController.getContext());
        addOverrideTo(psiMethod);
        psiMethod.getModifierList().addAfter(annotation, psiMethod.getModifierList().getAnnotations()[0]);
        return psiMethod;
    }

    @NotNull
    private PsiElement extractEditResourcePersistableMethod(PsiClass resourcePersistableController) {
        PsiAnnotation annotation = getElementFactory().createAnnotationFromText(String.format("@%s(\"%s/{resourcePersistableId}/edit\")", POST_MAPPING_QN, resourcePersistablePlural), resourcePersistableController.getContext());
        PsiMethod psiMethod = getElementFactory()
                .createMethodFromText(String.format("public String editResourcePersistable(" +
                                "@org.springframework.web.bind.annotation.PathVariable(\"resourcePersistableId\") %s resourcePersistableId, " +
                                "@javax.validation.Valid @org.springframework.web.bind.annotation.ModelAttribute(RESOURCE_PERSISTABLE_FORM_HOLDER) %s resourcePersistableForm, " +
                                "org.springframework.validation.BindingResult bindingResult, " +
                                "org.springframework.ui.Model model, " +
                                "org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) { " +
                                "return super.editResourcePersistable(resourcePersistableId, resourcePersistableForm, bindingResult, model, redirectAttributes); }",
                        swipRequest.getResourcePersistableIdQualifiedName(), swipRequest.getResourcePersistableFormClass().getQualifiedName()),
                        resourcePersistableController.getContext());
        addOverrideTo(psiMethod);
        psiMethod.getModifierList().addAfter(annotation, psiMethod.getModifierList().getAnnotations()[0]);
        return psiMethod;
    }

    @NotNull
    private PsiElement extractSearchResourcePersistablesByMethod(PsiClass resourcePersistableController) {
        PsiAnnotation annotation = getElementFactory().createAnnotationFromText(String.format("@%s(\"%s/search\")", POST_MAPPING_QN, resourcePersistablePlural), resourcePersistableController.getContext());
        PsiMethod psiMethod = getElementFactory()
                .createMethodFromText(String.format("public String searchResourcePersistablesBy(" +
                                "@javax.validation.Valid @org.springframework.web.bind.annotation.ModelAttribute(RESOURCE_PERSISTABLE_SEARCH_FORM_HOLDER) %s resourcePersistableSearchForm, " +
                                "org.springframework.validation.BindingResult bindingResult, " +
                                "org.springframework.ui.Model model, " +
                                "org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {" +
                                "return super.searchResourcePersistablesBy(resourcePersistableSearchForm, bindingResult, model, redirectAttributes); }",
                        swipRequest.getResourcePersistableSearchFormClass().getQualifiedName()), resourcePersistableController.getContext());
        addOverrideTo(psiMethod);
        psiMethod.getModifierList().addAfter(annotation, psiMethod.getModifierList().getAnnotations()[0]);
        return psiMethod;
    }

    private void setupConstructor(PsiClass resourcePersistableController) {

        String qualifiedResourcePersistableServiceName = String.format("ore.spring.web.initializr.service.ResourcePersistableService<%s,%s,%s>",
                swipRequest.getResourcePersistableClass().getQualifiedName(),
                swipRequest.getResourcePersistableIdQualifiedName(),
        swipRequest.getResourcePersistableFormClass().getQualifiedName());

        PsiField resourcePersistableServiceElement = getElementFactory()
                .createFieldFromText(
                        String.format("private final %s %s;", qualifiedResourcePersistableServiceName, toFirstLetterLowerCase(Objects.requireNonNull(resourcePersistableService.getName()))),
                        resourcePersistableService.getContext()
                );


        List<PsiField> constructorArguments = Collections.singletonList(resourcePersistableServiceElement);
        List<String> superArguments = constructorArguments
                .stream()
                .map(e -> e.getNameIdentifier().getText())
                .collect(Collectors.toList());

        PsiMethod constructor = extractConstructorForClass(resourcePersistableController, constructorArguments, constructorArguments, superArguments);
        PsiUtil.setModifierProperty(constructor, PsiModifier.PUBLIC, true);
        addAutowiredTo(constructor);

        resourcePersistableController.add(constructor);
    }

    private void setupConstantsAndGetters(PsiClass resourcePersistableController) {
        addConstantAndGetter(resourcePersistableController, "resourcePersistableBaseUri", getResourcePersistableBaseUri());
        addConstantAndGetter(resourcePersistableController, "resourcePersistableBaseViewPath", getResourcePersistableBaseViewPath());
        addConstantAndGetter(resourcePersistableController, "resourcePersistableEditViewPath", getResourcePersistableEditViewPath());
        addConstantAndGetter(resourcePersistableController, "resourcePersistableFormHolder", getResourcePersistableFormHolder());
        addConstantAndGetter(resourcePersistableController, "resourcePersistableSearchFormHolder", getResourcePersistableSearchFormHolder());
        addConstantAndGetter(resourcePersistableController, "resourcePersistableListHolder", getResourcePersistableListHolder());
    }

    private void addConstantAndGetter(PsiClass resourcePersistableController, String name, String value) {
        PsiField resourcePersistableField = getElementFactory().createFieldFromText(String.format("private static final String %s = \"%s\";", camelCaseToUpperCaseWithUnderScore(name), value), resourcePersistableController.getContext());
        resourcePersistableController.add(resourcePersistableField);

        PsiMethod resourcePersistableFieldGetter = getElementFactory().createMethodFromText(String.format(MANDATORY_PATH_METHOD_TEMPLATE, toFirstLetterUpperCase(name), camelCaseToUpperCaseWithUnderScore(name)), resourcePersistableController.getContext());
        addOverrideTo(resourcePersistableFieldGetter);
        resourcePersistableController.add(resourcePersistableFieldGetter);
    }

    String getResourcePersistableBaseUri() {
        return String.format("/%s", resourcePersistablePlural);
    }

    String getResourcePersistableBaseViewPath() {
        return String.format("/%s/%s", resourcePersistableSingular, resourcePersistablePlural);
    }

    String getResourcePersistableEditViewPath() {
        return String.format("/%s/edit-%s", resourcePersistableSingular, resourcePersistableSingular);
    }

    String getResourcePersistableFormHolder() {
        return String.format("%sForm", resourcePersistableSingular);
    }

    String getResourcePersistableSearchFormHolder() {
        return String.format("%sSearchForm", resourcePersistableSingular);
    }

    String getResourcePersistableListHolder() {
        return String.format("%sList", resourcePersistableSingular);
    }


}
