/*
 * Copyright 2012 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.idlesoft.android.apps.github.ui.activities.app;

import com.actionbarsherlock.app.ActionBar;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.authenticator.AuthConstants;
import net.idlesoft.android.apps.github.ui.activities.BaseActivity;
import net.idlesoft.android.apps.github.utils.TextWatcherAdapter;
import net.idlesoft.android.apps.github.utils.ToastUtil;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.UserService;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import roboguice.inject.InjectView;
import roboguice.util.RoboAsyncTask;

import static android.R.layout.simple_dropdown_item_1line;
import static android.accounts.AccountManager.ERROR_CODE_CANCELED;
import static android.accounts.AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE;
import static android.accounts.AccountManager.KEY_ACCOUNT_NAME;
import static android.accounts.AccountManager.KEY_ACCOUNT_TYPE;
import static android.accounts.AccountManager.KEY_AUTHTOKEN;
import static android.accounts.AccountManager.KEY_BOOLEAN_RESULT;
import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.KEYCODE_ENTER;
import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;
import static net.idlesoft.android.apps.github.HubroidConstants.USER_AGENT_STRING;
import static net.idlesoft.android.apps.github.authenticator.AuthConstants.GITHUB_ACCOUNT_TYPE;

/**
 * Activity to authenticate the user against gaug.es
 */
public class GitHubAuthenticatorActivity extends BaseActivity {

    /**
     * PARAM_CONFIRMCREDENTIALS
     */
    public static final String PARAM_CONFIRMCREDENTIALS = "confirmCredentials";

    /**
     * PARAM_PASSWORD
     */
    public static final String PARAM_PASSWORD = "password";

    /**
     * PARAM_LOGIN
     */
    public static final String PARAM_LOGIN = "account";

    /**
     * PARAM_AUTHTOKEN_TYPE
     */
    public static final String PARAM_AUTHTOKEN_TYPE = "authtokenType";

    private static final String TAG = "GitHubAuthActivity";

    private AccountManager mAccountManager;

    @InjectView(R.id.et_auth_field_login)
    private AutoCompleteTextView mLoginText;

    @InjectView(R.id.et_auth_field_password)
    private EditText mPasswordText;

    @InjectView(R.id.btn_auth_sign_in)
    private Button mSignInButton;

    private TextWatcher mWatcher = validationTextWatcher();

    private RoboAsyncTask<Boolean> mAuthenticationTask;

    private String mAuthToken;

    private String mAuthTokenType;

    private AccountAuthenticatorResponse mAccountAuthenticatorResponse = null;

    private Bundle mResultBundle = null;

    /**
     * If set we are just checking that the user knows their credentials; this doesn't cause the
     * user's mPassword to be changed on the device.
     */
    private Boolean mConfirmCredentials = false;

    private String mLogin;

    private String mPassword;

