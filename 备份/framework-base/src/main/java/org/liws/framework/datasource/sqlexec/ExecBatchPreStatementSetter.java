package org.liws.framework.datasource.sqlexec;


import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.liws.framework.util.DataSourceUtil;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;

public class ExecBatchPreStatementSetter implements BatchPreparedStatementSetter {

	private Object[][] paramArray;

	public ExecBatchPreStatementSetter(Object[][] paramArray) {
		this.paramArray = paramArray;
	}

	@Override
	public void setValues(PreparedStatement ps, int i) throws SQLException {
		Object[] params = paramArray[i];
		DataSourceUtil.objToPrepareStatement(params, ps);
	}

	@Override
	public int getBatchSize() {
		if (paramArray == null) {
			return 0;
		}
		return paramArray.length;
	}

}
