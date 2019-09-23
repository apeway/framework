package org.liws.framework.datasource.sqlexec;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.liws.framework.util.DataSourceUtil;
import org.springframework.jdbc.core.PreparedStatementSetter;

/**
 * 参数设置器，关联java.sql.PreparedStatement
 * 
 * @author zuoym
 *
 */
public class ExecPreStatementSetter implements PreparedStatementSetter {

	private Object[] params;

	public ExecPreStatementSetter(Object[] params) {
		this.params = params;
	}

	@Override
	public void setValues(PreparedStatement ps) throws SQLException {
		DataSourceUtil.objToPrepareStatement(params, ps);
	}
	
}
