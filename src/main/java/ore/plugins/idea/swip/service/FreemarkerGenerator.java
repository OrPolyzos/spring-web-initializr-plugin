package ore.plugins.idea.swip.service;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiType;
import ore.plugins.idea.lib.exception.InvalidStructureException;
import ore.plugins.idea.lib.exception.base.OrePluginRuntimeException;
import ore.plugins.idea.lib.service.base.OrePluginGenerator;
import ore.plugins.idea.swip.model.SwipRequest;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static ore.plugins.idea.lib.utils.FormatUtils.*;

public class FreemarkerGenerator extends OrePluginGenerator {

    private static final String BASE_RESOURCE_VIEW_TEMPLATE = "/templates/freemarker/base-resource-view";
    private static final String EDIT_RESOURCE_VIEW_TEMPLATE = "/templates/freemarker/edit-resource-view";
    private static final String RESOURCE_FORM_FIELD_TEMPLATE = "/templates/freemarker/section/resource-form-field";
    private static final String RESOURCE_TABLE_HEAD_FORM_FIELD_TEMPLATE = "/templates/freemarker/section/resource-table-head-field";
    private static final String RESOURCE_TABLE_BODY_FORM_FIELD_TEMPLATE = "/templates/freemarker/section/resource-table-body-field";
    private static final String CSS_STYLES_TEMPLATE = "/templates/freemarker/swip-styles.css";

    private SwipRequest swipRequest;

    private ControllerGenerator controllerGenerator;

    private String resourceSingular;
    private String resourcePlural;

    public FreemarkerGenerator(SwipRequest swipRequest, ControllerGenerator controllerGenerator) {
        super(swipRequest.getResourceClass());
        this.swipRequest = swipRequest;
        this.controllerGenerator = controllerGenerator;
        this.resourceSingular = toFirstLetterLowerCase(Objects.requireNonNull(swipRequest.getResourceClass().getName()));
        this.resourcePlural = toPlural(resourceSingular);

    }

    public void generateResources() {
        VirtualFile staticFolder = createBaseStructureTo(swipRequest.getResourceClass().getProject(), "/static");
        createCssStylesFile(staticFolder);

        VirtualFile templates = createBaseStructureTo(swipRequest.getResourceClass().getProject(), "/templates");
        String resourceFolderPath = String.format("%s/%s", templates.getPath(), resourceSingular);
        validateFileDoesNotExist(resourceFolderPath);

        createVirtualFileIfNotExists(swipRequest.getResourceClass().getProject(), resourceFolderPath, true);

        createBaseResourceView(templates);
        createEditResourceView(templates);
    }

    private void createCssStylesFile(VirtualFile staticFolder) {
        VirtualFile cssStylesFile = createVirtualFileIfNotExists(swipRequest.getResourceClass().getProject(), String.format("%s/swip-styles.css", staticFolder.getPath()), false);
        writeContentToFile(cssStylesFile, provideTemplateContent(CSS_STYLES_TEMPLATE));
    }


    private void createBaseResourceView(VirtualFile templates) {
        VirtualFile baseResourceView = createVirtualFileIfNotExists(swipRequest.getResourceClass().getProject(), String.format("%s/%s.ftl", templates.getPath(), controllerGenerator.getResourceViewPath()), false);
        List<PsiField> resourceFields = extractCandidateResourceFields(field -> !field.equals(swipRequest.getResourceIdPsiField()));

        String resourceFormContent = extractResourceFormContent(resourceFields);

        String resourceTableHeadContent = resourceFields
                .stream()
                .map(field -> provideTemplateContent(RESOURCE_TABLE_HEAD_FORM_FIELD_TEMPLATE)
                        .replace(extractTemplateReplacementValue("resourceFieldNameUpperCase"), toFirstLetterUpperCase(field.getNameIdentifier().getText())))
                .collect(Collectors.joining(""));

        String resourceTableBodyContent = resourceFields
                .stream()
                .map(field -> provideTemplateContent(RESOURCE_TABLE_BODY_FORM_FIELD_TEMPLATE)
                        .replace(extractTemplateReplacementValue("resourceFieldNameLowerCase"), field.getNameIdentifier().getText()))
                .collect(Collectors.joining(""));

        String baseResourceViewFtl = provideTemplateContent(BASE_RESOURCE_VIEW_TEMPLATE)
                .replaceAll(extractTemplateReplacementValue("baseResourceViewTitle"), toFirstLetterUpperCase(resourcePlural))
                .replaceAll(extractTemplateReplacementValue("resourceBaseUri"), controllerGenerator.getResourceBaseUri())
                .replaceAll(extractTemplateReplacementValue("resourceForm"), controllerGenerator.getResourceFormHolder())
                .replaceAll(extractTemplateReplacementValue("resourceSearchForm"), controllerGenerator.getResourceSearchFormHolder())
                .replaceAll(extractTemplateReplacementValue("resourceList"), controllerGenerator.getResourceListHolder())
                .replace(extractTemplateReplacementValue("resourceFormContent"), resourceFormContent)
                .replace(extractTemplateReplacementValue("resourceTableHeadContent"), resourceTableHeadContent)
                .replace(extractTemplateReplacementValue("resourceTableBodyContent"), resourceTableBodyContent);

        writeContentToFile(baseResourceView, baseResourceViewFtl);
    }

