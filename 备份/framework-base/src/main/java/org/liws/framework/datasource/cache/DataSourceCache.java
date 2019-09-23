package org.liws.framework.datasource.cache;

import java.util.List;

import org.liws.framework.repository.CurrentDomainMark;
import org.liws.framework.repository.SysadminConst;
import org.liws.framework.vo.DataSourceVO;
import org.liws.framework.vo.DbType;

import com.yonyou.bq.framework.service.InnerDataSourceService;
import com.yonyou.bq.framework.spring.AELocator;
import com.yonyou.bq.framework.vo.DataSourceObj;


/**
 * 数据源操作缓存,直接操作该类可维护DatasourceVO
 * 注意如果增加,修改或者删除数据源,需要把把该类在Service中使用,并在接口用@AETransactional声明事务
 * @author zuoym
 *
 */
public class DataSourceCache {
	private static DataSourceCache ONE = new DataSourceCache();
	
	private InnerDataSourceService dataSourceService ;
	
	private DataSourceCache() {
        dataSourceService = AELocator.lookup(InnerDataSourceService.class);
	}

	/**
	 * 获取单例
	 * @return
	 */
	public static DataSourceCache getInstance() {
		return ONE;
	}

	/**
	 * 根据PK查询
	 * @param pk
	 * @return
	 */
	public DataSourceVO findByPk(final String pk) {
		if(dataSourceService == null)return null;
		return  dataSourceService.findByPk(pk);
	}

	/**
	 * 根据名称查询
	 * @param dsName
	 * @return
	 */
	public DataSourceVO findByName(final String dsName) {
		if(dataSourceService == null)return null;
		return dataSourceService.findByName(dsName);
	}
	
	/**
	 * 根据名称或Pk查询
	 * @param pkOrName
	 * @return
	 */
	public DataSourceVO findByPkOrName(final String pkOrName) {
		if(dataSourceService == null)return null;
		if(DataSourceVO.EXEC_DS_NAME.equals(pkOrName)){
			try {
				CurrentDomainMark.setPrimaryCurrentDomainMark(SysadminConst.DOMAIN_MARK);
				return dataSourceService.findByPkOrName(pkOrName);
			}finally {
				CurrentDomainMark.setPrimaryCurrentDomainMark(null);
			}
		}else{
			return dataSourceService.findByPkOrName(pkOrName);
		}

	}
	
	
	/**
	 * 根据自定义jql语句查询
	 * @param whereJQLClause 条件语句，不要包含where关键字
	 * @param parameters 参数
	 * @return
	 */
	public List<DataSourceVO>  findList(final String whereJQLClause,final Object... parameters) {
		if(dataSourceService == null)return null;
		return dataSourceService.findList(whereJQLClause, parameters);
	}

	/**
	 * 是否导入数据源对象
	 * @param dsPk
	 * @return
	 */
	public boolean isImportDaObj(String dsPk) {
		return dataSourceService != null && dataSourceService.isImportDaObj(dsPk);
	}
	

	/**
	 * 添加
	 * @param ds
	 */
	public DataSourceVO add(DataSourceVO ds) {
		if(dataSourceService == null)return null;
		return dataSourceService.add(ds);
	}
	/**
	 * 根据PK查找数据连接导入对象
	 * @param dsObjPk
	 * @return 
	 */
	public DataSourceObj findDsObjByPk(String dsObjPk) {
		if(dataSourceService == null)return null;
		return this.dataSourceService.findDsObjByPk(dsObjPk);
	}
	
	/**
	 * 根据数据源PK查找数据连接导入对象
	 * @param dsPk
	 * @return 
	 */
	public List<DataSourceObj> findDsObjByDsPk(String dsPk) {
		if(dataSourceService == null)return null;
		return this.dataSourceService.findDsObjByDsPk(dsPk);
	}
	
	/**
	 * 清除再添加数据连接导入的对象
	 * @param dsObjs
	 */
	public void saveDsObj(String dsPk, DataSourceObj... dsObjs) {
		if(dataSourceService == null)return;
		this.dataSourceService.saveDsObj(dsPk, dsObjs);
	}
	
	/**
	 * 删除
	 * @param dsPk
	 */
	public void removeDsObjByDsPk(String dsPk) {
		if(dataSourceService == null)return;
		dataSourceService.removeDsObjByDsPk(dsPk);		
	}

	/**
	 * 修改
	 * @param ds
	 */
	public void modify(DataSourceVO ds) {
		if(dataSourceService == null)return;
		dataSourceService.modify(ds);
	}

	/**
	 * 根据PK删除
	 * @param pk
	 */
	public void removeByPk(String pk) {
		if(dataSourceService == null)return;
		dataSourceService.removeByPk( pk);
	}

	/**
	 * 根据名称删除
	 * @param dsName
	 */
	public void removeByName(String dsName) {
		if(dataSourceService == null)return;
		dataSourceService.removeByName(dsName);
	}
	
	/**
	 * 加载所有
	 * @return
	 */
	public List<DataSourceVO> findAll(){
		if(dataSourceService == null)return null;
		return dataSourceService.findAll();
	}
	
	/**
	 * 获取数据源的数据类型
	 * @param dsName
	 * @return
	 */
	public DbType getDbTypeByDsName(String dsName){
		if(dataSourceService == null)return null;
		return dataSourceService.getDbTypeByDsName(dsName);
	}
	
	/**
	 * 根据PK查找数据连接导入对象
	 * @param dsPk
     * @param dsObjCode
	 * @return 
	 */
	public DataSourceObj findDataSourceObj(String dsPk,String dsObjCode){
		if(dataSourceService == null)return null;
		return dataSourceService.findDataSourceObj( dsPk, dsObjCode);
	}

}
