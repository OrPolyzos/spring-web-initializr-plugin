package ore.plugins.idea.spring.web.initializr.generator.repository;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import ore.plugins.idea.base.functionality.TemplateReader;
import ore.plugins.idea.spring.web.initializr.generator.base.CodeGenerator;

import java.util.Objects;

public class RepositoryGenerator extends CodeGenerator implements TemplateReader {


    private static final String DEFAULT_JAVA_SRC_PATH = "/src/main/java/";

    private static final String RESOURCE_REPOSITORY_NAME_TEMPLATE = "%sResourceRepository";
    private static final String CRUD_REPOSITORY_QN = "org.springframework.data.repository.CrudRepository";
    private static final String REPOSITORY_ANNOTATION_QN = "org.springframework.stereotype.Repository";
    private static final String DEFAULT_RESOURCE_ID_TYPE = "java.lang.Long";

    private String packagePath;

    public RepositoryGenerator(PsiClass psiClass, String packagePath) {
        super(psiClass, psiClass.getProject());
        this.packagePath = packagePath;
    }

    @Override
    public void generate() {
        String fullPackagePath = getProjectRootManager().getContentRoots()[0].getPath().concat(DEFAULT_JAVA_SRC_PATH).concat(packagePath.replaceAll("\\.", "/"));
        VirtualFile vfPackage = createFolderIfNotExists(fullPackagePath);
        PsiDirectory pdPackage = getPsiManager().findDirectory(vfPackage);
        createRepositoryInterface(pdPackage);
    }

    private void createRepositoryInterface(PsiDirectory psiDirectory) {
        String resourceRepositoryName = String.format(RESOURCE_REPOSITORY_NAME_TEMPLATE, psiClass.getName());
        PsiJavaFile resourceRepositoryFile = createJavaFileInDirectory(psiDirectory, resourceRepositoryName);


        if (packagePath.length() > 0) {
            PsiPackageStatement packageStatement = getElementFactory().createPackageStatement(packagePath);
            resourceRepositoryFile.addAfter(packageStatement, null);
        }

        getJavaCodeStyleManager().addImport(resourceRepositoryFile, getClassFromQualifiedName(psiClass.getQualifiedName()));
        getJavaCodeStyleManager().addImport(resourceRepositoryFile, getClassFromQualifiedName(CRUD_REPOSITORY_QN));
        getJavaCodeStyleManager().addImport(resourceRepositoryFile, getClassFromQualifiedName(REPOSITORY_ANNOTATION_QN));

        PsiClass repositoryInterface = getElementFactory().createInterface(resourceRepositoryName);

        PsiModifierList modifierList = repositoryInterface.getModifierList();
        if (modifierList != null) {
            PsiAnnotation annotation = modifierList.addAnnotation(REPOSITORY_ANNOTATION_QN);
            getJavaCodeStyleManager().shortenClassReferences(annotation);
        }

        String gType = Objects.requireNonNull(psiClass.getImplementsList()).getReferencedTypes()[1].getParameters()[0].getCanonicalText();
        PsiJavaCodeReferenceElement psiJavaCodeReferenceElement = getElementFactory().createReferenceFromText(String.format("CrudRepository<%s,%s>", psiClass.getName(), gType), repositoryInterface);
        getJavaCodeStyleManager().shortenClassReferences(psiJavaCodeReferenceElement);

        PsiReferenceList extendsList = repositoryInterface.getExtendsList();
        if (extendsList != null) {
            extendsList.add(psiJavaCodeReferenceElement);
        }

        resourceRepositoryFile.add(repositoryInterface);
    }


}