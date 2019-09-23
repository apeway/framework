package org.liws.framework.spring.transaction;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.liws.framework.log.BQLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;


/**
 * Service控制的通知类
<bean id="transactionalAdvice"
          class="com.yonyoucloud.bq.framework.transactional.BQTransactionalAdvice"/>
<aop:config>
    <aop:aspect id="concurrentOperationRetry" ref="transactionalAdvice">
        <aop:pointcut id="servicePointcut"
                      expression="@within(org.springframework.stereotype.Service)"/>
        <aop:around pointcut-ref="servicePointcut" method="aroundService"/>
    </aop:aspect>
</aop:config>         
 */
//@Aspect
public class BQTransactionalAdvice {


	@Autowired
	@Qualifier("txManager")
	private PlatformTransactionManager ptm;

//	@Pointcut("@within(org.springframework.stereotype.Service)")
//	public void servicePointcut() {
//
//	}

//	@Around(value = "servicePointcut()")
	public Object aroundService(ProceedingJoinPoint pjp) throws Throwable {//NOSONAR
		if(pjp.getSignature() instanceof MethodSignature){
			MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
			BQTransactional txAnno = AnnotationUtils.findAnnotation(methodSignature.getMethod(), BQTransactional.class);
			DefaultTransactionDefinition dtd = null;
			BQTxType txType = BQTxType.SUPPORTS;
			if (txAnno != null) {
				txType = txAnno.value();
			}

			dtd = new DefaultTransactionDefinition(txType.getTransactionDefinitionInt());
			if(txType == BQTxType.SUPPORTS){
				dtd.setReadOnly(true);
			}
			BQLogger.debug("Transcaction target class: {} ,method : {} , txType:{}", pjp.getTarget(),methodSignature.getMethod(),txType);
			TransactionStatus ts = ptm.getTransaction(dtd);
			try {
				Object result = pjp.proceed();
				ptm.commit(ts);
				BQLogger.debug("class: {},method : {} transaction commit",pjp.getTarget(),methodSignature.getMethod());
				return result;
			} catch (Throwable ex) {//NOSONAR
                if(!ts.isCompleted()){
                    ptm.rollback(ts);
                }
				BQLogger.info("class: {},method : {} transaction rollback,Throwable is {}",pjp.getTarget(),methodSignature.getMethod(),ex);
				throw ex;
			}finally {

			}
		}else{
			try {
				Object result = pjp.proceed();
				return result;
			} catch (Throwable ex) {//NOSONAR
				throw ex;
			}
		}

	}

}
