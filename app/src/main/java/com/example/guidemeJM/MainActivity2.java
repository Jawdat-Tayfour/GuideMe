package com.example.guidemeJM;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.content.Intent;
import android.widget.ImageView;

public class MainActivity2 extends AppCompatActivity {
    private Button nDriver , mCostumer ;
    private ImageView imageView , imageView3 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        nDriver =  findViewById(R.id.button);
        mCostumer =  findViewById(R.id.button2);
        imageView = findViewById(R.id.imageView);
        imageView3 = findViewById(R.id.imageView3);
        Animation animation= AnimationUtils.loadAnimation(getApplicationContext(),R.animator.alpha);
        imageView.startAnimation(animation);
        imageView3.startAnimation(animation);
        nDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity2.this , TourGLoginActivity.class) ;
                startActivity(intent);
//                finish();
//                return;
            }
        });
        mCostumer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity2.this , CustomerLoginActivity.class) ;
                startActivity(intent);
//                finish();
//                return;
            }
        });
    }
}
