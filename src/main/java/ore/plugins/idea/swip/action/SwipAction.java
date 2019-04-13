package ore.plugins.idea.swip.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.search.GlobalSearchScope;
import ore.plugins.idea.lib.action.OrePluginAction;
import ore.plugins.idea.lib.dialog.InputDialog;
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

        SwipRequest swipRequest = SwipRequest.SwipRequestBuilder
                .aSwipRequest(resourcePsiClass, requestResourceIdField(resourcePsiClass))
                // TODO ADD SUPPORT FOR CUSTOM RESOURCE FORM
                .withResourceFormClass(resourcePsiClass)
                // TODO ADD SUPPORT FOR CUSTOM RESOURCE SEARCH FORM
                .withResourceSearchFormClass(resourcePsiClass)
                .withResourceRepositoryPackage(requestPackage(resourcePsiClass, String.format("Package for ResourceRepository (i.e. %sResourceRepository)", resourcePsiClass.getName()), "ResourceRepository"))
                .withResourceServicePackage(requestPackage(resourcePsiClass, String.format("Package for ResourceService (i.e. %sResourceService)", resourcePsiClass.getName()), "ResourceService"))
                .withResourceControllerPackage(requestPackage(resourcePsiClass, String.format("Package for ResourceController (i.e. %sResourceController)", resourcePsiClass.getName()), "ResourceController"))
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
                String.format("ID for Resource (i.e. %s)", resourcePsiClass.getName()),
                "Choose the field that is going to be the ID (primary key) for the Resource",
                resourceIdCandidates, ListSelectionModel.SINGLE_SELECTION, new NameListCelRenderer());
        resourceIdFieldDialog.waitForInput();
        return resourceIdFieldDialog.getSelectedStuff()
                .stream()
                .findFirst()
                .orElseThrow(() -> new ValidationException("Invalid selection for the ID field"));
    }

    @NotNull
    private String requestPackage(PsiClass resourcePsiClass, String title, String suffix) {
        String fullPackage;
        do {
            InputDialog packageInputDialog = new PackageInputDialog(resourcePsiClass, title, "Place in package (e.g. ore.swip.demo) or leave empty for default");
            packageInputDialog.waitForInput();
            fullPackage = packageInputDialog.getInput();
        } while (!packageExistsAlready(fullPackage, resourcePsiClass.getProject())
                || classExistsAlready(String.format("%s.%s%s", fullPackage, resourcePsiClass.getName(), suffix), resourcePsiClass.getProject()));
        return fullPackage;
    }

    private boolean packageExistsAlready(String fullPackagePath, Project project) {
        boolean packageExists = Files.exists(Paths.get(ProjectRootManager.getInstance(project).getContentRoots()[0].getPath().concat("/src/main/java/").concat(fullPackagePath.replaceAll("\\.", "/"))));
        if (!packageExists) {
            Messages.showWarningDialog("Package does not exist.", "Invalid Package");
        }
        return packageExists;
    }

    private boolean classExistsAlready(String qualifiedName, Project project) {
        boolean existsAlready = JavaPsiFacade.getInstance(project).findClass(qualifiedName, GlobalSearchScope.allScope(project)) != null;
        if (existsAlready) {
            Messages.showWarningDialog(String.format("There is already a class %s.", qualifiedName), "Duplicate File");
        }
        return existsAlready;
    }

    private boolean excludeStaticOrFinal(PsiField psiField) {
        return !Objects.requireNonNull(psiField.getModifierList()).hasModifierProperty(PsiModifier.STATIC) || Objects.requireNonNull(psiField.getModifierList()).hasModifierProperty(PsiModifier.FINAL);
    }

    private void act(SwipRequest swipRequest) {
        WriteCommandAction.runWriteCommandAction(swipRequest.getResourceClass().getProject(), () -> {

            new ResourcePersistableGenerator(swipRequest).generateJavaClass();

            PsiClass resourceRepositoryClass = new RepositoryGenerator(swipRequest).generateJavaClass();

            PsiClass resourceServiceClass = new ServiceGenerator(swipRequest, resourceRepositoryClass).generateJavaClass();

            ControllerGenerator controllerGenerator = new ControllerGenerator(swipRequest, resourceServiceClass);
            controllerGenerator.generateJavaClass();

            FreemarkerGenerator freemarkerGenerator = new FreemarkerGenerator(swipRequest, controllerGenerator);
            freemarkerGenerator.generateResources();

        });
    }


}
