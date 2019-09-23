package org.liws.framework.datasource.sqlexec.util;


import com.yonyou.bq.framework.cache.CacheBuilder;
import com.yonyou.bq.framework.cache.CacheManager;
import com.yonyou.bq.framework.cache.config.CacheConfig;
import com.yonyou.bq.framework.cache.config.CacheEvictionPolicy;
import com.yonyou.bq.framework.cache.config.DataCacheConfigItem;
import com.yonyou.bq.framework.cache.core.IDataCache;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.liws.framework.datasource.cache.AECachedRowSet;
import org.liws.framework.datasource.cache.DataSourceCache;
import org.liws.framework.datasource.cache.SQLDataSourceCache;
import org.liws.framework.datasource.sqlexec.ExecBatchPreStatementSetter;
import org.liws.framework.datasource.sqlexec.ExecConnectionCallback;
import org.liws.framework.datasource.sqlexec.ExecPreStatementSetter;
import org.liws.framework.datasource.sqlexec.ExecRowCallbackHandler;
import org.liws.framework.datasource.sqlexec.ResultSetProcessor;
import org.liws.framework.exception.BusinessRuntimeException;
import org.liws.framework.log.BQLogger;
import org.liws.framework.spring.SpringContextHolder;
import org.liws.framework.vo.DataSourceVO;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;

import javax.sql.DataSource;
import javax.sql.rowset.CachedRowSet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * 数据库SQL执行器，对JDBC的封装
 * 注意如果在使用自定义数据源时，自定义数据发生改变后，已经获得SQLExecutor的线程使用的是旧连接
 *
 * @author zuoym
 */
public class SQLExecutor {

    final static Map<Map<String, JdbcTemplate>, Integer> all_thread = new WeakHashMap<>();
    private final static ThreadLocal<Map<String, JdbcTemplate>> holder = new ThreadLocal<>();
    protected String keyStr;
    private JdbcTemplate jdbc = null;

    private SQLExecutor(JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbcTemplate;
    }

    private SQLExecutor(String pkOrName) {
        DataSource dataSource = null;
        if (StringUtils.isEmpty(pkOrName)) {
            this.jdbc = (JdbcTemplate) SpringContextHolder.getContext().getBean("jdbcTemplate");
        } else {
            keyStr = SQLDataSourceCache.getInstance().generateKey(pkOrName);
            if (holder.get() != null && holder.get().containsKey(keyStr)) {
                this.jdbc = holder.get().get(keyStr);
                if (all_thread.get(holder.get()) != null && Integer.valueOf(0).equals(all_thread.get(holder.get()))) {
                    try {
                        dataSource = SQLDataSourceCache.getInstance().getSQLDatasource(pkOrName);
                        createJdbcTemplate(dataSource, keyStr);
                    } catch (Exception e) {
                        BQLogger.error(e);
                        throw new BusinessRuntimeException(e.getMessage(), e);
                    }
                }
            } else {
                if (holder.get() == null) {
                    holder.set(new HashMap<>());
                }
                try {
                    dataSource = SQLDataSourceCache.getInstance().getSQLDatasource(pkOrName);
                    createJdbcTemplate(dataSource, keyStr);
                } catch (Exception e) {
                    BQLogger.error(e);
                    throw new BusinessRuntimeException(e.getMessage(), e);
                }
            }
        }

    }

    private SQLExecutor(DataSourceVO dsVO) {
        DataSource dataSource = null;

        String key = SQLDataSourceCache.getInstance().generateKey(dsVO);
        this.keyStr = key;
        if (holder.get() != null && holder.get().containsKey(key)) {
            this.jdbc = holder.get().get(key);
            if (all_thread.get(holder.get()) != null && Integer.valueOf(0).equals(all_thread.get(holder.get()))) {
                try {
                    dataSource = SQLDataSourceCache.getInstance().getSQLDatasource(dsVO);
                    createJdbcTemplate(dataSource, key);
                } catch (Exception e) {
                    BQLogger.error(e);
                    throw new BusinessRuntimeException(e.getMessage(), e);
                }
            }
        } else {
            if (holder.get() == null) {
                holder.set(new HashMap<>());
            }
            try {
                dataSource = SQLDataSourceCache.getInstance().getSQLDatasource(dsVO);
                createJdbcTemplate(dataSource, key);
            } catch (Exception e) {
                BQLogger.error(e);
                throw new BusinessRuntimeException(e.getMessage(), e);
            }
        }

    }

    /**
     * 创建一个SQLExecutor，传递DataSourceVO
     *
     * @param dataSourceVO
     * @return
     */
    public static SQLExecutor createSQLExecutor(DataSourceVO dataSourceVO) {
        return new SQLExecutor(dataSourceVO);
    }

    /**
     * 创建一个SQLExecutor，传递为null或空时返回的是资源数据库
     *
     * @param dsName
     * @return
     */
    public static SQLExecutor createSQLExecutor(String dsName) {
        return new SQLExecutor(dsName);
    }

