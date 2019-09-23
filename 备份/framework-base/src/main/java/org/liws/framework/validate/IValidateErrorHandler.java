package org.liws.framework.validate;

import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.liws.framework.vo.BaseVO;
import org.springframework.validation.BindingResult;

public interface IValidateErrorHandler {

	Map<String, String> handErrorResult(BindingResult results);

	<T extends BaseVO> String[] handErrorResult(Set<ConstraintViolation<T>> results);
}
