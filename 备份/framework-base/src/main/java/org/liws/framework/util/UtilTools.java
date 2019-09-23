package org.liws.framework.util;

//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import com.yonyoucloud.bq.framework.exception.BusinessRuntimeException;
//import com.yonyoucloud.bq.framework.log.BQLogger;
//import com.yonyoucloud.bq.framework.vo.json.*;
import org.apache.commons.beanutils.MethodUtils;
//import org.apache.commons.codec.binary.Base64;
//import org.apache.commons.lang.ObjectUtils;
//import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.*;

/**
 * 常用工具类
 */
public class UtilTools {

	private UtilTools() {
	}

	/**
	 * 获取jvm启动时参数 -Dbq_config
	 * @return 如果指定了相应的jvm启动参数则返回相应值，否则返回字符串"classpath"
	 */
	public static String getBQConfig() {
		String configPath = null;
		//configPath = System.getProperty("bq_config");
		try { // 安全问题???
			configPath = Objects.toString(MethodUtils.invokeStaticMethod(System.class, "getProperty", new Object[]{"bq_config"}));
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			//BQLogger.error(e);
		}
		
		if (StringUtils.isBlank(configPath) || "null".equalsIgnoreCase(configPath.trim())) {
			configPath = "classpath";
		}
		return configPath;
	}
	
	// 加密解密 <-- 
	private static final int RADIX = 16;
	private static final String SEED = "0933910847463829827159347601486730416058";
	public static final String encryptPassword(String password) {
		if (password == null || password.length() == 0)
			return "";

		BigInteger bi_passwd = new BigInteger(password.getBytes());

		BigInteger bi_r0 = new BigInteger(SEED);
		BigInteger bi_r1 = bi_r0.xor(bi_passwd);

		return bi_r1.toString(RADIX);
	}
	public static final String decryptPassword(String encrypted) {
		if (encrypted == null || encrypted.length() == 0)
			return "";

		BigInteger bi_confuse = new BigInteger(SEED);

		BigInteger bi_r1 = new BigInteger(encrypted, RADIX);
		BigInteger bi_r0 = bi_r1.xor(bi_confuse);

		return new String(bi_r0.toByteArray());
	}
	// -->
	
