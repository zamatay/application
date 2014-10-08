package ru.vkb.ui.login;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.requestmanager.RequestManager;

import ru.vkb.task.R;
import ru.vkb.common.messages;
import ru.vkb.model.RequestFactory;
import ru.vkb.model.RestRequestManager;
import ru.vkb.model.service.notificationService;


/**
 * A login screen that offers login via email/password.

 */
public class LoginActivity extends Activity{

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    // UI references.
    private EditText mHost;
    private EditText mLogin;
    private EditText mPassword;
    private View mProgress;
    private View mLoginFormView;
    //private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        //sp = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
        // Set up the login form.
        mLogin = (EditText) findViewById(R.id.login);
        mHost = (EditText) findViewById(R.id.host);

        mLogin.setText(PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.key_login), ""));
        mHost.setText(PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.key_host), "http://"));

        mPassword = (EditText) findViewById(R.id.password);
        mPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;

            }
        });

        Button mSignInButton = (Button) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_native_layout);
        mProgress = findViewById(R.id.login_progress);
        RequestFactory.setSessionID(null);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {

        // Сбрасываем все ошибки
        mLogin.setError(null);
        mPassword.setError(null);

        // Получаем данные в полях
        final String host = mHost.getText().toString();
        final String login = mLogin.getText().toString();
        final String password = mPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Проверяем логику на клиенте
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPassword.setError(getString(R.string.error_invalid_password));
            focusView = mPassword;
            cancel = true;
        }

        if (TextUtils.isEmpty(login)) {
            mLogin.setError(getString(R.string.error_field_required));
            focusView = mLogin;
            cancel = true;
        } else if (!isLoginValid(login)) {
            mLogin.setError(getString(R.string.error_invalid_login));
            focusView = mLogin;
            cancel = true;
        }


        if (cancel) {
            //если где то ошибка показываем ее
            focusView.requestFocus();
        } else {
            // если все хорошо шлем запрос на подключение
            showProgress(true);
            RestRequestManager.from(getApplicationContext()).execute(RequestFactory.getLogin(login, password, host), new RequestManager.RequestListener() {
                @Override
                public void onRequestFinished(Request request, Bundle resultData) {
                    String result = "false";
                    if (resultData.containsKey("result")) {
                        result = resultData.getString("result");
                    }
                    if (!result.equalsIgnoreCase("false")) {
                        RequestFactory.setSessionID(result);
                        saveLoginAndPassword(login, password, host);
                        // активити с списком задач
                        Intent intent = new Intent("ru.vkb.intent.action.SHOW_DISPOSALS");
                        startActivity(intent);
                        // запускаем сервис проверки задач
                        notificationService.startCheckDisposal(LoginActivity.this);
                        LoginActivity.this.finish();
                    }
                    else
                        setPasswordError();
                    showProgress(false);
                }

                private void saveLoginAndPassword(String login, String password, String host) {
                    // запоминаем введеный логин
                    SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this).edit();
                    //SharedPreferences.Editor edit = sp.edit();
                    edit.putString(getString(R.string.key_login), login);
                    edit.putString(getString(R.string.key_password), password);
                    edit.putString(getString(R.string.key_host), host);
                    edit.apply();
                    RequestFactory.HOST = host;
                }

                @Override
                public void onRequestConnectionError(Request request, int statusCode) {
                    showError(getString(R.string.ConnectError));
                }

                @Override
                public void onRequestDataError(Request request) {
                    showError(getString(R.string.ErrorData));
                }

                @Override
                public void onRequestCustomError(Request request, Bundle resultData) {
                    showError(getString(R.string.UnknownError));
                }

                private void showError(String text) {
                    showProgress(false);
                    setPasswordError();
                    messages.showError(LoginActivity.this, text);
                }

                private void setPasswordError(){
                    mPassword.setError(getString(R.string.error_incorrect_password));
                    mPassword.requestFocus();
                }
            });
        }
    }

    private boolean isLoginValid(String login) {
        return login.length() > 2;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgress.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgress.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}



