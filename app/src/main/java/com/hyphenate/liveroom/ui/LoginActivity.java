/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hyphenate.liveroom.ui;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.liveroom.R;
import com.hyphenate.liveroom.utils.CommonUtils;
import com.hyphenate.liveroom.manager.PreferenceManager;
import com.hyphenate.liveroom.widgets.EaseTipDialog;

public class LoginActivity extends BaseActivity {
    private static final String TAG = "LoginActivity";

    /**
     * Login username
     */
    private EditText etUsername;
    /**
     * Login password
     */
    private EditText etPassword;

    /**
     * progress is showing
     */
    private boolean progressShow;

    private ProgressDialog progressDialog;

    private EaseTipDialog tipDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (EMClient.getInstance().isLoggedInBefore()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);
        initViews();
        initListener();
        loadLastLoginUsername();
    }

    /**
     * Initialize views
     */
    private void initViews() {
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
    }

    private void initListener() {
        // if user changed, clear the password
        etUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                etPassword.setText(null);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE
                    || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                    && (event.getAction() == KeyEvent.ACTION_DOWN))) {
                login(null);
                return true;
            } else {
                return false;
            }
        });
    }

    /**
     * Set Last login username
     */
    private void loadLastLoginUsername() {
        if (PreferenceManager.getInstance().getCurrentUsername() != null) {
            etUsername.setText(PreferenceManager.getInstance().getCurrentUsername());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loadLastLoginUsername();
    }

    /**
     * check Input error
     *
     * @return Return true is has error, otherwise Return false.
     */
    private boolean checkInputError() {
        String currentUsername = getInputUsername();
        String currentPassword = getInputPassword();

        if (TextUtils.isEmpty(currentUsername)) {
            tipDialog = new EaseTipDialog.Builder(this)
                    .setStyle(EaseTipDialog.TipDialogStyle.INFO)
                    .setTitle(R.string.User_name_cannot_be_empty).build();
            tipDialog.show();
            return true;
        }
        if (TextUtils.isEmpty(currentPassword)) {
            tipDialog = new EaseTipDialog.Builder(this)
                    .setStyle(EaseTipDialog.TipDialogStyle.INFO)
                    .setTitle(R.string.Password_cannot_be_empty).build();
            tipDialog.show();
            return true;
        }
        return false;
    }

    /**
     * check network error
     *
     * @return Return true is has no network, otherwise Return false.
     */
    private boolean checkNetworkError() {
        if (!CommonUtils.isNetWorkConnected(this)) {
            tipDialog = new EaseTipDialog.Builder(this)
                    .setStyle(EaseTipDialog.TipDialogStyle.INFO)
                    .setTitle(R.string.network_not_available).build();
            tipDialog.show();
            return true;
        }
        return false;
    }


    private String getInputUsername() {
        return etUsername.getText().toString().trim();
    }

    private String getInputPassword() {
        return etPassword.getText().toString().trim();
    }

    private void showLoginPd() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    Log.d(TAG, "EMClient.getInstance().onCancel");
                    progressShow = false;
                }
            });
        }
        progressDialog.setMessage(getString(R.string.Is_landing));
        progressDialog.show();
    }

    private void showRegisterPd() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    Log.d(TAG, "EMClient.getInstance().onCancel");
                    progressShow = false;
                }
            });
        }
        progressDialog.setMessage(getString(R.string.Is_the_registered));
        progressDialog.show();
    }

    private void hidePd() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    /**
     * login
     *
     * @param view
     */
    public void login(View view) {

        if (checkNetworkError() || checkInputError()) {
            return;
        }
        showLoginPd();
        progressShow = true;

        final String inputUsername = getInputUsername();
        final String inputPassword = getInputPassword();

        // call login method
        Log.d(TAG, "EMClient.getInstance().login");
        EMClient.getInstance().login(inputUsername, inputPassword, new EMCallBack() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "login: onSuccess");
                if (isFinishing()) {
                    return;
                }
                runOnUiThread(() -> {
                    hidePd();
                    hideSoftKeyboard();
                    PreferenceManager.getInstance().setCurrentUserName(inputUsername);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onProgress(int progress, String status) {
            }

            @Override
            public void onError(final int code, final String message) {
                Log.d(TAG, "login: onError: " + code);
                if (!progressShow) {
                    return;
                }
                runOnUiThread(() -> {
                    hidePd();
                    if (code == EMError.USER_AUTHENTICATION_FAILED) {
                        tipDialog = new EaseTipDialog.Builder(LoginActivity.this)
                                .setStyle(EaseTipDialog.TipDialogStyle.ERROR)
                                .setTitle(R.string.Login_failed)
                                .setMessage(R.string.username_or_pwd_is_wrong).build();
                    } else if (code == EMError.USER_NOT_FOUND) {
                        tipDialog = new EaseTipDialog.Builder(LoginActivity.this)
                                .setStyle(EaseTipDialog.TipDialogStyle.ERROR)
                                .setTitle(R.string.Login_failed)
                                .setMessage(R.string.username_not_found).build();
                    } else {
                        tipDialog = new EaseTipDialog.Builder(LoginActivity.this)
                                .setStyle(EaseTipDialog.TipDialogStyle.ERROR)
                                .setTitle(R.string.Login_failed)
                                .setMessage(message).build();
                    }
                    tipDialog.show();
                });
            }
        });
    }

    /**
     * register
     *
     * @param view
     */
    public void register(View view) {

        if (checkNetworkError() || checkInputError()) {
            return;
        }

        showRegisterPd();

        final String inputUsername = getInputUsername();
        final String inputPassword = getInputPassword();

        new Thread(() -> {
            try {
                // call method in SDK
                EMClient.getInstance().createAccount(inputUsername, inputPassword);
                if (isFinishing()) {
                    return;
                }
                runOnUiThread(() -> {
                    hidePd();
                    // save current user
                    PreferenceManager.getInstance().setCurrentUserName(inputUsername);
                    tipDialog = new EaseTipDialog.Builder(LoginActivity.this)
                            .setStyle(EaseTipDialog.TipDialogStyle.INFO)
                            .setTitle(R.string.Registered_successfully)
                            .setMessage(R.string.Registered_successfully).build();

                    tipDialog.show();
                });
            } catch (final HyphenateException e) {
                if (isFinishing()) {
                    return;
                }
                runOnUiThread(() -> {
                    hidePd();
                    int errorCode = e.getErrorCode();
                    if (errorCode == EMError.NETWORK_ERROR) {
                        tipDialog = new EaseTipDialog.Builder(LoginActivity.this)
                                .setStyle(EaseTipDialog.TipDialogStyle.ERROR)
                                .setTitle(R.string.Registered_failed)
                                .setMessage(R.string.network_anomalies).build();

                        tipDialog.show();
                    } else if (errorCode == EMError.USER_ALREADY_EXIST) {
                        tipDialog = new EaseTipDialog.Builder(LoginActivity.this)
                                .setStyle(EaseTipDialog.TipDialogStyle.ERROR)
                                .setTitle(R.string.Registered_failed)
                                .setMessage(R.string.User_already_exists).build();

                        tipDialog.show();
                    } else if (errorCode == EMError.USER_AUTHENTICATION_FAILED) {
                        tipDialog = new EaseTipDialog.Builder(LoginActivity.this)
                                .setStyle(EaseTipDialog.TipDialogStyle.ERROR)
                                .setTitle(R.string.Registered_failed)
                                .setMessage(R.string.registration_failed_without_permission).build();

                        tipDialog.show();
                    } else if (errorCode == EMError.USER_ILLEGAL_ARGUMENT) {
                        tipDialog = new EaseTipDialog.Builder(LoginActivity.this)
                                .setStyle(EaseTipDialog.TipDialogStyle.ERROR)
                                .setTitle(R.string.Registered_failed)
                                .setMessage(R.string.illegal_user_name).build();

                        tipDialog.show();
                    } else if (errorCode == EMError.EXCEED_SERVICE_LIMIT) {
                        tipDialog = new EaseTipDialog.Builder(LoginActivity.this)
                                .setStyle(EaseTipDialog.TipDialogStyle.ERROR)
                                .setTitle(R.string.Registered_failed)
                                .setMessage(R.string.register_exceed_service_limit).build();

                        tipDialog.show();
                    } else {
                        tipDialog = new EaseTipDialog.Builder(LoginActivity.this)
                                .setStyle(EaseTipDialog.TipDialogStyle.ERROR)
                                .setTitle(R.string.Registered_failed)
                                .setMessage(R.string.Registered_failed).build();

                        tipDialog.show();
                    }

                });
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hidePd();
        if (tipDialog != null && tipDialog.isShowing()) {
            tipDialog.dismiss();
        }
    }
}
