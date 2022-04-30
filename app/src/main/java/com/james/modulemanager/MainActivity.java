package com.james.modulemanager;

import android.os.Bundle;

import com.james.library_c.ClassC;
import com.james.libraryb.LibraryB;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LibraryB.testHello();
        ClassC.testC();
    }
}