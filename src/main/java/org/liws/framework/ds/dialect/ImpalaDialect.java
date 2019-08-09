package org.liws.framework.ds.dialect;

import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.pagination.AbstractLimitHandler;
import org.hibernate.dialect.pagination.LimitHandler;
import org.hibernate.dialect.pagination.LimitHelper;
import org.hibernate.engine.spi.RowSelection;

import java.sql.Types;

public class ImpalaDialect extends Dialect {

	private static final LimitHandler LIMIT_HANDLER = new AbstractLimitHandler() {
		@Override
		public String processSql(String sql, RowSelection selection) {
			final boolean hasOffset = LimitHelper.hasFirstRow( selection );
			return sql + (hasOffset ? " limit ? offset ?" : " limit ?");
		}

		@Override
		public boolean supportsLimit() {
			return true;
		}
	};

	public ImpalaDialect() {
		super();
		registerColumnType( Types.BOOLEAN, "BOOLEAN" );
		registerColumnType( Types.BIGINT, "BIGINT" );
		registerColumnType( Types.BIT, "BOOLEAN" );
		registerColumnType( Types.CHAR, "STRING" );
		registerColumnType( Types.DATE, "TIMESTAMP" );
		registerColumnType( Types.DECIMAL, "DOUBLE" );
		registerColumnType( Types.NUMERIC, "DOUBLE" );
		registerColumnType( Types.DOUBLE, "DOUBLE" );
		registerColumnType( Types.FLOAT, "FLOAT" );
		registerColumnType( Types.INTEGER, "BIGINT" );
		registerColumnType( Types.SMALLINT, "BIGINT" );
		registerColumnType( Types.TINYINT, "BIGINT" );
		registerColumnType( Types.TIMESTAMP, "TIMESTAMP" );
		registerColumnType( Types.VARCHAR, "STRING" );
		
		//在有的mysql视图中，varchar(1000)会读出longvarchar类型
		registerColumnType( Types.LONGVARCHAR, "STRING" );
	}
	
	@Override
	public LimitHandler getLimitHandler() {
		return LIMIT_HANDLER;
	}

	@Override
	public String getTypeName(int code, long length, int precision, int scale) throws HibernateException {
		return super.getTypeName(code, length, precision, scale);
	}
	
	
}
