package org.liws.framework.datasource.vo;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.liws.framework.datasource.sqlexec.util.SQLExecutor;
import org.liws.framework.exception.BusinessRuntimeException;
import org.liws.framework.log.BQLogger;
import org.liws.framework.vo.DataSourceVO;
import org.liws.framework.vo.DbType;
import org.springframework.util.StringUtils;


/**
 * 获取数据库元数据
 * 
 * @author zuoym
 *
 */
public class DataSourceMetaInfo {

	protected String[] tables;
	protected Map<String, Set<String>> tableMap;
	protected String[] views;
	protected Map<String, Set<String>> viewMap;
	protected String[] synonyms;
	protected Map<String, Set<String>> synonymMap;
	protected Catalog[] catalogs;
	protected Schema[] schemas;
	protected String[] procedures;

	protected DataSourceVO dsVO;

	/**
	 * Create a new DatabaseMetaData object for the given database connection
	 */
	public DataSourceMetaInfo(DataSourceVO dsVO) {
		this.dsVO = dsVO;
	}

	/**
	 * @return Returns the catalogs.
	 */
	public Catalog[] getCatalogs() {
		return catalogs;
	}

	/**
	 * @param catalogs
	 *            The catalogs to set.
	 */
	public void setCatalogs(Catalog[] catalogs) {
		this.catalogs = catalogs;
	}

	/**
	 * @return Returns the DatabaseMeta.
	 */
	public DataSourceVO getDataSourceVO() {
		return dsVO;
	}

	/**
	 * @return Returns the schemas.
	 */
	public Schema[] getSchemas() {
		return schemas;
	}

	/**
	 * @param schemas
	 *            The schemas to set.
	 */
	public void setSchemas(Schema[] schemas) {
		this.schemas = schemas;
	}

	/**
	 * @return Returns the tables.
	 */
	public String[] getTables() {
		return tables;
	}

	/**
	 * @param tables
	 *            The tables to set.
	 */
	protected void setTables(String[] tables) {
		this.tables = tables;
	}

	/**
	 * @return Returns the views.
	 */
	public String[] getViews() {
		return views;
	}

	protected void setViews(String[] views) {
		this.views = views;
	}

	/**
	 * @return Returns the synonyms.
	 */
	public String[] getSynonyms() {
		return synonyms;
	}

	/**
	 * @return Returns the synonyms.
	 */
	protected void setSynonyms(String[] synonyms) {
		this.synonyms = synonyms;
	}

	/**
	 * @return Returns the procedures.
	 */
	public String[] getProcedures() {
		return procedures;
	}

	public void getData() throws BusinessRuntimeException {
		getData("TABLE","VIEW");
	}

	public void getDataOnlyTable() throws BusinessRuntimeException {
		getData("TABLE");
	}

	public void getDataOnlyView() throws BusinessRuntimeException {
		getData("VIEW");
	}

