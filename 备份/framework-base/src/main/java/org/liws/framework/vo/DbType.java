package org.liws.framework.vo;

/**
 * 数据库类型
 */
public enum DbType {
	ORACLE10, ORACLE11, ORACLE12, DB2, SQLSERVER2008, SQLSERVER2012, MYSQL, POSTGRESQL, GBASE, IMPALA, UNKNOWN;

	private static final String ORACLE_DRIVER_NAME = "oracle.jdbc.driver.OracleDriver";

	private static final String DB2_DRIVER_NAME = "com.ibm.db2.jcc.DB2Driver";

	private static final String SQLSERVER_DRIVER_NAME = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

	private static final String MYSQL_DRIVER_NAME = "com.mysql.cj.jdbc.Driver";

	private static final String POSTGRESQL_DRIVER_NAME = "org.postgresql.Driver";

	private static final String IMPALA_DRIVER_NAME = "com.cloudera.impala.jdbc41.Driver";

	private static final String GBASE_DRIVER_NAME = "com.gbase.jdbc.Driver";

	public String getDriverClass() {
		switch (this) {
		case DB2:
			return DB2_DRIVER_NAME;
		case MYSQL:
			return MYSQL_DRIVER_NAME;
		case ORACLE10:
		case ORACLE11:
		case ORACLE12:
			return ORACLE_DRIVER_NAME;
		case POSTGRESQL:
			return POSTGRESQL_DRIVER_NAME;
		case SQLSERVER2008:
		case SQLSERVER2012:
			return SQLSERVER_DRIVER_NAME;
		case IMPALA:
			return IMPALA_DRIVER_NAME;
		case GBASE:
			return GBASE_DRIVER_NAME;
		default:
			return "";
		}
	}

	public boolean supportsCatalogs() {
		switch (this) {
		case SQLSERVER2008:
		case SQLSERVER2012:
			return false;
		default:
			return true;
		}

	}

	public boolean supportsSchemas() {
		return true;
	}

	public boolean supportsViews() {
		return true;
	}
	
	public boolean isSystemTable(String tableName) {
		switch (this) {
		case MYSQL:
			if ( tableName.startsWith("sys")) return true;
			if ( tableName.equals("dtproperties")) return true;
		default:
			return false;
		}
		
	}
	
	public String getSQLListOfSchemas(){
		switch (this) {
		case SQLSERVER2008:
		case SQLSERVER2012:
			return "select name from sys.schemas";
		default:
			return "";
		}
	}
	
	public boolean useSchemaNameForTableList(){
		switch (this) {
		case ORACLE10:
		case ORACLE11:
		case ORACLE12:
		case DB2:
			return true;
		default:
			return false;
		}
	}
	
	public String getTruncateSQL(String tableName){
		switch (this) {
		case DB2:
			return "TRUNCATE TABLE " + tableName + " IMMEDIATE";
		default:
			return "TRUNCATE TABLE " + tableName;
		}
	}

	public boolean supportsSynonyms() {
		switch (this) {
		case POSTGRESQL:
		case SQLSERVER2008:
		case SQLSERVER2012:
			return false;
		default:
			return true;
		}
	}

	public static boolean isOracle(DbType dbType) {
		return dbType == ORACLE11 || dbType == DbType.ORACLE10 || dbType == DbType.ORACLE12;
	}

	public static boolean isSqlServer(DbType dbType) {
		return dbType == SQLSERVER2008 || dbType == DbType.SQLSERVER2012;
	}
}