    /**
     * Was the original caller asking for an entirely new account?
     */
    protected boolean mRequestNewAccount = false;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle, R.layout.login_activity);

        mAccountAuthenticatorResponse = getIntent()
                .getParcelableExtra(KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);

        if (mAccountAuthenticatorResponse != null) {
            mAccountAuthenticatorResponse.onRequestContinued();
        }
        mAccountManager = AccountManager.get(this);
        final Intent intent = getIntent();
        mLogin = intent.getStringExtra(PARAM_LOGIN);
        mAuthTokenType = intent.getStringExtra(PARAM_AUTHTOKEN_TYPE);
        mRequestNewAccount = mLogin == null;
        mConfirmCredentials = intent.getBooleanExtra(PARAM_CONFIRMCREDENTIALS, false);

        mLoginText.setAdapter(
                new ArrayAdapter<String>(this, simple_dropdown_item_1line, userLoginAccounts()));

        mPasswordText.setOnKeyListener(new OnKeyListener() {

            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event != null && ACTION_DOWN == event
                        .getAction() && keyCode == KEYCODE_ENTER && mSignInButton.isEnabled()) {
                    handleLogin(mSignInButton);
                    return true;
                }
                return false;
            }
        });

        mPasswordText.setOnEditorActionListener(new OnEditorActionListener() {

            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == IME_ACTION_DONE && mSignInButton.isEnabled()) {
                    handleLogin(mSignInButton);
                    return true;
                }
                return false;
            }
        });

        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin(mSignInButton);
            }
        });

        mLoginText.addTextChangedListener(mWatcher);
        mPasswordText.addTextChangedListener(mWatcher);

        TextView signupText = (TextView) findViewById(R.id.tv_auth_link_sign_up);
        signupText.setMovementMethod(LinkMovementMethod.getInstance());
        signupText.setText(Html.fromHtml(getString(R.string.auth_link_signup)));
    }

    @Override
    public void onCreateActionBar(ActionBar bar) {
        super.onCreateActionBar(bar);

        bar.setHomeButtonEnabled(false);
        bar.setDisplayHomeAsUpEnabled(false);
    }

    private List<String> userLoginAccounts() {
        Object[] accounts;
        List<Account> accountList = new ArrayList<Account>(Arrays.asList(
                mAccountManager.getAccountsByType("com.google")));
        accountList.addAll(Arrays.asList(mAccountManager.getAccountsByType("com.github.gauges")));
        accountList.addAll(Arrays.asList(mAccountManager.getAccountsByType("com.github")));
        accounts = accountList.toArray();
        List<String> logins = new ArrayList<String>(accounts.length);
        for (Object account : accounts) {
            logins.add(((Account) account).name);
        }
        return logins;
    }

    private TextWatcher validationTextWatcher() {
        return new TextWatcherAdapter() {
            public void afterTextChanged(Editable gitDirEditText) {
                updateUIWithValidation();
            }

        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUIWithValidation();
    }

    private void updateUIWithValidation() {
        boolean populated = populated(mLoginText) && populated(mPasswordText);
        mSignInButton.setEnabled(populated);
    }

    private boolean populated(EditText editText) {
        return editText.length() > 0;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getText(R.string.auth_message_signing_in));
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                if (mAuthenticationTask != null) {
                    mAuthenticationTask.cancel(true);
                }
            }
        });
        return dialog;
    }

    /**
     * Handles onClick event on the Submit button. Sends username/mPassword to the server for
     * authentication. <p/> Specified by android:onClick="handleLogin" in the layout xml
     */
    public void handleLogin(View view) {
        if (mAuthenticationTask != null) {
            return;
        }

        if (mRequestNewAccount) {
            mLogin = mLoginText.getText().toString();
        }
        mPassword = mPasswordText.getText().toString();
        showProgress();

        mAuthenticationTask = new RoboAsyncTask<Boolean>(this) {
            public Boolean call() throws Exception {
                GitHubClient client = new GitHubClient();
                client.setCredentials(mLogin, mPassword);
                client.setUserAgent(USER_AGENT_STRING);

                UserService service = new UserService(client);
                User user = service.getUser();

                if (user != null) {
                    /* Make sure we're using the user's username, rather than email */
                    mLogin = user.getLogin();

                    return true;
                }

                return false;
            }

            @Override
            protected void onException(Exception e) throws RuntimeException {
                Throwable cause = e.getCause() != null ? e.getCause() : e;

                String message;
                /* A 401 is returned as an IOException with this message */
                if ("Received authentication challenge is null".equals(cause.getMessage())) {
                    message = getResources().getString(R.string.auth_message_bad_credentials);
                } else {
                    message = cause.getMessage();
                }

                ToastUtil.toastOnUiThread(GitHubAuthenticatorActivity.this, message);
            }

            @Override
            public void onSuccess(Boolean authSuccess) {
                onAuthenticationResult(authSuccess);
            }

            @Override
            protected void onFinally() throws RuntimeException {
                hideProgress();
                mAuthenticationTask = null;
            }
        };
        mAuthenticationTask.execute();
    }

    /**
     * Called when response is received from the server for confirm credentials request. See
     * onAuthenticationResult(). Sets the AccountAuthenticatorResult which is sent back to the
     * caller.
     */
    protected void finishConfirmCredentials(boolean result) {
        final Account account = new Account(mLogin, GITHUB_ACCOUNT_TYPE);
        mAccountManager.setPassword(account, mPassword);

        final Intent intent = new Intent();
        intent.putExtra(KEY_BOOLEAN_RESULT, result);
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * Called when response is received from the server for authentication request. See
     * onAuthenticationResult(). Sets the AccountAuthenticatorResult which is sent back to the
     * caller. Also sets the mAuthToken in AccountManager for this account.
     */

    protected void finishLogin() {
        final Account account = new Account(mLogin, GITHUB_ACCOUNT_TYPE);

        if (mRequestNewAccount) {
            mAccountManager.addAccountExplicitly(account, mPassword, null);
        } else {
            mAccountManager.setPassword(account, mPassword);
        }
        final Intent intent = new Intent();
        mAuthToken = mPassword;
        intent.putExtra(KEY_ACCOUNT_NAME, mLogin);
        intent.putExtra(KEY_ACCOUNT_TYPE, GITHUB_ACCOUNT_TYPE);
        if (mAuthTokenType != null && mAuthTokenType.equals(AuthConstants.AUTHTOKEN_TYPE)) {
            intent.putExtra(KEY_AUTHTOKEN, mAuthToken);
        }
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * Hide progress dialog
     */
    protected void hideProgress() {
        dismissDialog(0);
    }

    /**
     * Show progress dialog
     */
    protected void showProgress() {
        showDialog(0);
    }

    /**
     * Called when the authentication process completes (see attemptLogin()).
     */
    public void onAuthenticationResult(boolean result) {
        if (result) {
            if (!mConfirmCredentials) {
                finishLogin();
            } else {
                finishConfirmCredentials(true);
            }
        } else {
            Log.e(TAG, "onAuthenticationResult: failed to authenticate");
            if (mRequestNewAccount) {
                ToastUtil.toastOnUiThread(GitHubAuthenticatorActivity.this,
                        R.string.auth_message_auth_failed_new_account);
            } else {
                ToastUtil.toastOnUiThread(GitHubAuthenticatorActivity.this,
                        R.string.auth_message_auth_failed);
            }
        }
    }

    /**
     * Set the result that is to be sent as the result of the request that caused this Activity to
     * be launched. If result is null or this method is never called then the request will be
     * canceled.
     *
     * @param result this is returned as the result of the AbstractAccountAuthenticator request
     */
    public final void setAccountAuthenticatorResult(Bundle result) {
        mResultBundle = result;
    }

    /**
     * Sends the result or a Constants.ERROR_CODE_CANCELED error if a result isn't present.
     */
    public void finish() {
        if (mAccountAuthenticatorResponse != null) {
            // send the result bundle back if set, otherwise send an error.
            if (mResultBundle != null) {
                mAccountAuthenticatorResponse.onResult(mResultBundle);
            } else {
                mAccountAuthenticatorResponse.onError(ERROR_CODE_CANCELED, "canceled");
            }

            mAccountAuthenticatorResponse = null;
        }
        super.finish();
    }
}