package com.lx.digitalwatermark;

import android.graphics.Bitmap;
import android.os.Environment;

import androidx.core.content.FileProvider;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * @author linxiao
 * @title：BitMapUtils
 * @projectName DigitalWaterMark
 * @description: <Description>
 * @data Created in 2020/07/09
 */
class BitMapUtils {
    public static File getFile(Bitmap bitmap, File file) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        try {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            InputStream is = new ByteArrayInputStream(baos.toByteArray());
            int x = 0;
            byte[] b = new byte[1024 * 100];
            while ((x = is.read(b)) != -1) {
                fos.write(b, 0, x);
            }
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    public static void saveBitmap(Bitmap bitmap, File file) throws IOException {
//        Date date = new Date();
//        String timestamp = String.valueOf(date.getTime());
//        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +timestamp + ".jpg");
        if (file.exists()) {
            file.delete();
        }
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                out.flush();
                out.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
