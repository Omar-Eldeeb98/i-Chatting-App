package com.example.ichatting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.AndroidException;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiveUserId , senderUserId ,  current_state;
    private CircleImageView userPhotoCircleImageView;
    private TextView userProfileNameTextView , userProfileStatusTextView;
    private Button sendMessageRequestButton , declineMessageRequestButton;

    private DatabaseReference userReference ,  chatRequestReference , contactsReference , notificationReference;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_profile );

        userReference = FirebaseDatabase.getInstance().getReference("Users");
        chatRequestReference = FirebaseDatabase.getInstance().getReference("Chat Requests");
        contactsReference = FirebaseDatabase.getInstance().getReference("Contacts");
        notificationReference = FirebaseDatabase.getInstance().getReference("Notifications");

        auth = FirebaseAuth.getInstance();
        receiveUserId = getIntent().getExtras().get( "visit_user_id" ).toString();

       // Toast.makeText( this, "User ID = " + receiveUserId, Toast.LENGTH_SHORT ).show();

        userPhotoCircleImageView = (CircleImageView) findViewById( R.id.visit_profile_image_view );
        userProfileNameTextView = (TextView) findViewById( R.id.visit_user_name_text_view );
        userProfileStatusTextView = (TextView) findViewById( R.id.visit_user_status_text_view );
        sendMessageRequestButton = (Button) findViewById( R.id.send_message_request_button );
        declineMessageRequestButton = (Button) findViewById( R.id.decline_message_request_button );
        senderUserId = auth.getCurrentUser().getUid();
        current_state = "new";

        retrieveUserInformation();




    }

    private void retrieveUserInformation()
    {

        userReference.child( receiveUserId ).addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.exists()) && (snapshot.hasChild( "image" )) )
                {
                    String userImage = snapshot.child( "image" ).getValue().toString();
                    String userName = snapshot.child( "name" ).getValue().toString();
                    String userStatus = snapshot.child( "status" ).getValue().toString();

                    Picasso.get().load( userImage ).placeholder( R.drawable.profile_photo ).into( userPhotoCircleImageView );
                    userProfileNameTextView.setText( userName );
                    userProfileStatusTextView.setText( userStatus );

                    manageChatRequests();
                }
                else
                {
                    String userName = snapshot.child( "name" ).getValue().toString();
                    String userStatus = snapshot.child( "status" ).getValue().toString();

                    userProfileNameTextView.setText( userName );
                    userProfileStatusTextView.setText( userStatus );

                    manageChatRequests();


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );

    }

    private void manageChatRequests() {

        chatRequestReference.child( senderUserId ).addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild( receiveUserId ))
                {
                    String request_type = snapshot.child( receiveUserId ).child( "request_type" ).getValue().toString();
                    if (request_type.equals( "sent" ))
                    {
                        current_state = "request_sent";
                        sendMessageRequestButton.setText( "cancel chat request " );

                    }
                    else if(request_type.equals( "received" ))
                    {
                        current_state = "request_received";
                        sendMessageRequestButton.setText( "Accept chat request" );

                        declineMessageRequestButton.setVisibility( View.VISIBLE );
                        declineMessageRequestButton.setEnabled( true );
                        declineMessageRequestButton.setOnClickListener( new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancelChatRequest();
                            }
                        } );

                    }

                }
                else
                {
                    contactsReference.child( senderUserId )
                            .addListenerForSingleValueEvent( new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.hasChild( receiveUserId ))
                                    {
                                        current_state = "friends";
                                        sendMessageRequestButton.setText( "remove contact" );

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            } );


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );

        if (!senderUserId.equals( receiveUserId ))
        {

            sendMessageRequestButton.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessageRequestButton.setEnabled( false );
                    if (current_state.equals( "new" ))
                    {
                        sendChatRequest();
                    }
                    if(current_state.equals( "request_sent" ))
                    {
                        cancelChatRequest();
                    }
                    if(current_state.equals( "request_received" ))
                    {
                        acceptChatRequest();
                    }
                    if(current_state.equals( "friends" ))
                    {
                        removeSpecificContact();
                    }


                }
            } );

        }
        else
        {
            sendMessageRequestButton.setVisibility( View.INVISIBLE );

        }



    }


    private void removeSpecificContact() {

        contactsReference.child( senderUserId ).child( receiveUserId )
                .removeValue()
                .addOnCompleteListener( new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful())
                        {
                            contactsReference.child( receiveUserId ).child( senderUserId )
                                    .removeValue()
                                    .addOnCompleteListener( new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful())
                                            {
                                                sendMessageRequestButton.setEnabled( true );
                                                current_state = "new";
                                                sendMessageRequestButton.setText( "send message request" );

                                                declineMessageRequestButton.setVisibility( View.INVISIBLE );
                                                declineMessageRequestButton.setEnabled( false );

                                            }

                                        }
                                    } );
                        }
                    }
                } );


    }

    private void acceptChatRequest() {

        contactsReference.child( senderUserId ).child( receiveUserId )
                .child( "Contacts" ).setValue( "Saved" )
                .addOnCompleteListener( new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            contactsReference.child( receiveUserId ).child( senderUserId )
                                    .child( "Contacts" ).setValue( "Saved" )
                                    .addOnCompleteListener( new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                            {

                                                chatRequestReference.child( senderUserId ).child( receiveUserId )
                                                        .removeValue()
                                                        .addOnCompleteListener( new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if (task.isSuccessful())
                                                                {
                                                                    chatRequestReference.child( receiveUserId ).child( senderUserId )
                                                                            .removeValue()
                                                                            .addOnCompleteListener( new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {


                                                                                    sendMessageRequestButton.setEnabled( true );
                                                                                    current_state = "friends";
                                                                                    sendMessageRequestButton.setText( "remove contact" );



                                                                                    declineMessageRequestButton.setVisibility( View.INVISIBLE );
                                                                                    declineMessageRequestButton.setEnabled( false );




                                                                                }
                                                                            } );

                                                                }




                                                            }
                                                        } );

                                            }
                                        }
                                    } );

                        }
                    }
                } );



    }


    private void cancelChatRequest()
    {
        chatRequestReference.child( senderUserId ).child( receiveUserId )
                .removeValue()
                .addOnCompleteListener( new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful())
                        {
                            chatRequestReference.child( receiveUserId ).child( senderUserId )
                                    .removeValue()
                                    .addOnCompleteListener( new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful())
                                            {
                                                sendMessageRequestButton.setEnabled( true );
                                                current_state = "new";
                                                sendMessageRequestButton.setText( "send message request" );

                                                declineMessageRequestButton.setVisibility( View.INVISIBLE );
                                                declineMessageRequestButton.setEnabled( false );

                                            }

                                        }
                                    } );
                        }
                    }
                } );

    }

    private void sendChatRequest() {

        chatRequestReference.child( senderUserId ).child( receiveUserId )
                .child( "request_type" ).setValue( "sent" )
                .addOnCompleteListener( new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            chatRequestReference.child( receiveUserId ).child( senderUserId )
                                    .child( "request_type" ).setValue( "received" )
                                    .addOnCompleteListener( new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                            {
                                                HashMap<String , String> chatNotification  = new HashMap<>();
                                                chatNotification.put( "from" , senderUserId );
                                                chatNotification.put( "type" , "request" );

                                                notificationReference.child( receiveUserId ).push()
                                                        .setValue( chatNotification )
                                                        .addOnCompleteListener( new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful())
                                                        {
                                                            sendMessageRequestButton.setEnabled( true );
                                                            current_state = "request_sent";
                                                            sendMessageRequestButton.setText( "cancel chat request" );


                                                        }

                                                    }
                                                } );





                                            }
                                        }
                                    } );

                        }
                    }
                } );

    }

}