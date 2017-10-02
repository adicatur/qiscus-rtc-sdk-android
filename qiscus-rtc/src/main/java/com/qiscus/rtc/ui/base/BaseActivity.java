package com.qiscus.rtc.ui.base;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.qiscus.rtc.R;

/**
 * Created by rahardyan on 31/05/17.
 */

public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = BaseActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_CODE = 101;
    private Toolbar toolbar;
    private ProgressDialog progressDialog;
    private AlertDialog alertDialog;
    protected boolean isPermissionDenied = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getLayout() != 0) {
            setContentView(getLayout());
            setUpProgressDialog();
            setUpAlertDialog();
        } else {
            Log.e(TAG, "Please return layout ids on getLayout");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE && permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onPermissionGranted();
        }
    }

    /**
     * get activity layout
     *
     * @return layout id
     */
    public abstract int getLayout();

    /**
     * set up toolbar
     * @param toolbarId - toolbar resource id
     * @param isBackButtonEnable - true if want to show back button
     * @param title - toolbar title
     */
    public void setUpToolbar(@IdRes int toolbarId, boolean isBackButtonEnable, String title) {
        toolbar = (Toolbar) findViewById(toolbarId);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(isBackButtonEnable);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
            toolbar.setTitle(title);
        }
    }

    /**
     * make screen always on
     */
    public void setAlwaysOn() {
        getWindow().addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    /**
     * set activity to fullscreen
     */
    public void setFullscreen() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void setUpProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
    }

    private void setUpAlertDialog() {
        alertDialog = new AlertDialog.Builder(this)
                .setTitle("Error")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .create();
    }

    /**
     * show progress dialog
     */
    public void showProgressDialog() {
        if (progressDialog == null && !isFinishing()) {
            setUpProgressDialog();
            showProgressDialog();
        } else if (!isFinishing()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.show();
                }
            });
        }
    }

    private void showAlertDialog(String message) {
        if (alertDialog == null && !isFinishing()) {
            setUpAlertDialog();
            showAlertDialog(message);
        } else if (!isFinishing()) {
            alertDialog.setMessage(message);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    alertDialog.show();
                }
            });
        }
    }

    /**
     * dismiss progress dialog
     */
    public void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                }
            });
        }
    }

    /**
     * handle activity when no internet connection
     */
    public void onNoInternetConnection() {
        dismissProgressDialog();
        showAlertDialog(getResources().getString(R.string.error_no_internet));
    }

    /**
     * handle activity when api request failed
     *
     * @param errorMessage - error message
     */
    public void onRequestFailed(String errorMessage) {
        dismissProgressDialog();
        showAlertDialog(errorMessage);
    }

    /**
     * show Toast
     *
     * @param message - string toast messagegit
     */
    public void showToast(String message) {
        if (getApplicationContext() != null) {
            Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    /**
     * request permission
     *
     * @param requestedPermission - array of premission
     */
    public void requestPermission(String[] requestedPermission) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            isPermissionDenied = true;
            ActivityCompat.requestPermissions(this,
                    requestedPermission,
                    PERMISSION_REQUEST_CODE);
        } else if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            isPermissionDenied = false;
            onPermissionGranted();
        }
    }

    protected abstract void onPermissionGranted();
}
