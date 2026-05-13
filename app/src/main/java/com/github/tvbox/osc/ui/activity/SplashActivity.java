package com.github.tvbox.osc.ui.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.base.BaseActivity;
import com.github.tvbox.osc.ui.dialog.UpdateDialog;
import com.github.tvbox.osc.util.UpdateConfig;
import com.github.tvbox.osc.util.UpdateManager;

import java.io.File;

public class SplashActivity extends BaseActivity {

    private static final int SPLASH_DELAY = 1500;
    private boolean updateDialogShowing = false;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_splash;
    }

    @Override
    protected void init() {
        checkForUpdate();
    }

    private void checkForUpdate() {
        UpdateManager.checkUpdate(this, new UpdateManager.UpdateCheckCallback() {
            @Override
            public void onUpdateAvailable(UpdateConfig config) {
                updateDialogShowing = true;
                UpdateDialog dialog = new UpdateDialog(SplashActivity.this, config);
                if (!config.isForceUpdate()) {
                    dialog.setOnSkipListener(() -> goToHome());
                }
                dialog.setOnUpdateListener(new UpdateDialog.OnUpdateListener() {
                    @Override
                    public void onStartDownload() {
                    }

                    @Override
                    public void onInstall(File apkFile) {
                        goToHome();
                    }
                });
                dialog.setOnDismissListener(d -> {
                    updateDialogShowing = false;
                    if (config.isForceUpdate()) {
                        finishAffinity();
                    }
                });
                dialog.show();
            }

            @Override
            public void onNoUpdate() {
                goToHome();
            }

            @Override
            public void onError(String message) {
                goToHome();
            }
        });
    }

    private void goToHome() {
        if (isFinishing() || isDestroyed()) return;
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isFinishing() || isDestroyed()) return;
            Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_DELAY);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (updateDialogShowing) {
            finish();
        }
    }
}
