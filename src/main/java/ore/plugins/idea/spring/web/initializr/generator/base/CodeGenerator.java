package ore.plugins.idea.spring.web.initializr.generator.base;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.search.GlobalSearchScope;
import ore.plugins.idea.base.functionality.TemplateReader;
import ore.plugins.idea.exception.OrePluginRuntimeException;

import java.io.File;

public abstract class CodeGenerator implements TemplateReader {

    protected PsiClass psiClass;
    protected Project project;

    public CodeGenerator(PsiClass psiClass, Project project) {
        this.psiClass = psiClass;
        this.project = project;
    }

    public abstract void generate();

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
