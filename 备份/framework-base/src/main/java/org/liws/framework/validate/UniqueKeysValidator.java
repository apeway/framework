package org.liws.framework.validate;


import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.TypedQuery;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.beanutils.PropertyUtils;
import org.liws.framework.exception.BusinessRuntimeException;
import org.liws.framework.spring.SpringContextHolder;
import org.liws.framework.util.AnnotationUtil;
import org.liws.framework.validate.annotation.UniqueKey;
import org.liws.framework.validate.annotation.UniqueKeys;
import org.liws.framework.vo.BaseVO;

public class UniqueKeysValidator implements ConstraintValidator<UniqueKeys, BaseVO> {

	private UniqueKey[] uniqueKeys;

	@Override
	public void initialize(UniqueKeys paramA) {

		uniqueKeys = paramA.value();
	}

	@Override
	public boolean isValid(BaseVO target, ConstraintValidatorContext context) {

		final Class<?> entityClass = target.getClass();
		EntityManager entityManager = null;
		context.disableDefaultConstraintViolation();
		boolean result = true;
		try {
			entityManager = SpringContextHolder.getEntityManager();
			for(UniqueKey uniqueKey : uniqueKeys){
				int count=1;
				final StringBuilder jql = new StringBuilder("select count(*) from ");
				jql.append(target.getClass().getSimpleName()).append(" p where 1=1 ");
				List<Object> parameters = new ArrayList<Object>();
				String[] properties = uniqueKey.properties();
				for (String prop : properties) {
					Object val = PropertyUtils.getProperty(target, prop);
					jql.append(" and ").append(prop).append(" = ?").append(count++);
					parameters.add(val);
				}
	
				String idName = AnnotationUtil.findFieldName(target.getClass(), Id.class);
				Object idValue = AnnotationUtil.findFieldValue(target, Id.class);
				if (idValue != null) {
					jql.append(" and ").append(idName).append(" <> ?").append(count++);
					parameters.add(idValue);
				}
				TypedQuery<Long> typedQuery = entityManager.createQuery(jql.toString(),Long.class);
				for (int i = 0; i < parameters.size(); i++) {
					typedQuery.setParameter(i + 1, parameters.get(i));
				}
				Long nr = (Long) typedQuery.getSingleResult();
				if(nr > 0){
					context.buildConstraintViolationWithTemplate(uniqueKey.message())
					.addPropertyNode(properties[0]).addConstraintViolation();
					result = false;
				}
			}
			return result;

		} catch (final Exception e) {
			throw new BusinessRuntimeException("An error occurred when trying to create the jql for the @UniqueKeys on bean " + entityClass + ".", e);
		}finally {
			if(entityManager != null)
				entityManager.close();
		}


	}
    
    


}
