package org.liws.framework.validate;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.liws.framework.i18n.I18MessageHandler;
import org.liws.framework.vo.BaseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;


public class ValidateErrorHandler implements IValidateErrorHandler {
	
	@Autowired
	private I18MessageHandler messageSource;


	@Override
	public Map<String,String> handErrorResult(BindingResult results){
		Map<String,String> result = new HashMap<>();
		Map<String,String> codeMap = new HashMap<>();
		List<ObjectError> allErrors = results.getAllErrors();
		if(allErrors != null){
			for(ObjectError e : allErrors){
				if(e instanceof FieldError){
					FieldError fe = (FieldError)e;
					Object[] args = new Object[e.getArguments().length];
					args[0] = fe.getRejectedValue();
					System.arraycopy(e.getArguments(), 1,args, 1, e.getArguments().length-1);
					String key = fe.getField()+"Error";
					String message = toI18Message(e.getDefaultMessage(),args);
					if(!result.containsKey(key)){
						result.put(key,message);
						codeMap.put(key, e.getCode());
					}else{
						String ms = codeMap.get(key);
						if(ms != null && ms.compareTo(e.getCode()) < 0){
							result.put(key,message);
							codeMap.put(key, e.getCode());
						}
					}
					
				}else{
					Object[] args = new Object[e.getArguments().length-1];
					System.arraycopy(e.getArguments(), 1,args, 1, e.getArguments().length-1);
					result.put(e.getObjectName()+"Error",messageSource.getMessage(e.getDefaultMessage(), args));
				}
				
			}
		}
		
		return result;
	}

	private String toI18Message(String code, Object[] args) {

        return messageSource.getMessage(code, args);
    }

    @Override
	public <T extends BaseVO> String[] handErrorResult(Set<ConstraintViolation<T>> results) {
		String[] r = new String[results.size()];
		int i = 0;
		for(ConstraintViolation<? extends BaseVO> cv : results){
			r[i++] = cv.getMessage();
		}
		return r;
	}
}
