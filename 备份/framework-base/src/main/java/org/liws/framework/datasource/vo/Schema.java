package org.liws.framework.datasource.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Schema {
	
	private String schemaName;
	private String[] items;

	public Schema(String schemaName, String[] items) {
		this.schemaName = schemaName;
		this.items = items;
	}
	public Schema(String schemaName) {
		this(schemaName, new String[] {});
	}

}
