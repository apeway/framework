package com.yonyou.bq.framework.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Kryo.DefaultInstantiatorStrategy;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;
import com.yonyou.bq.framework.datasource.AECachedRowSet;
import com.yonyou.bq.framework.spring.SpringContextHolder;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class KryoSerializeUtil {
    static KryoFactory factory;
    static KryoPool pool;

    static {
        factory = new KryoFactory() {
            public Kryo create() {
                Kryo kryo = new Kryo();
                kryo.setClassLoader(Thread.currentThread().getContextClassLoader());
                kryo.setRegistrationRequired(false);
                kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));


                kryo.register(java.util.Set.class);
                kryo.register(java.util.HashSet.class);
                kryo.register(java.util.List.class);
                kryo.register(java.util.ArrayList.class);
                kryo.register(java.util.Map.class);
                kryo.register(java.util.HashMap.class);
                kryo.register(AECachedRowSet.class);
                kryo.register(Class.class);
                kryo.register(org.apache.shiro.session.mgt.SimpleSession.class);
                kryo.register(com.yonyou.bq.framework.vo.BaseVO.class);
                kryo.register(com.yonyou.bq.framework.vo.UserVO.class);
                kryo.register(Throwable.class);

                kryo.register(String[].class);

                return kryo;
            }
        };
        pool = new KryoPool.Builder(factory).softReferences().build();
    }

    public static byte[] serialize(Object obj) throws SerializationException {
        try {
            if (obj == null)
                return new byte[0];
            ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
            Kryo kryo = pool.borrow();
            kryo.setClassLoader(obj.getClass().getClassLoader());
            try (Output out = new Output(baos)) {
                kryo.writeClassAndObject(out, obj);
                out.flush();
            } finally {
                kryo.setClassLoader(Thread.currentThread().getContextClassLoader());
                pool.release(kryo);
            }
            return baos.toByteArray();
        } catch (Exception e) {
            throw new SerializationException("serialize error", e);
        }

    }

    public static Object deserialize(InputStream inputStream) throws SerializationException {
        try {
            if (inputStream == null || inputStream.available() == 0)
                return null;
            Kryo kryo = pool.borrow();
            kryo.setClassLoader(Thread.currentThread().getContextClassLoader());
            try (Input input = new Input(inputStream)) {
                try {
                    return kryo.readClassAndObject(input);
                } catch (com.esotericsoftware.kryo.KryoException ex1) {
                    input.setPosition(0);
                    try {
                        kryo.setClassLoader(SpringContextHolder.getClassLoaderByContextName("/console"));
                        return kryo.readClassAndObject(input);
                    } catch (com.esotericsoftware.kryo.KryoException ex2) {
                        input.setPosition(0);
                        try {
                            kryo.setClassLoader(SpringContextHolder.getClassLoaderByContextName("/bq_self"));
                            return kryo.readClassAndObject(input);
                        } catch (com.esotericsoftware.kryo.KryoException ex3) {
                            input.setPosition(0);
                            try {
                                kryo.setClassLoader(SpringContextHolder.getClassLoaderByContextName("/di"));
                                return kryo.readClassAndObject(input);
                            } catch (com.esotericsoftware.kryo.KryoException ex4) {


                            }


                        }

                    }
                    throw new SerializationException("deserialize error", ex1.getCause());
                }
            } finally {
                kryo.setClassLoader(Thread.currentThread().getContextClassLoader());
                pool.release(kryo);
            }
        } catch (IOException e) {
            throw new SerializationException("deserialize error", e);
        }

    }

    public static Object deserialize(byte[] bytes) throws SerializationException {

        if (bytes == null || bytes.length == 0)
            return null;
        InputStream sbs = new ByteArrayInputStream(bytes);
        return deserialize(sbs);

    }
}
