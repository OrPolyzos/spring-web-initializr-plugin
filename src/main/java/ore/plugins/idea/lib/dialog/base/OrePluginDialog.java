package ore.plugins.idea.lib.dialog.base;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import ore.plugins.idea.lib.exception.CancelException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public abstract class OrePluginDialog extends DialogWrapper {

    protected Project project;

    protected OrePluginDialog(@Nullable Project project) {
        super(project);
        this.project = project;
    }

    public void showDialog() {
        init();
        show();
    }

    public void waitForInput() {
        if (super.isOK()) {
            return;
        }
        throw new CancelException();
    }

    @NotNull
    protected JPanel createVerticalPanel() {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));
        return jPanel;
    }

    @NotNull
    protected JPanel createAlignedPanel(int alignment) {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new FlowLayout(alignment));
        return jPanel;
    }

    public Project getProject() {
        return project;
    }
}
