package org.liws.framework.datasource.sqlexec;

import org.springframework.jdbc.core.RowCallbackHandler;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * RowCallbackHandler的默认实现，按行处理结果集，返回二维数组
 * 
 * @author huoqi
 *
 */
public class ExecRowCallbackHandler implements RowCallbackHandler {

	private List<Object[]> result = new ArrayList<Object[]>();
	private int columnCount = -1;

	public ExecRowCallbackHandler() {
	}

	@Override
	public void processRow(ResultSet rs) throws SQLException {
	    if(columnCount < 0) {
            ResultSetMetaData metaData = rs.getMetaData();
            columnCount = metaData.getColumnCount();
        }
		List<Object> row = new ArrayList<Object>();
		for (int index = 1; index <= columnCount; index++) {
			row.add(rs.getObject(index));
		}
		result.add(row.toArray(new Object[row.size()]));
	}

	public Object[][] getResult() {
		return result == null ? null : result
				.toArray(new Object[result.size()][]);
	}
}