	public static void main(String[] args) {
	}
	
	
//	private static final String[] hexDigits = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d",
//			"e", "f" };
//	
//	/**
//	 * 获取当前时间 时间格式为yyyy-MM-dd HH:mm:ss
//	 * 
//	 * @return
//	 */
//	public static String getCurrentTime() {
//		Date now = new Date();
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		String nowTime = sdf.format(now);
//		return nowTime;
//	}
//
//    public static String getDateTimeStr(LocalDateTime date) {
//	    if(date != null){
//	        return  date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//        }else {
//	        return null;
//        }
//    }
//	/**
//	 * 返回一个对象的json字符串
//	 *
//	 * @param data
//	 * @return
//	 */
//	public static String toJSON(Object data) {
//		return toJSON(data,false);
//	}
//
//	/**
//	 * 返回一个对象的json字符串
//	 * 
//	 * @param data
//	 * @param format  是否格式化
//	 * @return
//	 */
//	public static String toJSON(Object data,boolean format) {
//		GsonBuilder gsonBuilder = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss")
//                .setExclusionStrategies(new GsonIgnoreStrategy())
//				.registerTypeAdapter(java.sql.Timestamp.class, new TimestampTypeAdapter())
//				.registerTypeAdapter(java.sql.Date.class, new SQLDateTypeAdapter())
//				.registerTypeAdapter(java.time.LocalDate.class, new LocalDateTypeAdapter())
//				.registerTypeAdapter(java.time.LocalDateTime.class, new LocalDateTimeTypeAdapter())
//				.registerTypeAdapter(java.time.LocalTime.class, new LocalTimeTypeAdapter());
//				if(format){
//					gsonBuilder.setPrettyPrinting();
//				}
//		Gson gson = gsonBuilder.create();
//		return gson.toJson(data);
//	}
//
//	public static <T> T json2Object(String content, Class<T> clazz) {
//		Gson gson = new GsonBuilder().setDateFormat("yyyy/MM/dd HH:mm:ss")
//                .setExclusionStrategies(new GsonIgnoreStrategy())
//				.registerTypeAdapter(java.sql.Timestamp.class, new TimestampTypeAdapter())
//				.registerTypeAdapter(java.sql.Date.class, new SQLDateTypeAdapter())
//				.registerTypeAdapter(java.time.LocalDate.class, new LocalDateTypeAdapter())
//				.registerTypeAdapter(java.time.LocalDateTime.class, new LocalDateTimeTypeAdapter())
//				.registerTypeAdapter(java.time.LocalTime.class, new LocalTimeTypeAdapter())
//				.create();
//		return gson.fromJson(content,clazz);
//
//	}
//	public static <T> List<T> json2List(String content, Type type) {
//		Gson gson = new GsonBuilder().setDateFormat("yyyy/MM/dd HH:mm:ss")
//                .setExclusionStrategies(new GsonIgnoreStrategy())
//				.registerTypeAdapter(java.sql.Timestamp.class, new TimestampTypeAdapter())
//				.registerTypeAdapter(java.sql.Date.class, new SQLDateTypeAdapter())
//				.registerTypeAdapter(java.time.LocalDate.class, new LocalDateTypeAdapter())
//				.registerTypeAdapter(java.time.LocalDateTime.class, new LocalDateTimeTypeAdapter())
//				.registerTypeAdapter(java.time.LocalTime.class, new LocalTimeTypeAdapter())
//				.create();
//		return gson.fromJson(content,type);
//
//	}
//
//	public static String join(Collection<String> args, String spliter) {
//		StringBuilder sb = new StringBuilder();
//		if (args != null && !args.isEmpty()) {
//			for (Iterator<String> it = args.iterator(); it.hasNext();) {
//				sb.append(it.next());
//				if (it.hasNext()) {
//					sb.append(spliter);
//				}
//			}
//
//		}
//		return sb.toString();
//	}
//
//	public static String join(String[] args, String spliter) {
//		StringBuilder sb = new StringBuilder();
//		if (args != null && args.length > 0) {
//			for (int i = 0; i < args.length; i++) {
//				sb.append(args[i]);
//				if (i != args.length - 1) {
//					sb.append(spliter);
//				}
//			}
//
//		}
//		return sb.toString();
//	}
//
//	public static String toString(Object obj, String def) {
//		if (obj == null) {
//			return def;
//		} else {
//			return ObjectUtils.toString(obj);
//		}
//	}
//
//
//	public String serialize(java.io.Serializable obj) {
//		if (obj instanceof String || obj instanceof CharSequence) {
//			return obj.toString();
//		} else {
//
//			try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream(1024);
//					ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteStream);) {
//				objectOutputStream.writeObject(obj);
//				objectOutputStream.flush();
//				Base64 base64 = new Base64();
//				return base64.encodeToString(byteStream.toByteArray());
//			} catch (IOException e) {
//				throw new BusinessRuntimeException("Object to base64 string error",e);
//			}
//		}
//	}
//
//	/**
//	 * 压缩
//	 *
//	 * @param data 待压缩数据
//	 * @return byte[] 压缩后的数据
//	 */
//	public static byte[] compress(byte[] data) {
//		byte[] output = new byte[0];
//
//		Deflater compresser = new Deflater();
//
//		compresser.reset();
//		compresser.setInput(data);
//		compresser.finish();
//		ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
//		try {
//			byte[] buf = new byte[1024];
//			while (!compresser.finished()) {
//				int i = compresser.deflate(buf);
//				bos.write(buf, 0, i);
//			}
//			output = bos.toByteArray();
//		} catch (Exception e) {
//			output = data;
//			e.printStackTrace();
//		} finally {
//			try {
//				bos.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		compresser.end();
//		return output;
//	}
//
//	/**
//	 * 解压缩
//	 *
//	 * @param data 待压缩的数据
//	 * @return byte[] 解压缩后的数据
//	 */
//	public static byte[] decompress(byte[] data) {
//		byte[] output = new byte[0];
//
//		Inflater decompresser = new Inflater();
//		decompresser.reset();
//		decompresser.setInput(data);
//
//		ByteArrayOutputStream o = new ByteArrayOutputStream(data.length);
//		try {
//			byte[] buf = new byte[1024];
//			while (!decompresser.finished()) {
//				int i = decompresser.inflate(buf);
//				o.write(buf, 0, i);
//			}
//			output = o.toByteArray();
//		} catch (Exception e) {
//			output = data;
//			e.printStackTrace();
//		} finally {
//			try {
//				o.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//
//		decompresser.end();
//		return output;
//	}
//
//	public static String generateMultiValueInExpr(String field, String[] items) {
//		if (StringUtils.isEmpty(field) || items == null || items.length < 1) {
//			return "";
//		}
//		return generateMultiValueInExpr(field, Arrays.asList(items));
//	}
//
//	public static String generateMultiValueInExpr(String field, Collection<String> items) {
//		if (StringUtils.isEmpty(field) || items == null || items.size() < 1) {
//			return "";
//		}
//		StringBuilder sb = new StringBuilder(String.format("%s in (", field));
//		int count = 1;
//		for (String item : items) {
//			if (StringUtils.isEmpty(item)) {
//				continue;
//			} else if (item.indexOf('\'') >= 0) {
//				item = item.replace("'", "''");
//			}
//			if (count <= 1000) {
//				sb.append(String.format("'%s',", item));
//
//			} else {
//				count = 1;
//				sb.setLength(sb.length() - 1);
//				sb.append(String.format(") or %s in ('%s',", field, item));
//			}
//			count++;
//		}
//		if (count > 1) {
//			sb.setLength(sb.length() - 1);
//			sb.append(")");
//		}
//		return sb.toString();
//	}
//
//
//	public static boolean isDevelopMode() {
//		return "true".equalsIgnoreCase(System.getProperty("isDevelopMode"));
//	}
//
//	public static String getBQHome() {
//		String homePath = null;
//		try {//安全问题
//			homePath = Objects.toString(MethodUtils.invokeStaticMethod(System.class, "getProperty", new Object[]{"bq_home"}));
//		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
//			BQLogger.error(e);
//		}
//		if (isEmptyStr(homePath)|| "null".equals(homePath.trim())) {
//			homePath = "classpath";
//		}
//		return homePath;
//	}
//	
//	/**
//	 * 字符串s，是否能匹配通配符p，?表示一个字符，*表示0个或多个字符
//	 * 
//	 * @param s
//	 * @param p
//	 * @return
//	 */
//	public static boolean isMatch(String s, String p) {
//		if (p == null)
//			return false;
//		// without this optimization, it will fail for large data set
//		int plenNoStar = 0;
//		for (char c : p.toCharArray())
//			if (c != '*')
//				plenNoStar++;
//		if (plenNoStar > s.length())
//			return false;
//
//		s = " " + s;
//		p = " " + p;
//		int slen = s.length();
//		int plen = p.length();
//
//		boolean[] dp = new boolean[slen];
//		TreeSet<Integer> firstTrueSet = new TreeSet<Integer>();
//		firstTrueSet.add(0);
//		dp[0] = true;
//
//		boolean allStar = true;
//		for (int pi = 1; pi < plen; pi++) {
//			if (p.charAt(pi) != '*')
//				allStar = false;
//			for (int si = slen - 1; si >= 0; si--) {
//				if (si == 0) {
//					dp[si] = allStar ? true : false;
//				} else if (p.charAt(pi) != '*') {
//					if (s.charAt(si) == p.charAt(pi) || p.charAt(pi) == '?')
//						dp[si] = dp[si - 1];
//					else
//						dp[si] = false;
//				} else {
//					int firstTruePos = firstTrueSet.isEmpty() ? Integer.MAX_VALUE : firstTrueSet.first();
//					if (si >= firstTruePos)
//						dp[si] = true;
//					else
//						dp[si] = false;
//				}
//				if (dp[si])
//					firstTrueSet.add(si);
//				else
//					firstTrueSet.remove(si);
//			}
//		}
//		return dp[slen - 1];
//	}
//
//	/**
//	 * 将格式日期转为ms值
//	 * 
//	 * @param formateDate
//	 *            类似于yyyy-MM-dd的日期格式的值
//	 * @return
//	 */
//	public static Long getMsFromDate(String formateDate) {
//		Date parse = null;
//		try {
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//			parse = sdf.parse(formateDate);
//			return parse.getTime();
//		} catch (ParseException e) {
//			BQLogger.error("Convert date to ms occur error", e);
//		}
//		return -1L;
//	}
//
//	/**
//	 * 将int类型数组转为String数组
//	 * 
//	 * @param intArr
//	 *            int类型数组
//	 * @return 得到String数组
//	 */
//	public static String[] convertArrayIntToString(int[] intArr) {
//		// List<int[]> list = Arrays.asList(intArr);
//		// return (String[])list.toArray(new String[intArr.length]);
//		if (intArr == null)
//			return null;
//		String[] result = new String[intArr.length];
//		for (int i = 0; i < intArr.length; i++) {
//			result[i] = String.valueOf(intArr[i]);
//		}
//		return result;
//	}
//	
//	private static String byteArrayToHexString(byte[] b) {
//	    StringBuffer resultSb = new StringBuffer();
//	    for (int i = 0; i < b.length; i++) {
//	      resultSb.append(byteToHexString(b[i]));
//	    }
//	    return resultSb.toString();
//	}
//
//	private static String byteToHexString(byte b) {
//		int n = b;
//		if (n < 0)
//			n = 256 + n;
//		int d1 = n / 16;
//		int d2 = n % 16;
//		return hexDigits[d1] + hexDigits[d2];
//	}
//
//	/**
//	 * 将字符串加密成32位
//	 * @param origin
//	 * @return
//	 * @since 1.0.0.06
//	 */
//	public static String encode32(String origin) {
//		String resultString = null;
//		resultString = new String(origin);
//		MessageDigest md;
//		try {
//			md = MessageDigest.getInstance("MD5");//NOSONAR
//			resultString = byteArrayToHexString(md.digest(resultString.getBytes()));
//		} catch (NoSuchAlgorithmException e) {
//			BQLogger.error(e);
//		}
//		return resultString;
//	}
//	
//	public static DeployMode getDeployMode(){
//		String profiles = "";
//		try {//安全问题
//			profiles = Objects.toString(MethodUtils.invokeStaticMethod(System.class, "getProperty", new Object[]{"spring.profiles.active"}));
//		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
//			BQLogger.error(e);
//		}
//
//		return DeployMode.parseFromCode(profiles);
//	}
//
//	public static enum DeployMode{
//	    SINGLE("single"),CLUSTER("cluster"),CLOUD("cloud");
//        private String code;
//
//        private DeployMode(String code){
//            this.code = code;
//        }
//
//
//        public static DeployMode parseFromCode(String code){
//            return Arrays.stream(DeployMode.values()).filter(e -> e.code.equals(code)).findAny().orElse(SINGLE);
//        }
//    }
//
//    public static <P,T> P getOneProperty(List<T> list, Function<T,P> function){
//	    if(list != null && !list.isEmpty()){
//	        return function.apply(list.get(0));
//        }else{
//	        return null;
//        }
//    }
//
//    public static Map<String,Object> createResponseMessage(String message){
//		Map<String,Object> result = new HashMap<>();
//		if(StringUtils.isEmpty(message)){
//			result.put("success",Boolean.TRUE);
//		}else{
//			result.put("success",Boolean.FALSE);
//			result.put("message",message);
//		}
//		return result;
//	}
//
//	/**
//	 * 为参数添加序列号
//	 */
//	public static String sequenceParams(String target) {
//		StringBuilder sb = new StringBuilder(target);
//		int index = 0;
//		int count = 0;
//		while (true) {
//			index = sb.indexOf("?", index + 1);
//			if (index > 0) {
//				count++;
//				sb.replace(index, index + 1, "?" + count);
//			} else {
//				break;
//			}
//		}
//		return sb.toString();
//	}
//
////	public static void main(String[] args){
////		System.out.println(decryptPassword("2be98afc86aa7f2e4b216a069d1878f8b"));
////	}
//	
//	 /**
//     * string date to LocalDateTime
//     */
//    public static LocalDateTime getLocalDateTime(String strDate) throws ParseException {
//    		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//    		return LocalDateTime.ofInstant(formatter.parse(strDate).toInstant(), ZoneId.systemDefault());
//    }
//
//
//	public static String bytes2String(byte[] bytes) {
//    	if(bytes == null || bytes.length == 0)return null;
//		DESUtil des = new DESUtil();
//		byte[] _bytes = des.encrypt(bytes);
//		Base64 base64 = new Base64(Integer.MAX_VALUE,null,true);
//		return  base64.encodeToString(_bytes);
//	}
//
//	public static byte[] string2Bytes(String hexString){
//		if (hexString == null || hexString.equals("")) {
//			return null;
//		}
//		DESUtil des = new DESUtil();
//		Base64 base64 = new Base64(Integer.MAX_VALUE,null,true);
//		return des.descrypt(base64.decode(hexString.getBytes()));
//	}
//
//	public static String toDataToken(String domainMark,String userPk){
//		Random random = new Random();
//		char seed = (char) (random.nextInt(84) + 33);
//		return toDataToken(domainMark,userPk,seed+"");
//	}
//	public static String toDataToken(String domainMark,String userPk,String key){
//
//		DESUtil des = new DESUtil(String.join("Y", Collections.nCopies(10, key)));
//		String s = bytes2String( (key + domainMark + des.encrypt(userPk)).getBytes());
//		return s;
//	}
//	public static String[] parseDataToken(String token){
//		byte[] o = string2Bytes(token);
//		if(o == null)return null;
//		String str = new String(o);
//		String key = str.substring(0,1);
//		DESUtil des = new DESUtil(String.join("Y", Collections.nCopies(10, key)));
//		return new String[]{str.substring(1,6),des.decrypt(str.substring(6)),key};
//	}
//
//	@SafeVarargs
//	public static <K,V> Map<K,V> ofMap(Pair<K,V>... keyValues){
//		return Stream.of(keyValues).collect(Collectors.toMap(Pair::getKey,Pair::getValue));
//	}
//
//	public static Date toDate(LocalDateTime localDateTime){
//		return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
//	}
//
//	public static LocalDateTime toLocalDateTime(Date date){
//		return  LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
//	}
//
//	/**
//	 * Returns a string of the stack trace of the specified exception
//	 */
//	public static final String getStackTracker(Throwable e) {
//		if(e == null){
//			return NullPointerException.class.getSimpleName();
//		}
//		return getClassicStackTrace(e);
//	}
//
//	public static final String getClassicStackTrace(Throwable e) {
//		StringWriter stringWriter = new StringWriter();
//		PrintWriter printWriter = new PrintWriter(stringWriter);
//		e.printStackTrace(printWriter);
//		String string = stringWriter.toString();
//		try {
//			stringWriter.close();
//		} catch (IOException ioe) {
//		} // is this really required?
//		return string;
//	}
//
//	/**
//	 * @return True if the OS is a Windows derivate.
//	 */
//	public static final boolean isWindows() {
//		return getOS().startsWith("Windows");
//	}
//
//	/**
//	 * @return True if the OS is a Linux derivate.
//	 */
//	public static final boolean isLinux() {
//		return getOS().startsWith("Linux");
//	}
//
//	/**
//	 * @return True if the OS is an OSX derivate.
//	 */
//	public static final boolean isOSX() {
//		return getOS().toUpperCase().contains("OS X");
//	}
//
//	/**
//	 * determine the OS name
//	 *
//	 * @return The name of the OS
//	 */
//	public static final String getOS() {
//		return System.getProperty("os.name");
//	}
//
//	/**
//	 * Determine the quoting character depending on the OS. Often used for shell
//	 * calls, gives back " for Windows systems otherwise '
//	 *
//	 * @return quoting character
//	 */
//	public static String getQuoteCharByOS() {
//		if (isWindows()) {
//			return "\"";
//		} else {
//			return "'";
//		}
//	}
//
//	/**
//	 * Quote a string depending on the OS. Often used for shell calls.
//	 *
//	 * @return quoted string
//	 */
//	public static String optionallyQuoteStringByOS(String string) {
//		String quote = getQuoteCharByOS();
//		if (StringUtils.isNotEmpty(string))
//			return quote;
//
//		// If the field already contains quotes, we don't touch it anymore, just
//		// return the same string...
//		// also return it if no spaces are found
//		if (string.indexOf(quote) >= 0 || (string.indexOf(' ') < 0 && string.indexOf('=') < 0)) {
//			return string;
//		} else {
//			return quote + string + quote;
//		}
//	}



//	public static void main(String[] args) {
//		try {
//
//			//String uuid = UUID.randomUUID().toString();
//			String uuid = "208424e9-9503-4a2a-b129-cd6f2a5c47f1";
//			for(int i=0; i < 1;i++) {
//				String s = toDataToken("00000",uuid);
//				System.out.println(s);
//				String[] o = parseDataToken(s);
//				System.out.println(Arrays.toString(o));
//			}
//
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

}
