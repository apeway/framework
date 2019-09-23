package org.liws.framework.validate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.liws.framework.vo.BaseVO;

@Target({ElementType.ANNOTATION_TYPE,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ReferencedBy {
	String nameProperty() default "";
	
	String refProperty() default "";
	
	Class<? extends BaseVO> refClass() ;
	
	String listProperty()default "";
	
	String message() default "Object is referenced";
}
