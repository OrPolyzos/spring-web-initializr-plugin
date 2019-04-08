package ore.plugins.idea.spring.web.initializr.generator;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import ore.plugins.idea.exception.validation.InvalidStructureException;
import ore.plugins.idea.spring.web.initializr.generator.base.CodeGenerator;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ore.plugins.idea.utils.FormatUtils.*;

public class FreemarkerGenerator extends CodeGenerator {

    private static final String BASE_RESOURCE_VIEW_TEMPLATE = "/templates/freemarker/base-resource-view";
    private static final String RESOURCE_FORM_FIELD_TEMPLATE = "/templates/freemarker/section/resource-form-field";
    private static final String RESOURCE_TABLE_HEAD_FORM_FIELD_TEMPLATE = "/templates/freemarker/section/resource-table-head-field";
    private static final String RESOURCE_TABLE_BODY_FORM_FIELD_TEMPLATE = "/templates/freemarker/section/resource-table-body-field";
    private static final String CSS_STYLES_TEMPLATE = "/templates/freemarker/spring-web-initializr-plugin-styles.css";

    private ControllerGenerator controllerGenerator;

    private String resourceSingular;
    private String resourcePlural;

    private PsiClass resourceForm;
    private PsiClass resourceSearchForm;

    public FreemarkerGenerator(PsiClass psiClass, ControllerGenerator controllerGenerator) {
        super(psiClass, psiClass.getProject());
        this.controllerGenerator = controllerGenerator;
        this.resourceSingular = toFirstLetterLowerCase(Objects.requireNonNull(psiClass.getName()));
        this.resourcePlural = toPlural(resourceSingular);

    }

    @Override
    public PsiClass generate() {
        VirtualFile staticFolder = createBaseStructureTo(psiClass.getProject(), "/static");
        createCssStylesFile(staticFolder);

        VirtualFile templates = createBaseStructureTo(psiClass.getProject(), "/templates");

        String resourceFolderPath = String.format("%s/%s", templates.getPath(), resourceSingular);
        validateFileDoesNotExist(psiClass.getProject(), resourceFolderPath);

        VirtualFile resourceFolder = createVirtualFileIfNotExists(psiClass.getProject(), resourceFolderPath, true);
        VirtualFile editResourceView = createVirtualFileIfNotExists(psiClass.getProject(), String.format("%s/%s.ftl", templates.getPath(), controllerGenerator.getEditResourceViewPath()), false);

        createBaseResourceView(templates);
        //createEditResourceView(templates);
        return null;
    }

    private void createCssStylesFile(VirtualFile staticFolder) {
        VirtualFile cssStylesFile = createVirtualFileIfNotExists(psiClass.getProject(), String.format("%s/spring-web-initializr-plugin-styles.css", staticFolder.getPath()), false);
        try {
            Files.write(Paths.get(cssStylesFile.getPath()), getTemplate(CSS_STYLES_TEMPLATE).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void createBaseResourceView(VirtualFile templates) {
        VirtualFile baseResourceView = createVirtualFileIfNotExists(psiClass.getProject(), String.format("%s/%s.ftl", templates.getPath(), controllerGenerator.getResourceViewPath()), false);

        List<PsiField> resourceFields = extractCandidateResourceFields();

        String resourceFormContent = resourceFields
                .stream()
                .map(field -> {
                    String resourceFieldTemplate = getTemplate(RESOURCE_FORM_FIELD_TEMPLATE);
                    return resourceFieldTemplate
                            .replaceAll(getReplacementString("resourceFieldNameLowerCase"), toFirstLetterLowerCase(field.getNameIdentifier().getText()))
                            .replaceAll(getReplacementString("resourceFieldNameUpperCase"), toFirstLetterUpperCase(field.getNameIdentifier().getText()))
                            .replaceAll(getReplacementString("resourceFieldName"), field.getNameIdentifier().getText())
                            .replaceAll(getReplacementString("resourceForm"), controllerGenerator.getResourceFormHolder());
                }).collect(Collectors.joining(""));

        String resourceTableHeadContent = resourceFields
                .stream()
                .map(field -> getTemplate(RESOURCE_TABLE_HEAD_FORM_FIELD_TEMPLATE)
                        .replace(getReplacementString("resourceFieldNameUpperCase"), toFirstLetterUpperCase(field.getNameIdentifier().getText())))
                .collect(Collectors.joining(""));

        String resourceTableBodyContent = resourceFields
                .stream()
                .map(field -> getTemplate(RESOURCE_TABLE_BODY_FORM_FIELD_TEMPLATE)
                        .replace(getReplacementString("resourceFieldNameLowerCase"), field.getNameIdentifier().getText()))
                .collect(Collectors.joining(""));

        String baseResourceViewFtl = getTemplate(BASE_RESOURCE_VIEW_TEMPLATE)
                .replaceAll(getReplacementString("baseResourceViewTitle"), toFirstLetterUpperCase(resourcePlural))
                .replaceAll(getReplacementString("resourceBaseUri"), controllerGenerator.getResourceBaseUri())
                .replaceAll(getReplacementString("resourceForm"), controllerGenerator.getResourceFormHolder())
                .replaceAll(getReplacementString("resourceSearchForm"), controllerGenerator.getResourceSearchFormHolder())
                .replaceAll(getReplacementString("resourceList"), controllerGenerator.getResourceListHolder())
                .replace(getReplacementString("resourceFormContent"), resourceFormContent)
                .replace(getReplacementString("resourceTableHeadContent"), resourceTableHeadContent)
                .replace(getReplacementString("resourceTableBodyContent"), resourceTableBodyContent);
        try {
            Files.write(Paths.get(baseResourceView.getPath()), baseResourceViewFtl.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NotNull
    private List<PsiField> extractCandidateResourceFields() {
        return Arrays.stream(psiClass.getFields())
                .filter(field -> {
                    if (field.getType() instanceof PsiPrimitiveType && !field.getType().equals(PsiType.VOID)) {
                        return true;
                    }
                    if (field.getType() instanceof PsiClassType) {
                        switch (field.getType().getPresentableText()) {
                            case "String":
                            case "Integer":
                            case "Long":
                            case "Double":
                            case "Float":
                            case "Short":
                            case "Character":
                            case "Boolean":
                                return true;
                            default:
                                return false;
                        }
                    }
                    return false;
                }).collect(Collectors.toList());
    }

    private VirtualFile createBaseStructureTo(Project project, String afterResources) {
        VirtualFile root = ProjectRootManager.getInstance(project).getContentRoots()[0];
        VirtualFile src = createVirtualFileIfNotExists(project, root.getPath() + "/src", true);
        VirtualFile main = createVirtualFileIfNotExists(project, src.getPath() + "/main", true);
        VirtualFile resources = createVirtualFileIfNotExists(project, main.getPath() + "/resources", true);
        return createVirtualFileIfNotExists(project, resources.getPath() + afterResources, true);
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

}
