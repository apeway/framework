package org.liws.framework.datasource.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Catalog {
	
	private String catalogName;
	private String[] items;

	public Catalog(String catalogName, String[] items) {
		this.catalogName = catalogName;
		this.items = items;
	}
	public Catalog(String catalogName) {
		this(catalogName, new String[] {});
	}

}
