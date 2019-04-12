package ore.plugins.idea.swip.model;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;

public class SwipRequest {

    private PsiClass resourceClass;
    private PsiField resourceIdPsiField;

    private PsiClass resourceFormClass;
    private PsiClass resourceSearchFormClass;


    private String resourceRepositoryPackage = "";
    private String resourceServicePackage = "";
    private String resourceControllerPackage = "";
    private String resourceIdQualifiedName;

    private SwipRequest(PsiClass resourceClass, PsiField resourceIdPsiField) {
        this.resourceClass = resourceClass;
        this.resourceIdPsiField = resourceIdPsiField;
        this.resourceFormClass = resourceClass;
        this.resourceSearchFormClass = resourceClass;
    }

    public PsiField getResourceIdPsiField() {
        return resourceIdPsiField;
    }

    public PsiClass getResourceClass() {
        return resourceClass;
    }

    public PsiClass getResourceFormClass() {
        return resourceFormClass;
    }

    public PsiClass getResourceSearchFormClass() {
        return resourceSearchFormClass;
    }

    public String getResourceRepositoryPackage() {
        return resourceRepositoryPackage;
    }

    public String getResourceServicePackage() {
        return resourceServicePackage;
    }

    public String getResourceControllerPackage() {
        return resourceControllerPackage;
    }

    public void setResourceIdPsiField(PsiField resourceIdPsiField) {
        this.resourceIdPsiField = resourceIdPsiField;
    }

    public void setResourceClass(PsiClass resourceClass) {
        this.resourceClass = resourceClass;
    }

    public void setResourceFormClass(PsiClass resourceFormClass) {
        this.resourceFormClass = resourceFormClass;
    }

    public void setResourceSearchFormClass(PsiClass resourceSearchFormClass) {
        this.resourceSearchFormClass = resourceSearchFormClass;
    }

    public void setResourceRepositoryPackage(String resourceRepositoryPackage) {
        this.resourceRepositoryPackage = resourceRepositoryPackage;
    }

    public void setResourceServicePackage(String resourceServicePackage) {
        this.resourceServicePackage = resourceServicePackage;
    }

    public void setResourceControllerPackage(String resourceControllerPackage) {
        this.resourceControllerPackage = resourceControllerPackage;
    }

    public String getResourceIdQualifiedName() {
        return resourceIdQualifiedName;
    }

    public void setResourceIdQualifiedName(String resourceIdQualifiedName) {
        this.resourceIdQualifiedName = resourceIdQualifiedName;
    }

    public static class SwipRequestBuilder {
        private PsiField resourceIdPsiField;
        private PsiClass resourceClass;
        private PsiClass resourceFormClass;
        private PsiClass resourceSearchFormClass;
        private String resourceRepositoryPackage;
        private String resourceServicePackage;
        private String resourceControllerPackage;

        private SwipRequestBuilder(PsiClass resourceClass, PsiField resourceIdPsiField) {
            this.resourceClass = resourceClass;
            this.resourceIdPsiField = resourceIdPsiField;
        }

        public static SwipRequestBuilder aSwipRequest(PsiClass resourceClass, PsiField resourcePsiField) {
            return new SwipRequestBuilder(resourceClass, resourcePsiField);
        }

        public SwipRequestBuilder withResourceFormClass(PsiClass resourceFormClass) {
            this.resourceFormClass = resourceFormClass;
            return this;
        }

        public SwipRequestBuilder withResourceSearchFormClass(PsiClass resourceSearchFormClass) {
            this.resourceSearchFormClass = resourceSearchFormClass;
            return this;
        }

        public SwipRequestBuilder withResourceRepositoryPackage(String resourceRepositoryPackage) {
            this.resourceRepositoryPackage = resourceRepositoryPackage;
            return this;
        }

        public SwipRequestBuilder withResourceServicePackage(String resourceServicePackage) {
            this.resourceServicePackage = resourceServicePackage;
            return this;
        }

        public SwipRequestBuilder withResourceControllerPackage(String resourceControllerPackage) {
            this.resourceControllerPackage = resourceControllerPackage;
            return this;
        }

        public SwipRequest build() {
            SwipRequest swipRequest = new SwipRequest(resourceClass, resourceIdPsiField);
            swipRequest.setResourceFormClass(resourceFormClass);
            swipRequest.setResourceSearchFormClass(resourceSearchFormClass);
            swipRequest.setResourceRepositoryPackage(resourceRepositoryPackage);
            swipRequest.setResourceServicePackage(resourceServicePackage);
            swipRequest.setResourceControllerPackage(resourceControllerPackage);
            swipRequest.setResourceIdQualifiedName(resourceIdPsiField.getType().getCanonicalText());
            return swipRequest;
        }
    }
}
