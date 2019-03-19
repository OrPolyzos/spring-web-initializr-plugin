package ore.plugins.idea.spring.web.initializr.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import ore.plugins.idea.action.OrePluginAction;
import ore.plugins.idea.exception.validation.InvalidFileException;
import ore.plugins.idea.exception.validation.InvalidStructureException;
import ore.utils.initializrs.spring.web.initializr.base.domain.ResourcePersistable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class FreemarkerAction extends OrePluginAction {

    @Override
    public void safeActionPerformed(AnActionEvent anActionEvent) {
        PsiClass psiClass = extractPsiClass(anActionEvent);
        // TODO Currently only supporting "src/main/java/resources"
        VirtualFile templates = createBaseStructureToTemplates(psiClass.getProject());
        String resourceName = Objects.requireNonNull(psiClass.getName()).toLowerCase();

        String resourceFolderPath = templates.getPath() + "/" + resourceName;
        validateFileDoesNotExist(psiClass.getProject(), resourceFolderPath);

        VirtualFile resourceFolder = createVirtualFileIfNotExists(psiClass.getProject(), templates.getPath() + "/" + resourceName, true);
        VirtualFile sectionFolder = createVirtualFileIfNotExists(psiClass.getProject(), resourceFolder.getPath() + "/section", true);

        VirtualFile baseResourceView = createVirtualFileIfNotExists(psiClass.getProject(), resourceFolder.getPath() + "/" + poorMansPlural(resourceName) + ".ftl", false);
        VirtualFile editResourceView = createVirtualFileIfNotExists(psiClass.getProject(), resourceFolder.getPath() + "/edit-" + resourceName + ".ftl", false);
        VirtualFile createResourceViewSection = createVirtualFileIfNotExists(psiClass.getProject(), sectionFolder.getPath() + "/create-" + resourceName + "-section.ftl", false);
        VirtualFile searchResourceViewSection = createVirtualFileIfNotExists(psiClass.getProject(), sectionFolder.getPath() + "/search-" + resourceName + "-section.ftl", false);
        VirtualFile tableResourceViewSection = createVirtualFileIfNotExists(psiClass.getProject(), sectionFolder.getPath() + "/table-" + resourceName + "-section.ftl", false);
    }


    private VirtualFile createBaseStructureToTemplates(Project project) {
        VirtualFile root = ProjectRootManager.getInstance(project).getContentRoots()[0];
        VirtualFile src = createVirtualFileIfNotExists(project, root.getPath() + "/src", true);
        VirtualFile main = createVirtualFileIfNotExists(project, src.getPath() + "/main", true);
        VirtualFile resources = createVirtualFileIfNotExists(project, main.getPath() + "/resources", true);
        return createVirtualFileIfNotExists(project, resources.getPath() + "/custom-templates", true);
    }

    private void validateFileDoesNotExist(Project project, String path) {
        if (new File(path).exists()) {
            Messages.showMessageDialog(project, String.format("There is already a directory '%s'. Aborting.", path), "Error", Messages.getErrorIcon());
            throw new InvalidStructureException();
        }
    }

    private VirtualFile createVirtualFileIfNotExists(Project project, String path, boolean isDirectory) {
        File file = new File(path);
        if (!file.exists()) {
            if (isDirectory) {
                file.mkdir();
            } else {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    Messages.showMessageDialog(project, String.format("Could not create '%s'. Aborting.", path), "Error", Messages.getErrorIcon());
                    throw new InvalidStructureException();
                }
            }
        }
        return LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
    }

    private String poorMansPlural(String singular) {
        return singular + "s";
    }

    private VirtualFile getVirtualFolder(VirtualFile virtualFolder, String folderName) {
        return Arrays.stream(virtualFolder.getChildren()).filter(child -> child.getName().equals(folderName)).findFirst().orElseThrow(InvalidStructureException::new);
    }

    @Override
    public void update(@NotNull AnActionEvent anActionEvent) {
        safeExecute(() -> {
            super.update(anActionEvent);
            PsiClass psiClass = extractPsiClass(anActionEvent);
            // TODO Currently only supporting access from ResourcePersistable
            if (psiClass.getImplementsList() == null || Arrays.stream(psiClass.getImplementsList().getReferencedTypes())
                    .noneMatch(refType -> refType.getName().equals(ResourcePersistable.class.getSimpleName()))) {
                throw new InvalidFileException();
            }
        }, anActionEvent, LOGGER);
    }

}
