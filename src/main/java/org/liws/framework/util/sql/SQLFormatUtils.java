package org.liws.framework.util.sql;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.engine.jdbc.internal.FormatStyle;

public class SQLFormatUtils {
	
    /**
     * Formatting for SELECT, INSERT, UPDATE and DELETE statements
     */
	public static String formatBasicSQL(String sql){
		if(StringUtils.isEmpty(sql)){
			return "";
		}
		
		//格式完后，1.减少前缀4个空格   2.替换首行的换行 3.变换为大写 
		return FormatStyle.BASIC.getFormatter().format(sql.trim()).replaceAll("\\n\\s{4}+","\n").substring(1);
	}
	
	/**
     * Formatting for DDL (CREATE, ALTER, DROP, etc) statements
     */
    public static String formatDDLSQL(String sql){
        if(StringUtils.isEmpty(sql)){
            return "";
        }
        
        //格式完后，1.减少前缀4个空格   2.替换首行的换行 3.变换为大写 
        return FormatStyle.DDL.getFormatter().format(sql.trim()).replaceAll("\\n\\s{4}+","\n").substring(1);
    }
	
}