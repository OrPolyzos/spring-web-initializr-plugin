package ore.plugins.idea.swip.service;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiJavaFile;
import ore.plugins.idea.swip.model.SwipRequest;
import ore.plugins.idea.swip.service.base.SwipJavaCodeGenerator;

public class ResourcePersistableRepositoryGenerator extends SwipJavaCodeGenerator {

    private static final String REPOSITORY_NAME_TEMPLATE = "%sResourcePersistableRepository";
    private static final String REPOSITORY_ANNOTATION_QN = "org.springframework.stereotype.Repository";

    public ResourcePersistableRepositoryGenerator(SwipRequest swipRequest) {
        super(swipRequest);
    }

    @Override
    public PsiClass generateJavaClass() {
        String fullPackagePath = getProjectRootManager().getContentRoots()[0].getPath().concat(DEFAULT_JAVA_SRC_PATH).concat(swipRequest.getResourcePersistableRepositoryPackage().replaceAll("\\.", "/"));
        VirtualFile vfPackage = createFolderIfNotExists(fullPackagePath);
        PsiDirectory pdPackage = getPsiManager().findDirectory(vfPackage);
        return createResourcePersistableRepositoryInterface(pdPackage);
    }

    private PsiClass createResourcePersistableRepositoryInterface(PsiDirectory psiDirectory) {
        String resourcePersistableRepositoryName = String.format(REPOSITORY_NAME_TEMPLATE, swipRequest.getResourcePersistableClass().getName());
        PsiJavaFile resourcePersistableRepositoryFile = createJavaFileInDirectoryWithPackage(psiDirectory, resourcePersistableRepositoryName, swipRequest.getResourcePersistableRepositoryPackage());

        PsiClass resourcePersistableRepository = getElementFactory().createInterface(resourcePersistableRepositoryName);

        addQualifiedAnnotationNameTo(REPOSITORY_ANNOTATION_QN, resourcePersistableRepository);

        String crudRepositoryQn = String.format("org.springframework.data.repository.CrudRepository<%s,%s>",
                swipRequest.getResourcePersistableClass().getQualifiedName(),
                swipRequest.getResourcePersistableIdQualifiedName());

        addQualifiedExtendsToClass(crudRepositoryQn, resourcePersistableRepository);

        getJavaCodeStyleManager().shortenClassReferences(resourcePersistableRepository);
        resourcePersistableRepositoryFile.add(resourcePersistableRepository);
        return resourcePersistableRepository;
    }

}