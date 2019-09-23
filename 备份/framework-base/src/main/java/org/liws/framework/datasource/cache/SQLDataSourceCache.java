package org.liws.framework.datasource.cache;



import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.liws.framework.datasource.sqlexec.util.SQLExecutor;
import org.liws.framework.exception.BusinessRuntimeException;
import org.liws.framework.log.BQLogger;
import org.liws.framework.repository.CurrentDomainMark;
import org.liws.framework.util.ServerConfigReader;
import org.liws.framework.vo.DataSourceVO;
import org.liws.framework.vo.DbType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

/**
 * SQL数据源缓存，只能在服务器端使用
 * 
 * @author huoqi
 *
 */
public class SQLDataSourceCache {
	
	private Map<String, DataSource> sqlDsCache = new ConcurrentHashMap<>();
	private Map<String, DataSourceVO> voDsCache = new ConcurrentHashMap<>();
	private long refreshTime = -1;


	private static final SQLDataSourceCache instance = new SQLDataSourceCache();

	private SQLDataSourceCache() {

	}

	public static SQLDataSourceCache getInstance() {
		return instance;
	}

	public DataSource getSQLDatasource(String pkOrName){

		DataSourceVO vo = DataSourceCache.getInstance().findByPkOrName(pkOrName);
		if(vo == null){
			throw new BusinessRuntimeException("can not find DataSourceVO by pk Or Name: "+pkOrName);
		}
		return this.createDataSource(vo);
	}

	public DataSource getSQLDatasource(DataSourceVO vo){

		return this.createDataSource(vo);
	}

	private DataSource createDataSource(DataSourceVO vo){
		String sqlDsKey = generateKey(vo);
		DataSource ds = sqlDsCache.get(sqlDsKey);
		if(sqlDsCache.containsKey(sqlDsKey) && (ds == null || !((com.alibaba.druid.pool.DruidDataSource)ds).isEnable() || ((com.alibaba.druid.pool.DruidDataSource)ds).isClosed())){
			sqlDsCache.remove(sqlDsKey);
			voDsCache.remove(sqlDsKey);
			ds = null;
		}

		if (ds == null ) {
			synchronized (SQLDataSourceCache.class) {
				if (sqlDsCache.get(sqlDsKey) == null) {
					ds = createDataSource0(vo);
					sqlDsCache.put(sqlDsKey, ds);
					voDsCache.put(sqlDsKey, vo);
				}else{
					ds = sqlDsCache.get(sqlDsKey);
				}
			}
			
		}else {
			synchronized (SQLDataSourceCache.class) {
				if(!vo.sqlEquals(voDsCache.get(sqlDsKey))){
					this.destroyDatasource(vo);
					ds = createDataSource0(vo);
					sqlDsCache.put(sqlDsKey, ds);
					voDsCache.put(sqlDsKey, vo);
				}
			}
		}
		if(refreshTime < 0 || System.currentTimeMillis() - refreshTime > 60000) {
			refreshDataSource();
			refreshTime = System.currentTimeMillis();
		}
		return  ds;
	}
	
	private void refreshDataSource(){
		String currentDomain = CurrentDomainMark.getCurrentDomainMark();
		Thread thread = new Thread(){
			@Override
			public void run() {
				//CurrentDomainMark.setPrimaryCurrentDomainMark(currentDomain);
				voDsCache.forEach((k,v) -> {
					CurrentDomainMark.setPrimaryCurrentDomainMark(v.getDomainMark());
					DataSourceVO vo = DataSourceCache.getInstance().findByPk(v.getDsPk());

					if(vo == null || !vo.sqlEquals(v)){
						if(vo != null){
							BQLogger.info("data source "+vo.getDsName()+" connection pool be destroyed");
						}else{
							BQLogger.info("data source "+v.getDsName()+" connection pool be destroyed");
						}

						destroyDatasource(v);
					}
				});
				CurrentDomainMark.setPrimaryCurrentDomainMark(null);
			}
		};
		thread.setName("clean dataSource");
		thread.start();
	}
	public String generateKey(String pkOrName){
		if(DataSourceVO.EXEC_DS_NAME.equals(pkOrName)){
			return CurrentDomainMark.getCurrentDomainMark() + "^" + pkOrName;
		}else{
			return generateKey(DataSourceCache.getInstance().findByPkOrName(pkOrName));
		}
	}
	
	protected String generateKey(DataSourceVO vo){
		if(vo != null) {
			return (StringUtils.hasText(vo.getDomainMark()) ? vo.getDomainMark() :   CurrentDomainMark.getCurrentDomainMark() )+ "^"+ vo.getDsName();
		}else {
			return "unknown_ds";
		}
	}
	
	public void destroySQLDatasource(String dsPkOrName) {
		DataSourceVO vo = DataSourceCache.getInstance().findByPkOrName(dsPkOrName);
		destroyDatasource(vo);
	}
	
	private void destroyDatasource(DataSourceVO vo){
		if(vo != null){
			synchronized (SQLDataSourceCache.class) {
				String sqlDsKey = this.generateKey(vo);
				DataSource ds = sqlDsCache.get(sqlDsKey);
				try{
					sqlDsCache.remove(sqlDsKey);
					voDsCache.remove(sqlDsKey);
				}finally{
					if(ds != null){
						Set<Map<String, JdbcTemplate>> set =  SQLExecutor.all_thread.keySet();
						for(Map<String, JdbcTemplate> map : set){
							if(map != null && map.containsKey(sqlDsKey)){
								SQLExecutor.all_thread.put(map, 0);
							}
						}
						((com.alibaba.druid.pool.DruidDataSource)ds).close();
					}
					
				}
			}
			
		}
	}
	
//	public void renameSQLDatasource(DataSourceVO oldVO, DataSourceVO newVO) {
//		String sqlDsKey_o = this.generateKey(oldVO);
//		if(sqlDsCache.containsKey(sqlDsKey_o)){
//			String sqlDsKey_n = this.generateKey(newVO);
//			synchronized (this) {
//				if(sqlDsCache.containsKey(sqlDsKey_o)){
//					sqlDsCache.put(sqlDsKey_n, sqlDsCache.get(sqlDsKey_o));
//					sqlDsCache.remove(sqlDsKey_o);
//				}
//			}
//		}
//	}

