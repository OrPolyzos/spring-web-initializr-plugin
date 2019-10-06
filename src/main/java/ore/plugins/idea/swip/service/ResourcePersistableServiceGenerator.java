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

public class ResourcePersistableServiceGenerator extends SwipJavaCodeGenerator {

    private static final String SERVICE_NAME_TEMPLATE = "%sResourcePersistableService";
    private static final String SERVICE_ANNOTATION_QN = "org.springframework.stereotype.Service";

    private PsiClass resourcePersistableRepository;

    public ResourcePersistableServiceGenerator(SwipRequest swipRequest, PsiClass resourcePersistableRepository) {
        super(swipRequest);
        this.resourcePersistableRepository = resourcePersistableRepository;
    }


    @Override
    public PsiClass generateJavaClass() {
        String fullPackagePath = getProjectRootManager().getContentRoots()[0].getPath().concat(DEFAULT_JAVA_SRC_PATH).concat(swipRequest.getResourcePersistableServicePackage().replaceAll("\\.", "/"));
        VirtualFile vfPackage = createFolderIfNotExists(fullPackagePath);
        PsiDirectory pdPackage = getPsiManager().findDirectory(vfPackage);
        return createResourcePersistableService(pdPackage);
    }

    private PsiClass createResourcePersistableService(PsiDirectory psiDirectory) {
        String resourcePersistableServiceName = String.format(SERVICE_NAME_TEMPLATE, swipRequest.getResourcePersistableClass().getName());
        PsiJavaFile resourcePersistableServiceFile = createJavaFileInDirectoryWithPackage(psiDirectory, resourcePersistableServiceName, swipRequest.getResourcePersistableServicePackage());

        PsiClass resourcePersistableService = getElementFactory().createClass(resourcePersistableServiceName);
        addQualifiedAnnotationNameTo(SERVICE_ANNOTATION_QN, resourcePersistableService);

        String resourcePersistableServiceQualifiedName = String.format("ore.spring.web.initializr.service.ResourcePersistableService<%s,%s,%s>",
                swipRequest.getResourcePersistableClass().getQualifiedName(),
                swipRequest.getResourcePersistableSearchFormClass().getQualifiedName(),
                swipRequest.getResourcePersistableIdQualifiedName());
        addQualifiedExtendsToClass(resourcePersistableServiceQualifiedName, resourcePersistableService);

        String qualifiedResourcePersistableRepositoryName = String.format("org.springframework.data.repository.CrudRepository<%s,%s>",
                swipRequest.getResourcePersistableClass().getQualifiedName(), swipRequest.getResourcePersistableIdQualifiedName());

        PsiField resourcePersistableRepositoryElement = getElementFactory().createFieldFromText(String.format("private final %s %s;", qualifiedResourcePersistableRepositoryName, toFirstLetterLowerCase(Objects.requireNonNull(resourcePersistableRepository.getName()))), resourcePersistableService.getContext());

        List<PsiField> constructorArguments = Collections.singletonList(resourcePersistableRepositoryElement);

        PsiMethod constructor = extractConstructorForClass(resourcePersistableService, constructorArguments, constructorArguments, Collections.singletonList(resourcePersistableRepositoryElement.getNameIdentifier().getText()));
        PsiUtil.setModifierProperty(constructor, PsiModifier.PUBLIC, true);
        addAutowiredTo(constructor);
        resourcePersistableService.add(constructor);

        getJavaCodeStyleManager().shortenClassReferences(resourcePersistableService);
        resourcePersistableServiceFile.add(resourcePersistableService);

        return resourcePersistableService;
    }


}
