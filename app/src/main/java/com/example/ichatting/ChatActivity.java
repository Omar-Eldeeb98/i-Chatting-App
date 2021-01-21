package com.example.ichatting;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {    //_______________________ALL CLEAR IN THIS ACTIVITY "OE57"_______________________

    private String messageSenderId,
                   messageReceiverId ,
                   messageReceiverName ,
                   messageReceiverImage;

    private TextView userName ,
                     userLastSeen  ;

    private CircleImageView userImage;

    private Toolbar chatToolbar;

    private ImageButton sendMessageImageButton;
    private EditText messageInputEditText;

    private FirebaseAuth auth;
    private DatabaseReference reference;

    private final List<Message> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessages;

    private ImageButton attachFileImageButton;  //^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    private String checker = "" , myUrl = "";
    private Uri fileUri;
    private StorageTask uploadTask;
    private ProgressDialog loadingBar;

    private String saveCurrentTime  , saveCurrentDate;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_chat );

        reference = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        messageSenderId = auth.getCurrentUser().getUid();

        messageReceiverId = getIntent().getExtras().get( "visit_user_id" ).toString();
        messageReceiverName = getIntent().getExtras().get( "visit_user_name" ).toString();
        messageReceiverImage = getIntent().getExtras().get("visit_image" ).toString();

        initializeViews();

      //  Toast.makeText( this,messageReceiverId , Toast.LENGTH_SHORT ).show();
      //  Toast.makeText( this,messageReceiverName , Toast.LENGTH_SHORT ).show();     // OE57 ^_^ .....................

        userName.setText( messageReceiverName );
        Picasso.get().load( messageReceiverImage ).placeholder( R.drawable.profile_photo ).into( userImage );



        sendMessageImageButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        } );


        displayLastSeen();



        attachFileImageButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]
                        {
                                "Image" ,
                                "PDF File" ,
                                "Document"
                        };
                AlertDialog.Builder builder = new AlertDialog.Builder( ChatActivity.this );
                builder.setTitle( "Selection Options" );
                builder.setItems( options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position)
                    {
                        if (position == 0)
                        {
                            checker = "image";
                            Intent intent = new Intent();
                            intent.setAction( Intent.ACTION_GET_CONTENT );
                            intent.setType( "image/*" );
                            startActivityForResult( intent.createChooser( intent , "Select Image" ) , 101 );

                        }
                        if (position == 1)
                        {
                            checker = "pdf";


                        }
                        if (position == 2)
                        {
                            checker = "document";


                        }

                    }
                } );
                builder.show();


            }
        } );

        reference.child( "Messages" ).child( messageSenderId ).child( messageReceiverId )
                .addChildEventListener( new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                        Message message = snapshot.getValue(Message.class);
                        messagesList.add( message );
                        messageAdapter.notifyDataSetChanged();
                        userMessages.smoothScrollToPosition(userMessages.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                } );


    }



    private void initializeViews() {

        chatToolbar = (Toolbar) findViewById( R.id.chat_toolbar );
        setSupportActionBar( chatToolbar );

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled( false );  //---------------------------------------------oe57


        LayoutInflater inflater  = (LayoutInflater) this.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View actionBarView =  inflater.inflate( R.layout.custom_chat_bar , null );
        actionBar.setCustomView( actionBarView );

        userName = (TextView) findViewById( R.id.custom_profile_name );
        userLastSeen = (TextView) findViewById( R.id.custom_user_last_seen );
        userImage = (CircleImageView) findViewById( R.id.custom_profile_image );

        messageInputEditText = (EditText) findViewById( R.id.input_message );
        sendMessageImageButton = (ImageButton) findViewById( R.id.send_message );

        attachFileImageButton = (ImageButton) findViewById( R.id.attach_image_button ); //^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

        loadingBar = new ProgressDialog( this );


        

        userMessages = (RecyclerView) findViewById( R.id.users_messages_list_recycler_view );
        messageAdapter = new MessageAdapter( messagesList );
        linearLayoutManager = new LinearLayoutManager( this );
        userMessages.setLayoutManager( linearLayoutManager );
        userMessages.setAdapter( messageAdapter );
        userMessages.getRecycledViewPool().setMaxRecycledViews( 0  , 0  );  //++++++++++++++++++++++++++OE57++++++++++++++++


        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate  = new SimpleDateFormat("MMM dd,  yyyy");
        saveCurrentDate = currentDate.format( calendar.getTime() );

        SimpleDateFormat currentTime  = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format( calendar.getTime() );


    }


    private void displayLastSeen()
    {
        reference.child( "Users" ).child( messageReceiverId ).addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child( "user_Online_Availability" ).hasChild( "state" ))
                {
                    String availabilityState = snapshot.child( "user_Online_Availability" ).child( "state" ).getValue().toString();
                    String availabilityTime = snapshot.child( "user_Online_Availability" ).child( "time" ).getValue().toString();
                    String availabilityDate = snapshot.child( "user_Online_Availability" ).child( "date" ).getValue().toString();

                    if (availabilityState.equals( "online" ))
                    {
                       userLastSeen.setText( "online" );

                    }
                    else if (availabilityState.equals( "offline" ))
                    {
                        userLastSeen.setText( "Last Seen: " + availabilityDate + "  At " + availabilityTime);

                    }

                }
                else
                {
                    userLastSeen.setText( "offline" );

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }


      //_____________________________________________STILL WORKING ON IT_____________________________________________________
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        if (requestCode == 101 && resultCode == RESULT_OK && data != null && data.getData()!= null)
        {
            loadingBar.setTitle("Sending File");
            loadingBar.setMessage( "Please Wait..." );
            loadingBar.setCanceledOnTouchOutside( false );
            loadingBar.show();


            fileUri = data.getData();
            if (!checker.equals( "image" ))
            {


            }
            else if (checker.equals( "image" ))
            {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference("Image Files");
                final String messageSenderRef = "Messages/" + messageSenderId + "/" + messageReceiverId;
                final String messageReceiverRef = "Messages/" + messageReceiverId + "/" + messageSenderId;

                DatabaseReference userMessageKeyReference= reference.child( "Messages" ).child( messageSenderId )
                        .child( messageReceiverId ).push();

                final String messagePushId = userMessageKeyReference.getKey();

                final StorageReference filePath = storageReference.child( messagePushId + "_image" );
                uploadTask = filePath.putFile( fileUri );
                uploadTask.continueWithTask( new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful())
                        {
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                } ).addOnCompleteListener( new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful())
                        {
                            Uri downloadUri = task.getResult();
                            myUrl = downloadUri.toString();

                            Map messageTextBody  = new HashMap();
                            messageTextBody.put( "message" , myUrl );
                            messageTextBody.put( "name" , fileUri.getLastPathSegment() );
                            messageTextBody.put( "type" , checker );
                            messageTextBody.put( "from" , messageSenderId );

                             messageTextBody.put( "to" , messageReceiverId );
                             messageTextBody.put( "messageID" , messagePushId );
                             messageTextBody.put( "time" , saveCurrentTime );
                             messageTextBody.put( "date" , saveCurrentDate );


                            Map messageBodyDetails  = new HashMap();
                            messageBodyDetails.put( messageSenderRef + "/" + messagePushId , messageTextBody );
                            messageBodyDetails.put( messageReceiverRef + "/" + messagePushId , messageTextBody );


                            reference.updateChildren( messageBodyDetails ).addOnCompleteListener( new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {

                                    if (task.isSuccessful())
                                    {
                                        loadingBar.dismiss();
                                        Toast.makeText( ChatActivity.this, "message is sent ", Toast.LENGTH_SHORT ).show();

                                    }
                                    else
                                    {
                                        loadingBar.dismiss();
                                        Toast.makeText( ChatActivity.this, " Message Does not sent !!", Toast.LENGTH_SHORT ).show();

                                    }
                                    messageInputEditText.setText( "" );
                                }
                            } );


                        }
                    }
                } );



            }
            else
            {
                loadingBar.dismiss();
                Toast.makeText( this, " ERROR : You Do not Select image To be sent mother fucker !! ", Toast.LENGTH_SHORT ).show();
            }

        }
    }

    //_____________________________________________STILL WORKING ON IT_____________________________________________________

    /*
    @Override
    protected void onStart() {
        super.onStart();
        reference.child( "Messages" ).child( messageSenderId ).child( messageReceiverId )
                .addChildEventListener( new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                        Message message = snapshot.getValue(Message.class);
                        messagesList.add( message );
                        messageAdapter.notifyDataSetChanged();
                        userMessages.smoothScrollToPosition(userMessages.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                } );
    }

     */

    private void sendMessage() {
        String messageText  = messageInputEditText.getText().toString();
        if (TextUtils.isEmpty( messageText ))
        {
            Toast.makeText( this, "Type Your Fucken Message  First  ", Toast.LENGTH_SHORT ).show();
        }
        else
        {
            String messageSenderRef = "Messages/" + messageSenderId + "/" + messageReceiverId;
            String messageReceiverRef = "Messages/" + messageReceiverId + "/" + messageSenderId;

            DatabaseReference userMessageKeyReference= reference.child( "Messages" ).child( messageSenderId )
                    .child( messageReceiverId ).push();

            String messagePushId = userMessageKeyReference.getKey();

            Map messageTextBody  = new HashMap();
            messageTextBody.put( "message" , messageText );
            messageTextBody.put( "type" , "text" );
            messageTextBody.put( "from" , messageSenderId );

            messageTextBody.put( "to" , messageReceiverId );
            messageTextBody.put( "messageID" , messagePushId );
            messageTextBody.put( "time" , saveCurrentTime );
            messageTextBody.put( "date" , saveCurrentDate );

            Map messageBodyDetails  = new HashMap();
            messageBodyDetails.put( messageSenderRef + "/" + messagePushId , messageTextBody );
            messageBodyDetails.put( messageReceiverRef + "/" + messagePushId , messageTextBody );


            reference.updateChildren( messageBodyDetails ).addOnCompleteListener( new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if (task.isSuccessful())
                    {
                        Toast.makeText( ChatActivity.this, "message is sent ", Toast.LENGTH_SHORT ).show();

                    }
                    else
                    {
                        Toast.makeText( ChatActivity.this, " Message Does not sent !!", Toast.LENGTH_SHORT ).show();

                    }
                    messageInputEditText.setText( "" );
                }
            } );

        }
    }

    @Override
    protected void onDestroy() {   //+++++++++++++++++++++OE57+++++++++++++++++++++++++++++++++++++
        super.onDestroy();
        messagesList.clear();
    }

    /*
    @Override
    protected void onPause() {    //+++++++++++++++++++++++OE57+++++++++++++++++++++++++++++++++++
        super.onPause();
        messagesList.clear();
    }

     */
}





