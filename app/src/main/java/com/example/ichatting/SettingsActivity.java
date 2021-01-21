package com.example.ichatting;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private EditText userNameEditText ,
                     userStatusEditText;
    private Button updateInfoButton;
    private CircleImageView userProfileCircleImageView;
    private androidx.appcompat.widget.Toolbar toolbar ;

    String currentUserID;//-----------------------------------------------
    private FirebaseAuth auth;//-----------------------------------------------
    private FirebaseDatabase database;//-----------------------------------------------
    private DatabaseReference databaseReference;//-----------------------------------------------

    private static final int INTENT_CODE = 1;

  //  private StorageReference userProfileImageReference;  // to store the user image in the firebase storage.

    private StorageReference mStorageRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_settings );

        auth = FirebaseAuth.getInstance(); //-----------------------------------------------
        currentUserID = auth.getCurrentUser().getUid();//-----------------------------------------------
        database = FirebaseDatabase.getInstance();//-----------------------------------------------
        databaseReference = database.getReference("Users");//-----------------------------------------------
      // userProfileImageReference = FirebaseStorage.getInstance().getReference().child( "Profile Images" );

        mStorageRef = FirebaseStorage.getInstance().getReference("Profile Images");

        initializeViews();


        //-------------------------------------------------OE57------------------------------------------------

       // retrieveUserData(); //xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

        //-------------------------------------------------OE57-------------------------------------------------------
        updateInfoButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSittings();
            }
        } );


        userProfileCircleImageView.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setAction( Intent.ACTION_GET_CONTENT );
                intent.setType( "image/*" );
                startActivityForResult( intent , INTENT_CODE  );

            }
        } );



        // ___________________________OE57__________________________________

        retrieveUserData();

        // _________________________OE57_____________________________________


    }

    private void updateSittings()
    {

        String setUserName = userNameEditText.getText().toString();
        String setUserStatus = userStatusEditText.getText().toString();

        if (TextUtils.isEmpty( setUserName ))
        {
            Toast.makeText( this , "اكتب اسمك يا جاهل !!!"  , Toast.LENGTH_SHORT ).show();

        }
        if (TextUtils.isEmpty( setUserStatus ))
        {
            Toast.makeText( this , "اكتب نبذه عن نفسك يا جاااااهل !!!"  , Toast.LENGTH_SHORT ).show();

        }
        else
        {

            DatabaseReference currentUserIdDB = databaseReference.child( currentUserID );
            currentUserIdDB.child( "name" ).setValue( setUserName );
            currentUserIdDB.child( "status" ).setValue( setUserStatus);
            currentUserIdDB.child( "uID" ).setValue( currentUserID);
            
            sendUserToMainActivity();

             /*  //------------------------------------------------------------------------------------------------------
            HashMap<String , String> profileInfo = new HashMap<>();
            profileInfo.put( "uID" , currentUserID );
            profileInfo.put( "name" , setUserName );
            profileInfo.put( "status" , setUserStatus );
            databaseReference.child( currentUserID ).setValue( profileInfo).

            */  //------------------------------------------------------------------------------------------------------
        }
    }

    private void retrieveUserData()  //xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
    {

        databaseReference.child( currentUserID ).addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists() && (snapshot.hasChild( "name" ) && (snapshot.hasChild( "image" ))))
                {

                    String retrieveUserName = snapshot.child( "name" ).getValue().toString();
                    String retrieveUserStatus = snapshot.child( "status" ).getValue().toString();
                    String retrieveProfileImage = snapshot.child( "image" ).getValue().toString();

                    userNameEditText.setText( retrieveUserName );
                    userStatusEditText.setText( retrieveUserStatus );
                    Picasso.get().load(retrieveProfileImage).into(userProfileCircleImageView);

                }
                else if (snapshot.exists() && (snapshot.hasChild( "name" )))
                {

                    String retrieveUserName = snapshot.child( "name" ).getValue().toString();
                    String retrieveUserStatus = snapshot.child( "status" ).getValue().toString();

                    userNameEditText.setText( retrieveUserName );
                    userStatusEditText.setText( retrieveUserStatus );

                }
                /*
                else
                {
                    Toast.makeText( SettingsActivity.this , "Please update info" , Toast.LENGTH_SHORT ).show();
                }

                 */

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {


            }
        } );

    }


    private void initializeViews() {

        toolbar = (androidx.appcompat.widget.Toolbar) findViewById( R.id.settings_toolbar );
        setSupportActionBar( toolbar );
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );
        getSupportActionBar().setDisplayShowHomeEnabled( true );
        getSupportActionBar().setTitle( "Account Settings" );
        userNameEditText = (EditText) findViewById( R.id.user_profile_name_edit_text );
        userStatusEditText = (EditText) findViewById( R.id.user_status_edit_text );
        updateInfoButton = (Button) findViewById( R.id.update_user_info_btn );
        userProfileCircleImageView = (CircleImageView) findViewById( R.id.user_profile_image );



    }

    private void sendUserToMainActivity()
    {
        Intent intent = new Intent( SettingsActivity.this  , MainActivity.class );
        intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK  | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity( intent );
        finish();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult( requestCode, resultCode, data );

        if (requestCode == INTENT_CODE && resultCode == RESULT_OK && data !=null)
        {
            Uri imageUri = data.getData();
            CropImage.activity()
                    .setGuidelines( CropImageView.Guidelines.ON)
                    .setAspectRatio( 1 , 1 )
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            /*
            if (requestCode==RESULT_OK)
            {
                Uri resultUri = result.getUri();

                StorageReference filePath = userProfileImageReference.child( currentUserID + ".jpg" );

                filePath.putFile( resultUri ).addOnCompleteListener( new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {

                        if (task.isSuccessful())
                        {
                            Toast.makeText( SettingsActivity.this, "image uploaded ...", Toast.LENGTH_SHORT ).show();

                        }
                        else
                        {
                            String message = task.getException().toString();
                            Toast.makeText( SettingsActivity.this, "Error" + message, Toast.LENGTH_SHORT ).show();

                        }
                    }
                } );


            }

             */

            Uri resultUri = result.getUri();
            StorageReference reference = mStorageRef.child(currentUserID + ".jpg"  );
            reference.putFile(resultUri  ).addOnSuccessListener( new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener( new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            Toast.makeText( SettingsActivity.this, "image uploaded", Toast.LENGTH_SHORT ).show();

                            DatabaseReference currentUserIdDB = databaseReference.child( currentUserID );
                            currentUserIdDB.child( "image" ).setValue( String.valueOf( task.getResult() ) );

                        }
                    } ).addOnFailureListener( new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            //
                            Toast.makeText( SettingsActivity.this, "failed upload image", Toast.LENGTH_SHORT ).show();
                        }
                    } );
                }
            } );

        }
    }


}
