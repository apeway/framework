package org.liws.framework.util.generator;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 生成一个不重复的pk，在Long的64位中，其中符号位不用，42位时间毫秒，9位为机器码，12位序号
 */
public class AtomicPkGenerator {

    class Variant {
        private long sequence = 0;
        private long lastTimestamp = -1;
    }

    private AtomicReference<Variant> variant = new AtomicReference<>(new Variant());

    
    private static final long twepoch = 1409767200000L;

    private static final int workerIdBits = 9;
    private static final long maxWorkerId = -1L ^ (-1L << workerIdBits);
    private static final int sequenceBits = 12;
    private static final long workerId = Long.valueOf(System.getProperty("SERVER_ID","1"));

    private static final int workerIdShift = sequenceBits;
    private static final int timestampLeftShift = sequenceBits + workerIdBits;
    private static final long sequenceMask = -1L ^ (-1L << sequenceBits);

    // ------------------- 单例  start ----------------------
    private static class GeneratorHandler{
        static AtomicPkGenerator generator = new AtomicPkGenerator();
    }
    
    private AtomicPkGenerator() {
        // sanity check for workerId
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        // logger.debug("worker starting. timestamp left shift {}, worker id bits {}, sequence bits {}, workerId {}",
        //        timestampLeftShift, workerIdBits, sequenceBits, workerId);
    }
    // ------------------- 单例  end ----------------------
   
    

    /**
     * 
     * @return
     */
    public static long generateLong(){
        return GeneratorHandler.generator.nextPk();
    }
    /**
     * 
     * @return
     */
    public static String generate(){
		return Long.toString(generateLong(), Character.MAX_RADIX);
    }

    private long nextPk() {
        while (true) {
            // Save the old variant
            Variant varOld = variant.get();
            long sequence = varOld.sequence;
            long timestamp = timeGen();
            long lastTimestamp = varOld.lastTimestamp;


            if (lastTimestamp == timestamp) {
                sequence = (sequence + 1) & sequenceMask;
                if (sequence == 0) {
                    timestamp = tilNextMillis(lastTimestamp);
                }
            } else {
                sequence = 0;
            }

            Variant varNew = new Variant();
            varNew.sequence = sequence;
            varNew.lastTimestamp = timestamp;
            if (variant.compareAndSet(varOld, varNew)) {
                return ((timestamp - twepoch) << timestampLeftShift) |
                        (workerId << workerIdShift) |
                        sequence;
            }

        }
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        if (timestamp < lastTimestamp) {
        	System.out.println("System time was be modified slow");
            //timestamp = lastTimestamp;
        }
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }


}
