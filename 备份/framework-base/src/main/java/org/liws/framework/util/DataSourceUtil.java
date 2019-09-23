package org.liws.framework.util;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.liws.framework.i18n.I18nUtils;
import org.liws.framework.vo.DbType;
import org.springframework.util.StringUtils;

/**
 * 数据源工具类
 * 
 * @author huoqi
 *
 */
public class DataSourceUtil {

	public static Map<String, String[]> URLMap = new HashMap<String, String[]>();
	static {
		URLMap.put("ORACLE", new String[] { "(?i)jdbc:oracle:thin:@//([^/@:]+):(\\d+)(/[^/@:]+)",
				"(?i)jdbc:oracle:thin:@([^/@:]+):(\\d+):([^/@:]+)", "(?i)jdbc:oracle:thin:@([^/@:]+):(\\d+)(/[^/@:]+)",
				"(?i)jdbc:oracle:thin:@\\s*\\(\\s*description\\s*=\\s*\\(\\s*address_list.+",
				"(?i)jdbc:oracle:oci:@//([^/@:]+):(\\d+)(/[^/@:]+)",
				"(?i)jdbc:oracle:oci:@([^/@:]+):(\\d+):([^/@:]+)", "(?i)jdbc:oracle:oci:@([^/@:]+):(\\d+)(/[^/@:]+)",
				"(?i)jdbc:oracle:oci:@\\s*\\(\\s*description\\s*=\\s*\\(\\s*address_list.+"});
		URLMap.put("DB2", new String[] { "(?i)jdbc:db2://([^/@:]+):(\\d+)/([^/@:;=]+)(.*)" });
		URLMap.put("IMPALA", new String[] { "(?i)jdbc:impala://([^/@:]+):(\\d+)/([^/@:;=]+)(.*)", "(?i)jdbc:impala://([^/@:]+):(\\d+)/?([^/@:;=]*);(.*)"});
		URLMap.put("MYSQL", new String[] { "(?i)jdbc:mysql://([^/@:]+):(\\d+)/([^/@:=;?]+)(.*)" });

		URLMap.put("POSTGRESQL", new String[] { "(?i)jdbc:postgresql://([^/@:]+):(\\d+)/([^/@:=;]+)(.*)", "(?i)jdbc:alcedo://([^/@:]+):(\\d+)/([^/@:=;]+)(.*)"});

		URLMap.put("MSSQL", new String[] { "(?i)jdbc:jtds:sqlserver://([^/@:]+):(\\d+)/([^/@:=;]+)",
				"(?i)jdbc:jtds:sqlserver://([^/@:]+):(\\d+);databaseName=([^/@:;=]+)" });

		URLMap.put("MSSQLNATIVE", new String[] { "(?i)jdbc:sqlserver://([^/@:]+):(\\d+);database=([^/@=;:]+)(.*)",
				"(?i)jdbc:microsoft:sqlserver://([^/@:]+):(\\d+);database=([^/@;=:]+)(.*)",
				"(?i)jdbc:sqlserver://([^/@:]+):(\\d+);databaseName=([^/@:;=]+)(.*)",
				"(?i)jdbc:microsoft:sqlserver://([^/@:]+):(\\d+);DatabaseName=([^/@:;=]+)(.*)" });

		URLMap.put("SYBASE", new String[] { "(?i)jdbc:sybase:Tds:hostip:(\\d+)/([^/@:;=]+)" });
		URLMap.put("INFORMIX",
				new String[] { "(?i)jdbc:informix-sqli://([^/@:]+):(\\d+)/([^/@:]+):informixserver=([^/@:]+)" });
		URLMap.put("GBASE8A", new String[] { "(?i)jdbc:gbase://([^/@:]+):(\\d+)/([^/?@:=;]+)(.*)" });
		URLMap.put("DM", new String[] { "(?i)jdbc:dm://([^/@:]+):(\\d+)/([^/?@:=;]+)(.*)" });

	}