    /**
     * 创建一个SQLExecutor，返回的是资源数据库
     *
     * @return
     */
    public static SQLExecutor createSQLExecutor() {
        return new SQLExecutor("");
    }


    private void createJdbcTemplate(DataSource dataSource, String key) {
        synchronized (holder) {
            this.jdbc = new JdbcTemplate(dataSource);
            holder.get().put(key, this.jdbc);
            all_thread.put(holder.get(), 1);
        }

    }

    /**
     * 执行sql，返回二维对象结果集
     *
     * @param sql
     * @param params
     * @return
     */
    public Object[][] execQuery(String sql, Object... params) {
        ExecRowCallbackHandler rch = new ExecRowCallbackHandler();
        CachedRowSet crs = this.execQueryForResultSet(sql, params);
        try {
            while (crs.next()) {
                rch.processRow(crs);
            }
        } catch (SQLException e) {
            throw new BusinessRuntimeException(e);
        }


//        if (ArrayUtils.isEmpty(params)) {
//            jdbc.query(sql, rch);
//        } else {
//            PreparedStatementSetter paramSetter = new ExecPreStatementSetter(params);
//            jdbc.query(sql, paramSetter, rch);
//        }
        return rch.getResult();
    }

    /**
     * 执行SQL返回javax.sql.rowset.CachedRowSet,与ResultSet使用方法相同，但与数据库的连接是关闭的
     *
     * @param sql
     * @param params
     * @return SqlRowSet
     */
    public CachedRowSet execQueryForResultSet(String sql, Object... params) {
//        return this.execQuery(sql, params, rs -> {
//            CachedRowSet ocr = RowSetProvider.newFactory().createCachedRowSet();
//            ocr.populate(rs);
//            return ocr;
//        });
         if (ArrayUtils.isEmpty(params)) {
            return jdbc.query(sql, new ResultSetExtractor<CachedRowSet>() {
            	public CachedRowSet extractData(ResultSet rs) throws SQLException, DataAccessException{
            		CachedRowSet ocr = new AECachedRowSet();
	                ocr.populate(rs);
	                return ocr;
            	}
                
            });
        } else {
            PreparedStatementSetter paramSetter = new ExecPreStatementSetter(params);
            return jdbc.query(sql, paramSetter, new ResultSetExtractor<CachedRowSet>() {
            	public CachedRowSet extractData(ResultSet rs) throws SQLException, DataAccessException{
                CachedRowSet ocr = new AECachedRowSet();
                ocr.populate(rs);
                return ocr;
               }
                
            });
        }
    }


    /**
     * 执行sql，回调ResultSetExtractor处理结果集,返回指定对象结果
     *
     * @param sql
     * @param params
     * @param rsp
     * @return
     */
    public <T> T execQuery(String sql, Object[] params, ResultSetProcessor<T> rsp) {
//        if (ArrayUtils.isEmpty(params)) {
//            return jdbc.query(sql, rsp);
//        } else {
//            PreparedStatementSetter paramSetter = new ExecPreStatementSetter(params);
//            return jdbc.query(sql, paramSetter, rsp);
//        }


        if (rsp != null) {
            CachedRowSet crs = this.execQueryForResultSet(sql, params);
            try {
                return rsp.handleResultSet(crs);
            } catch (SQLException e) {
                throw new BusinessRuntimeException(e);
            }
        } else {
            throw new IllegalArgumentException("ResultSetProcessor can not be null");
        }

    }

    /**
     * 执行sql，回调ResultSetExtractor处理结果集,返回多个对象结果
     *
     * @param sql
     * @param params
     * @param params
     * @return
     */
    public List<Map<String, Object>> execQueryForMapList(String sql, Object... params) {
        ColumnMapRowMapper rowMapper = new ColumnMapRowMapper();
        return this.execQuery(sql, params, rs -> {
                    List<Map<String, Object>> results = null;
                    if (rs instanceof CachedRowSet) {
                        results = new ArrayList<>(((CachedRowSet) rs).size());
                    } else {
                        results = new ArrayList<>();
                    }
                    int rowNum = 0;
                    while (rs.next()) {
                        results.add(rowMapper.mapRow(rs, rowNum++));
                    }
                    return results;
                }
        );
    }

    /**
     * 批量执行SQL
     *
     * @param sqls
     * @return
     */
    public int[] execBatch(String... sqls) {
        int[] batchResult = jdbc.batchUpdate(sqls);
        return batchResult;
    }

    /**
     * 获得行数,用于select count 查询
     *
     * @param sql
     * @return
     * @throws Exception
     */
    public long getRecordCount(String sql, Object... params) {
//        Long result = jdbc.queryForObject(sql, Long.class);
//        return result;
        Object[][] result = this.execQuery(sql, params);
        return Long.valueOf(Objects.toString(result[0][0]));
    }

    /**
     * 执行DDL
     *
     * @param ddlSQL
     */
    public void execDDL(String ddlSQL) {
        jdbc.execute(ddlSQL);
    }