	private DataSource createDataSource0(DataSourceVO vo){
		com.alibaba.druid.pool.DruidDataSource dataSource = new com.alibaba.druid.pool.DruidDataSource();
		//基本属性 url、user、password
		dataSource.setDriverClassName(vo.getDbType() == null ? "" : vo.getDbType().getDriverClass());
		if(vo.getDbType() == DbType.MYSQL && vo.getConnectURL() != null && !vo.getConnectURL().contains("?")){
			dataSource.setUrl(vo.getConnectURL()+"?serverTimezone=Hongkong&nullCatalogMeansCurrent=true&nullNamePatternMatchesAll=true&useCursorFetch=true&defaultFetchSize=2000&useSSL=true&useUnicode=true&characterEncoding=utf8");
		}else{
			dataSource.setUrl(vo.getConnectURL());
		}
		dataSource.setUsername(vo.getUsername());
		dataSource.setPassword(vo.getPassword());

		//配置初始化大小、最小、最大
		dataSource.setInitialSize(vo.getPoolInitialSize());
		dataSource.setMinIdle(vo.getPoolMinIdle());
		dataSource.setMaxActive(vo.getPoolMaxActive());

		//配置获取连接等待超时的时间
		dataSource.setMaxWait(vo.getPoolMaxWait());

		//配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
		dataSource.setTimeBetweenEvictionRunsMillis(12000);

		//配置一个连接在池中最小生存的时间，单位是毫秒
		dataSource.setMinEvictableIdleTimeMillis(300000);

		if(DbType.isOracle(vo.getDbType())){
			dataSource.setValidationQuery("select 1 from dual");
		}else if(vo.getDbType() == DbType.DB2){
			dataSource.setValidationQuery("select 1 from sysibm.sysdummy1");
		}else{
			dataSource.setValidationQuery("select 1");
		}
		dataSource.setTestWhileIdle(true);
		dataSource.setTestOnBorrow(true);
		dataSource.setTestOnReturn(false);

		//打开PSCache，并且指定每个连接上PSCache的大小
		dataSource.setPoolPreparedStatements(true);
		dataSource.setMaxPoolPreparedStatementPerConnectionSize(150);

		dataSource.setDefaultAutoCommit(false);

		if (StringUtils.hasText(ServerConfigReader.getServerConfig().getProperty("datasource.removeAbandoned"))) {
			dataSource.setRemoveAbandoned(true);
			dataSource.setRemoveAbandonedTimeout(1800);
		}

		//配置监控统计拦截的filters
		try {
			dataSource.setFilters("log4j2");
			com.alibaba.druid.filter.stat.StatFilter statFilter = new com.alibaba.druid.filter.stat.StatFilter();
			statFilter.setLogSlowSql(true);
			statFilter.setSlowSqlMillis(10000);
			dataSource.setProxyFilters(Collections.singletonList(statFilter));
		} catch (SQLException e) {
			BQLogger.error(e);
		}

		return dataSource;
	}

/*	private PoolConfiguration getPoolConfiguration(DataSourceVO vo) {
		PoolConfiguration pc = new PoolProperties();
		pc.setDriverClassName(vo.getDbType() == null ? "" : vo.getDbType().getDriverClass());
		if(vo.getDbType() == DbType.MYSQL && vo.getConnectURL() != null && !vo.getConnectURL().contains("?")){
			pc.setUrl(vo.getConnectURL()+"?serverTimezone=Hongkong&nullCatalogMeansCurrent=true&nullNamePatternMatchesAll=true&useCursorFetch=true&defaultFetchSize=2000&useSSL=true&useUnicode=true&characterEncoding=utf8");
        }else{
            pc.setUrl(vo.getConnectURL());
        }

		pc.setUsername(vo.getUsername());
		pc.setPassword(vo.getPassword());

		pc.setMaxActive(vo.getPoolMaxActive());
		pc.setMaxIdle(vo.getPoolMaxIdle());
		pc.setMinIdle(vo.getPoolMinIdle());
		pc.setMaxWait(vo.getPoolMaxWait());
		pc.setInitialSize(vo.getPoolInitialSize());
		
		
		pc.setRemoveAbandoned(true);
		pc.setAbandonWhenPercentageFull(80);
		pc.setRemoveAbandonedTimeout(3600);
		pc.setLogAbandoned(true);

		pc.setDefaultAutoCommit(false);
		
		pc.setMinEvictableIdleTimeMillis(60000);
		pc.setTestOnBorrow(true);
		pc.setTestWhileIdle(true);
		pc.setTimeBetweenEvictionRunsMillis(5000);
		pc.setValidationQueryTimeout(3);
		if(DbType.isOracle(vo.getDbType())){
			pc.setValidationQuery("select 1 from dual");
		}else if(vo.getDbType() == DbType.DB2){
			pc.setValidationQuery("select 1 from sysibm.sysdummy1");
		}else{
			pc.setValidationQuery("select 1");
		}
		
		
		pc.setRemoveAbandonedTimeout(600);//超时 10 分钟
		pc.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.ResetAbandonedTimer");
		
		return pc;
	}*/
}
