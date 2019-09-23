package org.liws.framework.util;


import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.liws.framework.exception.BusinessRuntimeException;

public class AnnotationUtil {
	
	public static Annotation findClassAnnotation(Class<?> objClazz, Class<?> targetAnno) {
		if (objClazz == null || targetAnno == null) {
			return null;
		}
		Annotation[] annos = objClazz.getAnnotations();
		if (annos != null) {
			for (Annotation a : annos) {
				if (a.annotationType().equals(targetAnno)) {
					return a;
				}
			}

		}
		return null;
	}
	
	public static Annotation findFieldAnnotation(Class<?> objClazz,String fieldName,Class<?> annotationClass){
		   if(objClazz == null || annotationClass == null){
			   return null;
		   }
		   Field[] fields = FieldUtils.getAllFields(objClazz);
			if(fields != null){
				for(Field field : fields){
					Annotation[] annos = field.getAnnotations();
					if(annos != null && fieldName.equals(field.getName())){
							for(Annotation a : annos){
								if(a.annotationType().equals(annotationClass)){
									return a;
								}
							}
							
						}
						
					}
			}
			return null;
	   }
	
   public static Field findField(Class<?> objClazz,Class<?>... annotationClass){
	   if(objClazz == null || annotationClass == null || annotationClass.length == 0){
		   return null;
	   }
	   Field[] fields = FieldUtils.getAllFields(objClazz);
		if(fields != null){
			for(Field field : fields){
				Annotation[] annos = field.getAnnotations();
				if(annos != null){
					boolean find = false;
					for(Class<?> ta : annotationClass){
						boolean taFind = false;
						for(Annotation a : annos){
							if(a.annotationType().equals(ta)){
								taFind = true;
							}
						}
						find = taFind;
						if(!find){
							break;
						}
					}
					if(find){
						return field;
					}
				}
			}
		}
		return null;
   }
   
   public static String findFieldName(Class<?> objClazz,Class<?>... targetAnnos) throws BusinessRuntimeException {
	   if(objClazz == null || targetAnnos == null || targetAnnos.length == 0){
		   return null;
	   }
	  Field field = findField(objClazz, targetAnnos);
	  if(field != null){
		  return field.getName();
	  }else{
		  return null;
	  }
	  
		
   }
   
   public static Object findFieldValue(Object obj,Class<?>... annotationClass) throws BusinessRuntimeException{
	   if(obj == null || annotationClass == null || annotationClass.length == 0){
		   return null;
	   }
	  Field field = findField(obj.getClass(), annotationClass);
	  if(field != null){
		  try {
			field.setAccessible(true);
			return field.get(obj);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new BusinessRuntimeException("find Field Value error",e);
		} 
	  }else{
		  return null;
	  }
	  
		
   }
}
