/**
 * 
 */
package org.liws.framework.util.file;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang.StringUtils;



/**
 * 文件压缩工具类
 * @author limengg
 */
public class ZipFileUtil {
    /**
     * ZipFileUtil.
     * @Description:压缩工具类构造方法。
     */
    public ZipFileUtil() {
    }
    
    /**
     * 压缩文件 目前逻辑简单，支持的是单个文件的压缩
     * @param entryName
     *        压缩后内部文件名
     * @param in
     * @param zipFile
     *        压缩后文件
     * @return 压缩之前文件的大小
     * @throws IOException
     */
    public static long zipFile(String entryName, InputStream in, File zipFile) throws IOException {
        OutputStream out = null;
        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile, false));
            zos.setMethod(ZipOutputStream.DEFLATED);// 启用压缩
            zos.setLevel(Deflater.BEST_COMPRESSION);// 最强级别压缩，但是花费时间较多
            zos.putNextEntry(new ZipEntry(entryName));
            out = new BufferedOutputStream(zos);
            if (in == null) {// 创建一个空的文件
                zipFile.createNewFile();
                return 0;
            }
            long count = 0;
            int len = -1;
            byte[] buf = new byte[2048];// 2k
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
                count += len;
            }
            out.flush();
            return count;
        } catch (IOException e) {
           // AELogger.error("zip file error.", e);
        } finally {
            closeOutputStream(out);
            closeInputStream(in);
        }
        return 0;
    }

    public static InputStream unZipFile(String entryName, String zipPath) throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = ZipFileUtil.getInputStream(new File(zipPath), entryName);
            return inputStream;
        } catch (IOException e) {
            String msg = "Unzip File error:";
            // AELogger.debug(msg, e);
            throw new RuntimeException(msg, e);
        }finally {
            //closeInputStream(inputStream);//关闭后的输入流无法输出返回
        }
    }
    
    public static void zipFile(File[] input, File output) throws IOException {
        if (input == null || input.length == 0) {
            throw new RuntimeException(String.format("the input file %s compress fail as it's not exit.", ""));
        }
        output.createNewFile();
        if (!output.exists()) {
            throw new RuntimeException(String.format("the output file %s has stoped compress as it's not exit.",
                    output.getName()));
        }
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(output),Charset.forName("GBK"));
        try {
            for (File file : input) {
                zipFile(file, zos, null);
            }
        } finally {
            zos.close();
        }
    }
    
    /**
     * @param input
     *        输入文件
     * @param zos
     *        输出流
     * @param parentFileName
     *        带有分隔符的上级目录名称
     * @throws IOException
     */
    private static void zipFile(File input, ZipOutputStream zos, String parentFileName) throws IOException {
        String entryName = StringUtils.isEmpty(parentFileName) ? input.getName() : parentFileName + input.getName();
        if (input.isDirectory()) {
            entryName = entryName + '/';
            zos.putNextEntry(new ZipEntry(entryName));
            File[] subFiles = input.listFiles();
            for (File subFile : subFiles) {
                zipFile(subFile, zos, entryName);
            }
        } else {
            zos.putNextEntry(new ZipEntry(entryName));
            FileInputStream is = new FileInputStream(input);
            byte[] buff = null;
            try {
                buff = new byte[is.available()];
                while (buff.length > 0 && is.read(buff) != -1) {
                    zos.write(buff);
                    buff = new byte[is.available()];
                }
            } finally {
               is.close();
            }
        }
    }
    
    /**
     * 从压缩文件中，根据指定的名称获取其输入流
     * @param file
     * @param entryName
     * @return
     * @throws IOException
     */
    public static InputStream getInputStream(File file, String entryName) throws IOException {
        if (!file.exists()) {
            throw new RuntimeException(String.format("the file %s does not exit.", file.getName()));

        }
        ZipInputStream zis = new ZipInputStream(new FileInputStream(file), Charset.forName("GBK"));
        while (zis.available() != 0) {
            ZipEntry entry = zis.getNextEntry();
            if (entry != null && entry.getName().equals(entryName)) {
                break;
            }
        }
        return zis;
    }
    
    public static void closeInputStream(InputStream in) {
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
           //AELogger.error(e.getMessage(), e);
        }
    }

    public static void closeOutputStream(OutputStream out) {
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
           //AELogger.error(e.getMessage(), e);
        }
    }
    
    /**
     * 获取指定压缩文件的内容
     * @param filePath
     * @return
     * @throws IOException
     */
    public static ZipEntry[] getZipEntries(String filePath) throws IOException {
        ZipInputStream zs = new ZipInputStream(new FileInputStream(new File(filePath)),Charset.forName("GBK"));
        List<ZipEntry> entryList = new ArrayList<ZipEntry>();
        try {
            while (zs.available() != 0) {
                ZipEntry entry = zs.getNextEntry();
                if (entry != null) entryList.add(entry);
            }
        } finally {
            zs.close();
        }
        return entryList.toArray(new ZipEntry[0]);
    }
    
}
