package com.example.ichatting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.TextView;
import android.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {


    private androidx.appcompat.widget.Toolbar toolbar;
    private RecyclerView findFriendsRecyclerView;

    private DatabaseReference usersReference;

    private EditText searchEditText;  //++++++++++++++++++++++++++++++++++++++

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_find_friends );

        usersReference = FirebaseDatabase.getInstance().getReference().child( "Users" );

        findFriendsRecyclerView = (RecyclerView) findViewById( R.id.find_friends_recycler_view );
        findFriendsRecyclerView.setLayoutManager( new LinearLayoutManager( this ) );
        searchEditText = (EditText) findViewById( R.id.search_friends_edit_text );  //++++++++++++++++++++++++++++++


       toolbar = (androidx.appcompat.widget.Toolbar) findViewById( R.id.find_friends_tool_bar );
        setSupportActionBar( toolbar );
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle( "Fiend Friends " );



    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contact>  options =
                new FirebaseRecyclerOptions.Builder<Contact>()
                .setQuery(usersReference , Contact.class  )
                .build();


        FirebaseRecyclerAdapter<Contact ,FindFriendViewHolder > adapter =
                new FirebaseRecyclerAdapter<Contact, FindFriendViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, final int position, @NonNull Contact model) {

                        holder.userName.setText( model.getName() );
                        holder.userStatus.setText( model.getStatus() );
                        Picasso.get().load( model.getImage() ).placeholder( R.drawable.profile_photo ).into( holder.profileImage );

                        holder.itemView.setOnClickListener( new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                String visit_user_id = getRef( position ).getKey();
                                Intent intent = new Intent(FindFriendsActivity.this  , ProfileActivity.class);
                                 intent.putExtra( "visit_user_id" , visit_user_id  );
                                startActivity( intent );

                            }
                        } );
                        
                    }

                    @NonNull
                    @Override
                    public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                       View view = LayoutInflater.from( parent.getContext() )
                               .inflate( R.layout.recycler_view_custom_row , parent , false);

                       FindFriendViewHolder viewHolder = new FindFriendViewHolder( view );
                       return viewHolder;
                    }
                };

        findFriendsRecyclerView.setAdapter( adapter );
        adapter.startListening();
    }


    public static class FindFriendViewHolder extends RecyclerView.ViewHolder
    {

        TextView userName , userStatus;
        CircleImageView profileImage;


        public FindFriendViewHolder(@NonNull View itemView) {
            super( itemView );



            userName = itemView.findViewById( R.id.user_profile_name_text_view );
            userStatus = itemView.findViewById( R.id.user_profile_status_text_view );
            profileImage = itemView.findViewById( R.id.user_profile_circle_image_view );

        }

    }

}



