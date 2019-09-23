package org.liws.framework.datasource.sqlexec;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 结果集处理器<br>
 * 需要继承实现handleResultSet方法
 * 
 * @author huoqi
 *
 * @param <T>
 */
@FunctionalInterface
public interface ResultSetProcessor<T> extends ResultSetExtractor<T> {

	@Override
	default public T extractData(ResultSet rs) throws SQLException, DataAccessException {
		return handleResultSet(rs);
	}
	
	T handleResultSet(ResultSet rs) throws SQLException;
}
