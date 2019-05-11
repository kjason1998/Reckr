package com.example.kevin.recordit.Activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.kevin.recordit.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

/**
 * login activity
 */
public class ActivityLogin extends AppCompatActivity {

    private Toolbar mToolbar;
    private FirebaseAuth mAuth;

    private LinearLayout background;

    private  Button login;

    private EditText registerEmail;
    private EditText registerPassword;

    private ProgressDialog loadingDialog;
    private DatabaseReference userRefrence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        mAuth = FirebaseAuth.getInstance();
        userRefrence = FirebaseDatabase.getInstance().getReference()
                .child(getResources().getString(R.string.database_user));

        mToolbar = (Toolbar) findViewById(R.id.login_tool_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Sign in");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        background = (LinearLayout) findViewById(R.id.login_background);

        registerEmail = (EditText) findViewById(R.id.fillTextEmail);
        registerPassword= (EditText) findViewById(R.id.fillTextPassword);

        login = (Button) findViewById(R.id.buttonEnter);
        loadingDialog = new ProgressDialog(this);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = registerEmail.getText().toString();
                String password = registerPassword.getText().toString();

                signIn(email,password);
            }
        });

        background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(ActivityLogin.this);
            }
        });
    }

    //using firebase create account
    private void signIn(String email, String password) {

        //check the EditText validation
        if(TextUtils.isEmpty(email)){
            Toast.makeText
                    (ActivityLogin.this,"please enter Email",Toast.LENGTH_LONG);
        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText
                    (ActivityLogin.this,"please enter Password",Toast.LENGTH_LONG);
        }
        else{
            //initiate the loading bar if the email and password are not empty.
            loadingDialog.setTitle("Logging in");
            loadingDialog.setMessage("Please wait a moment");
            loadingDialog.show();

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            //if the mAuth method is successful
                            if(task.isSuccessful()){
                                final String onlineUserId = mAuth.getCurrentUser().getUid();
                                FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( new OnSuccessListener<InstanceIdResult>() {
                                    @Override
                                    public void onSuccess(InstanceIdResult instanceIdResult) {
                                        String deviceToken = instanceIdResult.getToken();

                                        userRefrence.child(onlineUserId)
                                                .child(getResources().getString(R.string.database_user_token))
                                        .setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Intent mainIntent =
                                                        new Intent(ActivityLogin.this,
                                                                ActivityMain.class);
                                                //cant go back - only cant if log out
                                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|
                                                        Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(mainIntent);
                                                finish();
                                            }
                                        });
                                    }
                                });
                            }
                            else{
                                Toast.makeText(ActivityLogin.this,
                                        "Email or password are incorrect",Toast.LENGTH_LONG);
                            }
                            //dismissing the loading bar
                            loadingDialog.dismiss();
                        }
                    });
        }

    }
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
