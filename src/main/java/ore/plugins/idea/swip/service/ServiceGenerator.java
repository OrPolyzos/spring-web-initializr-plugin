package ore.plugins.idea.swip.service;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import ore.plugins.idea.swip.model.SwipRequest;
import ore.plugins.idea.swip.service.base.SwipJavaCodeGenerator;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static ore.plugins.idea.lib.utils.FormatUtils.toFirstLetterLowerCase;

public class ServiceGenerator extends SwipJavaCodeGenerator {

    private static final String RESOURCE_SERVICE_NAME_TEMPLATE = "%sResourceService";
    private static final String SERVICE_ANNOTATION_QN = "org.springframework.stereotype.Service";

    private PsiClass resourceRepository;

    public ServiceGenerator(SwipRequest swipRequest, PsiClass resourceRepository) {
        super(swipRequest);
        this.resourceRepository = resourceRepository;
    }


    @Override
    public PsiClass generateJavaClass() {
        String fullPackagePath = getProjectRootManager().getContentRoots()[0].getPath().concat(DEFAULT_JAVA_SRC_PATH).concat(swipRequest.getResourceServicePackage().replaceAll("\\.", "/"));
        VirtualFile vfPackage = createFolderIfNotExists(fullPackagePath);
        PsiDirectory pdPackage = getPsiManager().findDirectory(vfPackage);
        return createResourceService(pdPackage);
    }

    private PsiClass createResourceService(PsiDirectory psiDirectory) {
        String resourceServiceName = String.format(RESOURCE_SERVICE_NAME_TEMPLATE, swipRequest.getResourceClass().getName());
        PsiJavaFile resourceServiceFile = createJavaFileInDirectoryWithPackage(psiDirectory, resourceServiceName, swipRequest.getResourceServicePackage());

        PsiClass resourceService = getElementFactory().createClass(resourceServiceName);
        addQualifiedAnnotationNameTo(SERVICE_ANNOTATION_QN, resourceService);

        String resourceServiceQualifiedName = String.format("ore.spring.web.initializr.service.ResourceService<%s,%s,%s>",
                swipRequest.getResourceClass().getQualifiedName(),
                swipRequest.getResourceSearchFormClass().getQualifiedName(),
                swipRequest.getResourceIdQualifiedName());
        addQualifiedExtendsToClass(resourceServiceQualifiedName, resourceService);

        String qualifiedResourceRepositoryName = String.format("org.springframework.data.repository.CrudRepository<%s,%s>",
                swipRequest.getResourceClass().getQualifiedName(), swipRequest.getResourceIdQualifiedName());

        PsiField resourceRepositoryElement = getElementFactory().createFieldFromText(String.format("private final %s %s;", qualifiedResourceRepositoryName, toFirstLetterLowerCase(Objects.requireNonNull(resourceRepository.getName()))), resourceService.getContext());

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
