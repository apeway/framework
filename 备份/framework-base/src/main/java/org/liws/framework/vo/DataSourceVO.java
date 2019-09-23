package org.liws.framework.vo;

//import com.yonyou.bq.framework.annotation.AddOrModify;
//import com.yonyou.bq.framework.annotation.UniqueKey;
//import com.yonyou.bq.framework.annotation.UniqueKeys;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.*;

/**
 * 数据源
 * 
 * @author huoqi
 *
 */
@Entity
@Setter
@Getter
@Table(name = "CONSOLE_DATASOURCE")
//@UniqueKeys(value = @UniqueKey(properties = "dsName"),groups = AddOrModify.class)
public class DataSourceVO extends BaseDomainVO /*implements IModelWrapper*/{

	/**
	 * 执行库Name
	 */
	public static String EXEC_DS_NAME = "__bq_self_exec_ds__";

	private static final long serialVersionUID = 1L;
	
//	@Transient
//	protected Map<String, IModelWrapper[]> children = new HashMap<String, IModelWrapper[]>();

	@Transient
	protected boolean isPrivateDs = false;

	@Transient
	protected boolean importDsObj = false;

	/**
	 * 主键
	 */
    @Id
	@Column(name = "PK_DATASOURCE", length = 36, nullable = false, columnDefinition = "CHAR(36)")
	private String dsPk;

	/**
	 * 数据源名称
	 */
	@Column(name = "DATASOURCE_NAME", length = 255, nullable = false)
	private String dsName;

	/**
	 * 数据源显示名称
	 */
	@Column(name = "DATASOURCE_CAPTION", length = 255)
	private String dsCaption;

	/**
	 * 数据库类型<br>
	 *
	 */
	@Column(name = "DATABASE_TYPE", length = 20)
	@Enumerated(EnumType.STRING)
	private DbType dbType;

	/**
	 * 连接字符串
	 */
	@Column(name = "CONNECT_URL", length = 500)
	private String connectURL;

	/**
	 * 是否启用
	 */
	@Column(name = "IS_ENABLED")
	private int enable = 1;

	/**
	 * 用户名
	 */
	@Column(name = "USERNAME", length = 50)
	private String username;

	/**
	 * 密码
	 */
	@Column(name = "PASSWORD", length = 50)
	private String password;

	
	/**
	 * 最大活跃数，默认50
	 */
	@Column(name = "POOL_MAX_ACTIVE")
	private int poolMaxActive = 300;

	/**
	 * 最大空闲数，默认10
	 */
	@Column(name = "POOL_MAX_IDLE")
	private int poolMaxIdle = 10;

	/**
	 * 最小空闲数，默认0
	 */
	@Column(name = "POOL_MIN_IDLE")
	private int poolMinIdle = 1;

	/**
	 * 最大等待时间(毫秒)，默认30秒
	 */
	@Column(name = "POOL_MAX_WAIT")
	private int poolMaxWait = 30000;

	/**
	 * 初始化连接，默认2
	 */
	@Column(name = "POOL_INITIAL_SIZE")
	private int poolInitialSize = 2;
	
	
	/**
	 * 备注说明
	 */
	@Column(name = "COMMENTS", length = 500)
	private String comments;
	
	/**
	 * 备用字段1
	 */
	@Column(name = "MARK1", length = 80)
	private String mark1;
	
	/**
	 * 备用字段2
	 */
	@Column(name = "MARK2", length = 80)
	private String mark2;
	
	/**
	 * 备用字段3
	 */
	@Column(name = "MARK3", length = 80)
	private String mark3;


    /**
     * 缓存最大数
     */
    @Column(name = "CACHE_MAX_ELE")
    private int cacheMaxElementsInMemory = 300;

    /**
     * 最大空闲时间
     */
    @Column(name = "CACHE_IDLE_SEC")
    private int cacheTimeToIdleSeconds = 1800;


	@Transient
	private String host;
	
	@Transient
	private String port;
	
	@Transient
	private String databaseName;
	
	
	@Transient
	private boolean editAble;
	
	@Transient
	private String kuduIp;
	
	@Transient
	private String kuduPort;
	

	
	/*@Override
	public boolean hasChild() {
		return !children.isEmpty();
	}
	
	@Override
	public IModelWrapper[] getChildren(String className) {
		return children.get(className);
	}

	@Override
	public void addModelToChildren(String className, IModelWrapper...models) {
		if (models == null || models.length == 0) {
			return;
		}
		IModelWrapper[] originModels = getChildren(className);
		if (originModels == null || originModels.length == 0) {
			children.put(className, models);
			return;
		}
		
		IModelWrapper[] newModels = new IModelWrapper[originModels.length + models.length];
		System.arraycopy(originModels, 0, newModels, 0, originModels.length);
		System.arraycopy(models, 0, newModels, originModels.length, models.length);
		
		children.put(className, newModels);
	}

	@Override
	public void removeModelFromChildren(IModelWrapper model) {
		if (model == null) {
			return;
		}
		String className = model.getClass().getName();
		IModelWrapper[] originModels = getChildren(className);
		if (originModels == null || originModels.length == 0) {
			return;
		}

		List<IModelWrapper> newModels = new ArrayList<IModelWrapper>();
		for (IModelWrapper m : originModels) {
			if (!(m.getModelPk().equals(model.getModelPk()))) {
				newModels.add(m);
			}
		}
		children.put(className, newModels.toArray(new IModelWrapper[newModels.size()]));
	}

	@Override
	public String getModelPk() {
		return dsPk;
	}*/
	


	public boolean sqlEquals(DataSourceVO other) {
		if(other == null)return false;
		if (connectURL == null) {
			if (other.connectURL != null)
				return false;
		} else if (!connectURL.equals(other.connectURL))
			return false;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
//		if (poolInitialSize != other.poolInitialSize)
//			return false;
//		if (poolMaxActive != other.poolMaxActive)
//			return false;
//		if (poolMaxIdle != other.poolMaxIdle)
//			return false;
//		if (poolMaxWait != other.poolMaxWait)
//			return false;
//		if (poolMinIdle != other.poolMinIdle)
//			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}

    public boolean cacheEquals(DataSourceVO other) {
            return this.sqlEquals(other) && other.cacheMaxElementsInMemory == this.cacheMaxElementsInMemory
                    && other.cacheTimeToIdleSeconds == this.cacheTimeToIdleSeconds;

    }
	
	

}
