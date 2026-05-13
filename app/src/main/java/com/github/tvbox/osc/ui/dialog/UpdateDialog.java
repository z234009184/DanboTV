package com.github.tvbox.osc.ui.dialog;

import android.content.Context;
import android.graphics.Color;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.github.tvbox.osc.util.UpdateConfig;
import com.github.tvbox.osc.util.UpdateManager;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public class UpdateDialog extends BaseDialog {

    private final UpdateConfig config;
    private OnUpdateListener onUpdateListener;
    private OnSkipListener onSkipListener;

    private TextView tvTitle;
    private TextView tvFileSize;
    private TextView tvUpdateLog;
    private View progressLayout;
    private ProgressBar progressBar;
    private TextView tvProgress;
    private TextView btnUpdate;
    private TextView btnSkip;
    private View btnLayout;

    private boolean isDownloading = false;

    public interface OnUpdateListener {
        void onStartDownload();
        void onInstall(File apkFile);
    }

    public interface OnSkipListener {
        void onSkip();
    }

    public UpdateDialog(@NonNull @NotNull Context context, UpdateConfig config) {
        super(context, R.style.CustomDialogStyleDim);
        this.config = config;
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.dialog_update);
        initView();
        bindData();
        bindEvent();
    }

    private void initView() {
        tvTitle = findViewById(R.id.updateTitle);
        tvFileSize = findViewById(R.id.updateFileSize);
        tvUpdateLog = findViewById(R.id.updateLog);
        progressLayout = findViewById(R.id.updateProgressLayout);
        progressBar = findViewById(R.id.updateProgressBar);
        tvProgress = findViewById(R.id.updateProgressText);
        btnUpdate = findViewById(R.id.updateBtn);
        btnSkip = findViewById(R.id.updateSkipBtn);
        btnLayout = findViewById(R.id.updateBtnLayout);
    }

    private void bindData() {
        tvTitle.setText("发现新版本 V" + config.getVersionName());

        // 文件大小
        if (config.getFileSize() > 0) {
            tvFileSize.setVisibility(View.VISIBLE);
            tvFileSize.setText("大小: " + formatFileSize(config.getFileSize()));
        }

        // 更新日志
        String log = config.getUpdateLog();
        if (log != null && !log.isEmpty()) {
            tvUpdateLog.setText(log);
        } else {
            tvUpdateLog.setText("优化用户体验，修复已知问题");
        }

        // 强制更新：隐藏跳过按钮，不可取消
        if (config.isForceUpdate()) {
            btnSkip.setVisibility(View.GONE);
            setCancelable(false);
        }
    }

    private void bindEvent() {
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                if (isDownloading) return;
                startDownload();
            }
        });

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                if (isDownloading) return;
                if (onSkipListener != null) {
                    onSkipListener.onSkip();
                }
                dismiss();
            }
        });

        setOnDismissListener(dialog -> {
            if (isDownloading) {
                UpdateManager.cancel();
            }
        });

        // 强制更新拦截返回键
        if (config.isForceUpdate()) {
            setOnKeyListener((dialog, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    return true;
                }
                return false;
            });
        }
    }

    private void startDownload() {
        isDownloading = true;
        setBtnEnabled(false);
        showProgress();

        if (onUpdateListener != null) {
            onUpdateListener.onStartDownload();
        }

        UpdateManager.downloadApk(getContext(), config, new UpdateManager.DownloadCallback() {
            @Override
            public void onProgress(int percent) {
                progressBar.setProgress(percent);
                tvProgress.setText(percent + "%");
            }

            @Override
            public void onSuccess(File apkFile) {
                isDownloading = false;
                dismiss();
                if (onUpdateListener != null) {
                    onUpdateListener.onInstall(apkFile);
                }
                UpdateManager.installApk(getContext(), apkFile);
            }

            @Override
            public void onError(String message) {
                isDownloading = false;
                setBtnEnabled(true);
                hideProgress();
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProgress() {
        progressLayout.setVisibility(View.VISIBLE);
        btnLayout.setVisibility(View.GONE);
    }

    private void hideProgress() {
        progressLayout.setVisibility(View.GONE);
        btnLayout.setVisibility(View.VISIBLE);
    }

    private void setBtnEnabled(boolean enabled) {
        btnUpdate.setEnabled(enabled);
        btnUpdate.setTextColor(enabled ? Color.WHITE : Color.GRAY);
        btnSkip.setEnabled(enabled);
        btnSkip.setTextColor(enabled ? Color.WHITE : Color.GRAY);
    }

    private String formatFileSize(long size) {
        if (size < 1024) return size + "B";
        if (size < 1024 * 1024) return String.format("%.1fKB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1fMB", size / (1024.0 * 1024));
        return String.format("%.2fGB", size / (1024.0 * 1024 * 1024));
    }

    public UpdateDialog setOnUpdateListener(OnUpdateListener listener) {
        this.onUpdateListener = listener;
        return this;
    }

    public UpdateDialog setOnSkipListener(OnSkipListener listener) {
        this.onSkipListener = listener;
        return this;
    }
}
