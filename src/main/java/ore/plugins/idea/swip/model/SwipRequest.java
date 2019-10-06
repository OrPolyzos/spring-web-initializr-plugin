package ore.plugins.idea.swip.model;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;

public class SwipRequest {

    private PsiClass resourcePersistableClass;
    private PsiField resourcePersistableIdField;

    private PsiClass resourcePersistableFormClass;
    private PsiClass resourcePersistableSearchFormClass;


    private String resourcePersistableRepositoryPackage = "";
    private String resourcePersistableServicePackage = "";
    private String resourcePersistableControllerPackage = "";
    private String resourcePersistableIdQualifiedName;

    private SwipRequest(PsiClass resourcePersistableClass, PsiField resourcePersistableIdField) {
        this.resourcePersistableClass = resourcePersistableClass;
        this.resourcePersistableIdField = resourcePersistableIdField;
        this.resourcePersistableFormClass = resourcePersistableClass;
        this.resourcePersistableSearchFormClass = resourcePersistableClass;
    }

    public PsiField getResourcePersistableIdField() {
        return resourcePersistableIdField;
    }

    public PsiClass getResourcePersistableClass() {
        return resourcePersistableClass;
    }

    public PsiClass getResourcePersistableFormClass() {
        return resourcePersistableFormClass;
    }

    public PsiClass getResourcePersistableSearchFormClass() {
        return resourcePersistableSearchFormClass;
    }

    public String getResourcePersistableRepositoryPackage() {
        return resourcePersistableRepositoryPackage;
    }

    public String getResourcePersistableServicePackage() {
        return resourcePersistableServicePackage;
    }

    public String getResourcePersistableControllerPackage() {
        return resourcePersistableControllerPackage;
    }

    public void setResourcePersistableIdField(PsiField resourcePersistableIdField) {
        this.resourcePersistableIdField = resourcePersistableIdField;
    }

    public void setResourcePersistableClass(PsiClass resourcePersistableClass) {
        this.resourcePersistableClass = resourcePersistableClass;
    }

    public void setResourcePersistableFormClass(PsiClass resourcePersistableFormClass) {
        this.resourcePersistableFormClass = resourcePersistableFormClass;
    }

    public void setResourcePersistableSearchFormClass(PsiClass resourcePersistableSearchFormClass) {
        this.resourcePersistableSearchFormClass = resourcePersistableSearchFormClass;
    }

    public void setResourcePersistableRepositoryPackage(String resourcePersistableRepositoryPackage) {
        this.resourcePersistableRepositoryPackage = resourcePersistableRepositoryPackage;
    }

    public void setResourcePersistableServicePackage(String resourcePersistableServicePackage) {
        this.resourcePersistableServicePackage = resourcePersistableServicePackage;
    }

    public void setResourcePersistableControllerPackage(String resourcePersistableControllerPackage) {
        this.resourcePersistableControllerPackage = resourcePersistableControllerPackage;
    }

    public String getResourcePersistableIdQualifiedName() {
        return resourcePersistableIdQualifiedName;
    }

    public void setResourcePersistableIdQualifiedName(String resourcePersistableIdQualifiedName) {
        this.resourcePersistableIdQualifiedName = resourcePersistableIdQualifiedName;
    }

    public static class SwipRequestBuilder {
        private PsiField resourcePersistableIdPsiField;
        private PsiClass resourcePersistableClass;
        private PsiClass resourcePersistableFormClass;
        private PsiClass resourcePersistableSearchFormClass;
        private String resourcePersistableRepositoryPackage;
        private String resourcePersistableServicePackage;
        private String resourcePersistableControllerPackage;

        private SwipRequestBuilder(PsiClass resourcePersistableClass, PsiField resourcePersistableIdPsiField) {
            this.resourcePersistableClass = resourcePersistableClass;
            this.resourcePersistableIdPsiField = resourcePersistableIdPsiField;
        }

        public static SwipRequestBuilder aSwipRequest(PsiClass resourceClass, PsiField resourcePsiField) {
            return new SwipRequestBuilder(resourceClass, resourcePsiField);
        }

        public SwipRequestBuilder withResourcePersistableFormClass(PsiClass resourcePersistableFormClass) {
            this.resourcePersistableFormClass = resourcePersistableFormClass;
            return this;
        }

        public SwipRequestBuilder withResourcePersistableSearchFormClass(PsiClass resourcePersistableSearchFormClass) {
            this.resourcePersistableSearchFormClass = resourcePersistableSearchFormClass;
            return this;
        }

        public SwipRequestBuilder withResourcePersistableRepositoryPackage(String resourceRepositoryPackage) {
            this.resourcePersistableRepositoryPackage = resourceRepositoryPackage;
            return this;
        }

        public SwipRequestBuilder withResourcePersistableServicePackage(String resourceServicePackage) {
            this.resourcePersistableServicePackage = resourceServicePackage;
            return this;
        }

        public SwipRequestBuilder withResourcePersistableControllerPackage(String resourceControllerPackage) {
            this.resourcePersistableControllerPackage = resourceControllerPackage;
            return this;
        }

        public SwipRequest build() {
            SwipRequest swipRequest = new SwipRequest(resourcePersistableClass, resourcePersistableIdPsiField);
            swipRequest.setResourcePersistableFormClass(resourcePersistableFormClass);
            swipRequest.setResourcePersistableSearchFormClass(resourcePersistableSearchFormClass);
            swipRequest.setResourcePersistableRepositoryPackage(resourcePersistableRepositoryPackage);
            swipRequest.setResourcePersistableServicePackage(resourcePersistableServicePackage);
            swipRequest.setResourcePersistableControllerPackage(resourcePersistableControllerPackage);
            swipRequest.setResourcePersistableIdQualifiedName(resourcePersistableIdPsiField.getType().getCanonicalText());
            return swipRequest;
        }
    }
}
