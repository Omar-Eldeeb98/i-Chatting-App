package com.example.ichatting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

  private androidx.appcompat.widget.Toolbar toolbar ;
  private ViewPager viewPager;
  private TabLayout tabLayout ;
  private TabsAccessAdapter tabsAccessAdapter;

   // ------------------------------------------------------

   // private FirebaseUser currentUser;
    private FirebaseAuth auth;

    //---------------------------------------------------------


    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private DatabaseReference mdatabaseReference;
    private String currentUserId;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );


        auth = FirebaseAuth.getInstance();   //------------------------------------------
       // currentUser = auth.getCurrentUser(); //------------------------------------------


        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("Users");
        mdatabaseReference = database.getReference("Groups");



        toolbar = (Toolbar) findViewById( R.id.main_page_toolbar );
        setSupportActionBar( toolbar );
        toolbar.setLogo( R.drawable.i0);
        getSupportActionBar().setTitle("    i   C H A T T I N G ");


        //----------------------------------------------------------------------------------------------------------------

        viewPager = (ViewPager) findViewById( R.id.tabs_pager );
        tabsAccessAdapter = new TabsAccessAdapter( getSupportFragmentManager());
        viewPager.setAdapter( tabsAccessAdapter );

        //----------------------------------------------------------------------------------------------------------

        tabLayout = (TabLayout) findViewById( R.id.tabs_layout );
        tabLayout.setupWithViewPager( viewPager );

        //--------------------------------------------------------------------------------------------------------

    }

    @Override
    protected void onStart()
    {
        FirebaseUser currentUser = auth.getCurrentUser();
        super.onStart();
        if (currentUser == null)
        {
            sendUserToLoginActivity();

        }
        else
        {
            userAvailability("online");
            verifyUserAvailability();
        }

    }

/*
    @Override
    protected void onStop() {
        super.onStop();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser !=null)
        {
            userAvailability("offline");
        }
    }

 */



    @Override
    protected void onDestroy() {
        super.onDestroy();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser !=null)
        {
            userAvailability("offline");
        }

    }

    private void verifyUserAvailability()
    {

        String currentUserID = auth.getCurrentUser().getUid();
        databaseReference.child( currentUserID ).addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.child( "name" ).exists()))
                {
                  //  Toast.makeText( MainActivity.this, "You Are In ", Toast.LENGTH_SHORT ).show();

                }
                else
                {
                    sendUserToSettingsActivityFirstTime();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );




    }


    private void sendUserToLoginActivity()
    {
        Intent loginIntent = new Intent( MainActivity.this , LoginActivity.class );
        loginIntent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK  | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity( loginIntent );
        finish();

    }

    private void sendUserToSettingsActivityFirstTime()
    {
        Intent loginIntent = new Intent( MainActivity.this , SettingsActivity.class );
      //  loginIntent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK  | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity( loginIntent );
      //  finish();

    }
    private void sendUserToSettingsActivityOptional()
    {
        Intent loginIntent = new Intent( MainActivity.this , SettingsActivity.class );
        startActivity( loginIntent );


    }

    private void userAvailability(String state)   //________________________user online or not !_____________________
    {
        String saveCurrentTime  , saveCurrentDate;

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate  = new SimpleDateFormat("MMM dd,  yyyy");
        saveCurrentDate = currentDate.format( calendar.getTime() );

        SimpleDateFormat currentTime  = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format( calendar.getTime() );


        HashMap<String , Object> onlineState = new HashMap<>();
        onlineState.put( "time" , saveCurrentTime );
        onlineState.put( "date" , saveCurrentDate );
        onlineState.put( "state"  , state);

        currentUserId = auth.getCurrentUser().getUid();

        databaseReference.child( currentUserId ).child( "user_Online_Availability" )
                .updateChildren( onlineState );

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)    //  --------------------- the main option menu
    {
        super.onCreateOptionsMenu( menu );
        getMenuInflater().inflate( R.menu.options_menu , menu );
        return true;

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
         super.onOptionsItemSelected( item );
         if (item.getItemId() == R.id.find_friends_option)
         {

             sendUserToFinedFriendActivity();
         }

        if (item.getItemId() == R.id.settings_option)
        {


            sendUserToSettingsActivityOptional();


        }
        if (item.getItemId() == R.id.logout_option)   // ------------ sign out from the app
        {

            userAvailability("offline");
            auth.signOut();
            sendUserToLoginActivity();


        }
        if (item.getItemId() == R.id.about_us)   // ------------ sign out from the app
        {
            Intent intent = new Intent(MainActivity.this , AboutUsActivity.class);
            startActivity( intent );

        }
        return true;
    }



    private void sendUserToFinedFriendActivity()
    {
        Intent intent = new Intent(MainActivity.this , FindFriendsActivity.class);
        startActivity( intent );
    }




}