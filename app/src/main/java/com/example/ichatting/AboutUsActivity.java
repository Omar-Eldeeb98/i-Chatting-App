package com.example.ichatting;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class AboutUsActivity extends AppCompatActivity {

    private androidx.appcompat.widget.Toolbar toolbar ;
    private ImageView github  , stackOverFlow , twitter , facebook;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_about_us );

        toolbar = (androidx.appcompat.widget.Toolbar) findViewById( R.id.about_us_toolbar );
        setSupportActionBar( toolbar );
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );
        getSupportActionBar().setDisplayShowHomeEnabled( true );
        getSupportActionBar().setDisplayShowTitleEnabled( false );

        github = (ImageView) findViewById( R.id.github_image_view );
        stackOverFlow = (ImageView) findViewById( R.id.stackOverFlow_image_view );
        twitter = (ImageView) findViewById( R.id.twitter_image_view );
        facebook = (ImageView) findViewById( R.id.facebook_image_view );


        github.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData( Uri.parse("https://github.com/Omar-Eldeeb98"));
                startActivity(intent);

            }
        } );

        stackOverFlow.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData( Uri.parse("https://stackoverflow.com/users/13633632/omar-eldeeb?tab=profile"));
                startActivity(intent);

            }
        } );

        twitter.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData( Uri.parse("https://twitter.com/Omar__Eldeeb__"));
                startActivity(intent);

            }
        } );


        facebook.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData( Uri.parse("https://www.facebook.com/profile.php?id=100005167204388"));
                startActivity(intent);

            }
        } );


    }
}