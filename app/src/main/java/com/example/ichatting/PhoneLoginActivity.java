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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private Button sendVerificationCodeButton ,
                   verifyButton;

    private EditText inputPhoneNumber  ,
                     inputVerificationCode;

    private FirebaseAuth mAuth;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_phone_login );


        mAuth =  FirebaseAuth.getInstance();




        sendVerificationCodeButton = (Button) findViewById( R.id.send_ver_code_button );
        verifyButton = (Button) findViewById( R.id.verify_button );
        inputPhoneNumber = (EditText) findViewById( R.id.phone_number_input );
        inputVerificationCode = (EditText) findViewById( R.id.verification_code_input );
        loadingBar = new ProgressDialog( this );


        sendVerificationCodeButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                String phoneNumber = inputPhoneNumber.getText().toString();
                if (TextUtils.isEmpty( phoneNumber ))
                {
                    Toast.makeText( PhoneLoginActivity.this, "phone number required", Toast.LENGTH_SHORT ).show();

                }
                else
                {

                    loadingBar.setTitle( "Phone Verification" );
                    loadingBar.setMessage( "please wait .... " );
                    loadingBar.setCanceledOnTouchOutside( false );
                    loadingBar.show();

                    PhoneAuthOptions options =
                            PhoneAuthOptions.newBuilder(mAuth)
                                    .setPhoneNumber(phoneNumber)       // Phone number to verify
                                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                    .setActivity(PhoneLoginActivity.this)                 // Activity (for callback binding)
                                    .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                                    .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);

                }
            }
        } );

        verifyButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputPhoneNumber.setVisibility( View.INVISIBLE );
                sendVerificationCodeButton.setVisibility( View.INVISIBLE );

                String verificationCode = inputVerificationCode.getText().toString();
                if (TextUtils.isEmpty( verificationCode ))
                {
                    Toast.makeText( PhoneLoginActivity.this, "Please type code of 6 digits ", Toast.LENGTH_SHORT ).show();

                }
                else
                {
                    loadingBar.setTitle( "Code Verification" );
                    loadingBar.setMessage( "please wait .... " );
                    loadingBar.setCanceledOnTouchOutside( false );
                    loadingBar.show();


                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential( credential );

                }
            }
        } );


        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential)
            {

                signInWithPhoneAuthCredential( phoneAuthCredential );
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e)
            {

                Toast.makeText( PhoneLoginActivity.this, "Invalid Phone Number ...", Toast.LENGTH_SHORT ).show();

                inputPhoneNumber.setVisibility( View.VISIBLE );
                sendVerificationCodeButton.setVisibility( View.VISIBLE );

                inputVerificationCode.setVisibility( View.INVISIBLE );
                verifyButton.setVisibility( View.INVISIBLE );
            }


            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token)
            {

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
                loadingBar.dismiss();
                Toast.makeText( PhoneLoginActivity.this, "Code is sent , check your messages", Toast.LENGTH_SHORT ).show();

                inputPhoneNumber.setVisibility( View.INVISIBLE );
                sendVerificationCodeButton.setVisibility( View.INVISIBLE );

                inputVerificationCode.setVisibility( View.VISIBLE );
                verifyButton.setVisibility( View.VISIBLE );


            }

        };

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            loadingBar.dismiss();
                            Toast.makeText( PhoneLoginActivity.this, "you are logged in ...", Toast.LENGTH_SHORT ).show();
                            sendUserToMainActivity();

                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child( mAuth.getCurrentUser().getUid().toString() );
                            databaseReference.child( "phone number" ).setValue( inputPhoneNumber.getText().toString() );
                            databaseReference.child( "uID" ).setValue( mAuth.getCurrentUser().getUid().toString() );


                        }
                        else

                            {
                                String message = task.getException().toString();
                                Toast.makeText( PhoneLoginActivity.this, "Error"  + message, Toast.LENGTH_SHORT ).show();

                           }
                    }
                });
    }

    private void sendUserToMainActivity()
    {

        Intent intent = new Intent(PhoneLoginActivity.this , MainActivity.class);
        startActivity( intent );
        finish();

    }


}