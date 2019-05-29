package cn.wqgallery.myapplication;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import cn.wqgallery.myapplication.command.CommandUtils;

/**
 * @author Damon
 * @date 2019/05/27
 */

public class ZipHelper {
    /**
     * 执行结果回调
     */
    public interface OnResultListener {
        void onSuccess(String msg);

        void onFailure(int error, String msg);

        void onProgress(String msg);
    }

    /**
     * 将可执行文件 从assets拷贝到 /data/data/包名 下
     *
     * @param context
     * @param binaryName
     * @return
     */
    public static boolean loadBinary(Context context, String binaryName) {
        //context.getFilesDir()  获取/data/data//files目录 binaryName为目录下的文件名
        File binaryFile = new File(context.getFilesDir(), binaryName);
        if (binaryFile.exists()) {
            //存在 但不能执行
            if (!binaryFile.canExecute()) {
                //设置可执行并 返回加过
                binaryFile.setExecutable(true);
            }
        } else {
            //根据cpu abi拷贝可执行文件
            if (CommandUtils.copyAssets2File(context, binaryName)) {
                if (!binaryFile.canExecute()) {
                    //设置可执行并 返回加过
                    binaryFile.setExecutable(true);
                }
            }
        }
        return binaryFile.exists() && binaryFile.canExecute();
    }

    /**
     *使用runtime执行Linux指令
     * cmd为指令
     * listener为执行结果回调
     */
    public static void execute(Context context, String cmd, OnResultListener listener) {
        File filesDir = context.getFilesDir();
        // /data/data/包名/7zr
        new ExecuteAsyncTask(filesDir.getAbsolutePath() + "/" + cmd, listener).execute();
    }

    /**
     * 结果记录
     */
    static class Result {
        boolean success;
        String output;
        int errorno;

        public Result(boolean success, String output, int errorno) {
            this.success = success;
            this.output = output;
            this.errorno = errorno;
        }
    }


    /**
     * 异步任务
     */
    static class ExecuteAsyncTask extends AsyncTask<Void, String, Result> {
        private OnResultListener listener;
        private String cmd;

        public ExecuteAsyncTask(String cmd, OnResultListener listener) {
            this.cmd = cmd;
            this.listener = listener;
        }

        /**
         * 出来耗时任务
         */
        @Override
        protected Result doInBackground(Void... voids) {
            Process process = null;
            //执行结果输出
            String out;
            try {
                /**
                 * Runtime执行任务 cmd为执行命令
                 */
                process = Runtime.getRuntime().exec(cmd);
                //查看是否执行完成
                while (!isComplete(process)) {
                    //读取运行过程中的输出信息
                    BufferedReader reader = new BufferedReader(new InputStreamReader
                            (process.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        //报告执行过程
                        publishProgress(line);
                    }
                }
                int exitValue = process.exitValue();
                //成功
                if (exitValue == 0) {
                    out = CommandUtils.inputStream2String(process.getInputStream());
                } else {
                    out = CommandUtils.inputStream2String(process.getErrorStream());
                }
                return new Result(exitValue == 0, out, exitValue);
            } catch (IOException e) {
                e.printStackTrace();
                out = e.getMessage();
            } finally {
                if (null != process) {
                    process.destroy();
                }
            }
            return new Result(false, out, -1);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            listener.onProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Result result) {
            if (result.success) {
                listener.onSuccess(result.output);
            } else {
                listener.onFailure(result.errorno, result.output);
            }
        }

        /**
         * 查看程序是否结束
         *
         * @param process
         * @return
         */
        private boolean isComplete(Process process) {
            try {
                //如果已经结束则返回结果 否则会出现IllegalThreadStateException异常
                process.exitValue();
                return true;
            } catch (IllegalThreadStateException e) {
            }
            return false;
        }
    }
}