	/**
	 * 获取总体的元数据
	 * 
	 * @throws BusinessRuntimeException
	 */
	public void getData(String... types) throws BusinessRuntimeException {
		if (dsVO == null || dsVO.getDbType() == null)
			return;
		if(types == null || types.length == 0){
			return;
		}
		Set<String> set = new TreeSet<>(String::compareToIgnoreCase);
		set.addAll(Arrays.asList(types));
		SQLExecutor sqlExecutor = SQLExecutor.createSQLExecutor(dsVO);
		sqlExecutor.execConnectionCallBack(conn -> {

			DatabaseMetaData dbmd = conn.getMetaData();
			DbType dbType = dsVO.getDbType();

			if (dbType.supportsCatalogs() && dbmd.supportsCatalogsInTableDefinitions()) {
				ArrayList<Catalog> catalogList = new ArrayList<Catalog>();

				try (ResultSet catalogResultSet = dbmd.getCatalogs();) {
					while (catalogResultSet != null && catalogResultSet.next()) {
						String catalogName = catalogResultSet.getString(1);
						catalogList.add(new Catalog(catalogName));
					}
				}

				// Now loop over the catalogs...
				//
				for (Catalog catalog : catalogList) {
					ArrayList<String> catalogTables = new ArrayList<String>();
					try (ResultSet catalogTablesResultSet = dbmd.getTables(catalog.getCatalogName(), null, null,
							null)) {

						while (catalogTablesResultSet.next()) {
							String tableName = catalogTablesResultSet.getString(3);

							if (!dbType.isSystemTable(tableName)) {
								catalogTables.add(tableName);
							}
						}
						Collections.sort(catalogTables);
					}

					// Save the list of tables in the catalog (can be empty)
					//
					catalog.setItems(catalogTables.toArray(new String[catalogTables.size()]));
				}

				// Save for later...
				setCatalogs(catalogList.toArray(new Catalog[catalogList.size()]));
			}
			if (dbType.supportsSchemas() && dbmd.supportsSchemasInTableDefinitions()) {
				ArrayList<Schema> schemaList = new ArrayList<Schema>();
				if (schemaList.size() == 0) {
					String sql = dbType.getSQLListOfSchemas();
					if (StringUtils.hasText(sql)) {
						try (Statement schemaStatement = conn.createStatement();
								ResultSet schemaResultSet = schemaStatement.executeQuery(sql);) {
							while (schemaResultSet != null && schemaResultSet.next()) {
								String schemaName = schemaResultSet.getString("name");
								schemaList.add(new Schema(schemaName));
							}
						}
					} else {
						try (ResultSet schemaResultSet = dbmd.getSchemas();) {
							while (schemaResultSet != null && schemaResultSet.next()) {
								String schemaName = schemaResultSet.getString(1);
								schemaList.add(new Schema(schemaName));
							}
						}
					}
				}

				// Save for later...
				setSchemas(schemaList.toArray(new Schema[schemaList.size()]));
			}

			if(set.contains("TABLE")){
				setTables(getTables(null, false, new String[] { "TABLE" })); // legacy
				// call
				setTableMap(getTableMap(null, new String[] { "TABLE" }));
			}


			if (set.contains("VIEW") && dbType.supportsViews()) {
				setViews(getTables(null, false, new String[] { "VIEW" })); // legacy
				// call
				setViewMap(getTableMap(null, new String[] { "VIEW" }));
			}

			if (set.contains("SYNONYM") && dbType.supportsSynonyms()) {
				setSynonyms(getTables(null, false, new String[] { "SYNONYM" })); // legacy
				// call
				setSynonymMap(getTableMap(null, new String[] { "SYNONYM" }));
			}

			// setProcedures(db.getProcedures());
			return null;
		});
	}

	/**
	 * 获取指定schema下的元数据
	 * 
	 * @param schemanamein
	 * @param includeSchema
	 * @param types
	 * @return
	 * @throws BusinessRuntimeException
	 */
	public String[] getTables(String schemanamein, boolean includeSchema, String[] types)
			throws BusinessRuntimeException {
		Map<String, Set<String>> tableMap = getTableMap(schemanamein, types);
		List<String> res = new ArrayList<String>();
		for (String schema : tableMap.keySet()) {
			Collection<String> synonyms = tableMap.get(schema);
			for (String synonym : synonyms)
				if (includeSchema) {
					res.add(getSchemaTableCombination(schema, synonym));
				} else {
					res.add(synonym);
				}
		}
		return res.toArray(new String[res.size()]);
	}
	
