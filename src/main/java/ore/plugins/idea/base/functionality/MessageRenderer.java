package ore.plugins.idea.base.functionality;

import com.intellij.psi.PsiClass;
import ore.plugins.idea.dialog.MessageBoxDialog;

public interface MessageRenderer {

    default void showAlertMessage(PsiClass psiClass, String message) {
        MessageBoxDialog.MessageBoxDialogBuilder
                .aMessageBoxDialog(psiClass, message)
                .build()
                .showDialog();
    }
}
