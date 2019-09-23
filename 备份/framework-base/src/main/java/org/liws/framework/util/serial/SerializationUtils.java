package org.liws.framework.util.serial;

import java.io.*;

import org.liws.framework.log.BQLogger;
import org.liws.framework.spring.SpringContextHolder;

/**
 * 序列化Util类
 */
public class SerializationUtils {

	private SerializationUtils() {

	}

	/**
	 * 通过序列化深度克隆对象
	 * 
	 * @param object
	 * @return
	 */
	public static <T extends Serializable> T clone(final T object) {
		if (object == null) {
			return null;
		}
		final byte[] objectData = serialize(object);
		final ByteArrayInputStream bais = new ByteArrayInputStream(objectData);

		try (AEObjectInputStream in = new AEObjectInputStream(bais, object.getClass().getClassLoader());) {
			// stream closed in the finally

			/*
			 * when we serialize and deserialize an object, it is reasonable to assume the
			 * deserialized object is of the same type as the original serialized object
			 */
			@SuppressWarnings("unchecked") // see above
			final T readObject = (T) in.readObject();
			return readObject;

		} catch (final ClassNotFoundException ex) {
			BQLogger.error(ex);
			throw new SerializationException("ClassNotFoundException while reading cloned object data", ex);
		} catch (final IOException ex) {
			BQLogger.error(ex);
			throw new SerializationException("IOException while reading cloned object data", ex);
		}
	}

	/**
	 * 序列化对象，输出到流
	 * 
	 * @param obj
	 * @param outputStream
	 */
	public static void serialize(final Serializable obj, final OutputStream outputStream) {
		if (outputStream == null) {
			throw new IllegalArgumentException("The OutputStream must not be null");
		}
		try (ObjectOutputStream out = new ObjectOutputStream(outputStream);) {
			// stream closed in the finally
			out.writeObject(obj);

		} catch (final IOException ex) {
			BQLogger.error(ex);
			throw new SerializationException(ex);
		}
	}

	/**
	 * 序列化对象，返回byte数组
	 * 
	 * @param obj
	 * @return
	 */
	public static byte[] serialize(final Serializable obj) {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
		serialize(obj, baos);
		return baos.toByteArray();
	}

	/**
	 * 从流中反序列化对象
	 * 
	 * @param inputStream
	 * @return
	 */
	public static <T> T deserialize(final InputStream inputStream) {
		return deserialize(inputStream, null);
	}

	/**
	 * 指定ClassLoader，从流中反序列化对象
	 * 
	 * @param inputStream
	 * @param classLoader
	 * @return
	 */
	public static <T> T deserialize(final InputStream inputStream, ClassLoader classLoader) {
		if (inputStream == null) {
			throw new IllegalArgumentException("The InputStream must not be null");
		}
		try (ObjectInputStream in = new AEObjectInputStream(inputStream, classLoader);) {
			// stream closed in the finally

			@SuppressWarnings("unchecked") // may fail with CCE if serialised form is incorrect
			final T obj = (T) in.readObject();
			return obj;

		} catch (final ClassCastException | ClassNotFoundException | IOException ex) {
			BQLogger.error(ex);
			throw new SerializationException(ex);
		}
	}

	/**
	 * 指定ClassLoader，从byte数组中反序列化对象
	 * 
	 * @param objectData
	 * @param classLoader
	 * @return
	 */
	public static <T> T deserialize(final byte[] objectData, ClassLoader classLoader) {
		if (objectData == null) {
			throw new IllegalArgumentException("The byte[] must not be null");
		}
		return SerializationUtils.<T>deserialize(new ByteArrayInputStream(objectData), classLoader);
	}

	/**
	 * 从byte数组中反序列化对象
	 * 
	 * @param objectData
	 * @return
	 */
	public static <T> T deserialize(final byte[] objectData) {
		return deserialize(objectData, null);
	}

}

/**
 * 加入自定义ClassLoader解析 
 * XXX why ?
 */
class AEObjectInputStream extends ObjectInputStream {
	private ClassLoader classLoader;

	public AEObjectInputStream(InputStream in) throws IOException {
		this(in, null);
	}

	public AEObjectInputStream(InputStream in, ClassLoader classLoader) throws IOException {
		super(in);
		this.classLoader = classLoader;
	}

	@Override
	protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
		String name = desc.getName();
		try {
			return super.resolveClass(desc);
		} catch (ClassNotFoundException ex) {// NOSONAR
			try {
				if (this.classLoader == null) {
					return Class.forName(name, false, Thread.currentThread().getContextClassLoader());
				} else {
					return Class.forName(name, false, classLoader);
				}
			} catch (ClassNotFoundException ex1) {
				try {
					return Class.forName(name, false, SpringContextHolder.getClassLoaderByContextName("/bq_self"));
				} catch (ClassNotFoundException ex2) {
					try {
						return Class.forName(name, false, SpringContextHolder.getClassLoaderByContextName("/console"));
					} catch (ClassNotFoundException ex3) {
						return Class.forName(name, false, SpringContextHolder.getClassLoaderByContextName("/di"));
					}
				}
			}
		}
	}

}
