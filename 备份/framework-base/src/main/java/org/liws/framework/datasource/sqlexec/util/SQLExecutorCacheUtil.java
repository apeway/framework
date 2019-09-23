package org.liws.framework.datasource.sqlexec.util;


import com.yonyou.bq.framework.cache.CacheManager;
import com.yonyou.bq.framework.cache.core.IDataCache;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;

import javax.sql.rowset.CachedRowSet;

import org.liws.framework.datasource.sqlexec.util.SQLExecutor.CacheSQLExecutor;

import java.util.*;

/**
 * SQLExecutor 开启缓存和清除缓存的Util类
 * Created by zuoym on 2017/6/19.
 */
public class SQLExecutorCacheUtil {

    /**
     * 把SQLExecutor变成缓存查询结果的
     * @param sqlExecutor
     * @return
     */
    public static SQLExecutor makeCacheable(SQLExecutor sqlExecutor) {
        if (sqlExecutor == null) throw new IllegalArgumentException("Parameter sqlExecutor can not be null");
        return new SQLExecutor.CacheSQLExecutor(sqlExecutor);
    }

    /**
     * 去除整个数据源的缓存
     * @param dsPkOrName
     */
    public static void invalidateWholeCache(String dsPkOrName) {
        String key = SQLDataSourceCache.getInstance().generateKey(dsPkOrName);
        IDataCache<CachedRowSet> cache = CacheManager.getCache("ds_data_" + key);
        if (cache != null) {
            cache.clear();
            CacheManager.removeCache("ds_data_" + key);
        }


    }

    /**
     * 根据sql去除缓存
     * @param dsPkOrName
     * @param sql
     * @param parameters
     */
    public static void invalidateCacheBySQL(String dsPkOrName, String sql, Object... parameters) {
        String key = SQLDataSourceCache.getInstance().generateKey(DataSourceCache.getInstance().findByPkOrName(dsPkOrName));
        String sqlKey = sql2String(sql, parameters);
        IDataCache<CachedRowSet> cache = CacheManager.getCache("ds_data_" + key);
        if (cache != null) {
            cache.remove(sqlKey);
        }
    }

    /**
     * 根据表名去除缓存
     * @param dsPkOrName
     * @param tableNames
     */
    public static void invalidateCacheByTableName(String dsPkOrName, String... tableNames) {
        if(tableNames == null || tableNames.length ==0) return;
        Set<String> tableSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for(String tableName : tableNames){
            if(tableName != null)
                tableSet.add(tableName);
        }
        String key = SQLDataSourceCache.getInstance().generateKey(DataSourceCache.getInstance().findByPkOrName(dsPkOrName));
        IDataCache<CachedRowSet> cache = CacheManager.getCache("ds_data_" + key);
        if (cache != null) {
            Set<String> invalidedKeys = new HashSet<>();
            Set<String> set = cache.getKeys();
            for (String k : set) {
                String sql = k;
                if (sql.contains("``|``")) {
                    sql = sql.substring(0, sql.indexOf("``|``"));
                }
                try {
                    Statement st = CCJSqlParserUtil.parse(sql);
                    if (st instanceof Select) {
                        Select select = (Select) st;
                        TablesNamesFinder tnf = new TablesNamesFinder();
                        List<String> tables = tnf.getTableList(select);

                        for (String table : tables) {
                            String tableName = table.indexOf('.') >= 0 ? table.substring(table.lastIndexOf('.') + 1) : table;
                            if (tableSet.contains(tableName)) {
                                invalidedKeys.add(k);
                            }
                        }
                    }
                } catch (JSQLParserException e) {
                    //eat it
                }
            }

            cache.remove(invalidedKeys.toArray(new String[0]));
        }
    }

    /**
     * sql转化为缓存key
     * @param sql
     * @param parameters
     * @return
     */
    public static String sql2String(String sql, Object... parameters) {
        StringBuilder sb = new StringBuilder(sql);
        if (parameters != null && parameters.length > 0) {
            sb.append("``|``");
            for (Object parameter : parameters) {
                sb.append(Objects.toString(parameter));
            }
        }
        return sb.toString();
    }
}
