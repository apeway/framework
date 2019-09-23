package org.liws.framework.datasource.sqlexec;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public  interface  ExecConnectionCallback<T> extends ConnectionCallback<T> {

	
	T doInConnection(Connection con) throws SQLException, DataAccessException;

}
