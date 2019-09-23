package org.liws.framework.vo;


import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.liws.framework.shiro.UserManager;
import org.liws.framework.util.AnnotationUtil;
import org.liws.framework.vo.json.CustomLocalDateTimeDeserializer1;
import org.liws.framework.vo.json.CustomLocalDateTimeDeserializer2;
import org.liws.framework.vo.json.CustomLocalDateTimeSerializer1;
import org.liws.framework.vo.json.CustomLocalDateTimeSerializer2;
import org.springframework.data.domain.Persistable;
import org.springframework.util.StringUtils;

import lombok.Getter;

/**
 * PO基础类
 * @author zuoym
 *
 */
@MappedSuperclass
public abstract class BaseVO implements Serializable, Persistable<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	@Column(name = "CREATE_TIME")
	@Getter
    @org.codehaus.jackson.map.annotate.JsonSerialize(using = CustomLocalDateTimeSerializer1.class)
    @org.codehaus.jackson.map.annotate.JsonDeserialize(using = CustomLocalDateTimeDeserializer1.class)
    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = CustomLocalDateTimeSerializer2.class)
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	protected LocalDateTime createTime;

	@Column(name = "MODIFY_TIME")
	@Getter
    @org.codehaus.jackson.map.annotate.JsonSerialize(using = CustomLocalDateTimeSerializer1.class)
    @org.codehaus.jackson.map.annotate.JsonDeserialize(using = CustomLocalDateTimeDeserializer1.class)
    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = CustomLocalDateTimeSerializer2.class)
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	protected LocalDateTime  modifyTime;

    @Column(name = "CREATOR", length = 50)
    @Getter
    protected String creator;

    @Column(name = "MODIFIER", length = 50)
    @Getter
    protected String modifier;
	
	@PrePersist
	public void makeCreateTime(){
        createTime = LocalDateTime.now();
//        UserVO user = UserManager.getLoginUser();
//        if(user != null){
//            creator = user.getUserName();
//        }else{
//            creator = "#BQ#";
//        }
        if(this instanceof IVersion){
			((IVersion)this).setLockedVersion(java.util.UUID.randomUUID().toString());
		}
	}
	
	@PreUpdate
	public void makeModifyTime(){
		modifyTime = LocalDateTime.now();
//        UserVO user = UserManager.getLoginUser();
//        if(user != null){
//            modifier = user.getUserName();
//        }else{
//            modifier = "#BQ#";
//        }
		if(this instanceof IVersion){
			((IVersion)this).setLockedVersion(java.util.UUID.randomUUID().toString());
		}
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {

        return super.clone();

	}

    @Override
    public boolean equals(Object that) {
        if (this == that)
            return true;
        if (that == null || getClass() != that.getClass())
            return false;
        String thisId = this.getId();
        String thatId = ((BaseVO)that).getId();
        return thisId != null && thisId.equals(thatId);

    }
    @Override
    public int hashCode() {
        String id = getId();
        return id == null ? super.hashCode() : id.hashCode();
    }

    public String getId(){
	    return Objects.toString(AnnotationUtil.findFieldValue(this,Id.class),null);
    }
    public String getPk(){
        return getId();
    }


    public boolean isNew() {
        return null == getId() || !StringUtils.hasText(getId());
    }
}
