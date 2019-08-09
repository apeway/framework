package org.liws.framework.util.wechat;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
 
/**
 * 基于com.google.zxing/core 
 */
public class QrCodeUtil {
	
	/**
	 * 根据url生成二维码并写到流中
	 * @param url
	 * @param out
	 * @throws IOException 
	 * @throws WriterException 
	 */
	public static void genatateQrCodeByUrlThenWrite(String url, OutputStream out) throws IOException, WriterException {
		Map<EncodeHintType, String> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		BitMatrix bitMatrix = new MultiFormatWriter().encode(url, BarcodeFormat.QR_CODE, 400, 400, hints);
		BufferedImage image = toBufferedImage(bitMatrix);
		ImageIO.write(image, "jpg", out);
	}
	
 
    private static final int BLACK = 0xFF000000;
    private static final int WHITE = 0xFFFFFFFF;
 
    private static BufferedImage toBufferedImage(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? BLACK : WHITE);
            }
        }
        return image;
    }
}
