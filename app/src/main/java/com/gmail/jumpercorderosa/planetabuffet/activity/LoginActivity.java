package com.gmail.jumpercorderosa.planetabuffet.activity;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.gmail.jumpercorderosa.planetabuffet.R;
import com.gmail.jumpercorderosa.planetabuffet.db.DBHandler;

import org.json.JSONObject;

/**
 * A login screen that offers login via email/password.
 */

public class LoginActivity extends AppCompatActivity {

    // UI references.
    private AutoCompleteTextView tvEmail;
    private EditText etPassword;
    private CheckBox cbKeepConnected;
    private TextView tvForgottenPassword;
    private Button btnLogin;
    private LoginButton btnLoginFacebook;
    private TextView tvSignUp;
    CallbackManager callbackManager;
    private View mProgressView;
    private View mLoginFormView;
    private static final String KEEP_CONNECTED = "keep_connected";
    private static final String USER_ID = "user_id";
    private static final String PREFS_NAME = "pref";

    private DBHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);

        db = new DBHandler(this);

        //Set up the login form.
        tvEmail = (AutoCompleteTextView) findViewById(R.id.tvEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        cbKeepConnected = (CheckBox) findViewById(R.id.cbKeepConnected);
        tvForgottenPassword = (TextView) findViewById(R.id.tvPasswordForgotten);
        btnLogin = (Button) findViewById(R.id.btnEmailLogin);

        tvSignUp = (TextView) findViewById(R.id.tvSigUp);

        skipLogin();

        //tvEmail.setText("dani");
        //etPassword.setText("dani");

        //LOGIN EMAIL
        btnLogin.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                //antes fazia o login pelo email, portanto tvEmail eh tvLogin (arrumar)
                String email = tvEmail.getText().toString();
                String password = etPassword.getText().toString();

                if(email.equals("")) {
                        //|| !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    //Toast.makeText(v.getContext(), "Please enter a valid email address", Toast.LENGTH_LONG).show();
                    Toast.makeText(v.getContext(), "Please enter a login", Toast.LENGTH_LONG).show();
                    return;
                }

                if(password.equals("")) {
                    Toast.makeText(v.getContext(), "Please a password", Toast.LENGTH_LONG).show();
                    return;
                }

                //verifica se o usuario/senha estão na base, ou faz um baca
                if (db.checkUser(email, password) ||
                        (email.equals("dani") && password.equals("dani"))) {

                    //salva USER_ID no sharedPreferences
                    int user_id = db.getUserId(email);

                    SharedPreferences sharedPref = LoginActivity.this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt(USER_ID, user_id);
                    editor.apply();

                    // btnLogin.setEnabled(true);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    LoginActivity.this.finish(); //destroe esta activity, para não voltar para tela de splash

                } else {
                    //Login ou senha inválidos
                    Toast.makeText(v.getContext(), "Login or password was entered incorrectly!", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });


        //Salva o keep connected no shared preferences
        cbKeepConnected.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SharedPreferences sharedPref = LoginActivity.this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                if (cbKeepConnected.isChecked()) {
                    editor.putBoolean(KEEP_CONNECTED, true);
                    editor.apply();
                } else {
                    editor.putBoolean(KEEP_CONNECTED, false);
                    editor.apply();
                }
            }

        });

        //LOGIN FACEBOOK
        callbackManager = CallbackManager.Factory.create();

        btnLoginFacebook = (LoginButton) findViewById(R.id.login_button);
        btnLoginFacebook.setReadPermissions("email");

        // Callback registration
        btnLoginFacebook.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();

                //Obtenho os dados do facebook
                Bundle bundle = new Bundle();
                bundle.putString("fields", "name, last_name, email, picture");

                GraphRequest graphRequest = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.i("LOG", "onCompleted: " + object.toString());
                    }
                });

                graphRequest.setParameters(bundle);
                graphRequest.executeAsync();

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                LoginActivity.this.finish(); //destroe esta activity, para não voltar para tela de splash
            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(), "onCancel", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(getApplicationContext(), "onError", Toast.LENGTH_SHORT).show();
            }
        });

        //NOVO CADASTRO
        tvSignUp.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(intent);
            }
        });
    }

    public void skipLogin(){
        SharedPreferences sharedPref = LoginActivity.this.getPreferences(Context.MODE_PRIVATE);
        boolean connected = sharedPref.getBoolean(KEEP_CONNECTED, false);
        if (connected){
            // skip login
            cbKeepConnected.setChecked(true);
            Toast.makeText(LoginActivity.this, "You are already connected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}

