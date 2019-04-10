package ore.plugins.idea.spring.web.initializr.generator.base;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.search.GlobalSearchScope;
import ore.plugins.idea.base.functionality.ConstructorProvider;
import ore.plugins.idea.base.functionality.TemplateReader;
import ore.plugins.idea.exception.OrePluginRuntimeException;

import java.io.File;
import java.util.Objects;

public abstract class CodeGenerator implements TemplateReader, ConstructorProvider {

    protected static final String DEFAULT_JAVA_SRC_PATH = "/src/main/java/";
    protected PsiClass psiClass;
    protected Project project;

    public CodeGenerator(PsiClass psiClass, Project project) {
        this.psiClass = psiClass;
        this.project = project;
    }

    public abstract PsiClass generate() throws Exception;

    protected VirtualFile createFolderIfNotExists(String path) {
        File packageFile = new File(path);
        if (!packageFile.exists() && !packageFile.mkdirs()) {
            throw new OrePluginRuntimeException(String.format("Failed to generate package at '%s'", packageFile));
        }
        return LocalFileSystem.getInstance().refreshAndFindFileByIoFile(packageFile);
    }

    protected PsiJavaFile createJavaFileInDirectory(PsiDirectory psiDirectory, String resourceRepositoryName) {
        return (PsiJavaFile) psiDirectory.createFile(resourceRepositoryName.concat(".java"));
    }

    protected PsiJavaFile createJavaFileInDirectoryWithPackage(PsiDirectory psiDirectory, String resourceServiceName, String fullPackagePath) {
        PsiJavaFile resourceServiceFile = createJavaFileInDirectory(psiDirectory, resourceServiceName);

        if (fullPackagePath.length() > 0) {
            PsiPackageStatement packageStatement = getElementFactory().createPackageStatement(fullPackagePath);
            resourceServiceFile.addAfter(packageStatement, null);
        }
        return resourceServiceFile;
    }

    protected void addQualifiedAnnotationNameTo(String qualifiedAnnotationName, PsiMember psiMember) {
        Objects.requireNonNull(psiMember.getModifierList()).addAnnotation(qualifiedAnnotationName);
    }

    protected void addOverrideTo(PsiMethod resourceFieldGetter) {
        addQualifiedAnnotationNameTo("java.lang.Override", resourceFieldGetter);
    }

    protected void addQualifiedExtendsToClass(String qualifiedExtendsName, PsiClass psiClass) {
        PsiJavaCodeReferenceElement psiJavaCodeReferenceElement = getElementFactory().createReferenceFromText(qualifiedExtendsName, psiClass);
        PsiReferenceList extendsList = psiClass.getExtendsList();
        Objects.requireNonNull(extendsList).add(psiJavaCodeReferenceElement);
    }

    protected void addQualifiedImplementsToClass(String qualifiedImplementsName, PsiClass psiClass) {
        PsiJavaCodeReferenceElement psiJavaCodeReferenceElement = getElementFactory().createReferenceFromText(qualifiedImplementsName, psiClass);
        PsiReferenceList implementsList = psiClass.getImplementsList();
        Objects.requireNonNull(implementsList).add(psiJavaCodeReferenceElement);
    }

    protected ProjectRootManager getProjectRootManager() {
        return ProjectRootManager.getInstance(project);
    }

    protected PsiManager getPsiManager() {
        return PsiManager.getInstance(project);
    }

    protected JavaCodeStyleManager getJavaCodeStyleManager() {
        return JavaCodeStyleManager.getInstance(project);
    }

    protected JavaPsiFacade getJavaPsiFacade() {
        return JavaPsiFacade.getInstance(project);
    }

    protected PsiElementFactory getElementFactory() {
        return JavaPsiFacade.getInstance(project).getElementFactory();
    }

    protected PsiClass getClassFromQualifiedName(String qualifiedName) {
        return JavaPsiFacade.getInstance(project).findClass(qualifiedName, GlobalSearchScope.allScope(project));
    }
}
