package com.james.modulemanager;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.james.librarya.LibraryAClass;
import com.james.libraryb.LibraryB;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LibraryAClass.testHello();
        LibraryB.testHello();
    }
}