package cn.wqgallery.myapplication;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * 1 将assets下的可执行文件拷贝到应用私有目录
     *
     * @param view
     */

    public void cp(View view) {
        boolean result = ZipHelper.loadBinary(getApplicationContext(), "7zr");
        Toast.makeText(this, "结果" + result, Toast.LENGTH_SHORT).show();
    }

    /**
     * 2使用Runtime执行Linux命令
     * 压缩  命令  7zr a 输出文件（压缩后的文件） 需要压缩的文件 -mx=9
     * 例如  7zr a /sdcard/7-Zip.7z /sdcard/7-Zip
     * 注释：(把sdcard目录下的7-Zip文件压缩到sdcard文件下 压缩后的文件名7-Zip 格式7z  -mx=9设置压缩模式9为极限模式)
     *
     * 0	不压缩
     * 1	快速压缩
     * 5	正常压缩(默认值)
     * 7	最大压缩
     * 9	极限压缩
     * <p>
     * 注意你使用的是7zr可执行文件 那命令行就是7zr 如果是7z 命令就是7z
     *
     * @param view
     */
    public void compress(View view) {
//  获取sdcard目录 方法      Environment.getExternalStorageDirectory()
//        (我事先把一个7-Zip文件放到手机sdcard目录里面，实际业务你可以下载到Environment.getExternalStorageDirectory()目录里面)
        File src = new File(Environment.getExternalStorageDirectory(), "7-Zip");//被压缩的文件
        File out = new File(Environment.getExternalStorageDirectory(), "7-Text.7z");//压缩后的文件
        //命令
        String cmd = "7zr a " + out.getAbsolutePath() + " " + src.getAbsolutePath() + " -mx=9";
        ZipHelper.execute(getApplicationContext(), cmd, new ZipHelper.OnResultListener() {
            @Override
            public void onSuccess(String msg) {
                Log.e("zdh", "---------成功" + msg);
            }

            @Override
            public void onFailure(int error, String msg) {
                Log.e("zdh", "---------onFailure" + msg + "----------error" + error);
            }

            @Override
            public void onProgress(String msg) {
                Log.e("zdh", "---------onProgress" + msg);
            }
        });

    }

    /**
     * 3按使用7zr
     * 解压
     * 命令 7zr 需要解压文件路径 -o解压后的文件路径
     * 例如 7zr x /sdcard/7-Zip.7z -o/sdcard/7-Zip
     * 注释 （把sdcard目录下的7-Zip.7z 文件解压到sdcard目录里面名字为7-Zip）
     *
     * 注意-o后面不能有空格
     * @param view
     */
    public void decompress(View view) {
        File src = new File(Environment.getExternalStorageDirectory(), "7-Text.7z");//需要解压的7z文件
        File out = new File(Environment.getExternalStorageDirectory(), "7-newText");//解压后的文件

        //命令
        String cmd = "7zr x "+src.getAbsolutePath()+" -o"+out.getAbsolutePath();
        ZipHelper.execute(getApplicationContext(), cmd, new ZipHelper.OnResultListener() {
            @Override
            public void onSuccess(String msg) {
                Log.e("zdh", "---------成功" + msg);
            }

            @Override
            public void onFailure(int error, String msg) {
                Log.e("zdh", "---------onFailure" + msg + "----------error" + error);
            }

            @Override
            public void onProgress(String msg) {
                Log.e("zdh", "---------onProgress" + msg);
            }
        });


    }



}
