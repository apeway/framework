package org.liws.framework.datasource.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 表的字段
 */
@Getter
@Setter
public class Field {

	private String name;
	private boolean nullAble;
	private int dataType;
	private String dataTypeName;
	private int length;
	private int precision;
	private int scale;
	private String comment;
	private String tableName;
	private String tableComment;
	
}
