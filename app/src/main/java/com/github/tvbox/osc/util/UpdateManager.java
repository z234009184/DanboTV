package com.github.tvbox.osc.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.orhanobut.hawk.Hawk;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

/**
 * 应用内更新管理器
 * 负责版本检查、APK 下载、MD5 校验、安装
 */
public class UpdateManager {

    private static final String DOWNLOAD_TAG = "app_update";
    private static final String APK_FILE_NAME = "update.apk";
    private static final Gson GSON = new Gson();

    // ==================== 回调接口 ====================

    public interface UpdateCheckCallback {
        void onUpdateAvailable(UpdateConfig config);
        void onNoUpdate();
        void onError(String message);
    }

    public interface DownloadCallback {
        void onProgress(int percent);
        void onSuccess(File apkFile);
        void onError(String message);
    }

    // ==================== 版本检查 ====================

    /**
     * 从服务端检查更新
     * @param context  上下文
     * @param callback 检查结果回调
     */
    public static void checkUpdate(Context context, UpdateCheckCallback callback) {
        String updateUrl = Hawk.get(HawkConfig.UPDATE_CHECK_URL, "https://danboxingqiu.cn/updateTV.json");
        if (updateUrl.isEmpty()) {
            callback.onNoUpdate();
            return;
        }

        // 异步网络请求
        OkGo.<String>get(updateUrl)
                .tag(DOWNLOAD_TAG)
                .execute(new com.lzy.okgo.callback.StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            String json = response.body();
                            UpdateConfig config = GSON.fromJson(json, UpdateConfig.class);

                            if (config == null || config.getVersionCode() <= 0) {
                                callback.onError("配置解析失败");
                                return;
                            }

                            int localVersion = DefaultConfig.getAppVersionCode(context);
                            if (config.getVersionCode() > localVersion) {
                                callback.onUpdateAvailable(config);
                            } else {
                                callback.onNoUpdate();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            callback.onError("配置解析异常: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        callback.onError("网络请求失败: " +
                                (response.getException() != null ? response.getException().getMessage() : "未知错误"));
                    }
                });
    }

    // ==================== APK 下载 ====================

    /**
     * 下载 APK 文件
     * @param context   上下文
     * @param config    更新配置
     * @param callback  下载进度回调
     */
    public static void downloadApk(Context context, UpdateConfig config, DownloadCallback callback) {
        File downloadDir = context.getExternalCacheDir();
        if (downloadDir == null) {
            downloadDir = context.getCacheDir();
        }

        // 清理旧文件
        File oldApk = new File(downloadDir, APK_FILE_NAME);
        if (oldApk.exists()) {
            oldApk.delete();
        }

        OkGo.<File>get(config.getUpdateUrl())
                .tag(DOWNLOAD_TAG)
                .execute(new FileCallback(downloadDir.getAbsolutePath(), APK_FILE_NAME) {
                    @Override
                    public void onSuccess(Response<File> response) {
                        File apkFile = response.body();
                        // MD5 校验
                        if (config.getFileMd5() != null && !config.getFileMd5().isEmpty()) {
                            String fileMd5 = getFileMd5(apkFile);
                            if (fileMd5 != null && !fileMd5.equalsIgnoreCase(config.getFileMd5())) {
                                callback.onError("文件校验失败，请重新下载");
                                return;
                            }
                        }
                        callback.onSuccess(apkFile);
                    }

                    @Override
                    public void onError(Response<File> response) {
                        String msg = response.getException() != null ?
                                response.getException().getMessage() : "下载失败";
                        callback.onError(msg);
                    }

                    @Override
                    public void downloadProgress(Progress progress) {
                        callback.onProgress((int) (progress.fraction * 100));
                    }
                });
    }

    // ==================== 取消 ====================

    public static void cancel() {
        OkGo.getInstance().cancelTag(DOWNLOAD_TAG);
    }

    // ==================== APK 安装 ====================

    /**
     * 安装 APK 文件
     * @param context 上下文
     * @param apkFile APK 文件
     */
    public static void installApk(Context context, File apkFile) {
        if (!apkFile.exists()) {
            Toast.makeText(context, "安装包不存在", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Uri uri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                apkFile
        );
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    // ==================== MD5 工具 ====================

    /**
     * 获取文件的 MD5 值
     */
    private static String getFileMd5(File file) {
        if (!file.exists()) return null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            InputStream is = new FileInputStream(file);
            byte[] buffer = new byte[4096];
            int len;
            while ((len = is.read(buffer)) != -1) {
                md.update(buffer, 0, len);
            }
            is.close();
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