    private void createEditResourceView(VirtualFile templates) {
        VirtualFile editResourceView = createVirtualFileIfNotExists(swipRequest.getResourceClass().getProject(), String.format("%s/%s.ftl", templates.getPath(), controllerGenerator.getEditResourceViewPath()), false);
        List<PsiField> resourceFields = extractCandidateResourceFields(field -> !field.equals(swipRequest.getResourceIdPsiField()));
        String resourceFormContent = extractResourceFormContent(resourceFields);
        String editResourceViewContent = provideTemplateContent(EDIT_RESOURCE_VIEW_TEMPLATE)
                .replaceAll(extractTemplateReplacementValue("editResourceViewTitle"), String.format("Edit %s", toFirstLetterUpperCase(resourceSingular)))
                .replaceAll(extractTemplateReplacementValue("resourceBaseUri"), controllerGenerator.getResourceBaseUri())
                .replaceAll(extractTemplateReplacementValue("resourceForm"), controllerGenerator.getResourceFormHolder())
                .replace(extractTemplateReplacementValue("resourceFormContent"), resourceFormContent);

        writeContentToFile(editResourceView, editResourceViewContent);
    }

    private String extractResourceFormContent(List<PsiField> resourceFields) {
        return resourceFields
                .stream()
                .filter(field -> !field.equals(swipRequest.getResourceIdPsiField()))
                .map(field -> {
                    String resourceFieldTemplate = provideTemplateContent(RESOURCE_FORM_FIELD_TEMPLATE);
                    return resourceFieldTemplate
                            .replaceAll(extractTemplateReplacementValue("resourceFieldNameLowerCase"), toFirstLetterLowerCase(field.getNameIdentifier().getText()))
                            .replaceAll(extractTemplateReplacementValue("resourceFieldNameUpperCase"), toFirstLetterUpperCase(field.getNameIdentifier().getText()))
                            .replaceAll(extractTemplateReplacementValue("resourceFieldName"), field.getNameIdentifier().getText())
                            .replaceAll(extractTemplateReplacementValue("resourceForm"), controllerGenerator.getResourceFormHolder());
                }).collect(Collectors.joining(""));
    }

    @NotNull
    private List<PsiField> extractCandidateResourceFields(Predicate<PsiField> predicate) {
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
                })
                .filter(predicate)
                .collect(Collectors.toList());
    }

    private VirtualFile createBaseStructureTo(Project project, String afterResources) {
        VirtualFile root = ProjectRootManager.getInstance(project).getContentRoots()[0];
        VirtualFile src = createVirtualFileIfNotExists(project, root.getPath() + "/src", true);
        VirtualFile main = createVirtualFileIfNotExists(project, src.getPath() + "/main", true);
        VirtualFile resources = createVirtualFileIfNotExists(project, main.getPath() + "/resources", true);
        return createVirtualFileIfNotExists(project, resources.getPath() + afterResources, true);
    }

    private void validateFileDoesNotExist(String path) {
        if (new File(path).exists()) {
            throw new InvalidStructureException(String.format("There is already a directory '%s'. Skipping freemarker resources creation...", path));
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


    private void writeContentToFile(VirtualFile virtualFile, String content) {
        try {
            Files.write(Paths.get(virtualFile.getPath()), content.getBytes());
        } catch (IOException e) {
            throw new OrePluginRuntimeException(String.format("Failed to write to file. Message %s: ", e.getMessage()));
        }
    }

}
