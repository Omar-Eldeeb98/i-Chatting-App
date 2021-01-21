package com.example.ichatting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {


    private FirebaseAuth auth;     // ---------------login
    private DatabaseReference usersReference;

    private Button loginButton ;
    private Button phoneLoginButton;

    private EditText loginEmailEditText ;
    private EditText loginPasswordEditText;

    private TextView forgetPasswordTextView;
    private TextView createAccountTextView;

    private ProgressDialog progressDialog;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_login );


        auth = FirebaseAuth.getInstance();   //--------------- login
        usersReference = FirebaseDatabase.getInstance().getReference("Users");


        initializeViews();




        createAccountTextView.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( LoginActivity.this , RegisterActivity.class );
                startActivity( intent );


            }
        } );

        loginButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allowUserToLogin();
            }
        } );


        phoneLoginButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(LoginActivity.this , PhoneLoginActivity.class);
                startActivity( intent );

            }
        } );

    }


    



    private void allowUserToLogin()
    {
        String email = loginEmailEditText.getText().toString();
        String password = loginPasswordEditText.getText().toString();

        if (TextUtils.isEmpty( email ))
        {
            Toast.makeText( this , "Please Enter Email ... " , Toast.LENGTH_SHORT ).show();
        }
        if (TextUtils.isEmpty( password ))
        {
            Toast.makeText( this , "Please Enter Password ... " , Toast.LENGTH_SHORT ).show();
        }

        else
        {

            progressDialog.setTitle( "Sign In " );
            progressDialog.setMessage( "Please Wait..." );
            progressDialog.setCanceledOnTouchOutside( true );
            progressDialog.show();

            auth.signInWithEmailAndPassword( email ,  password )
                    .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful())
                            {
                                String currentUserId = auth.getCurrentUser().getUid();
                                String deviceToken = FirebaseInstanceId.getInstance().getToken();
                                usersReference.child( currentUserId ).child( "device_token" ).setValue( deviceToken )
                                        .addOnCompleteListener( new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful())
                                                {
                                                    sendUserToMainActivity();
                                                    Toast.makeText( LoginActivity.this , "Logged in " , Toast.LENGTH_SHORT ).show();
                                                    progressDialog.dismiss();

                                                }

                                            }
                                        } );

                            }
                            else
                            {
                                String errorMessage = task.getException().toString();
                                Toast.makeText( LoginActivity.this , errorMessage ,  Toast.LENGTH_SHORT ).show();
                                progressDialog.dismiss();

                            }

                        }
                    } );

        }


    }

    private void initializeViews()
    {
        loginButton = (Button) findViewById( R.id.login_btn );
        phoneLoginButton = (Button) findViewById( R.id.login_phone_btn );

        loginEmailEditText = (EditText) findViewById( R.id.login_email );
        loginPasswordEditText = (EditText) findViewById( R.id.login_password );

        forgetPasswordTextView = (TextView) findViewById( R.id.forget_password_link ) ;
        createAccountTextView = (TextView) findViewById( R.id.create_new_account_link );

        progressDialog = new ProgressDialog( this );

    }





    private void sendUserToMainActivity()
    {
        Intent intent = new Intent( LoginActivity.this  , MainActivity.class );
        intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK  | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity( intent );
        finish();

    }




}