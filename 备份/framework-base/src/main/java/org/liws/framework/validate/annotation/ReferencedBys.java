package org.liws.framework.validate.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import org.liws.framework.validate.ReferencedBysValidator;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy=ReferencedBysValidator.class)
public @interface ReferencedBys {
	ReferencedBy[] value();
	
	String message() default "Object is referenced";
	 
    Class<?>[] groups() default {};
 
    Class<? extends Payload>[] payload() default {};
    
    
    @Target({ElementType.FIELD,ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List
    {
        ReferencedBy[] value();
    }
    
    
    
}
