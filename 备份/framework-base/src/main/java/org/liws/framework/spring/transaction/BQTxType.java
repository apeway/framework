package org.liws.framework.spring.transaction;

public enum BQTxType {
	
	
	/**
	 * 如果当前存在事务，则加入该事务；如果当前没有事务，则创建一个新的事务。
	 */
	REQUIRED(TransactionDefinition.PROPAGATION_REQUIRED),
//	/**
//	 * 创建一个新的事务，如果当前存在事务，则把当前事务挂起。
//	 */
	REQUIRES_NEW(TransactionDefinition.PROPAGATION_REQUIRES_NEW),

//	/**
//	 * 以非事务方式运行，如果当前存在事务，则把当前事务挂起。
//	 */
//	NOT_SUPPORTED(TransactionDefinition.PROPAGATION_NOT_SUPPORTED),
//	/**
//	 * 以非事务方式运行，如果当前存在事务，则抛出异常。
//	 */
//	NEVER(TransactionDefinition.PROPAGATION_NEVER),
//	/**
//	 * 如果当前存在事务，则加入该事务；如果当前没有事务，则抛出异常。
//	 */
//	MANDATORY(TransactionDefinition.PROPAGATION_MANDATORY),
//	
//	/**
//	 * 如果当前存在事务，则创建一个事务作为当前事务的嵌套事务来运行；如果当前没有事务，则该取值等价于TransactionDefinition.PROPAGATION_REQUIRED
//	 */
//	NESTED(TransactionDefinition.PROPAGATION_NESTED),
//	
	/**
	 * 如果当前存在事务，则加入该事务；如果当前没有事务，则以非事务的方式继续运行。
	 */
	SUPPORTS(TransactionDefinition.PROPAGATION_SUPPORTS);
	
	private int propagationInt;
	
	private BQTxType(int propagationInt){
		this.propagationInt = propagationInt;
	}
	
	
	public int getTransactionDefinitionInt(){
		return this.propagationInt;
	}
}
