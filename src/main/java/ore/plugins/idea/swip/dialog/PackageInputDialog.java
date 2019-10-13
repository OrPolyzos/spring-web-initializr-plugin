package ore.plugins.idea.swip.dialog;

import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import ore.plugins.idea.lib.dialog.base.OrePluginDialog;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PackageInputDialog extends OrePluginDialog {

    private static final String TITLE = "Choose Package";
    private static final String COMPONENT_TEXT = "Choose a package for each component (e.g. ore.swip.demo) or leave empty for default:";
    private final LabeledComponent<JPanel> labeledComponent;

    private String controllerClassName;
    private JBTextField controllerPackageField;
    private JBLabel controllerPackageLabel;

    private String serviceClassName;
    private JBTextField servicePackageField;
    private JBLabel servicePackageLabel;

    private String repositoryClassName;
    private JBTextField repositoryPackageField;
    private JBLabel repositoryPackageLabel;

    public PackageInputDialog(PsiClass resourceClass) {
        super(resourceClass.getProject());
        this.controllerClassName = String.format("%sResourcePersistableController", resourceClass.getName());
        this.serviceClassName = String.format("%sResourcePersistableService", resourceClass.getName());
        this.repositoryClassName = String.format("%sResourcePersistableRepository", resourceClass.getName());

        setTitle(TITLE);
        setResizable(false);

        JPanel jPanel = createVerticalPanel();
        jPanel.add(new JBLabel("\r\n"));

        controllerPackageField = new JBTextField();
        controllerPackageLabel = new JBLabel();
        setupFieldWithLabel(controllerPackageField, controllerPackageLabel, controllerClassName + ":", jPanel);

        servicePackageField = new JBTextField();
        servicePackageLabel = new JBLabel();
        setupFieldWithLabel(servicePackageField, servicePackageLabel, serviceClassName + ":", jPanel);

        repositoryPackageField = new JBTextField();
        repositoryPackageLabel = new JBLabel();
        setupFieldWithLabel(repositoryPackageField, repositoryPackageLabel, repositoryClassName + ":", jPanel);

        labeledComponent = LabeledComponent.create(jPanel, COMPONENT_TEXT);

        validateFields();
        showDialog();

    }

    private void setupFieldWithLabel(JBTextField jbTextField, JBLabel jbLabel, String label, JPanel jPanel) {
        jbLabel.setText(label);
        jbLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        jbTextField.setEditable(true);
        jbTextField.setFocusable(true);
        jbTextField.setColumns(40);
        jbTextField.setAlignmentX(Component.LEFT_ALIGNMENT);
        jbTextField.addKeyListener(packageValidationKeyListener());

        JPanel fieldPanel = createVerticalPanel();
        fieldPanel.add(jbLabel);
        fieldPanel.add(jbTextField);

        jPanel.add(fieldPanel);
    }

    private boolean packageExistsAlready(String fullPackagePath) {
        return fullPackagePath.length() == 0 || Files.exists(Paths.get(ProjectRootManager.getInstance(getProject()).getContentRoots()[0].getPath().concat("/src/main/java/").concat(fullPackagePath.replaceAll("\\.", "/"))));
    }

    private boolean classExistsAlready(String qualifiedName) {
        return JavaPsiFacade.getInstance(getProject()).findClass(qualifiedName, GlobalSearchScope.allScope(getProject())) != null;
    }

    private KeyListener packageValidationKeyListener() {

        return new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {

            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {
                validateFields();
            }

        };
    }

    private void validateFields() {
        boolean isControllerValid = isValidField(controllerPackageField, controllerPackageLabel, controllerClassName);
        boolean isServiceValid = isValidField(servicePackageField, servicePackageLabel, serviceClassName);
        boolean isRepositoryValid = isValidField(repositoryPackageField, repositoryPackageLabel, repositoryClassName);
        getOKAction().setEnabled(isControllerValid && isServiceValid && isRepositoryValid);
    }

    private boolean isValidField(JBTextField jbTextField, JBLabel jbLabel, String className) {
        boolean packageExists = packageExistsAlready(jbTextField.getText().trim());
        boolean classExists = classExistsAlready(String.format("%s.%s", jbTextField.getText().trim(), className));
        if (jbTextField.getText().trim().length() == 0) {
            classExists = classExistsAlready(className);
        }

        boolean isValid = packageExists && !classExists;
        if (isValid) {
            jbTextField.setForeground(JBColor.BLUE);
            jbLabel.setForeground(JBColor.foreground());
            jbLabel.setText(String.format("%s:", className));
        } else {
            jbTextField.setForeground(JBColor.RED);
            jbLabel.setForeground(JBColor.RED);
            if (!packageExists) {
                jbLabel.setText(String.format("%s: (Package does not exist!)", className));
            } else {
                jbLabel.setText(String.format("%s: (%s already exists in this package!)", className, className));
            }
        }
        return isValid;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return labeledComponent;
    }

    public String getControllerPackageField() {
        return controllerPackageField.getText().trim();
    }

    public String getServicePackageField() {
        return servicePackageField.getText().trim();
    }

    public String getRepositoryPackageField() {
        return repositoryPackageField.getText().trim();
    }

}
