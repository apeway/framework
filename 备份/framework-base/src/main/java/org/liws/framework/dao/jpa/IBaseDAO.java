package org.liws.framework.dao.jpa;



import java.util.List;

import org.liws.framework.vo.BaseVO;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author zuoym
 *
 * @param <T>
 */
public interface IBaseDAO<T extends BaseVO> extends JpaRepository<T, String> {

    /**
     * 通过jsql查询数据
     * @param whereClaus 查询的条件语句，可以包含order by
     * @param pageIndex 查询的页数，从1开始
     * @param recordPerPage 每页的大小
     * @param paramVarArgs 条件参数
     * @return
     */
    List<T> findList(String whereClaus, int pageIndex, int recordPerPage,Object... paramVarArgs);

    /**
     *
     * @param sql
     * @param paramVarArgs
     */
    int executeSQL(String sql, Object... paramVarArgs);

    /**
     * 执行jsql
     * @param jql
     * @param paramVarArgs
     */
    int executeJQL(String jql, Object... paramVarArgs);

    /**
     * 使用本地SQL查询
     * @param sql
     * @param paramVarArgs
     * @return
     */
    List<Object[]> queryUsingSQL(String sql, Object... paramVarArgs);

    /**
     * 执行本地sql
     * @param whereClause
     * @param paramObject
     */
     void deleteByClause(String whereClause, Object... paramObject);

    /**
     * 删除验证
     * @param vo
     * @return
     */
     String[] validateDelete(T vo);

    /**
     * 删除验证
     * @param pk
     * @return
     */
    String[] validateDelete(String pk);

    /**
     * 保存验证
     * @param vo
     * @return
     */
    String[] validateSave(T vo);

    /**
     * 重置vo的主键，可用于复制,只重置该VO本身
     * @param vo
     */
     void resetPk(T vo);

    /**
     * 重置vo的主键，可用于复制，如果deep为true，则重置其子元素
     * @param vo
     */
    void resetPk(T vo,boolean deep);

    /**
     * 泛化类的类型
     * @return
     */
    Class<T> getGenericClass();

    /**
     * 条件计数
     * @param whereClaus
     * @param paramVarArgs
     * @return
     */
    long count(String whereClaus,Object... paramVarArgs);

    /**
     * 条件求和
     * @param columnName
     * @param whereClaus
     * @param paramVarArgs
     * @return
     */
    double sum(String columnName, String whereClaus, Object... paramVarArgs);

    /**
     * 获得当前域
     * @return
     */
    String getCurrentDomainMark();

    /**
     *
     * @param code
     * @return
     */
    long getSerialCount(String code);

//    /**
//     * 分布式同步执行，能保证检测和执行同步执行，即：多个执行该方法时,相同的code，包括跨tomcat，只能顺序进行
//     * @param code
//     * @param predicate
//     * @param procedure
//     * @return
//     */
//    boolean distributedExecute(String code, Predicate predicate, Procedure procedure);

    /**
     * 通过主键查询
     * @param pk
     * @return
     */
    T findByPk(String pk);

    /**
     * 通过主键删除
     * @param pk
     * @return
     */
    void deleteByPk(String pk);

    /**
     * 更新审计信息
     */
    void updateModifyInfo(String pk);
}
