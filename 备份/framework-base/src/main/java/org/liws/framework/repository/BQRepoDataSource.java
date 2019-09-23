package org.liws.framework.repository;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
<bean id="dataSource" class="org.liws.framework.datasource.repository.BQRepoDataSource" 
	destroy-method="close"/>
 */
public class BQRepoDataSource extends AbstractRoutingDataSource {

    private volatile Map<Object, DataSource> resolvedDataSources;
    private DataSource defaultTargetDataSource;

    public BQRepoDataSource() {
    }

    @Override
    public void afterPropertiesSet() {
        this.resolvedDataSources = new ConcurrentHashMap<>(16);
        BQManagerRepoDataSource defaultDs = new BQManagerRepoDataSource();
        resolvedDataSources.put(SysadminConst.DOMAIN_MARK, new BQManagerRepoDataSource());
        //resolvedDataSources.put("00001", new BQTestRepoDataSource());
        defaultTargetDataSource = defaultDs;
    }
    
    @Override
    protected Object determineCurrentLookupKey() {
        return CurrentDomainMark.getCurrentDomainMark();
    }

    @Override
    protected DataSource determineTargetDataSource() {
        Assert.notNull(this.resolvedDataSources, "DataSource router not initialized");
        Object lookupKey = this.determineCurrentLookupKey();
        DataSource dataSource = null;
        if ("00000".equals(lookupKey) || StringUtils.isEmpty(lookupKey)) {
            dataSource = defaultTargetDataSource;
        } else if (resolvedDataSources.containsKey(lookupKey)) {
            dataSource = this.resolvedDataSources.get(lookupKey);
        } else {
            if (resolvedDataSources.containsKey(lookupKey)) {
                dataSource = resolvedDataSources.get(lookupKey);
            } else {
                synchronized (BQRepoDataSource.class) {
                    String domainMark = Objects.toString(lookupKey);
                    String dbName = "DB" + domainMark;
                    String username = 'U' + domainMark;
                    String password = "PD" + domainMark;
                    dataSource = new BQDomainRepoDatasource(dbName, username, password);
                    resolvedDataSources.put(lookupKey, dataSource);
                }
            }
        }

        if (dataSource == null) {
            throw new IllegalStateException("Cannot determine target DataSource for lookup key [" + lookupKey + "]");
        } else {
            //打印获取连接的日志
//            String url = null;
//            try(Connection conn = dataSource.getConnection()){
//                url = conn.getMetaData().getURL();
//            }catch (SQLException e){
//                AELogger.error(e);
//            }
//            AELogger.info("session user: "+ UserManager.getLoginUser() +"\n  determineCurrentLookupKey:"+lookupKey
//            + "\n  datasource url:"+url);
            //打印获取连接的日志
            return dataSource;
        }
    }

    

    /**
     * 关闭所有DataSource
     */
    public void close(){
        close(defaultTargetDataSource);
        for(Map.Entry<Object,DataSource> entry : resolvedDataSources.entrySet()){
            if(!SysadminConst.DOMAIN_MARK.equals(entry.getKey())){
                close(entry.getValue());
            }
        }
    }
    /*public*/private void close(DataSource dataSource){
        if(dataSource instanceof com.alibaba.druid.pool.DruidDataSource
                &&!((com.alibaba.druid.pool.DruidDataSource)dataSource).isClosed()){
            ((com.alibaba.druid.pool.DruidDataSource)dataSource).close();
        }
    }

}
