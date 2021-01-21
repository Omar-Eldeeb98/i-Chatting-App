package com.example.ichatting;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
 * Use the {@link ChatsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatsFragment extends Fragment {

    private View chatView;
    private RecyclerView chatRecyclerView;
    private DatabaseReference chatReference , usersReference;
    private FirebaseAuth auth;
    String currentUserId;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ChatsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatsFragment newInstance(String param1, String param2) {
        ChatsFragment fragment = new ChatsFragment();
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
        chatView =  inflater.inflate( R.layout.fragment_chats, container, false );
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        chatReference = FirebaseDatabase.getInstance().getReference("Contacts").child( currentUserId );
        usersReference = FirebaseDatabase.getInstance().getReference("Users");
        chatRecyclerView = (RecyclerView) chatView.findViewById( R.id.chat_recycler_view );
        chatRecyclerView.setLayoutManager( new LinearLayoutManager(getContext()));


        return chatView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contact> options = new FirebaseRecyclerOptions.Builder<Contact>()
                .setQuery(chatReference , Contact.class  )
                .build();

        FirebaseRecyclerAdapter<Contact , ChatViewHolder > adapter  = new FirebaseRecyclerAdapter<Contact, ChatViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatViewHolder holder, int position, @NonNull Contact model) {

                final String usersId = getRef( position ).getKey();
                final String[] retImage = {"default_image"};
                usersReference.child( usersId ).addValueEventListener( new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {

                        if(snapshot.exists())
                        {
                            if (snapshot.hasChild( "image" ))
                            {
                               retImage[0] = snapshot.child( "image" ).getValue().toString();
                                Picasso.get().load( retImage[0] ).placeholder( R.drawable.profile_photo ).into( holder.profileImage );
                            }

                            final String retName = snapshot.child( "name" ).getValue().toString();
                            final String retStatus = snapshot.child( "status" ).getValue().toString();

                            holder.userName.setText( retName );


                            if (snapshot.child( "user_Online_Availability" ).hasChild( "state" ))
                            {
                                String availabilityState = snapshot.child( "user_Online_Availability" ).child( "state" ).getValue().toString();
                                String availabilityTime = snapshot.child( "user_Online_Availability" ).child( "time" ).getValue().toString();
                                String availabilityDate = snapshot.child( "user_Online_Availability" ).child( "date" ).getValue().toString();

                                if (availabilityState.equals( "online" ))
                                {
                                    holder.userStatus.setText( "online" );

                                }
                               else if (availabilityState.equals( "offline" ))
                                {
                                    holder.userStatus.setText( "Last Seen: " + availabilityDate + " At:  " + availabilityTime);

                                }

                            }
                            else
                            {
                                holder.userStatus.setText( "offline" );

                            }


                            // holder.userStatus.setText( retStatus );

                            holder.itemView.setOnClickListener( new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(getContext() , ChatActivity.class);
                                    intent.putExtra( "visit_user_id" , usersId );
                                    intent.putExtra( "visit_user_name" , retName );
                                    intent.putExtra( "visit_image" , retImage[0] );
                                    startActivity( intent );
                                }
                            } );

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                } );


            }

            @NonNull
            @Override
            public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
               View view = LayoutInflater.from( parent.getContext() ).inflate( R.layout.recycler_view_custom_row , parent , false );
               return new ChatViewHolder( view );


            }
        };

        chatRecyclerView.setAdapter( adapter );
        adapter.startListening();

    }
    public static class ChatViewHolder extends RecyclerView.ViewHolder
    {
        CircleImageView profileImage;
        TextView userName ,
                 userStatus;
        public ChatViewHolder(@NonNull View itemView)
        {
            super( itemView );
            profileImage = itemView.findViewById( R.id.user_profile_circle_image_view );
            userName = itemView.findViewById( R.id.user_profile_name_text_view );
            userStatus = itemView.findViewById( R.id.user_profile_status_text_view );
        }
    }
}