    /**
     * 执行ExecConnectionCallback,注意在ExecConnectionCallback内部要关闭创建的statement和resultset
     *
     * @param callBack
     */
    public <T> T execConnectionCallBack(ExecConnectionCallback<T> callBack) {
        return jdbc.execute(callBack);
    }

    /**
     * 执行插入、更新和删除
     *
     * @param sql
     * @return
     */
    public int execUpdate(String sql, Object... params) {

        return this.execConnectionCallBack(new ExecConnectionCallback<Integer>(){

            @Override
            public Integer doInConnection(Connection con) throws SQLException, DataAccessException {
                try(PreparedStatement ps = con.prepareStatement(sql)) {
                    if (ArrayUtils.isEmpty(params)) {
                        int singleResult = ps.executeUpdate();
                        return singleResult;
                    } else {
                        ExecPreStatementSetter preStatementSetter = new ExecPreStatementSetter(params);
                        preStatementSetter.setValues(ps);
                        return ps.executeUpdate();
                    }
                }finally {
                    con.commit();
                }
            }
        });
    }

    /**
     * 执行批量更新
     *
     * @param sql
     * @return
     */
    public int[] execBatchUpdate(final String sql, final Object[][] paramArray) {
        return this.execConnectionCallBack(new ExecConnectionCallback<int[]>(){

            @Override
            public int[] doInConnection(Connection con) throws SQLException, DataAccessException {
                try(PreparedStatement ps = con.prepareStatement(sql)) {
                    if (ArrayUtils.isEmpty(paramArray)) {
                        int singleResult = ps.executeUpdate(sql);
                        return new int[]{singleResult};
                    } else {
                        ExecBatchPreStatementSetter preStatementSetter = new ExecBatchPreStatementSetter(paramArray);
                        for (int i = 0; i < paramArray.length; i++) {
                            preStatementSetter.setValues(ps,i);
                            ps.addBatch();
                        }
                        return ps.executeBatch();
                    }
                }finally {
                    con.commit();
                }
            }
        });

    }

    static class CacheSQLExecutor extends SQLExecutor {
        private volatile IDataCache<CachedRowSet> cache;
        private final String cacheKey;

        CacheSQLExecutor(SQLExecutor dsVO) {
            super(dsVO.jdbc);
            this.keyStr = dsVO.keyStr;
            cacheKey = "ds_data_" + keyStr;
            createCache();
        }

        private boolean createCache() {
            this.cache = CacheManager.getCache(cacheKey);
            String name = keyStr.substring(keyStr.indexOf('^')+1);
            if(name.indexOf('^')>0)
                name=name.substring(0,name.lastIndexOf('^'));
            DataSourceVO dsVO = DataSourceCache.getInstance().findByName(name);
            if(dsVO != null && dsVO.getCacheTimeToIdleSeconds() <= 0){
                return false;
            }
            synchronized (SQLExecutor.class) {
                this.cache = CacheManager.getCache(cacheKey);
                if (cache == null) {
                    CacheBuilder cacheBuilder = CacheBuilder.newCacheBuilder(cacheKey);
                    cacheBuilder.isCluster(false).isLarge(false);
                    cacheBuilder.maxElementsInMemory(dsVO != null && dsVO.getCacheMaxElementsInMemory() > 0 ? dsVO.getCacheMaxElementsInMemory() : 300);
                    cacheBuilder.timeToIdleSeconds(dsVO != null && dsVO.getCacheTimeToIdleSeconds() > 0 ? dsVO.getCacheTimeToIdleSeconds() : 1800);
                    cacheBuilder.evictionPolicy(CacheEvictionPolicy.LRU);
                    cacheBuilder.serializeType(DataCacheConfigItem.SERIALIZE_TYPE_KRYO);
                    this.cache = cacheBuilder.createCache();
                    //this.cache.clear();
                }

            }
            return true;
        }

        @Override
        public CachedRowSet execQueryForResultSet(String sql, Object... params) {
            boolean isCache = true;
            if(this.cache == null){
                isCache = createCache();
            }
            if(!isCache){//如果缓存时间小于等于0,则不适用缓存
                return super.execQueryForResultSet(sql, params);
            }else {
                String key = SQLExecutorCacheUtil.sql2String(sql, params);
                CachedRowSet result;
                if (cache.exists(key)) {
                    result = cache.get(key);
                } else {
                    DataCacheConfigItem cacheConfig = CacheManager.getCacheConfig(cacheKey);
                    int maxTime = (cacheConfig != null && cacheConfig.getTimeToIdleSeconds() > 0) ? cacheConfig.getTimeToIdleSeconds() : 1800;
                    result = super.execQueryForResultSet(sql, params);
                    if (result != null && result.size() <= 500000) {//50万以上的结果不缓存
                        cache.put(key, result, maxTime);
                    }
                }

                try {
                    if (result != null && !result.isBeforeFirst()) {
                        result.beforeFirst();
                    }
                } catch (SQLException e) {
                    result = super.execQueryForResultSet(sql, params);
                }
                return result;
            }


        }


    }

}
