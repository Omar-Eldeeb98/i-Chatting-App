package com.example.ichatting;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RequestFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RequestFragment extends Fragment {

    private View requestFragmentView;
    private RecyclerView requestRecyclerView;

    private DatabaseReference chatRequestReference , userReference , contactsReference;
    private FirebaseAuth auth;
    private String currentUserId;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public RequestFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RequestFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RequestFragment newInstance(String param1, String param2) {
        RequestFragment fragment = new RequestFragment();
        Bundle args = new Bundle();
        args.putString( ARG_PARAM1, param1 );
        args.putString( ARG_PARAM2, param2 );
        fragment.setArguments( args );
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        if (getArguments() != null) {
            mParam1 = getArguments().getString( ARG_PARAM1 );
            mParam2 = getArguments().getString( ARG_PARAM2 );
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        requestFragmentView =  inflater.inflate( R.layout.fragment_request, container, false );

        contactsReference = FirebaseDatabase.getInstance().getReference("Contacts");
        chatRequestReference = FirebaseDatabase.getInstance().getReference("Chat Requests");
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        requestRecyclerView  =  (RecyclerView) requestFragmentView.findViewById( R.id.chat_request_recycler_view );
        requestRecyclerView.setLayoutManager( new LinearLayoutManager( getContext() ) );

        return requestFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();


        FirebaseRecyclerOptions<Contact> options =
                new FirebaseRecyclerOptions.Builder<Contact>()
                        .setQuery(chatRequestReference.child( currentUserId ) , Contact.class)
                        .build();

        FirebaseRecyclerAdapter<Contact , RequestViewHolder > adapter  = new FirebaseRecyclerAdapter<Contact, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder holder, int position, @NonNull Contact model) {

                holder.itemView.findViewById( R.id.accept_button ).setVisibility( View.VISIBLE );
                holder.itemView.findViewById( R.id.cancel_button ).setVisibility( View.VISIBLE );

                final String usersID = getRef( position ).getKey();

                DatabaseReference getTypeReference = getRef( position ).child( "request_type" ).getRef();
                getTypeReference.addValueEventListener( new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists())
                        {
                            String type = snapshot.getValue().toString();
                            if (type.equals( "received" ))
                            {

                                userReference.child( usersID ).addValueEventListener( new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.hasChild( "image" ))
                                        {

                                            final String requestProfileImage = snapshot.child( "image" ).getValue().toString();
                                            Picasso.get().load( requestProfileImage ).placeholder( R.drawable.profile_photo )
                                                    .into( holder.profileImage );

                                        }


                                            final String requestUserName = snapshot.child( "name" ).getValue().toString();
                                            final String requestUserStatus = snapshot.child( "status" ).getValue().toString();

                                            holder.userName.setText( requestUserName );
                                            holder.userStatus.setText( requestUserStatus );



                                        holder.itemView.setOnClickListener( new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                CharSequence options [] =  new CharSequence[]
                                                        {
                                                                "Accept" ,
                                                                "Cancel"
                                                        } ;
                                                AlertDialog.Builder  builder = new AlertDialog.Builder( getContext());
                                                builder.setTitle(  requestUserName + " Chat Request " );
                                                builder.setItems( options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int i) {
                                                        if (i==0)
                                                        {
                                                            contactsReference.child( currentUserId ).child( usersID ).child( "Contacts" )
                                                                    .setValue( "saved" ).addOnCompleteListener( new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful())
                                                                    {
                                                                        contactsReference.child( usersID ).child( currentUserId ).child( "Contacts" )
                                                                                .setValue( "saved" ).addOnCompleteListener( new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful())
                                                                                {
                                                                                    chatRequestReference.child( currentUserId ).child( usersID )
                                                                                            .removeValue()
                                                                                            .addOnCompleteListener( new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                                    if (task.isSuccessful())
                                                                                                    {
                                                                                                        chatRequestReference.child( usersID ).child( currentUserId )
                                                                                                                .removeValue()
                                                                                                                .addOnCompleteListener( new OnCompleteListener<Void>() {
                                                                                                                    @Override
                                                                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                                                                        if (task.isSuccessful())
                                                                                                                        {
                                                                                                                            Toast.makeText( getContext(), "New Contact added", Toast.LENGTH_SHORT ).show();
                                                                                                                            

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
                                                            } );

                                                        }
                                                        if (i==1)
                                                        {
                                                            chatRequestReference.child( currentUserId ).child( usersID )
                                                                    .removeValue()
                                                                    .addOnCompleteListener( new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                            if (task.isSuccessful())
                                                                            {
                                                                                chatRequestReference.child( usersID ).child( currentUserId )
                                                                                        .removeValue()
                                                                                        .addOnCompleteListener( new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                                if (task.isSuccessful())
                                                                                                {
                                                                                                    Toast.makeText( getContext(), "Request Deleted", Toast.LENGTH_SHORT ).show();


                                                                                                }
                                                                                            }
                                                                                        } );


                                                                            }
                                                                        }
                                                                    } );


                                                        }
                                                    }
                                                } );

                                                builder.show();


                                            }
                                        } );






                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                } );

                            }
                            else if (type.equals( "sent" ))
                            {
                                Button request_sent_btn = holder.itemView.findViewById( R.id.accept_button );
                                request_sent_btn.setText( "Requested" );



                                holder.itemView.findViewById( R.id.cancel_button ).setVisibility( View.INVISIBLE );

                                userReference.child( usersID ).addValueEventListener( new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.hasChild( "image" ))
                                        {

                                            final String requestProfileImage = snapshot.child( "image" ).getValue().toString();
                                            Picasso.get().load( requestProfileImage ).placeholder( R.drawable.profile_photo )
                                                    .into( holder.profileImage );

                                        }


                                        final String requestUserName = snapshot.child( "name" ).getValue().toString();
                                        final String requestUserStatus = snapshot.child( "status" ).getValue().toString();

                                        holder.userName.setText( requestUserName );
                                        holder.userStatus.setText( " you sent chat request to " + requestUserName );




                                        holder.itemView.setOnClickListener( new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                CharSequence options [] =  new CharSequence[]
                                                        {
                                                                "Cancel Chat Request"
                                                        } ;
                                                AlertDialog.Builder  builder = new AlertDialog.Builder( getContext());
                                                builder.setTitle( "Already Request sent");
                                                builder.setItems( options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int i) {


                                                        if (i==0)
                                                        {
                                                            chatRequestReference.child( currentUserId ).child( usersID )
                                                                    .removeValue()
                                                                    .addOnCompleteListener( new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                            if (task.isSuccessful())
                                                                            {
                                                                                chatRequestReference.child( usersID ).child( currentUserId )
                                                                                        .removeValue()
                                                                                        .addOnCompleteListener( new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                                if (task.isSuccessful())
                                                                                                {
                                                                                                    Toast.makeText( getContext(), "Request Canceled", Toast.LENGTH_SHORT ).show();


                                                                                                }
                                                                                            }
                                                                                        } );


                                                                            }
                                                                        }
                                                                    } );


                                                        }
                                                    }
                                                } );

                                                builder.show();


                                            }
                                        } );






                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                } );
                            }



                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                } );

            }

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate( R.layout.recycler_view_custom_row , parent ,  false );
                RequestViewHolder holder = new RequestViewHolder( view );
                return holder;
            }
        };


        requestRecyclerView.setAdapter( adapter );
        adapter.startListening();

     }
    public static class RequestViewHolder extends RecyclerView.ViewHolder
    {
        CircleImageView profileImage;
        TextView userName
                , userStatus;
        Button acceptButton
                , cancelButton;


        public RequestViewHolder(@NonNull View itemView) {
            super( itemView );

            profileImage = itemView.findViewById( R.id.user_profile_circle_image_view );
            userName = itemView.findViewById( R.id.user_profile_name_text_view );
            userStatus = itemView.findViewById( R.id.user_profile_status_text_view );
            acceptButton = itemView.findViewById( R.id.accept_button );
            cancelButton = itemView.findViewById( R.id.cancel_button );


        }
    }

}