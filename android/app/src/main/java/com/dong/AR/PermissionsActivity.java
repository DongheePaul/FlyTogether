package com.dong.AR;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.dong.AR.analytics.Fa;
import com.google.ar.core.ArCoreApk;
import com.nhancv.kurentoandroid.R;

public class PermissionsActivity extends BaseActivity {

    private static final String TAG = "PermissionsActivity";

    @SuppressWarnings("FieldCanBeLocal")
    private final int SPLASH_DISPLAY_LENGTH = 2000;

    private Fa mAnalytics;

    private boolean mLaunchDrawAR = true;

    private AlertDialog mPermissionRationaleDialog;

    // Set to true ensures requestInstall() triggers installation if necessary.
    private boolean mUserRequestedARCoreInstall = true;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mLaunchDrawAR = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);

        mAnalytics = Fa.get();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check if ARCore is installed/up-to-date
        int message = -1;
        Exception exception = null;
        try {
            switch (ArCoreApk.getInstance()
                    .requestInstall(this, mUserRequestedARCoreInstall)) {
                case INSTALLED:
                    // Success.
                    break;
                case INSTALL_REQUESTED:
                    // Ensures next invocation of requestInstall() will either return
                    // INSTALLED or throw an exception.
                    mUserRequestedARCoreInstall = false;
                    // at this point, the activity is paused and user will go through
                    // installation process
                    return;
            }

        } catch (Exception e) {
            message = getARCoreInstallErrorMessage(e);
            exception = e;
        }

        // display possible ARCore error to user
        if (message >= 0) {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            Log.e(TAG, "Exception installing ARCore", exception);
            finish();
            return;
        }

        // when user comes back from system settings, send them to Draw AR if they
        // gave us the permissions
        if ((mPermissionRationaleDialog == null || !mPermissionRationaleDialog.isShowing())
                && !PermissionHelper.hasRequiredPermissions(this)) {
            PermissionHelper.requestRequiredPermissions(this, false);
        } else if (PermissionHelper.hasRequiredPermissions(this)) {
            startDrawARDelayed();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        PermissionHelper.sendPermissionsAnalytics(mAnalytics, permissions, grantResults);

        if (!PermissionHelper.hasRequiredPermissions(this)) {
            mPermissionRationaleDialog = PermissionHelper.requestRequiredPermissions(this, false);
        }
    }

    private void startDrawARDelayed() {
        // pass through any intent data we have
        final Intent startingIntent = getIntent();

        //hold here for 2 seconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mLaunchDrawAR) {
                    Intent drawArIntent = new Intent(PermissionsActivity.this,
                            DrawARActivity.class);
                    drawArIntent.setData(startingIntent.getData());
                    startActivity(drawArIntent);
                }
                finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}
