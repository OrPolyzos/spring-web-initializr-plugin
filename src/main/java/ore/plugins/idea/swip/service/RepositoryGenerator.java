package ore.plugins.idea.swip.service;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiJavaFile;
import ore.plugins.idea.swip.service.base.SwipJavaCodeGenerator;
import ore.plugins.idea.swip.model.SwipRequest;

public class RepositoryGenerator extends SwipJavaCodeGenerator {

    private static final String RESOURCE_REPOSITORY_NAME_TEMPLATE = "%sResourceRepository";
    private static final String REPOSITORY_ANNOTATION_QN = "org.springframework.stereotype.Repository";

    public RepositoryGenerator(SwipRequest swipRequest) {
        super(swipRequest);
    }

    @Override
    public PsiClass generateJavaClass() {
        String fullPackagePath = getProjectRootManager().getContentRoots()[0].getPath().concat(DEFAULT_JAVA_SRC_PATH).concat(swipRequest.getResourceRepositoryPackage().replaceAll("\\.", "/"));
        VirtualFile vfPackage = createFolderIfNotExists(fullPackagePath);
        PsiDirectory pdPackage = getPsiManager().findDirectory(vfPackage);
        return createRepositoryInterface(pdPackage);
    }

    private PsiClass createRepositoryInterface(PsiDirectory psiDirectory) {
        String resourceRepositoryName = String.format(RESOURCE_REPOSITORY_NAME_TEMPLATE, swipRequest.getResourceClass().getName());
        PsiJavaFile resourceRepositoryFile = createJavaFileInDirectoryWithPackage(psiDirectory, resourceRepositoryName, swipRequest.getResourceRepositoryPackage());

        PsiClass resourceRepository = getElementFactory().createInterface(resourceRepositoryName);

        addQualifiedAnnotationNameTo(REPOSITORY_ANNOTATION_QN, resourceRepository);

        String crudRepositoryQn = String.format("org.springframework.data.repository.CrudRepository<%s,%s>",
                swipRequest.getResourceClass().getQualifiedName(),
                swipRequest.getResourceIdQualifiedName());

        addQualifiedExtendsToClass(crudRepositoryQn, resourceRepository);

        getJavaCodeStyleManager().shortenClassReferences(resourceRepository);
        resourceRepositoryFile.add(resourceRepository);
        return resourceRepository;
    }

}