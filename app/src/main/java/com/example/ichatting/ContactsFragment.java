package com.example.ichatting;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
 * Use the {@link ContactsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContactsFragment extends Fragment {

    private View contactsView;
    private RecyclerView contactRecyclerView;

    private DatabaseReference contactsReference ,
                               usersReference;
    private FirebaseAuth auth;
    private String currentUserId;



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ContactsFragment() {
        // Required empty public constructor

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ContactsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ContactsFragment newInstance(String param1, String param2) {
        ContactsFragment fragment = new ContactsFragment();
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
        contactsView =  inflater.inflate( R.layout.fragment_contacts, container, false );

        contactRecyclerView = (RecyclerView)  contactsView.findViewById( R.id.contacts_recycler_view );
        contactRecyclerView.setLayoutManager( new LinearLayoutManager( getContext() ) );

        auth = FirebaseAuth.getInstance();
        currentUserId =  auth.getCurrentUser().getUid();
        contactsReference = FirebaseDatabase.getInstance().getReference("Contacts").child( currentUserId );
        usersReference = FirebaseDatabase.getInstance().getReference("Users");




        return contactsView;

    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contact>()
                .setQuery(contactsReference , Contact.class  )
                .build();

        FirebaseRecyclerAdapter<Contact , ContactViewHolder > adapter  = new FirebaseRecyclerAdapter<Contact, ContactViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactViewHolder holder, int position, @NonNull Contact model)
            {
                String usersID = getRef( position ).getKey();

                usersReference.child( usersID ).addValueEventListener( new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if (snapshot.exists())
                        {

                            if (snapshot.child( "user_Online_Availability" ).hasChild( "state" ))
                            {
                                String availabilityState = snapshot.child( "user_Online_Availability" ).child( "state" ).getValue().toString();
                                String availabilityTime = snapshot.child( "user_Online_Availability" ).child( "time" ).getValue().toString();
                                String availabilityDate = snapshot.child( "user_Online_Availability" ).child( "date" ).getValue().toString();

                                if (availabilityState.equals( "online" ))
                                {
                                    holder.onlineDot.setVisibility( View.VISIBLE );

                                }
                                else if (availabilityState.equals( "offline" ))
                                {
                                    holder.onlineDot.setVisibility( View.INVISIBLE );

                                }

                            }
                            else
                            {
                                holder.onlineDot.setVisibility( View.INVISIBLE );

                            }



                            if (snapshot.hasChild( "image" ) )
                            {
                                String profileImage = snapshot.child( "image" ).getValue().toString();
                                String profileName     = snapshot.child( "name" ).getValue().toString();
                                String profileStatus   = snapshot.child( "status" ).getValue().toString();

                                holder.userName.setText( profileName );
                                holder.userStatus.setText( profileStatus );

                                Picasso.get().load( profileImage ).placeholder( R.drawable.profile_photo ).into( holder.profileImage );


                            }
                            else
                            {
                                String profileName     = snapshot.child( "name" ).getValue().toString();
                                String profileStatus   = snapshot.child( "status" ).getValue().toString();

                                holder.userName.setText( profileName );
                                holder.userStatus.setText( profileStatus );


                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {


                    }
                } );


            }

            @NonNull
            @Override
            public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
              View view = LayoutInflater.from( parent.getContext() ).inflate( R.layout.recycler_view_custom_row , parent , false );
              ContactViewHolder viewHolder = new ContactViewHolder( view );
              return viewHolder;
            }
        };
        contactRecyclerView.setAdapter( adapter );
        adapter.startListening();

    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName , userStatus;
        CircleImageView profileImage;
        ImageView onlineDot;


        public ContactViewHolder(@NonNull View itemView)
        {
            super( itemView );
            userName = itemView.findViewById( R.id.user_profile_name_text_view );
            userStatus = itemView.findViewById( R.id.user_profile_status_text_view );
            profileImage = itemView.findViewById( R.id.user_profile_circle_image_view );
            onlineDot = itemView.findViewById( R.id.user_online_dot );


        }

    }

}