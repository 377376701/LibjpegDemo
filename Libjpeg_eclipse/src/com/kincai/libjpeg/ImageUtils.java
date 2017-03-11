package com.kincai.libjpeg;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.util.Log;

/**
 * ͼƬ������
 * 
 * @author KINCAI
 * 
 */
public class ImageUtils {
	static {
		System.loadLibrary("jpeg");// libjpeg
		System.loadLibrary("imagerar");// �����Լ��Ŀ�
	}

	/**
	 * ���ط��� JNI����ͼƬ
	 * 
	 * @param bitmap
	 *            bitmap
	 * @param width
	 *            ���
	 * @param height
	 *            �߶�
	 * @param quality
	 *            ͼƬ���� 100��ʾ���� ԽС��ѹ��Խ����
	 * @param fileName
	 *            �ļ�·����byte����
	 * @param optimize
	 *            �Ƿ���ù����������ݼ���
	 * @return "0"ʧ��, "1"�ɹ�
	 */
	public static native String compressBitmap(Bitmap bitmap, int width,
			int height, int quality, byte[] fileName, boolean optimize);

	/**
	 * �ߴ������ѹ�� û��oom����
	 * 
	 * @param bitmap
	 *            bitmap
	 * @param filePath
	 *            �ļ�·��
	 */
	public static void compressBitmap(Bitmap bitmap, String filePath) {
		// ���ͼƬ��С 150k
		int maxSize = 150;
		// �����趨�����ֱ��ʻ�ȡѹ������
		int ratio = ImageUtils.getRatioSize(bitmap.getWidth(),
				bitmap.getHeight());

		int afterWidth = bitmap.getWidth() / ratio;
		int afterHeight = bitmap.getHeight() / ratio;
		// ���ݱ���ѹ��Bitmap����Ӧ�ߴ�
		Bitmap result = Bitmap.createBitmap(afterWidth, afterHeight,
				Config.ARGB_8888);
		Canvas canvas = new Canvas(result);
		Rect rect = new Rect(0, 0, afterWidth, afterHeight);
		canvas.drawBitmap(bitmap, null, rect, null);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// ����ѹ������������100��ʾ��ѹ������ѹ��������ݴ�ŵ�baos��
		int options = 100;
		result.compress(Bitmap.CompressFormat.JPEG, options, baos);
		// ѭ���ж����ѹ����ͼƬ�Ƿ����100kb,���ڼ���ѹ��
		while (baos.toByteArray().length / 1024 > maxSize) {
			// ����baos
			baos.reset();
			options -= 10;
			result.compress(Bitmap.CompressFormat.JPEG, options, baos);
		}

		Log.e("ImageUtils", "save image " + options);

		// ����ͼƬ true��ʾʹ�ù������㷨
		ImageUtils.saveBitmap(result, options, filePath, true);

		if (result != null && !result.isRecycled()) {
			result.recycle();
			result = null;
		}
	}

	/**
	 * �ߴ������ѹ�� û��oom����
	 * 
	 * @param compressFilepath
	 *            ԭʼ�ļ�·��
	 * @param filePath
	 *            Ŀ���ļ�·��
	 */
	public static void compressBitmap(String compressFilepath, String filePath) {
		// ���ͼƬ��С 150KB
		int maxSize = 150;
		// ���ݵ�ַ��ȡbitmap
		Bitmap result = getBitmapFromFile(compressFilepath);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// ����ѹ������������100��ʾ��ѹ������ѹ��������ݴ�ŵ�baos��
		int quality = 100;
		result.compress(Bitmap.CompressFormat.JPEG, quality, baos);
		// ѭ���ж����ѹ����ͼƬ�Ƿ����100kb,���ڼ���ѹ��
		while (baos.toByteArray().length / 1024 > maxSize) {
			// ����baos�����baos
			baos.reset();
			quality -= 10;
			result.compress(Bitmap.CompressFormat.JPEG, quality, baos);
		}
		// ����ͼƬ true��ʾʹ�ù������㷨
		ImageUtils.saveBitmap(result, quality, filePath, true);
		// �ͷ�Bitmap
		if (!result.isRecycled()) {
			result.recycle();
		}

	}

	/**
	 * ͨ���ļ�·������ȡBitmap��ֹOOM�Լ����ͼƬ��ת����
	 * 
	 * @param filePath
	 * @return
	 */
	public static Bitmap getBitmapFromFile(String filePath) {
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		newOpts.inJustDecodeBounds = true;// ֻ����,��������
		BitmapFactory.decodeFile(filePath, newOpts);
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		// ��ȡ�ߴ�ѹ������
		newOpts.inSampleSize = ImageUtils.getRatioSize(w, h);
		newOpts.inJustDecodeBounds = false;// ��ȡ��������
		newOpts.inDither = false;
		newOpts.inPurgeable = true;
		newOpts.inInputShareable = true;
		newOpts.inTempStorage = new byte[32 * 1024];
		Bitmap bitmap = null;
		File file = new File(filePath);
		FileInputStream fs = null;
		try {
			fs = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			if (fs != null) {
				bitmap = BitmapFactory.decodeFileDescriptor(fs.getFD(), null,
						newOpts);
				// ��תͼƬ
				int photoDegree = readPictureDegree(filePath);
				if (photoDegree != 0) {
					Matrix matrix = new Matrix();
					matrix.postRotate(photoDegree);
					// �����µ�ͼƬ
					bitmap = Bitmap
							.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
									bitmap.getHeight(), matrix, true);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fs != null) {
				try {
					fs.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return bitmap;
	}

	/**
	 * ��ȡ��ת�Ƕ�
	 * 
	 * @param path
	 *            �ļ�·��
	 * @return �Ƕ�
	 */
	public static int readPictureDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}

	/**
	 * �������ű�
	 * 
	 * @param bitWidth
	 *            ͼƬ���
	 * @param bitHeight
	 *            ͼƬ�߶�
	 * @return ����
	 */
	public static int getRatioSize(int bitWidth, int bitHeight) {
		// ͼƬ���ֱ���
		int imageHeight = 1280;
		int imageWidth = 960;
		// ���ű�
		int ratio = 1;
		// ���ű�,�����ǹ̶��������ţ�ֻ�ø߻��߿�����һ�����ݽ��м��㼴��
		if (bitWidth > bitHeight && bitWidth > imageWidth) {
			// ���ͼƬ��ȱȸ߶ȴ�,�Կ��Ϊ��׼
			ratio = bitWidth / imageWidth;
		} else if (bitWidth < bitHeight && bitHeight > imageHeight) {
			// ���ͼƬ�߶ȱȿ�ȴ��Ը߶�Ϊ��׼
			ratio = bitHeight / imageHeight;
		}
		// ��С����Ϊ1
		if (ratio <= 0)
			ratio = 1;
		return ratio;
	}

	/**
	 * ����java��ѹ������ͨ��libjpeg��ѹ��
	 * 
	 * @param bitmap
	 *            bitmap
	 * @param quality
	 *            ͼƬ���� 100��ʾ���� ԽС��ѹ��Խ����
	 * @param fileName
	 *            �ļ�·����byte����
	 * @param optimize
	 *            �Ƿ���ù����������ݼ���
	 */
	private static void saveBitmap(Bitmap bitmap, int quality, String fileName,
			boolean optimize) {
		String code = compressBitmap(bitmap, bitmap.getWidth(),
				bitmap.getHeight(), quality, fileName.getBytes(), optimize);
		Log.e("ImageUtils", "code " + code);
	}

}
