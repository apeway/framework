package org.liws.framework.validate;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.liws.framework.exception.BusinessRuntimeException;
import org.liws.framework.spring.SpringContextHolder;
import org.liws.framework.util.AnnotationUtil;
import org.liws.framework.util.UtilTools;
import org.liws.framework.validate.annotation.ReferencedBy;
import org.liws.framework.validate.annotation.ReferencedBys;
import org.liws.framework.vo.BaseVO;
import org.springframework.util.StringUtils;

public class ReferencedBysValidator implements ConstraintValidator<ReferencedBys, BaseVO> {

	private ReferencedBy[] refBys;

	@Override
	public void initialize(ReferencedBys paramA) {
		refBys = paramA.value();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean isValid(BaseVO paramT, ConstraintValidatorContext context) {
		context.disableDefaultConstraintViolation();
		boolean result = true;
		EntityManager entityManager = null;
		try {
			entityManager = SpringContextHolder.getEntityManager();
			
			for (ReferencedBy refBy : refBys) {
				int count = 1;
				if (refBy.refClass() != null && StringUtils.hasText(refBy.refProperty())) {
					if (StringUtils.hasText(refBy.nameProperty())) {
						StringBuilder jql = new StringBuilder("select ");
						jql.append(refBy.nameProperty());
						jql.append(" from ");
						jql.append(refBy.refClass().getSimpleName()).append(" p where ");
						List<Object> parameters = new ArrayList<Object>();

						jql.append(refBy.refProperty()).append(" = ?").append(count++);
						parameters.add(paramT);

						Query query = entityManager.createQuery(jql.toString());
						for (int i = 0; i < parameters.size(); i++) {
							query.setParameter(i + 1, parameters.get(i));
						}
						@SuppressWarnings("unchecked")
						List<String> list = query.getResultList();
						if (list.size() > 0) {
							result = false;
							List<String> copyList = null;
							if(list.size() >= 2){
								copyList = list.subList(0, 2);
							}else{
								copyList = new ArrayList<>(list);
							}
							if (context instanceof HibernateConstraintValidatorContext) {
								HibernateConstraintValidatorContext hConext = (HibernateConstraintValidatorContext) context;
								hConext.addExpressionVariable("refName", UtilTools.join(copyList, ","));
								hConext.addExpressionVariable("refNr", list.size());
							}
							context.buildConstraintViolationWithTemplate(refBy.message())
									.addPropertyNode(AnnotationUtil.findFieldName(paramT.getClass(), Id.class))
									.addConstraintViolation();

						}
					} else {
						StringBuilder jql = new StringBuilder("select count(*) from ");
						jql.append(refBy.refClass().getSimpleName()).append(" p where ");
						List<Object> parameters = new ArrayList<Object>();

						jql.append(refBy.refProperty()).append(" = ?").append(count++);
						parameters.add(paramT);

						TypedQuery<Long> query = entityManager.createQuery(jql.toString(), Long.class);
						for (int i = 0; i < parameters.size(); i++) {
							query.setParameter(i + 1, parameters.get(i));
						}
						Long nr = query.getSingleResult();
						if (nr > 0) {
							result = false;
							context.buildConstraintViolationWithTemplate(refBy.message())
									.addPropertyNode(AnnotationUtil.findFieldName(paramT.getClass(), Id.class))
									.addConstraintViolation();

						}
					}
				} else if (StringUtils.hasText(refBy.listProperty())) {
					BaseVO meta = entityManager.find(paramT.getClass(), AnnotationUtil.findFieldValue(paramT,Id.class));
					Object listProp = PropertyUtils.getProperty(meta, refBy.listProperty());
					if (listProp instanceof Collection) {
						Collection collection = (Collection) listProp;
						if (collection.size() > 0) {
							result = false;
						}
						if (StringUtils.hasText(refBy.nameProperty())) {
							List<String> list = new ArrayList<>(collection.size());
							for (Object e : collection) {
								list.add(UtilTools.toString(PropertyUtils.getProperty(e, refBy.nameProperty()), ""));
								if(list.size() >= 2)break;
							}
							if (context instanceof HibernateConstraintValidatorContext) {
								HibernateConstraintValidatorContext hConext = (HibernateConstraintValidatorContext) context;
								hConext.addExpressionVariable("refName", UtilTools.join(list, ","));
								hConext.addExpressionVariable("refNr", collection.size());
							}
						}
						context.buildConstraintViolationWithTemplate(refBy.message())
								.addPropertyNode(AnnotationUtil.findFieldName(paramT.getClass(), Id.class))
								.addConstraintViolation();
					}
				}

			}

		} catch (final Exception e) {
			throw new BusinessRuntimeException(
					"An error occurred when trying to create the jql for the @ReferencedBys  on class "
							+ paramT.getClass() + ".",
					e);
		}finally {
			if(entityManager != null){
				entityManager.close();
			}
		}
		return result;
	}
	

}
