package ore.plugins.idea.spring.web.initializr.generator;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import ore.plugins.idea.base.functionality.TemplateReader;
import ore.plugins.idea.spring.web.initializr.generator.base.SpringWebInitializrCodeGenerator;
import ore.plugins.idea.spring.web.initializr.model.SpringWebInitializrRequest;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static ore.plugins.idea.utils.FormatUtils.toFirstLetterLowerCase;

public class ServiceGenerator extends SpringWebInitializrCodeGenerator implements TemplateReader {

    private static final String RESOURCE_SERVICE_NAME_TEMPLATE = "%sResourceService";
    private static final String SERVICE_ANNOTATION_QN = "org.springframework.stereotype.Service";

    private PsiClass resourceRepository;

    public ServiceGenerator(SpringWebInitializrRequest springWebInitializrRequest, PsiClass resourceRepository) {
        super(springWebInitializrRequest);
        this.resourceRepository = resourceRepository;
    }


    @Override
    public PsiClass generate() {
        String fullPackagePath = getProjectRootManager().getContentRoots()[0].getPath().concat(DEFAULT_JAVA_SRC_PATH).concat(springWebInitializrRequest.getResourceServicePackage().replaceAll("\\.", "/"));
        VirtualFile vfPackage = createFolderIfNotExists(fullPackagePath);
        PsiDirectory pdPackage = getPsiManager().findDirectory(vfPackage);
        return createResourceService(pdPackage);
    }

    private PsiClass createResourceService(PsiDirectory psiDirectory) {
        String resourceServiceName = String.format(RESOURCE_SERVICE_NAME_TEMPLATE, springWebInitializrRequest.getResourceClass().getName());
        PsiJavaFile resourceServiceFile = createJavaFileInDirectoryWithPackage(psiDirectory, resourceServiceName, springWebInitializrRequest.getResourceServicePackage());

        PsiClass resourceService = getElementFactory().createClass(resourceServiceName);
        addQualifiedAnnotationNameTo(SERVICE_ANNOTATION_QN, resourceService);

        String resourceServiceQualifiedName = String.format("ore.spring.web.initializr.service.ResourceService<%s,%s,%s>",
                springWebInitializrRequest.getResourceClass().getQualifiedName(),
                springWebInitializrRequest.getResourceSearchFormClass().getQualifiedName(),
                springWebInitializrRequest.getResourceIdQualifiedName());
        addQualifiedExtendsToClass(resourceServiceQualifiedName, resourceService);

        PsiField resourceRepositoryElement = getElementFactory().createFieldFromText(String.format("private final %s.%s %s;", springWebInitializrRequest.getResourceRepositoryPackage(), resourceRepository.getName(), toFirstLetterLowerCase(Objects.requireNonNull(resourceRepository.getName()))), resourceService.getContext());

        List<PsiField> constructorArguments = Collections.singletonList(resourceRepositoryElement);

        PsiMethod constructor = extractConstructorForClass(resourceService, constructorArguments, constructorArguments, Collections.singletonList(resourceRepositoryElement.getNameIdentifier().getText()));
        PsiUtil.setModifierProperty(constructor, PsiModifier.PUBLIC, true);
        addAutowiredTo(constructor);
        resourceService.add(constructor);

        getJavaCodeStyleManager().shortenClassReferences(resourceService);
        resourceServiceFile.add(resourceService);

        return resourceService;
    }


}
