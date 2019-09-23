package org.liws.framework.repository;


import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Collections;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.liws.framework.log.BQLogger;
import org.liws.framework.util.ServerConfigReader;
import org.springframework.util.StringUtils;

public class AbstractBQRepoDataSource implements javax.sql.DataSource {
   
	protected DataSource dataSource; // 内部代理

    public AbstractBQRepoDataSource() {
        if (dataSource == null) {
            Properties prop = ServerConfigReader.getServerConfig();
            dataSource = createDataSource(prop);
        }
    }

    public AbstractBQRepoDataSource(String username,String password) {
        if (dataSource == null) {
            Properties prop = ServerConfigReader.getServerConfig();
            dataSource = createDataSource(prop,username,password);
        }
    }

    public AbstractBQRepoDataSource(String dataBaseName,String username,String password) {
        if (dataSource == null) {
            Properties prop = ServerConfigReader.getServerConfig();
            dataSource = createDataSource(prop,dataBaseName,username,password);
        }
    }

    protected DataSource createDataSource(Properties prop){
        String username = prop.getProperty("jdbc.username");
        String password = prop.getProperty("jdbc.password");
        if (StringUtils.hasText(prop.getProperty("jdbc.mima"))) {
            password = prop.getProperty("jdbc.mima");
        }
        return this.createDataSource(prop,username,password);
    }

    protected DataSource createDataSource(Properties prop,String username,String password){
        return this.createDataSource(prop,null,username,password);
    }
    
    protected DataSource createDataSource(Properties prop,String databaseName,String username,String password){
        com.alibaba.druid.pool.DruidDataSource dataSource = new com.alibaba.druid.pool.DruidDataSource();
        //基本属性 url、user、password
        dataSource.setDriverClassName(prop.getProperty("jdbc.driverClassName"));
        if(StringUtils.hasText(databaseName)){
            dataSource.setUrl(prop.getProperty("jdbc.url").replace("domain_mng",databaseName));
        }else{
            dataSource.setUrl(prop.getProperty("jdbc.url"));
        }
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        //配置初始化大小、最小、最大
        dataSource.setInitialSize(Integer.valueOf(prop.getProperty("jdbc.initialSize", "5")));
        dataSource.setMinIdle(Integer.valueOf(prop.getProperty("jdbc.minIdle", "2")));
        dataSource.setMaxActive(Integer.valueOf(prop.getProperty("jdbc.maxActive", "50")));

        //配置获取连接等待超时的时间
        dataSource.setMaxWait(Integer.valueOf(prop.getProperty("jdbc.maxWait", "120000")));

        //配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
        dataSource.setTimeBetweenEvictionRunsMillis(12000);

        //配置一个连接在池中最小生存的时间，单位是毫秒
        dataSource.setMinEvictableIdleTimeMillis(300000);

        if (StringUtils.hasText(prop.getProperty("jdbc.validationQuery"))) {
            dataSource.setValidationQuery(prop.getProperty("jdbc.validationQuery"));
            dataSource.setTestWhileIdle(true);
            dataSource.setTestOnBorrow(true);
            dataSource.setTestOnReturn(false);
        }

        dataSource.setDefaultAutoCommit(false);

        //打开PSCache，并且指定每个连接上PSCache的大小
        dataSource.setPoolPreparedStatements(true);
        dataSource.setMaxPoolPreparedStatementPerConnectionSize(150);

        if (StringUtils.hasText(prop.getProperty("jdbc.removeAbandoned"))) {
            dataSource.setRemoveAbandoned(true);
            dataSource.setRemoveAbandonedTimeout(1800);
        }

        //配置监控统计拦截的filters 
        try {
            dataSource.setFilters("log4j2");
            com.alibaba.druid.filter.stat.StatFilter statFilter = new com.alibaba.druid.filter.stat.StatFilter();
            statFilter.setSlowSqlMillis(10000);
            statFilter.setLogSlowSql(true);
            dataSource.setProxyFilters(Collections.singletonList(statFilter));

        } catch (SQLException e) {
            BQLogger.error(e);
        }

        return dataSource;
    }


/*
    protected PoolConfiguration getPoolConfiguration(Properties prop){
       String username = prop.getProperty("jdbc.username");
       String password = prop.getProperty("jdbc.password");
        if (StringUtils.hasText(prop.getProperty("jdbc.mima"))) {
            password = prop.getProperty("jdbc.mima");
        }
        return this.getPoolConfiguration(prop,username,password);
    }

    protected PoolConfiguration getPoolConfiguration(Properties prop,String databaseName,String username,String password) {
        PoolConfiguration pc = new PoolProperties();
        pc.setDriverClassName(prop.getProperty("jdbc.driverClassName"));
        if(StringUtils.hasText(databaseName)){
            pc.setUrl(prop.getProperty("jdbc.url").replace("domain_mng",databaseName));
        }else{
            pc.setUrl(prop.getProperty("jdbc.url"));
        }
        pc.setUsername(username);
        pc.setPassword(password);


        pc.setMaxActive(Integer.valueOf(prop.getProperty("jdbc.maxActive", "50")));
        pc.setMaxIdle(Integer.valueOf(prop.getProperty("jdbc.maxIdle", "10")));
        pc.setMinIdle(Integer.valueOf(prop.getProperty("jdbc.minIdle", "2")));
        pc.setMaxWait(Integer.valueOf(prop.getProperty("jdbc.maxWait", "120000")));
        pc.setInitialSize(Integer.valueOf(prop.getProperty("jdbc.initialSize", "5")));

        pc.setRemoveAbandoned(true);
        pc.setAbandonWhenPercentageFull(80);
        pc.setLogAbandoned(true);
        pc.setTimeBetweenEvictionRunsMillis(5000);
        pc.setMinEvictableIdleTimeMillis(60000);
        if (StringUtils.hasText("jdbc.validationQuery")) {
            pc.setTestWhileIdle(true);
            pc.setValidationQueryTimeout(3);
            pc.setValidationQuery(prop.getProperty("jdbc.validationQuery"));
            pc.setLogValidationErrors(true);
        }


        pc.setRemoveAbandonedTimeout(180);//超时 3 分钟
        pc.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.ResetAbandonedTimer");

        return pc;
    }

    protected PoolConfiguration getPoolConfiguration(Properties prop,String username,String password) {
        return this.getPoolConfiguration(prop,null, username, password);
    }*/

    @Override
    public Connection getConnection() throws SQLException {
//        org.apache.tomcat.jdbc.pool.DataSource ds = (org.apache.tomcat.jdbc.pool.DataSource)dataSource;
//        BQLogger.error(String.format("NumActive:%d,NumIdle:%d,borrowCount:%d,returnedCount:%d",ds.getNumActive(),ds.getNumIdle(),ds.getBorrowedCount(),ds.getReturnedCount()));
        return dataSource.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return dataSource.getConnection(username, password);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return dataSource.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        dataSource.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        dataSource.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return dataSource.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return dataSource.getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return dataSource.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return dataSource.isWrapperFor(iface);
    }

    public void close(){
        if(dataSource instanceof com.alibaba.druid.pool.DruidDataSource
            &&!((com.alibaba.druid.pool.DruidDataSource)dataSource).isClosed()){
            ((com.alibaba.druid.pool.DruidDataSource)dataSource).close();
        }
    }
}
