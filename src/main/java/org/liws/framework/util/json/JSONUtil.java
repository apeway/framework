package org.liws.framework.util.json;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.introspect.AnnotatedClass;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.map.ser.FilterProvider;
import org.codehaus.jackson.map.ser.impl.SimpleBeanPropertyFilter;
import org.codehaus.jackson.map.ser.impl.SimpleFilterProvider;
import org.codehaus.jackson.type.JavaType;


/**
 * 基于jackson-jaxrs
 * org.codehaus.jackson.map.ObjectMapper
 */
@SuppressWarnings({"deprecation", "unchecked" })
public abstract class JSONUtil {
	private JSONUtil() {}
	
	private static ObjectMapper mapper;
	private static Consumer<ObjectMapper> defaultObjectMapperConsumer; // 默认设置
	
	static {
		mapper = new ObjectMapper();
		defaultObjectMapperConsumer = new Consumer<ObjectMapper>(){
			
			@Override
			public void accept(ObjectMapper t) {
				// 反序列化时，遇到在json串中有的属性在实体类中不存在的情况，是否抛异常。（默认为true）
				mapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);  
				// XXX 这句需要吗？
				mapper.getDeserializationConfig().set(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				
				// 序列化时的日期格式处理。（默认是转成时间戳）
				// 取消jackjson默认处理时间的格式，设置后输出的时间格式为"1970-01-01T00:00:00.000+0000"
				mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
				// 可以自定义时间格式
				mapper.setDateFormat(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
			}
		};
		// 应用默认设置
		defaultObjectMapperConsumer.accept(mapper);
	}
	

	/**
	 * @param consumerForNewMapper
	 * 		若此属性不为null，则表示需要重新申请一个ObjectMapper实例来使用，consumerForNewMapper用来对新的ObjectMapper实例进行一些自定义设置。
	 */
	public static synchronized ObjectMapper getMapperInstance(Consumer<ObjectMapper> consumerForNewMapper) {
		if(consumerForNewMapper != null) {
			ObjectMapper newMapper = new ObjectMapper();
			consumerForNewMapper.accept(newMapper);
			return newMapper;
		}
		return mapper;
	}
	
	public static String toJson(Object obj) throws RuntimeException {
		return toJson(obj, (Set<String>) null);
	}

	/**
	 * @param ignoreFields 忽略的字段集合
	 */
	public static String toJson(Object obj, Set<String> ignoreFields) throws RuntimeException {
		return toJson(obj, ignoreFields, null);
	}

	/**
	 * @param consumerForNewMapper 若此属性不为null，则表示需要重新申请一个ObjectMapper实例来使用，consumerForNewMapper用来对新的ObjectMapper实例进行一些自定义设置。
	 */
	public static String toJson(Object obj, Consumer<ObjectMapper> consumerForNewMapper) throws RuntimeException {
		return toJson(obj, (Set<String>) null, consumerForNewMapper);
	}
	
	/**
	 * @param ignoreFields 忽略的字段集合
	 * @param consumerForNewMapper 
	 * 		若此属性不为null，则表示需要重新申请一个ObjectMapper实例来使用，consumerForNewMapper用来对新的ObjectMapper实例进行一些自定义设置。
	 */
	public static String toJson(Object obj, Set<String> ignoreFields, Consumer<ObjectMapper> consumerForNewMapper) throws RuntimeException {
		if (obj == null) {
			return null;
		}
		
		try {
			if (obj.getClass() == String.class) {
				return (String) obj;
			} else {
				ObjectMapper objectMapper = getMapperInstance(consumerForNewMapper);
				
				if (ignoreFields != null) {
					objectMapper.setSerializationConfig(objectMapper.getSerializationConfig().withSerializationInclusion( 
		                    JsonSerialize.Inclusion.NON_NULL)); 
		
		            FilterProvider filters = new SimpleFilterProvider().addFilter(obj.getClass().getName(), 
		                    SimpleBeanPropertyFilter.serializeAllExcept(ignoreFields)); 
		            objectMapper.setFilters(filters); 
		
		            objectMapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector() { 
		                @Override 
		                public Object findFilterId(AnnotatedClass ac) { 
		                    return ac.getName(); 
		                } 
		            }); 
				}
				
				String json = objectMapper.writeValueAsString(obj);
				return json;
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(),e);
		}
	}

	
	
	/**
	 * 转换为对象
	 */
	public static <T> T toBean(String json, Class<T> cls) throws RuntimeException {
		return toBean(json, cls, null);
	}
	/**
	 * 转换为对象
	 * @param consumerForNewMapper 
	 * 		若此属性不为null，则表示需要重新申请一个ObjectMapper实例来使用，consumerForNewMapper用来对新的ObjectMapper实例进行一些自定义设置。
	 */
	public static <T> T toBean(String json, Class<T> cls, Consumer<ObjectMapper> consumerForNewMapper) 
			throws RuntimeException {
		if (json == null) {
			return null;
		}
		
		try {
			if (cls == String.class) {
				return (T) json;
			} else {
				return getMapperInstance(consumerForNewMapper).readValue(json, cls);
			}
		} catch (Exception e) {
            throw new RuntimeException(e.getMessage(),e);
		}
	}
	
	
	/**
	 * 转换泛型list
	 */
	public static <T> List<T> toListBean(String json, Class<T> cls) throws RuntimeException {
		return toListBean(json, cls, null);
	}
	/**
	 * 转换泛型list
	 * @param consumerForNewMapper 
	 * 		若此属性不为null，则表示需要重新申请一个ObjectMapper实例来使用，consumerForNewMapper用来对新的ObjectMapper实例进行一些自定义设置。
	 */
	public static <T> List<T> toListBean(String json, Class<T> cls, Consumer<ObjectMapper> consumerForNewMapper) throws RuntimeException {
		try{
			JavaType javaType = mapper.getTypeFactory().constructParametricType(List.class, cls);
			return (List<T>)getMapperInstance(consumerForNewMapper).readValue(json, javaType);
		}catch(Exception e){
			throw new RuntimeException(e.getMessage(),e);
		}
	}

	/**
	 * 转换泛型数组
	 */
	public static <T> T[] toArrayBean(String json, Class<T> cls) throws RuntimeException{
		return toArrayBean(json, cls, null);
	}
	/**
	 * 转换泛型数组
	 * @param consumerForNewMapper 
	 * 		若此属性不为null，则表示需要重新申请一个ObjectMapper实例来使用，consumerForNewMapper用来对新的ObjectMapper实例进行一些自定义设置。
	 */
	public static <T> T[] toArrayBean(String json, Class<T> cls, Consumer<ObjectMapper> consumerForNewMapper) throws RuntimeException{
		try{
			List<T> listBean = toListBean(json, cls, consumerForNewMapper);
			T[] array = (T[])Array.newInstance(cls, listBean.size());
			return listBean.toArray(array);
		}catch(Exception e){
			throw new RuntimeException(e.getMessage(),e);
		}
	}
}