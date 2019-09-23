package org.liws.framework.repository;

import com.yonyou.bq.framework.shiro.UserManager;
import com.yonyou.bq.framework.vo.UserVO;

import java.util.Collections;

import org.liws.framework.exception.BusinessException;

public abstract class SysadminConst {

    private SysadminConst() throws BusinessException {
        throw new BusinessException("Can not instance");
    }

    public static final String PK = String.join("", Collections.nCopies(36,"p"));
    public static final String ROLE_NAME = "sysadmin";
    public static final String NAME = "admin";
    public static final String DOMAIN_MARK = "00000";


    public static final String PERSONAL_DOMAIN_MARK = "00001";

    /**
     * 是否是系统管理员
     * @return
     */
    public static boolean isSysAdmin(){
        UserVO user = UserManager.getLoginUser();
        return user != null && PK.equals(user.getUserPk());
    }
}
