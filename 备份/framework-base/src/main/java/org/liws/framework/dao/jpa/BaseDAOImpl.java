package org.liws.framework.dao.jpa;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.commons.beanutils.PropertyUtils;
import org.liws.framework.exception.BusinessRuntimeException;
import org.liws.framework.log.BQLogger;
import org.liws.framework.shiro.UserManager;
import org.liws.framework.spring.SpringContextHolder;
import org.liws.framework.util.AnnotationUtil;
import org.liws.framework.util.ServerConfigReader;
import org.liws.framework.validate.IValidateErrorHandler;
import org.liws.framework.vo.BaseVO;
import org.liws.framework.vo.UserVO;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.util.StringUtils;

import com.yonyou.bq.framework.annotation.AddOrModify;
import com.yonyou.bq.framework.annotation.Remove;
import com.yonyou.bq.framework.vo.TableCounterVO;

public class BaseDAOImpl<T extends BaseVO> extends SimpleJpaRepository<T, String> implements IBaseDAO<T> {

    private final EntityManager entityManager;

    private final Class<T> voClass;
//    public BaseDAOImpl(Class<T> domainClass, EntityManager entityManager) {
//        super(domainClass, entityManager);
//        this.entityManager = entityManager;
//    }

    public BaseDAOImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation,entityManager);
        voClass = entityInformation.getJavaType();
        this.entityManager = entityManager;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<T> getGenericClass() {
        return voClass;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> findList(String queryWhereClause, int pageIndex, int recordPerPage, Object... paramVarArgs) {
        if (queryWhereClause == null) {
            return null;
        }
        try {
            String jql = "select p from " + this.getGenericClass().getSimpleName() + " p ";
            if (StringUtils.hasText(queryWhereClause)) {
                if (queryWhereClause.toLowerCase().trim().startsWith("order ")) {
                    jql = jql + " " + queryWhereClause;
                } else {
                    jql = jql + " where " + queryWhereClause;
                }

            }
            TypedQuery<T> typedQuery = entityManager.createQuery(jql, this.getGenericClass());
            if (paramVarArgs != null) {
                for (int i = 0; i < paramVarArgs.length; i++) {
                    typedQuery.setParameter(i + 1, paramVarArgs[i]);
                }
            }
            if (recordPerPage > 0) {
                if (pageIndex <= 0)
                    pageIndex = 1;
                typedQuery.setFirstResult((pageIndex - 1) * recordPerPage);
                typedQuery.setMaxResults(recordPerPage);
            }
            List<T> results = typedQuery.getResultList();
            return results;
        } catch (NoResultException ex) {
            BQLogger.info(ex);
            return null;
        } catch (Exception ex) {
            throw new BusinessRuntimeException("find List error", ex);
        }
    }

    @Override
    public int executeSQL(String sql, Object... paramVarArgs) {
        if (sql == null) {
            return 0;
        }
        try {
            Query query = entityManager.createNativeQuery(sql);
            for (int i = 0; i < paramVarArgs.length; i++) {
                query.setParameter(i + 1, paramVarArgs[i]);
            }
            return query.executeUpdate();
        } catch (Exception ex) {
            throw new BusinessRuntimeException("execute SQL " + sql + " error", ex);
        }
    }

    @Override
    public int executeJQL(String jql, Object... paramVarArgs){
        if (jql == null) {
            return 0;
        }
        try {
            Query query = entityManager.createQuery(jql);
            if(paramVarArgs != null){
                for (int i = 0; i < paramVarArgs.length; i++) {
                    query.setParameter(i + 1, paramVarArgs[i]);
                }
            }
            return query.executeUpdate();
        } catch (Exception ex) {
            throw new BusinessRuntimeException("execute JQL " + jql + " error", ex);
        }

    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object[]> queryUsingSQL(String sql, Object... paramVarArgs) {
        Query query = entityManager.createNativeQuery(sql);

        if(paramVarArgs != null && paramVarArgs.length > 0){
            for(int i=0; i < paramVarArgs.length;i++){
                query.setParameter(i+1,paramVarArgs[i]);
            }
        }
        return (List<Object[]> )query.getResultList();
    }

    @Override
    public void deleteByClause(String whereClause, Object... paramObject) throws BusinessRuntimeException {
        String jql = "delete from " + this.getGenericClass().getSimpleName() + " where " + whereClause;
        this.executeJQL(jql, paramObject);

    }

    @Override
    public String[] validateDelete(T vo) {
        if (vo == null) {
            return new String[0];
        }
        Set<ConstraintViolation<BaseVO>> set = ((Validator)SpringContextHolder.getBean("validator")).validate(vo, new Class[] { Remove.class });
        if (set != null && set.size() > 0) {
            return SpringContextHolder.getBean(IValidateErrorHandler.class).handErrorResult(set);
        } else {
            return new String[0];
        }
    }

    @Override
    public String[] validateDelete(String pk) {
        T vo = this.findOne(pk);
        return  this.validateDelete(vo);
    }

    @Override
    public String[] validateSave(T vo) {
        if (vo == null) {
            return new String[0];
        }
        Set<ConstraintViolation<BaseVO>> set = ((Validator)SpringContextHolder.getBean("validator")).validate(vo, new Class[] {AddOrModify.class });
        if (set != null && set.size() > 0) {
            return SpringContextHolder.getBean(IValidateErrorHandler.class).handErrorResult(set);
        } else {
            return new String[0];
        }
    }

    @Override
    @SuppressWarnings({"rawtypes","unchecked"})
    public void resetPk(T vo) {
        this.resetPk(vo,false);
    }

    @Override
    @SuppressWarnings({"rawtypes","unchecked"})
    public void resetPk(T vo,boolean deep) {
        Stack<T> stack = new Stack<>();
        stack.push(vo);
        Set<T> walked = new HashSet<>();
        walked.add(vo);
        try {
            while (!stack.isEmpty()) {
                T obj = stack.pop();
                this.setVOPk(obj, UUID.randomUUID().toString());
                if(!deep)break;
                PropertyDescriptor[] pds = PropertyUtils.getPropertyDescriptors(obj.getClass());
                if (pds != null && pds.length > 0) {
                    for (int i = 0; i < pds.length; i++) {
                        if (pds[i] != null) {
                            if (BaseVO.class.isAssignableFrom(pds[i].getPropertyType())) {

                                BaseVO sub = (BaseVO) PropertyUtils.getProperty(obj, pds[i].getName());
                                if (sub != null) {
                                    //setPk(sub);
                                    if (!walked.contains(sub)) {
                                        stack.push((T) sub);
                                        walked.add((T) sub);
                                    }
                                }
                            } else if (java.util.Collection.class.isAssignableFrom(pds[i].getPropertyType())) {
                                java.util.Collection subs = (java.util.Collection) PropertyUtils.getProperty(obj, pds[i].getName());
                                if (subs != null && subs.size() > 0) {
                                    for (Object sub : subs) {
                                        if (sub != null && BaseVO.class.isAssignableFrom(sub.getClass())) {
                                            //setPk( (BaseVO) sub);
                                            if (!walked.contains(sub)) {
                                                walked.add((T) sub);
                                                stack.push((T) sub);
                                            }
                                        }
                                    }
                                }

                            }
                        }
                    }
                }
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            BQLogger.error(e);
        }
    }

    private void setVOPk(T vo, String pk) throws IllegalAccessException {
        Field field = AnnotationUtil.findField(vo.getClass(), Id.class);
        if (field != null) {
            field.setAccessible(true);
            field.set(vo, pk);
        }
    }



    @Override
    public long count(String whereClaus,Object... paramVarArgs) {
        String jql = "select count(*) from " + this.getGenericClass().getSimpleName();
        if(StringUtils.hasText(whereClaus)){
            jql = jql + " where " + whereClaus;
        }
        try{
            TypedQuery<Long> typedQuery = entityManager.createQuery(jql, Long.class);
            if (paramVarArgs != null) {
                for (int i = 0; i < paramVarArgs.length; i++) {
                    typedQuery.setParameter(i + 1, paramVarArgs[i]);
                }
            }

            Long result = typedQuery.getSingleResult();
            return result;
        }catch (Exception ex){
            throw new BusinessRuntimeException("count ["+whereClaus+"] error", ex);
        }


    }

    
    @Override
    public double sum(String columnName, String whereClaus, Object... paramVarArgs) {
        String jql = "select sum(" + columnName + ") from " + this.getGenericClass().getSimpleName();
        if(StringUtils.hasText(whereClaus)){
            jql = jql + " where " + whereClaus;
        }
        try{
            TypedQuery<Long> typedQuery = entityManager.createQuery(jql, Long.class);
            if (paramVarArgs != null) {
                for (int i = 0; i < paramVarArgs.length; i++) {
                    typedQuery.setParameter(i + 1, paramVarArgs[i]);
                }
            }

            double result = typedQuery.getSingleResult();
            return result;
        }catch (Exception ex){
            throw new BusinessRuntimeException("sum ["+whereClaus+"] error", ex);
        }


    }
    
    @Override
    public String getCurrentDomainMark() {
        if(ServerConfigReader.getServerConfig().getProperty("jdbc.url").contains("postgresql")){
            return "00000";
        }
        String sql = "select database()";
        Query query = this.entityManager.createNativeQuery(sql);
        Object result = query.getSingleResult();
        String dbName = Objects.toString(result);
        if("domain_mng".equals(dbName)){
            return "00000";
        }else if("domain_test".equals(dbName)){
            return "00001";
        }else{
            return dbName.substring(2);
        }
    }

    @Override
    public long getSerialCount(String code){
        String selectJQL = "select maxSerial from TableCounterVO where code = ?1";
        try {
            TypedQuery<Long> query = entityManager.createQuery(selectJQL,Long.class);
            query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
            query.setHint("javax.persistence.lock.timeout",30000);

            query.setParameter(1, code);
            long result = 1000;
            try {
                result = query.getSingleResult();
            }catch (NoResultException e){
                TableCounterVO tc = new TableCounterVO();
                tc.setCode(code);
                tc.setMaxSerial(result+1);
                entityManager.persist(tc);
            }

            if(result > 0) {
                String updateJQL = "update TableCounterVO SET maxSerial = ?1 where code = ?2";
                Query query2 = entityManager.createQuery(updateJQL);
                query2.setParameter(1, result + 1);
                query2.setParameter(2, code);
                query2.executeUpdate();
            }

            entityManager.flush();
            return result+1;
        } catch (Exception ex) {
            throw new BusinessRuntimeException("getSerialCount error", ex);
        }
    }

//    @Override
//    public boolean distributedExecute(String code, Predicate predicate,Procedure procedure){
//        try {
//            String selectJQL = "select maxSerial from TableCounterVO where code = ?1";
//            TypedQuery<Long> query = entityManager.createQuery(selectJQL,Long.class);
//            query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
//            query.setHint("javax.persistence.lock.timeout",60000);
//
//            query.setParameter(1, code);
//
//            query.getSingleResult();
//            boolean result = false;
//            if(predicate.test()){
//                result = true;
//                procedure.call();
//            }
//            entityManager.flush();
//            return result;
//        } catch (Exception ex) {
//            throw new BusinessRuntimeException("getSerialCount error", ex);
//        }
//    }

    @Override
    public <S extends T> S save(S vo) {
        if(vo.isNew()){
            resetPk(vo);
        }
        return super.save(vo);
    }

    @Override
    public T findByPk(String pk) {
//        Optional<T> optional = this.findOne(pk);
        return  this.findOne(pk);
    }

    @Override
    public void deleteByPk(String pk) {
        this.delete(pk);
    }

    @Override
    public void updateModifyInfo(String pk) {
        Class<T> clazz = this.getGenericClass();
        Field field = AnnotationUtil.findField(clazz, Id.class);
        if(field != null){
            String jsql = "update "+clazz.getSimpleName()+" set modifier=?1, modifyTime=?2 where "+field.getName()+"=?3";
            LocalDateTime modifyTime = LocalDateTime.now();
            UserVO user = UserManager.getLoginUser();
            String modifier = null;
            if(user != null){
                modifier = user.getUserName();
            }else{
                modifier = "#BQ#";
            }
            this.executeJQL(jsql,modifier,modifyTime,pk);
        }

    }


}
