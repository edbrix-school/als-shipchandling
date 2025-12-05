package com.asg.shipchandling.stockcategory.dto.request;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidParentCategory.ParentCategoryValidator.class)
@Documented
public @interface ValidParentCategory {
    String message() default "Parent category is required when category type is SUB_GROUP";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
    class ParentCategoryValidator implements ConstraintValidator<ValidParentCategory, Object> {
        @Override
        public void initialize(ValidParentCategory constraintAnnotation) {
        }

        @Override
        public boolean isValid(Object request, ConstraintValidatorContext context) {
            if (request == null) {
                return true; // Let @NotNull handle null checks
            }

            String categoryType = null;
            Long parentCategoryPoid = null;

            // Handle both CreateStockCategoryRequest and UpdateStockCategoryRequest
            if (request instanceof CreateStockCategoryRequest) {
                CreateStockCategoryRequest createRequest = (CreateStockCategoryRequest) request;
                categoryType = createRequest.getCategoryType();
                parentCategoryPoid = createRequest.getParentCategoryPoid();
            } else if (request instanceof UpdateStockCategoryRequest) {
                UpdateStockCategoryRequest updateRequest = (UpdateStockCategoryRequest) request;
                categoryType = updateRequest.getCategoryType();
                parentCategoryPoid = updateRequest.getParentCategoryPoid();
            } else {
                return true; // Unknown type, skip validation
            }

            // If categoryType is SUB_GROUP, parentCategoryPoid must not be null
            if ("SUB_GROUP".equals(categoryType)) {
                if (parentCategoryPoid == null) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("Parent category is required when category type is SUB_GROUP")
                            .addPropertyNode("parentCategoryPoid")
                            .addConstraintViolation();
                    return false;
                }
            }

            return true;
        }
    }
}

