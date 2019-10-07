package ore.plugins.idea.swip.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifier;
import ore.plugins.idea.lib.action.OrePluginAction;
import ore.plugins.idea.lib.dialog.SelectStuffDialog;
import ore.plugins.idea.lib.exception.CancelException;
import ore.plugins.idea.lib.exception.InvalidFileException;
import ore.plugins.idea.lib.exception.ValidationException;
import ore.plugins.idea.lib.exception.base.OrePluginRuntimeException;
import ore.plugins.idea.lib.model.pom.PsiPom;
import ore.plugins.idea.lib.model.ui.NameListCelRenderer;
import ore.plugins.idea.swip.dialog.PackageInputDialog;
import ore.plugins.idea.swip.model.SwipRequest;
import ore.plugins.idea.swip.service.*;
import ore.spring.web.initializr.domain.ResourcePersistable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SwipAction extends OrePluginAction {

    private static final String SPRING_WEB_INITIALIZR_DEPENDENCY_TEMPLATE = "/templates/maven/spring-web-initializr-dependency";

    @Override
    public void update(@NotNull AnActionEvent anActionEvent) {
        safeExecute(() -> {
            super.update(anActionEvent);
            PsiClass psiClass = extractPsiClass(anActionEvent);
            if (psiClass.getImplementsList() != null
                    && Arrays.stream(psiClass.getImplementsList().getReferencedTypes())
                    .anyMatch(refType -> refType.getClassName().contains(ResourcePersistable.class.getSimpleName()))) {
                throw new InvalidFileException();
            }
        }, anActionEvent);
    }

    @Override
    public void safeActionPerformed(AnActionEvent anActionEvent) {
        PsiClass resourcePsiClass = extractPsiClass(anActionEvent);

        File pom = requestPomXml(resourcePsiClass);
        validateMavenDependencies(pom);

        PsiField idPsiField = requestResourceIdField(resourcePsiClass);

        PackageInputDialog packageInputDialog = new PackageInputDialog(resourcePsiClass);
        packageInputDialog.waitForInput();



        SwipRequest swipRequest = SwipRequest.SwipRequestBuilder
                .aSwipRequest(resourcePsiClass, idPsiField)
                // TODO ADD SUPPORT FOR CUSTOM RESOURCE FORM
                .withResourcePersistableFormClass(resourcePsiClass)
                // TODO ADD SUPPORT FOR CUSTOM RESOURCE SEARCH FORM
                .withResourcePersistableSearchFormClass(resourcePsiClass)
                .withResourcePersistableRepositoryPackage(packageInputDialog.getRepositoryPackageField())
                .withResourcePersistableServicePackage(packageInputDialog.getServicePackageField())
                .withResourcePersistableControllerPackage(packageInputDialog.getControllerPackageField())
                .build();

        act(swipRequest);
    }

    private File requestPomXml(PsiClass resourcePsiClass) {
        String pomXmlPath = ProjectRootManager.getInstance(resourcePsiClass.getProject()).getContentRoots()[0].getPath().concat("/pom.xml");
        if (!Files.exists(Paths.get(pomXmlPath))) {
            Messages.showMessageDialog(resourcePsiClass.getProject(), "Maven project expected. Please convert this project to continue.", "Invalid Structure", Messages.getErrorIcon());
            throw new CancelException();
        }
        return new File(pomXmlPath);
    }

    private void validateMavenDependencies(File pom) {
        ObjectMapper xmlMapper = new XmlMapper();
        try {
            PsiPom psiPom = xmlMapper.readValue(pom, PsiPom.class);
            if (psiPom.getDependencies() == null ||
                    psiPom.getDependencies()
                            .stream()
                            .noneMatch(e -> e.getGroupId().equals("io.github.orpolyzos") && e.getArtifactId().equals("spring-web-initializr"))) {
                Messages.showErrorDialog(provideTemplateContent(SPRING_WEB_INITIALIZR_DEPENDENCY_TEMPLATE), "Insufficient Dependencies");
                throw new CancelException();
            }
        } catch (IOException e) {
            throw new OrePluginRuntimeException(e.getMessage());
        }

    }

    @NotNull
    private PsiField requestResourceIdField(PsiClass resourcePsiClass) {
        List<PsiField> resourceIdCandidates = Arrays.stream(resourcePsiClass.getFields())
                .filter(this::excludeStaticOrFinal)
                .collect(Collectors.toList());
        SelectStuffDialog<PsiField> resourceIdFieldDialog = new SelectStuffDialog<>(
                resourcePsiClass.getProject(),
                String.format("ResourcePersistable ID: (i.e. %s)", resourcePsiClass.getName()),
                "Choose the field that is going to be the ID (primary key) for the Resource",
                resourceIdCandidates, ListSelectionModel.SINGLE_SELECTION, new NameListCelRenderer());
        resourceIdFieldDialog.waitForInput();
        return resourceIdFieldDialog.getSelectedStuff()
                .stream()
                .findFirst()
                .orElseThrow(() -> new ValidationException("Invalid selection for the ResourcePersistable ID"));
    }

    private boolean excludeStaticOrFinal(PsiField psiField) {
        return !Objects.requireNonNull(psiField.getModifierList()).hasModifierProperty(PsiModifier.STATIC) || Objects.requireNonNull(psiField.getModifierList()).hasModifierProperty(PsiModifier.FINAL);
    }

    private void act(SwipRequest swipRequest) {
        WriteCommandAction.runWriteCommandAction(swipRequest.getResourcePersistableClass().getProject(), () -> {

            new ResourcePersistableGenerator(swipRequest).generateJavaClass();

            PsiClass resourceRepositoryClass = new ResourcePersistableRepositoryGenerator(swipRequest).generateJavaClass();

            PsiClass resourceServiceClass = new ResourcePersistableServiceGenerator(swipRequest, resourceRepositoryClass).generateJavaClass();

            ResourcePersistableControllerGenerator controllerGenerator = new ResourcePersistableControllerGenerator(swipRequest, resourceServiceClass);
            controllerGenerator.generateJavaClass();

            new FreemarkerGenerator(swipRequest, controllerGenerator).generateResources();
        });
    }


}
