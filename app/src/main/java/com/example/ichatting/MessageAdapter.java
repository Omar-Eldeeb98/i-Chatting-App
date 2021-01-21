package com.example.ichatting;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
{
    private List<Message> userMessagesList;
    private FirebaseAuth  auth;
    private DatabaseReference usersReference;


    public MessageAdapter(List<Message> userMessagesList) {
        this.userMessagesList = userMessagesList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from( parent.getContext() ).inflate( R.layout.custom_messages_layout , parent ,  false );
       auth = FirebaseAuth.getInstance();
       return new MessageViewHolder( view );
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {

        String messageSenderId = auth.getCurrentUser().getUid();
        Message message = userMessagesList.get( position );

        String fromUserId  = message.getFrom();
        String fromMessageType  = message.getType();

        usersReference = FirebaseDatabase.getInstance().getReference("Users").child( fromUserId );
        usersReference.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild( "image" ))
                {
                    String receiverImage  = snapshot.child( "image" ).getValue().toString();
                    Picasso.get().load( receiverImage ).placeholder( R.drawable.profile_photo ).into( holder.receiverProfileImage );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );

        holder.receiverMessageText.setVisibility( View.GONE );
        holder.receiverProfileImage.setVisibility( View.GONE );
        holder.senderMessageText.setVisibility( View.GONE );
        holder.messageSenderPicture.setVisibility( View.GONE );
        holder.messageReceiverPicture.setVisibility( View.GONE );



        if (fromMessageType.equals( "text" ))
        {



            if (fromUserId.equals( messageSenderId ))
            {
                holder.senderMessageText.setVisibility( View.VISIBLE );
                holder.senderMessageText.setBackgroundResource( R.drawable.message_sender_layout );
                holder.senderMessageText.setTextColor( Color.WHITE );
                holder.senderMessageText.setText( message.getMessage() + "\n \n" + message.getTime() +  " - " +  message.getDate() );




            }
            else
            {


                holder.receiverProfileImage.setVisibility( View.VISIBLE );
                holder.receiverMessageText.setVisibility( View.VISIBLE );

                holder.receiverMessageText.setBackgroundResource( R.drawable.message_receiver_layout );
                holder.receiverMessageText.setTextColor( Color.WHITE );
                holder.receiverMessageText.setText( message.getMessage() + "\n \n" + message.getTime() +  " - " +  message.getDate() );

            }
        }
        else if (fromMessageType.equals( "image" ))
        {
            if (fromUserId.equals( messageSenderId ))
            {
                holder.messageSenderPicture.setVisibility( View.VISIBLE );
                Picasso.get().load( message.getMessage()).into( holder.messageSenderPicture );

            }
            else
            {
                holder.receiverProfileImage.setVisibility( View.VISIBLE );
                holder.messageReceiverPicture.setVisibility( View.VISIBLE );
                Picasso.get().load( message.getMessage()).into( holder.messageReceiverPicture );

            }
        }
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView senderMessageText , receiverMessageText;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderPicture ,
                         messageReceiverPicture;


        public MessageViewHolder(@NonNull View itemView) {
            super( itemView );

            senderMessageText = (TextView) itemView.findViewById( R.id.message_sender_text );
            receiverMessageText = (TextView) itemView.findViewById( R.id.message_receiver_text );
            receiverProfileImage = (CircleImageView) itemView.findViewById( R.id.message_profile_image );
            messageSenderPicture = (ImageView) itemView.findViewById( R.id.message_sender_image_view );
            messageReceiverPicture = (ImageView) itemView.findViewById( R.id.message_receiver_image_view );


        }
    }
}
