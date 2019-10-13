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

    private static final String BASE_VIEW_TEMPLATE = "/templates/freemarker/base-resource-view";
    private static final String EDIT_VIEW_TEMPLATE = "/templates/freemarker/edit-resource-view";
    private static final String FORM_FIELD_TEMPLATE = "/templates/freemarker/section/resource-form-field";
    private static final String THEAD_FIELD_TEMPLATE = "/templates/freemarker/section/resource-table-head-field";
    private static final String TBODY_FIELD_TEMPLATE = "/templates/freemarker/section/resource-table-body-field";
    private static final String CSS_STYLES_TEMPLATE = "/templates/freemarker/swip-styles.css";

    private SwipRequest swipRequest;

    private ResourcePersistableControllerGenerator controllerGenerator;

    private String resourcePersistableSingular;
    private String resourcePersistablePlural;

    public FreemarkerGenerator(SwipRequest swipRequest, ResourcePersistableControllerGenerator controllerGenerator) {
        super(swipRequest.getResourcePersistableClass());
        this.swipRequest = swipRequest;
        this.controllerGenerator = controllerGenerator;
        this.resourcePersistableSingular = toFirstLetterLowerCase(Objects.requireNonNull(swipRequest.getResourcePersistableClass().getName()));
        this.resourcePersistablePlural = toPlural(resourcePersistableSingular);

    }

    public void generateResources() {
        VirtualFile staticFolder = createBaseStructureTo(swipRequest.getResourcePersistableClass().getProject(), "/static");
        createCssStylesFile(staticFolder);

        VirtualFile templates = createBaseStructureTo(swipRequest.getResourcePersistableClass().getProject(), "/templates");
        String resourceFolderPath = String.format("%s/%s", templates.getPath(), resourcePersistableSingular);
        validateFileDoesNotExist(resourceFolderPath);

        createVirtualFileIfNotExists(swipRequest.getResourcePersistableClass().getProject(), resourceFolderPath, true);

        createBaseResourceView(templates);
        createEditResourceView(templates);
    }

    private void createCssStylesFile(VirtualFile staticFolder) {
        VirtualFile cssStylesFile = createVirtualFileIfNotExists(swipRequest.getResourcePersistableClass().getProject(), String.format("%s/swip-styles.css", staticFolder.getPath()), false);
        writeContentToFile(cssStylesFile, provideTemplateContent(CSS_STYLES_TEMPLATE));
    }


    private void createBaseResourceView(VirtualFile templates) {
        VirtualFile baseResourceView = createVirtualFileIfNotExists(swipRequest.getResourcePersistableClass().getProject(), String.format("%s/%s.ftl", templates.getPath(), controllerGenerator.getResourcePersistableBaseViewPath()), false);
        List<PsiField> resourceFields = extractCandidateResourceFields(field -> !field.equals(swipRequest.getResourcePersistableIdField()));

        String resourcePersistableFormContent = extractresourcePersistableFormContent(resourceFields);

        String resourcePersistableTHeadContent = resourceFields
                .stream()
                .map(field -> provideTemplateContent(THEAD_FIELD_TEMPLATE)
                        .replace(extractTemplateReplacementValue("resourcePersistableFieldNameUpperCase"), toFirstLetterUpperCase(field.getNameIdentifier().getText())))
                .collect(Collectors.joining(""));

        String resourcePersistableTBodyContent = resourceFields
                .stream()
                .map(field -> provideTemplateContent(TBODY_FIELD_TEMPLATE)
                        .replace(extractTemplateReplacementValue("resourcePersistableFieldNameLowerCase"), field.getNameIdentifier().getText()))
                .collect(Collectors.joining(""));

        String baseResourceViewFtl = provideTemplateContent(BASE_VIEW_TEMPLATE)
                .replaceAll(extractTemplateReplacementValue("resourcePersistableBaseViewTitle"), toFirstLetterUpperCase(resourcePersistablePlural))
                .replaceAll(extractTemplateReplacementValue("resourcePersistableBaseUri"), controllerGenerator.getResourcePersistableBaseUri())
                .replaceAll(extractTemplateReplacementValue("resourcePersistableForm"), controllerGenerator.getResourcePersistableFormHolder())
                .replaceAll(extractTemplateReplacementValue("resourcePersistableSearchForm"), controllerGenerator.getResourcePersistableSearchFormHolder())
                .replaceAll(extractTemplateReplacementValue("resourcePersistableList"), controllerGenerator.getResourcePersistableListHolder())
                .replace(extractTemplateReplacementValue("resourcePersistableFormContent"), resourcePersistableFormContent)
                .replace(extractTemplateReplacementValue("resourcePersistableTHeadContent"), resourcePersistableTHeadContent)
                .replace(extractTemplateReplacementValue("resourcePersistableTBodyContent"), resourcePersistableTBodyContent);

        writeContentToFile(baseResourceView, baseResourceViewFtl);
    }

    private void createEditResourceView(VirtualFile templates) {
        VirtualFile editResourceView = createVirtualFileIfNotExists(swipRequest.getResourcePersistableClass().getProject(), String.format("%s/%s.ftl", templates.getPath(), controllerGenerator.getResourcePersistableEditViewPath()), false);
        List<PsiField> resourceFields = extractCandidateResourceFields(field -> !field.equals(swipRequest.getResourcePersistableIdField()));
        String resourcePersistableFormContent = extractresourcePersistableFormContent(resourceFields);
        String editResourceViewContent = provideTemplateContent(EDIT_VIEW_TEMPLATE)
                .replaceAll(extractTemplateReplacementValue("editResourcePersistableViewTitle"), String.format("Edit %s", toFirstLetterUpperCase(resourcePersistableSingular)))
                .replaceAll(extractTemplateReplacementValue("resourcePersistableBaseUri"), controllerGenerator.getResourcePersistableBaseUri())
                .replaceAll(extractTemplateReplacementValue("resourcePersistableForm"), controllerGenerator.getResourcePersistableFormHolder())
                .replace(extractTemplateReplacementValue("resourcePersistableFormContent"), resourcePersistableFormContent);

        writeContentToFile(editResourceView, editResourceViewContent);
    }

    private String extractresourcePersistableFormContent(List<PsiField> resourceFields) {
        return resourceFields
                .stream()
                .filter(field -> !field.equals(swipRequest.getResourcePersistableIdField()))
                .map(field -> {
                    String resourceFieldTemplate = provideTemplateContent(FORM_FIELD_TEMPLATE);
                    return resourceFieldTemplate
                            .replaceAll(extractTemplateReplacementValue("resourcePersistableFieldNameLowerCase"), toFirstLetterLowerCase(field.getNameIdentifier().getText()))
                            .replaceAll(extractTemplateReplacementValue("resourcePersistableFieldNameUpperCase"), toFirstLetterUpperCase(field.getNameIdentifier().getText()))
                            .replaceAll(extractTemplateReplacementValue("resourcePersistableFieldName"), field.getNameIdentifier().getText())
                            .replaceAll(extractTemplateReplacementValue("resourcePersistableForm"), controllerGenerator.getResourcePersistableFormHolder());
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
            throw new InvalidStructureException(String.format("There is already a directory '%s'. Skipping template resources creation...", path));
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
