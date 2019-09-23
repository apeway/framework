package org.liws.framework.util.bitset;

import java.util.Arrays;
import java.util.BitSet;

/**
 * 计算权限Util
 */
public abstract class BitSetPrivilegeUtils {

    public static final int TYPE_LENGTH = 128;

    private BitSetPrivilegeUtils() {

    }

    /**
     * 直接赋给一个对象权限
     * @param ori
     * @param elementTypeId 对象权限类别ID
     * @param elementId     对象权限ID
     * @return
     */
    public static BitSet addPrivilege(BitSet ori, int elementTypeId, int elementId) {
        if (ori == null) {
            ori = new BitSet();
        }
        ori.set(TYPE_LENGTH * elementTypeId + elementId, true);

        return ori;

    }


    /**
     * 直接移除一个对象权限
     * @param ori
     * @param elementTypeId 对象权限类别ID
     * @param elementId     对象权限ID
     */
    public static BitSet removePrivilege(BitSet ori, int elementTypeId, int elementId) {
        if (ori == null) {
            ori = new BitSet();
        }
        int position = TYPE_LENGTH * elementTypeId + elementId;
        if (position > ori.length()) {
            return ori;
        }
        ori.set(position, false);

        return ori;
    }

    /**
     * 清除某一对象权限类别的所有权限
     * @param ori
     * @param elementTypeId 对象权限类别ID
     * @return
     */
    public static BitSet clearPrivilege(BitSet ori, int elementTypeId) {
        if (ori == null) {
            ori = new BitSet();
        }
        ori.set(TYPE_LENGTH * elementTypeId,TYPE_LENGTH * (elementTypeId+1),false);

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
     * @param other 对象权限类别ID
     * @return
     */
    public static boolean hasAnyPrivilege(BitSet ori,  BitSet other) {
        for (int i = other.size() - 1; i >= 0; i--){
            if(other.get(i) && ori.get(i)){
                return true;
            }
        }
        return false;
    }

    /**
     * 是否拥有所有的权限
     * @param ori
     * @param others 对象权限类别ID
     * @return
     */
    public static boolean hasAllPrivilege(BitSet ori,  BitSet... others) {
        if (ori == null) {
            return false;
        }
        if(others == null || others.length == 0){
            return true;
        }
        BitSet it = addPrivilege(null,null,others);
        for (int i = Math.max(ori.size(), it.size()) - 1; i >= 0; i--){
            if(it.get(i)){
                if(!ori.get(i)){
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * 赋给权限集合
     *
     * @param ori
     * @param adder
     * @param others
     * @return
     */
    public static BitSet addPrivilege(BitSet ori, BitSet adder, BitSet... others) {
        if (ori == null) {
            ori = new BitSet();
        }

        if (adder != null && !adder.isEmpty()) {
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
     * 移除权限集合
     *
     * @param ori
     * @param removers
     * @return
     */
    public static BitSet removePrivilege(BitSet ori, BitSet... removers) {
        if (ori == null) {
            ori = new BitSet();
        }
        if (removers == null || removers.length == 0) {
            return ori;
        } else {
            BitSet remover = new BitSet(1024);
            Arrays.stream(removers).forEach(e -> {
                if (e != null) {
                    remover.or(e);
                }
            });
            int length = Math.max(ori.length(), remover.length());
            for (int i = 0; i < length; i++) {
                if (remover.get(i) && i < ori.length()) {
                    ori.set(i, false);
                }
            }
            return ori;

        }
    }


}
