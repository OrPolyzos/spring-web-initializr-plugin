package ore.plugins.idea.spring.web.initializr.model;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;

public class SpringWebInitializrRequest {

    private PsiClass resourceClass;
    private PsiField resourceIdPsiField;

    private PsiClass resourceFormClass;
    private PsiClass resourceSearchFormClass;


    private String resourceRepositoryPackage = "";
    private String resourceServicePackage = "";
    private String resourceControllerPackage = "";
    private String resourceIdQualifiedName;

    private SpringWebInitializrRequest(PsiClass resourceClass, PsiField resourceIdPsiField) {
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

    public static class SpringWebInitializrRequestBuilder {
        private PsiField resourceIdPsiField;
        private PsiClass resourceClass;
        private PsiClass resourceFormClass;
        private PsiClass resourceSearchFormClass;
        private String resourceRepositoryPackage;
        private String resourceServicePackage;
        private String resourceControllerPackage;

        private SpringWebInitializrRequestBuilder(PsiClass resourceClass, PsiField resourceIdPsiField) {
            this.resourceClass = resourceClass;
            this.resourceIdPsiField = resourceIdPsiField;
        }

        public static SpringWebInitializrRequestBuilder aSpringWebInitializrRequest(PsiClass resourceClass, PsiField resourcePsiField) {
            return new SpringWebInitializrRequestBuilder(resourceClass, resourcePsiField);
        }

        public SpringWebInitializrRequestBuilder withResourceFormClass(PsiClass resourceFormClass) {
            this.resourceFormClass = resourceFormClass;
            return this;
        }

        public SpringWebInitializrRequestBuilder withResourceSearchFormClass(PsiClass resourceSearchFormClass) {
            this.resourceSearchFormClass = resourceSearchFormClass;
            return this;
        }

        public SpringWebInitializrRequestBuilder withResourceRepositoryPackage(String resourceRepositoryPackage) {
            this.resourceRepositoryPackage = resourceRepositoryPackage;
            return this;
        }

        public SpringWebInitializrRequestBuilder withResourceServicePackage(String resourceServicePackage) {
            this.resourceServicePackage = resourceServicePackage;
            return this;
        }

        public SpringWebInitializrRequestBuilder withResourceControllerPackage(String resourceControllerPackage) {
            this.resourceControllerPackage = resourceControllerPackage;
            return this;
        }

        public SpringWebInitializrRequest build() {
            SpringWebInitializrRequest springWebInitializrRequest = new SpringWebInitializrRequest(resourceClass, resourceIdPsiField);
            springWebInitializrRequest.setResourceFormClass(resourceFormClass);
            springWebInitializrRequest.setResourceSearchFormClass(resourceSearchFormClass);
            springWebInitializrRequest.setResourceRepositoryPackage(resourceRepositoryPackage);
            springWebInitializrRequest.setResourceServicePackage(resourceServicePackage);
            springWebInitializrRequest.setResourceControllerPackage(resourceControllerPackage);
            springWebInitializrRequest.setResourceIdQualifiedName(resourceIdPsiField.getType().getCanonicalText());
            return springWebInitializrRequest;
        }
    }
}