	/**
	 * 测试数据库连接
	 * 
	 * @param dbType
	 * @param url
	 * @param username
	 * @param password
	 * @return
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @since 1.0.0.07
	 */
	public static boolean testDataSource(DbType dbType, String url, String username, String password)
			throws SQLException, ClassNotFoundException {
		String sql = null;
		if (DbType.isOracle(dbType)) {
			sql = "select 1 from dual";
		} else if (dbType == DbType.DB2) {
			sql = "select 1 from sysibm.sysdummy1";
		} else {
			sql = "select 1";
		}

        if(dbType == DbType.MYSQL){
            if(url != null && url.indexOf("?") > 0){
                url = url + "&serverTimezone=UTC&nullCatalogMeansCurrent=true&nullNamePatternMatchesAll=true";
            }else{
                url = url +"?serverTimezone=UTC&nullCatalogMeansCurrent=true&nullNamePatternMatchesAll=true";
            }
        }
		if(!dbTypeCompatible(dbType , getDbTypeFormURL(url))){
        	throw new SQLException(I18nUtils.getMessage("bq.framework.util.DataSourceUtil.L0000"));
		}

		Class.forName(dbType.getDriverClass());
		try (Connection conn = DriverManager.getConnection(url, username, password);
				Statement st = conn.createStatement();
				ResultSet rs = st.executeQuery(sql)) {
			if (rs.next()) {
				return rs.getInt(1) == 1;
			}
		}
		return false;
	}
	private static boolean dbTypeCompatible(DbType db1, DbType db2){
		if(db1 == db2){
			return true;
		}else if(DbType.isOracle(db1) && DbType.isOracle(db2)){
			return true;
		}else if(DbType.isSqlServer(db1) && DbType.isSqlServer(db2)){
			return true;
		}else{
			return false;
		}

	}
	/**
	 * 
	 * @param url
	 *            jdbc URL
	 * @return 长度为3的数组，[0] 主机名或IP,[1] 端口，[2]数据库名
	 */
	public static String[] parseJdbcURL(String url) {
		if(StringUtils.isEmpty(url))return null;
		url = url.trim();
		for (Map.Entry<String, String[]> entry : DataSourceUtil.URLMap.entrySet()) {
			for (String s : entry.getValue()) {
				if (url.matches(s)) {
					Pattern pattern = Pattern.compile(s);
					Matcher matcher = pattern.matcher(url);
					if (matcher.find()) {
						if (matcher.groupCount() >= 3) {
							if("IMPALA".equals(entry.getKey()) && !StringUtils.hasText(matcher.group(3))){
								return new String[]{matcher.group(1), matcher.group(2), "default"};
							}else {
								return new String[]{matcher.group(1), matcher.group(2), matcher.group(3)};
							}
						} else {
							return null;
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 *
	 * @param url
	 *            jdbc URL
	 * @return 数据库类型
	 */
	public static DbType getDbTypeFormURL(String url) {
		if(StringUtils.isEmpty(url)){
			return DbType.UNKNOWN;
		}
		String dbTypeStr = "";
		for (Map.Entry<String, String[]> entry : URLMap.entrySet()) {
			for (String s : entry.getValue()) {
				Pattern pattern = Pattern.compile(s, Pattern.CASE_INSENSITIVE);
				Matcher matcher = pattern.matcher(url);
				if (matcher.matches()) {
					dbTypeStr = entry.getKey();
				}
			}
		}
		switch (dbTypeStr){
			case "ORACLE":
				return DbType.ORACLE11;
			case "DB2":
				return DbType.DB2;
			case "MYSQL":
				return DbType.MYSQL;
			case "POSTGRESQL":
				return DbType.POSTGRESQL;
			case "MSSQL":
				return DbType.SQLSERVER2012;
			case "GBASE":
				return DbType.GBASE;
			case "IMPALA":
				return DbType.IMPALA;
				default:
					return DbType.UNKNOWN;
		}
	}

	/**
	 * 连接成jdbcURL
	 *
	 *            jdbc URL
	 * @return
	 */
	public static String connectJdbcURL(DbType dbType, String ip, String port, String dbName) {
		if (dbType == null)
			return "";
		String portStr = "";
		if(StringUtils.hasText(port)){
			portStr = ":" + port;
		}
		switch (dbType) {
		case DB2:
			return "jdbc:db2://" + ip + portStr + "/" + dbName;
		case GBASE:
			return "jdbc:gbase://" + ip + portStr + "/" + dbName;
		case IMPALA:
			if(!StringUtils.hasText(dbName)) {
				return "jdbc:impala://" + ip + portStr + ";AuthMech=0" ;
			}else {
				return "jdbc:impala://" + ip + portStr + "/" + dbName + ";AuthMech=0" ;
			}
			
		case MYSQL:
			return "jdbc:mysql://" + ip + portStr + "/" + dbName;
		case ORACLE10:
		case ORACLE11:
		case ORACLE12:
			if (dbName != null && (dbName.startsWith(":") || dbName.startsWith("/"))) {
				if(dbName.startsWith(":")){
					return "jdbc:oracle:thin:@" + ip + portStr + dbName;
				}else{
					return "jdbc:oracle:thin:@//" + ip + portStr + dbName;
				}
			}else {
				return "jdbc:oracle:thin:@" + ip + portStr + ":" + dbName;
			}

		case POSTGRESQL:
			return "jdbc:postgresql://" + ip + portStr + "/" + dbName;
		case SQLSERVER2008:
		case SQLSERVER2012:
			return "jdbc:sqlserver://" + ip + portStr + ";DatabaseName=" + dbName;

		default:
			break;
		}
		return "";
	}

	/**
	 * PreparedStatement 设置参数
	 * 
	 * @param params
	 * @param ps
	 * @throws SQLException
	 */
	public static void objToPrepareStatement(Object[] params, PreparedStatement ps) throws SQLException {
		if (params == null) {
			return;
		}
		int parameterIndex = 1;
		for (Object param : params) {
			if (param == null) {
				ps.setString(parameterIndex, null);
				parameterIndex++;
				continue;
			}
			Class<? extends Object> paramClass = param.getClass();
			if (paramClass == String.class) {
				ps.setString(parameterIndex, param.toString());
			} else if (paramClass == int.class || paramClass == Integer.class) {
				ps.setInt(parameterIndex, (int) param);
			} else if (paramClass == float.class || paramClass == Float.class) {
				ps.setFloat(parameterIndex, (float) param);
			} else if (paramClass == double.class || paramClass == Double.class) {
				ps.setDouble(parameterIndex, (double) param);
			} else if (paramClass == BigDecimal.class) {
				ps.setBigDecimal(parameterIndex, (BigDecimal) param);
			} else if (paramClass == Date.class) {
				ps.setDate(parameterIndex, (Date) param);
			} else if (paramClass == byte[].class) {
				byte[] bytes = (byte[]) param;
				ps.setBinaryStream(parameterIndex, new ByteArrayInputStream(bytes), bytes.length);
			} else if (paramClass == Object.class) {
				byte[] bytes = objectToByte(param);
				ps.setBinaryStream(parameterIndex, new ByteArrayInputStream(bytes), bytes.length);
			}
			parameterIndex++;
		}
	}

	/**
	 * Object转换byte[]
	 * 
	 * @param obj
	 * @return
	 */
	public static byte[] objectToByte(Object obj) {
		ObjectOutputStream oos = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);
			oos.writeObject(obj);
			byte[] bytes = bos.toByteArray();
			return bytes;
		} catch (IOException e) {// NOSONAR
			return null;
		} finally {
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {// NOSONAR
					oos = null;
				}
			}
		}
	}
	
	public static String removeComments(String sql){
		return SqlCommentRemover.removeComments(sql);
		
	}
}
