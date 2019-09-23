package org.liws.framework.vo;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseDomainVO extends BaseVO{

    @Transient
    protected String domainMark;

    @Transient
    protected String domainTemplateCode;
}
