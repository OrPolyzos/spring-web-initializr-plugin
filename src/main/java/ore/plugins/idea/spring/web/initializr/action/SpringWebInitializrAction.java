package ore.plugins.idea.spring.web.initializr.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifier;
import ore.plugins.idea.action.OrePluginAction;
import ore.plugins.idea.base.functionality.TemplateReader;
import ore.plugins.idea.base.model.PsiPom;
import ore.plugins.idea.dialog.InputValueDialog;
import ore.plugins.idea.dialog.PackageInputDialog;
import ore.plugins.idea.dialog.SelectStuffDialog;
import ore.plugins.idea.exception.CancelException;
import ore.plugins.idea.exception.validation.InvalidStructureException;
import ore.plugins.idea.exception.validation.ValidationException;
import ore.plugins.idea.spring.web.initializr.generator.*;
import ore.plugins.idea.spring.web.initializr.model.SpringWebInitializrRequest;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;

public class SpringWebInitializrAction extends OrePluginAction implements TemplateReader {

    private static final String SPRING_WEB_INITIALIZR_DEPENDENCY_TEMPLATE = "/templates/maven/spring-web-initializr-dependency";
    private static final String POM_SAMPLE_TEMPLATE = "/templates/maven/pom-sample";

    @Override
    public void safeActionPerformed(AnActionEvent anActionEvent) {
        PsiClass resourcePsiClass = extractPsiClass(anActionEvent);

        File pom = requestPomXml(resourcePsiClass);
        validateMavenDependencies(pom);

        SpringWebInitializrRequest springWebInitializrRequest = SpringWebInitializrRequest.SpringWebInitializrRequestBuilder
                .aSpringWebInitializrRequest(resourcePsiClass, requestResourceIdField(resourcePsiClass))
                // TODO ADD SUPPORT FOR CUSTOM RESOURCE FORM
                .withResourceFormClass(resourcePsiClass)
                // TODO ADD SUPPORT FOR CUSTOM RESOURCE SEARCH FORM
                .withResourceSearchFormClass(resourcePsiClass)
                .withResourceRepositoryPackage(requestPackage(resourcePsiClass, String.format("Package for ResourceRepository (i.e. %sRepository)", resourcePsiClass.getName())))
                .withResourceServicePackage(requestPackage(resourcePsiClass, String.format("Package for ResourceService (i.e. %sService)", resourcePsiClass.getName())))
                .withResourceControllerPackage(requestPackage(resourcePsiClass, String.format("Package for ResourceController (i.e. %sController)", resourcePsiClass.getName())))
                .build();

        act(springWebInitializrRequest);
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
            if (psiPom.getDependencies() == null || psiPom.getDependencies().stream().noneMatch(e -> e.getGroupId().equals("ore.utils.initializrs") && e.getArtifactId().equals("spring-web-initializr"))) {
                Messages.showErrorDialog(getTemplate(SPRING_WEB_INITIALIZR_DEPENDENCY_TEMPLATE), "Insufficient Dependencies");
                throw new CancelException();
            } else if (psiPom.getDependencies().stream().noneMatch(e -> e.getGroupId().contains("org.springframework.boot"))) {
                Messages.showMessageDialog(getTemplate(POM_SAMPLE_TEMPLATE), "Insufficient Dependencies", Messages.getWarningIcon());
            }
        } catch (Exception e) {
            throw new InvalidStructureException(e.getMessage());
        }

    }

    @NotNull
    private PsiField requestResourceIdField(PsiClass resourcePsiClass) {
        SelectStuffDialog<PsiField> resourceIdFieldDialog = new SelectStuffDialog<>(resourcePsiClass, Arrays.asList(resourcePsiClass.getFields()), this::excludeStaticOrFinal, String.format("ID for Resource (i.e. %s)", resourcePsiClass.getName()), "Choose the field that is going to be the ID (primary key) for the Resource", ListSelectionModel.SINGLE_SELECTION);
        resourceIdFieldDialog.waitForInput();
        return resourceIdFieldDialog.getSelectedStuff().stream().findFirst().orElseThrow(() -> new ValidationException(resourcePsiClass, "Invalid selection for the ID field."));
    }

    @NotNull
    private String requestPackage(PsiClass resourcePsiClass, String title) {
        InputValueDialog packageInputDialog = new PackageInputDialog(resourcePsiClass, title, "Place in package (e.g. ore.swip.demo) or leave empty for default");
        packageInputDialog.waitForInput();
        return packageInputDialog.getInput();
    }

    private boolean excludeStaticOrFinal(PsiField psiField) {
        return !Objects.requireNonNull(psiField.getModifierList()).hasModifierProperty(PsiModifier.STATIC) || Objects.requireNonNull(psiField.getModifierList()).hasModifierProperty(PsiModifier.FINAL);
    }

    private void act(SpringWebInitializrRequest springWebInitializrRequest) {
        WriteCommandAction.runWriteCommandAction(springWebInitializrRequest.getResourceClass().getProject(), () -> {

            new ResourcePersistableGenerator(springWebInitializrRequest).generate();
            PsiClass resourceRepositoryClass = new RepositoryGenerator(springWebInitializrRequest).generate();
            PsiClass resourceServiceClass = new ServiceGenerator(springWebInitializrRequest, resourceRepositoryClass).generate();
            ControllerGenerator controllerGenerator = new ControllerGenerator(springWebInitializrRequest, resourceServiceClass);
            controllerGenerator.generate();
            new FreemarkerGenerator(springWebInitializrRequest, controllerGenerator).generate();
        });
    }


}