	/**
	 * 检查表是否存在
	 * @param schemaName
	 * @param tableName
	 * @return
	 * @throws BusinessRuntimeException
	 */
	public boolean checkTableExist(String schemaName, String tableName){
		if (dsVO == null || dsVO.getDbType() == null)
			return false;
		SQLExecutor sqlExecutor = SQLExecutor.createSQLExecutor(dsVO);
		return sqlExecutor.execConnectionCallBack(conn -> {
			try (PreparedStatement pst = conn.prepareStatement(
					"select * from " + getSchemaTableCombination(schemaName, tableName) + " where 0=1 ",
					ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {

				pst.setMaxRows(1);
				try (ResultSet rs = pst.executeQuery();) {
					return true;
				}
			} catch (SQLException e) {
				return false;
			}
		});
	}
	
	/**
	 * 根据查询获取字段
	 * @param sql
	 * @return
	 * @throws BusinessRuntimeException
	 */
	public Field[] getQueryFields(String sql) throws BusinessRuntimeException {
		if (dsVO == null || dsVO.getDbType() == null)
			return null;
		SQLExecutor sqlExecutor = SQLExecutor.createSQLExecutor(dsVO);
		Field[] result = sqlExecutor.execConnectionCallBack(conn -> {
				try (PreparedStatement pst = conn.prepareStatement(sql)) {

					pst.setMaxRows(1);
					try (ResultSet rs = pst.executeQuery();) {
						ResultSetMetaData metadata = rs.getMetaData();
						int count = metadata.getColumnCount();
						Field[] fields = new Field[count];
						for (int i = 1; i <= count; i++) {
							Field field = new Field();
							field.setName(metadata.getColumnName(i));
							field.setDataType(metadata.getColumnType(i));
							field.setDataTypeName(metadata.getColumnTypeName(i));
							field.setNullAble(metadata.isNullable(i) == ResultSetMetaData.columnNullable);
							field.setLength(metadata.getColumnDisplaySize(i));
							field.setPrecision(metadata.getPrecision(i));
							field.setScale(metadata.getScale(i));
							fields[i - 1] = field;

						}
						return fields;
					}

				} catch (SQLException e) {
					throw new BusinessRuntimeException("Error getting fields from  [" + sql + "]",e);
				}
		});
		return result;
	}

	/**
	 * 获取指定schema下的元数据
	 * 
	 * @param schemaName
	 * @param tableName
	 * @return
	 * @throws BusinessRuntimeException
	 */
	public Field[] getFields(String schemaName, String tableName) throws BusinessRuntimeException {
		return this.getFields(schemaName,tableName,false);
	}

    /**
     * 获取指定schema下的元数据
     *
     * @param schemaName
     * @param tableName
     * @return
     * @throws BusinessRuntimeException
     */
    public Field[] getFields(String schemaName, String tableName,boolean includeComment) throws BusinessRuntimeException {
        String sql = "select * from " + getSchemaTableCombination(schemaName, tableName);
        Field[] fields =  this.getQueryFields(sql);
        if(!includeComment){
            return fields;
        }
        boolean useSchema = false;
        sql = null;
        if(DbType.isOracle(this.dsVO.getDbType())){
            useSchema = true;
            sql = "select t.table_name,t.comments table_comment,c.column_name ,c.comments column_comment " +
                    " from all_col_comments c  " +
                    " left join all_tab_comments t on (c.table_name=t.table_name and c.owner=t.owner) " +
                    " where upper(c.table_name)=upper(?) and Upper(c.owner)=upper(?)";
        }else if(DbType.isSqlServer(dsVO.getDbType())){
            sql = "select t.name table_name,cast(tp.value as varchar) table_comment,c.name column_name, cast(cp.value as varchar) column_comment from sys.columns c " +
                    "  inner join sys.sysobjects t on(t.xtype in ('U','V') and c.object_id=t.id) " +
                    "  left join sys.extended_properties tp on (t.id=tp.major_id and tp.name = 'MS_Description' and tp.minor_id=0) " +
                    "  left join sys.extended_properties cp on (t.id=cp.major_id and cp.name = 'MS_Description' and cp.minor_id=c.column_id) " +
                    "  where upper(t.name)=upper(?)";
        }else if(this.dsVO.getDbType() == DbType.DB2){
            useSchema = true;
            sql = "SELECT t.tabname table_name,t.remarks table_comment,colname column_name,c.remarks column_comment" +
                    " FROM syscat.columns c left join syscat.tables t on (c.tabname=t.tabname and c.tabschema=t.tabschema)" +
                    " where upper(c.tabname)=upper(?) and Upper(c.tabschema)=upper(?)";
        }else if(this.dsVO.getDbType() == DbType.POSTGRESQL){
            sql = "select tabs.table_name,pg_catalog.obj_description(ctabs.oid) table_comment,column_name ,pg_catalog.obj_description(ctabs.oid) column_comment" +
                    " from information_schema.columns cols left join information_schema.tables tabs on cols.table_name=tabs.table_name" +
                    " left join pg_catalog.pg_class ctabs on ctabs.relname=tabs.table_name left join pg_catalog.pg_class ccols on ccols.relname=cols.column_name" +
                    " where upper(tabs.table_name)=upper(?)";
        }else if(this.dsVO.getDbType() == DbType.MYSQL){
            sql = "SELECT t.table_name,table_comment,column_name,column_comment" +
                    " FROM information_schema.columns c left join information_schema.tables t on (c.table_name=t.table_name and c.table_schema=t.table_schema)" +
                    " where upper(c.table_name)=upper(?) and c.table_schema=(select database())";
        }
        if(sql != null) {
            SQLExecutor sqlExecutor = SQLExecutor.createSQLExecutor(dsVO);
            Object[][] queryResult = null;
            if (useSchema) {
                if (!StringUtils.hasText(schemaName)) {
                    schemaName = dsVO.getUsername();
                }
                queryResult = sqlExecutor.execQuery(sql, tableName, schemaName);
            } else {
                queryResult = sqlExecutor.execQuery(sql, tableName);
            }
            if (queryResult != null) {
                for (Object[] row : queryResult) {
                    for (Field f : fields) {
                        if (f.getName().equalsIgnoreCase(Objects.toString(row[2]))) {
                            f.setTableComment(Objects.toString(row[1]));
                            f.setTableName(tableName);
                            f.setComment(Objects.toString(row[3]));
                            break;
                        }
                    }
                }
            }
        }

        return fields;
    }
    public Map<String,String> getTableComments(String schemaName, String[] tableNames) throws BusinessRuntimeException {
        Map<String,String> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        boolean useSchema = false;
        String sql = null;
        if(DbType.isOracle(this.dsVO.getDbType())){
            useSchema = true;
            sql = "select t.table_name,t.comments table_comment from " +
                    " all_tab_comments t  " +
                    " where Upper(t.owner)=upper(?) order by table_name";
        }else if(DbType.isSqlServer(dsVO.getDbType())){
            sql = "select t.name table_name,cast(tp.value as varchar) table_comment from sys.sysobjects t  " +
                    "  left join sys.extended_properties tp on (t.id=tp.major_id and tp.name = 'MS_Description' and tp.minor_id=0) " +
                    "  where t.xtype in ('U','V')";
        }else if(this.dsVO.getDbType() == DbType.DB2){
            useSchema = true;
            sql = "SELECT t.tabname table_name,t.remarks table_comment " +
                    " FROM syscat.tables t " +
                    " where Upper(t.tabschema)=upper(?)";
        }else if(this.dsVO.getDbType() == DbType.POSTGRESQL){
            sql = "select tabs.table_name,pg_catalog.obj_description(ctabs.oid) table_comment" +
                    " from information_schema.tables tabs  " +
                    " left join pg_catalog.pg_class ctabs on ctabs.relname=tabs.table_name " ;
        }else if(this.dsVO.getDbType() == DbType.MYSQL){
            sql = "SELECT t.table_name,table_comment from information_schema.tables t  where " +
                    " t.table_schema=(select database()) order by table_name";
        }
        if(sql != null) {
            SQLExecutor sqlExecutor = SQLExecutor.createSQLExecutor(dsVO);
            Object[][] queryResult = null;
            if (useSchema) {
                if (!StringUtils.hasText(schemaName)) {
                    schemaName = dsVO.getUsername();
                }
                queryResult = sqlExecutor.execQuery(sql, schemaName);
            } else {
                queryResult = sqlExecutor.execQuery(sql);
            }
            if (queryResult != null) {
                for (Object[] row : queryResult) {
                    for (String t : tableNames) {
                        if (t != null && t.equalsIgnoreCase(Objects.toString(row[0]))) {
                            result.put(t,Objects.toString(row[1]));
                            break;
                        }
                    }
                }
            }
        }else{
            if(tableNames != null){
                Arrays.stream(tableNames).forEach(e -> result.put(e,e));
            }
        }

        return result;
    }




	public String getSchemaTableCombination(String schemaName, String table_part) {
		if (StringUtils.hasText(schemaName)) {
			return schemaName + "." + table_part;
		} else {
			return table_part;
		}

	}

	private Map<String, Set<String>> getTableMap(String schemaName, String[] types) throws BusinessRuntimeException {
		if (dsVO == null || dsVO.getDbType() == null)
			return null;
		final String finalSchemaName;
		if (schemaName == null && dsVO.getDbType().useSchemaNameForTableList()) {
			finalSchemaName = dsVO.getUsername().toUpperCase();
		}else{
			finalSchemaName = schemaName;
		}
		Map<String, Set<String>> tableMap = new HashMap<String, Set<String>>();
		SQLExecutor sqlExecutor = SQLExecutor.createSQLExecutor(dsVO);
		sqlExecutor.execConnectionCallBack(conn -> {
			try (ResultSet alltables = conn.getMetaData().getTables(null, finalSchemaName, null, types);) {

				while (alltables.next()) {
					// due to PDI-743 with ODBC and MS SQL Server the order is
					// changed and
					// try/catch included for safety
					String cat = alltables.getString("TABLE_CAT");
					String schema = alltables.getString("TABLE_SCHEM");

					if (StringUtils.isEmpty(schema))
						schema = cat;

					String table = alltables.getString("TABLE_NAME");

					multimapPut(schema, table, tableMap);
				}
			} catch (SQLException e) {
				BQLogger.error("Error getting tablenames from schema [" + finalSchemaName + "]");
			}
			return null;
		});

		return tableMap;
	}

	private <K, V> void multimapPut(final K key, final V value, final Map<K, Set<V>> map) {
		Set<V> valueCollection = map.get(key);
		if (valueCollection == null) {
			valueCollection = new HashSet<V>();
		}
		valueCollection.add(value);
		map.put(key, valueCollection);
	}
	
	public String getCurrentSchema(){
		if (dsVO == null || dsVO.getDbType() == null)
			return null;
		if(DbType.isOracle(dsVO.getDbType()) || dsVO.getDbType() == DbType.DB2){
			return dsVO.getUsername() != null ? dsVO.getUsername().toUpperCase() : null;
		}else if(DbType.isSqlServer(dsVO.getDbType())){
			return "dbo";
		}else{
			SQLExecutor sqlExecutor = SQLExecutor.createSQLExecutor(dsVO);
			return sqlExecutor.execConnectionCallBack(Connection::getSchema);
		}
	}

	public Map<String, Set<String>> getTableMap() {
		return tableMap;
	}

	public void setTableMap(Map<String, Set<String>> tableMap) {
		this.tableMap = tableMap;
	}

	public Map<String, Set<String>> getViewMap() {
		return viewMap;
	}

	public void setViewMap(Map<String, Set<String>> viewMap) {
		this.viewMap = viewMap;
	}

	public Map<String, Set<String>> getSynonymMap() {
		return synonymMap;
	}

	public void setSynonymMap(Map<String, Set<String>> synonymMap) {
		this.synonymMap = synonymMap;
	}

}
