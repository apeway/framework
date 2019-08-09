package test.datasource;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
/**
 * 数据源
 * @author koujp
 *
 */
public class JtaDataSource extends PooledDataSource{// extends  AtomikosNonXADataSourceBean implements DataSource{

	@Override
	public void setPassword( String string )
    {
		//在此作密码解密
		String decodeString=string;
		if(string != null && string.startsWith("Encrypt:")){
			// decodeString = EncryptUtil.decryptPassword(string);
		}
		super.setPassword(decodeString);
    }
}
