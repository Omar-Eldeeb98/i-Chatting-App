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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

public class    RegisterActivity extends AppCompatActivity {

    private EditText registerEmailEditText;
    private EditText registerPasswordEditText;
    private Button createAccountButton;
    private TextView alreadyHaveAnAccountTextView;
    private ProgressDialog progressDialog;
    private FirebaseAuth auth;  // ------------- register
   // private static final String TAG = "RegisterActivity";



    private FirebaseDatabase database;
    private DatabaseReference databaseReference;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_register );

        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("Users");


        auth = FirebaseAuth.getInstance();// --------------- register


        initializeViews();



        alreadyHaveAnAccountTextView.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               sendUserToLoginActivity();
            }
        } );

        createAccountButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAccount();
            }
        } );



    }

    private void createNewAccount()
    {
         final String email = registerEmailEditText.getText().toString();
         final String password = registerPasswordEditText.getText().toString();

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

            progressDialog.setTitle( "Creating New Account ... " );
            progressDialog.setMessage( "Please Wait  , Util Finishing Creating Your Account " );
            progressDialog.setCanceledOnTouchOutside( true );
            progressDialog.show();



            auth.createUserWithEmailAndPassword( email , password )  // ---------------------- register
                      .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful())
                {

                    String currentUserID = auth.getCurrentUser().getUid();
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();
                    databaseReference.child(currentUserID  ).child( "device_token" ).setValue( deviceToken );
                    databaseReference.child( currentUserID ).child( "email" ).setValue( email );
                    databaseReference.child( currentUserID ).child( "password" ).setValue( password );
                     // databaseReference.child( currentUserID ).setValue( "" );
                    //    Log.d(TAG , "FARES UID" + auth.getCurrentUser().getUid());

                   sendUserToMainActivity();
                    Toast.makeText( RegisterActivity.this , "Account Created  " ,  Toast.LENGTH_SHORT ).show();
                    progressDialog.dismiss();
                }
                else
                {
                    String errorMessage = task.getException().toString();
                    Toast.makeText( RegisterActivity.this , errorMessage ,  Toast.LENGTH_SHORT ).show();
                    progressDialog.dismiss();



                }


            }
        } );


        }
    }

    private void initializeViews() {

        registerEmailEditText  = (EditText) findViewById( R.id.register_email );
        registerPasswordEditText = (EditText) findViewById( R.id.register_password );

        createAccountButton = (Button ) findViewById( R.id.register_btn );

        alreadyHaveAnAccountTextView = (TextView) findViewById( R.id.already_have_account );

        progressDialog = new ProgressDialog( this );

    }

    private void sendUserToLoginActivity()
    {
        Intent intent = new Intent( RegisterActivity.this  , LoginActivity.class );
        startActivity( intent );

    }




    private void sendUserToMainActivity()
    {
        Intent intent = new Intent( RegisterActivity.this  , MainActivity.class );
        intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK  | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity( intent );
        finish();

    }

}


