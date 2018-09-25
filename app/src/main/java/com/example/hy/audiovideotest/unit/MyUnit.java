package com.example.hy.audiovideotest.unit;

import android.os.Environment;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

/**
 * 一些公共工具类
 * Created by 陈健宇 at 2018/9/21
 */
public class MyUnit {

    /**
     * 关闭文件流接口
     * @param closeable
     */
    public static void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建外部文件夹
     * @param name 文件名
     */
    public static File getExternalPath(String name){
        File file = null;
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            file = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + name);
        }
        return file;
    }
}
