package org.liws.framework.util;

import java.util.Arrays;
import java.util.BitSet;

/**
 * 计算权限Util
 */
public abstract class BitSetPrivilegeUtils {
	private BitSetPrivilegeUtils() {}

    public static final int TYPE_LENGTH = 128;

    /**
     * 直接赋给一个对象权限
     * @param ori 
     * @param elementTypeId 对象权限类别ID
     * @param elementId     对象权限ID
     * @return
     */
    public static BitSet addPrivilege(BitSet ori, int elementTypeId, int elementId) {
        ori = createIfNull(ori);
        /*
         * XXX 权限存储规则：
         * 每一类权限中最多有TYPE_LENGTH种权限。
         * 对应BitSet中的位置为[TYPE_LENGTH * elementTypeId, (TYPE_LENGTH + 1) * elementTypeId -1]
         */
        ori.set(TYPE_LENGTH * elementTypeId + elementId, true);
        return ori;
    }
    
    /**
     * 赋给权限集合
     * @param ori
     * @param adder
     * @param others
     * @return
     */
    public static BitSet addPrivilege(BitSet ori, BitSet adder, BitSet... others) {
        ori = createIfNull(ori);

        if (adder != null) {
            ori.or(adder);
        }

        if (others != null && others.length > 0) {
            for (BitSet one : others) {
                if (one != null) {
                    ori.or(one);
                }
            }
        }
        return ori;
    }

    
    /**
     * 直接移除一个对象权限
     * @param ori
     * @param elementTypeId 对象权限类别ID
     * @param elementId     对象权限ID
     */
    public static BitSet removePrivilege(BitSet ori, int elementTypeId, int elementId) {
        ori = createIfNull(ori);
        int position = TYPE_LENGTH * elementTypeId + elementId;
        if (position <= ori.length()) {
            ori.set(position, false);
        }
        return ori;
    }

    /**
     * 清除某一对象权限类别的所有权限
     * @param ori
     * @param elementTypeId 对象权限类别ID
     * @return
     */
    public static BitSet clearPrivilege(BitSet ori, int elementTypeId) {
		ori = createIfNull(ori);
		ori.set(TYPE_LENGTH * elementTypeId, TYPE_LENGTH * (elementTypeId + 1), false);
		return ori;
    }

    /**
	 * 移除权限集合
	 *
	 * @param ori
	 * @param removers
	 * @return
	 */
	public static BitSet removePrivilege(BitSet ori, BitSet... removers) {
	    ori = createIfNull(ori);
	    if (removers != null && removers.length > 0) {
	    	// 计算所有removers的并集
	        BitSet remover = new BitSet(1024); 
	        Arrays.stream(removers).forEach(e -> {
	            if (e != null) {
	                remover.or(e);
	            }
	        });
	        // 执行移除
	        int length = Math.max(ori.length(), remover.length());
	        for (int i = 0; i < length; i++) {
	            if (remover.get(i)) {
	                ori.set(i, false);
	            }
	        }
	    }
	    return ori;
	}

	/**
     * 是否含有对象权限
     * @param ori
     * @param elementTypeId 对象权限类别ID
     * @param elementId     对象权限ID
     * @return
     */
    public static boolean hasPrivilege(BitSet ori, int elementTypeId, int elementId) {
        if (ori == null) {
            return false;
        }
        int position = TYPE_LENGTH * elementTypeId + elementId;
        return position < ori.length() && ori.get(position);
    }

    /**
     * 是否拥有任何一個的权限
     * @param ori
     * @param other 待检测的所有对象权限ID的集合
     * @return
     */
    public static boolean hasAnyPrivilege(BitSet ori, BitSet... others) {
    	if (ori == null) {
            return false;
        }
    	if(others == null || others.length == 0){
            return true;
        }
    	
        for (BitSet bitSet : others) {
        	for (int i = bitSet.size() - 1; i >= 0; i--){
        		if(bitSet.get(i) && ori.get(i)){
        			return true;
        		}
        	}
		}
        return false;
    }

    /**
     * 是否拥有所有的权限
     * @param ori
     * @param others 待检测的所有对象权限ID的集合
     * @return
     */
    public static boolean hasAllPrivilege(BitSet ori,  BitSet... others) {
        if (ori == null) {
            return false;
        }
        if(others == null || others.length == 0){
            return true;
        }
        
        // 先求并集
        BitSet all = addPrivilege(null,null,others);
        // 再遍历检测
        for (int i = Math.max(ori.size(), all.size()) - 1; i >= 0; i--){
            if(all.get(i)){
                if(!ori.get(i)){
                    return false;
                }
            }
        }
        return true;
    }


    

    private static BitSet createIfNull(BitSet ori) {
		if (ori == null) {
            ori = new BitSet();
        }
		return ori;
	}
}
