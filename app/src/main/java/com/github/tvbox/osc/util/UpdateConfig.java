package com.github.tvbox.osc.util;

import com.google.gson.annotations.SerializedName;

/**
 * 应用更新配置数据模型
 * 对应服务端 update.json 结构
 */
public class UpdateConfig {

    @SerializedName("versionCode")
    private int versionCode;

    @SerializedName("versionName")
    private String versionName;

    @SerializedName("forceUpdate")
    private boolean forceUpdate;

    @SerializedName("updateUrl")
    private String updateUrl;

    @SerializedName("updateLog")
    private String updateLog;

    @SerializedName("fileSize")
    private long fileSize;

    @SerializedName("fileMd5")
    private String fileMd5;

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public boolean isForceUpdate() {
        return forceUpdate;
    }

    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

    public String getUpdateUrl() {
        return updateUrl;
    }

    public void setUpdateUrl(String updateUrl) {
        this.updateUrl = updateUrl;
    }

    public String getUpdateLog() {
        return updateLog;
    }

    public void setUpdateLog(String updateLog) {
        this.updateLog = updateLog;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }
}
