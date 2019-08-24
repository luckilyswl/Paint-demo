package com.qingshangzuo.paint;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


/**
 *  base64 和 bitmap 互转的工具类
 */
public class Base64BitmapUtil {

    public static String bitmapToBase64(Bitmap bitmap){

        String result = null;
        ByteArrayOutputStream baos = null;

        try{
            if(bitmap != null){
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes,Base64.DEFAULT);
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try{
                if(baos != null){
                    baos.flush();
                    baos.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return result;
    }

    public static Bitmap base64ToBitmap(String base64Data){
        byte[] bytes = Base64.decode(base64Data,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }
}
