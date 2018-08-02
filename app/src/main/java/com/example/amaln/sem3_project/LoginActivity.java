package com.example.amaln.sem3_project;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private static final String TAG = "Login.java";
    private Button mLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();

        final EditText emailEditText = findViewById(R.id.edit_text_email_login);
        final EditText passWordEditText = findViewById(R.id.edit_text_password_login);

        mLoginButton = findViewById(R.id.login_button_login_page);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                String passWord = passWordEditText.getText().toString();
                mLoginButton.setText(R.string.Logging_in);
                if(!TextUtils.isEmpty(passWord) && !TextUtils.isEmpty(email)) {
                    logIn(email, passWord);
                } else {
                    showSnackbar(findViewById(R.id.activity_login), "Please enter a valid email and password", Snackbar.LENGTH_LONG);
                    mLoginButton.setText(R.string.log_in);
                }
            }
        });
    }

    private void logIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            Intent intent = new Intent(LoginActivity.this, ScanActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            mLoginButton.setText(R.string.log_in);
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            showSnackbar(findViewById(R.id.activity_login), "Please enter a valid email and password", Snackbar.LENGTH_LONG);
                        }
                    }
                });
    }

    private void showSnackbar(View view, String message, int duration) {

        final Snackbar snackbar = Snackbar.make(view, message, duration);
        snackbar.setAction("DISMISS", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

}
