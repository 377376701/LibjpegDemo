package com.kincai.libjpeg;

import java.io.File;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity implements OnClickListener {
    private Button mCombineCompress, mCompressBtn,mOriginalBtn,mAfterCompressBtn;
    private ImageView mImage;


    /** ͼƬ��Ÿ�Ŀ¼*/
    private final String mImageRootDir = Environment
            .getExternalStorageDirectory().getPath() + "/jpeg_picture/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ѹ���󱣴���ʱ�ļ�Ŀ¼
        File tempFile = new File(mImageRootDir);
        if (!tempFile.exists()) {
            tempFile.mkdirs();
        }

        mCompressBtn = (Button) findViewById(R.id.compress_btn);
        mCombineCompress = (Button) findViewById(R.id.size_quality_libjpeg_compress_btn);
        mAfterCompressBtn = (Button) findViewById(R.id.after_compress_btn);
        mOriginalBtn = (Button) findViewById(R.id.original_btn);
        mImage = (ImageView) findViewById(R.id.image);


        mCompressBtn.setOnClickListener(this);
        mCombineCompress.setOnClickListener(this);
        mOriginalBtn.setOnClickListener(this);
        mAfterCompressBtn.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.compress_btn://ֱ��jni libjpegѹ��

                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        final File afterCompressImgFile = new File(mImageRootDir
                                + "/�ռ�ѹ��666.jpg");

                        String tempCompressImgPath = mImageRootDir+File.separator+"temp.jpg";//����׼���õ�sd��Ŀ¼�µ�ͼƬ
                        //ֱ��ʹ��jni libjpegѹ��
                        Bitmap bitmap = BitmapFactory.decodeFile(tempCompressImgPath);
                        String codeString = ImageUtils.compressBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), 40, afterCompressImgFile.getAbsolutePath().getBytes(), true);
                        Log.e("code", "code "+codeString);

                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mImage.setImageBitmap(BitmapFactory
                                        .decodeFile(afterCompressImgFile.getPath()));
                            }
                        });
                    }
                }).start();

                break;
            case R.id.size_quality_libjpeg_compress_btn://�ߴ� ���� libjpeg���ѹ��

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final File afterCompressImgFile = new File(mImageRootDir
                                + "/�ռ�ѹ��666.jpg");

                        //�ȳߴ�����ѹ��������jni libjpegѹ��   (�ȱ�֤SD��Ŀ¼��/jpeg_picture/temp.jpg����)
                        String tempCompressImgPath = mImageRootDir+File.separator+"temp.jpg";
                        ImageUtils.compressBitmap(tempCompressImgPath, afterCompressImgFile.getPath());

                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mImage.setImageBitmap(BitmapFactory
                                        .decodeFile(afterCompressImgFile.getPath()));
                            }
                        });
                    }
                }).start();


                break;
            case R.id.after_compress_btn://ѹ����

                final File afterCompressImgFile = new File(mImageRootDir
                        + "/�ռ�ѹ��666.jpg");
                mImage.setImageBitmap(BitmapFactory
                        .decodeFile(afterCompressImgFile.getPath()));
                break;
            case R.id.original_btn://ԭͼ

                String tempCompressImgPath = mImageRootDir+File.separator+"temp.jpg";
                mImage.setImageBitmap(BitmapFactory
                        .decodeFile(tempCompressImgPath));
                break;

            default:
                break;
        }

    }

}
