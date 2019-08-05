package com.mrndokist.app.travelmantics;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {

    ArrayList<TravelDeal> deals;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_return, menu);

        MenuItem insertMenu = menu.findItem(R.id.action_new_deal);

        if(FirebaseUtil.isAdmin==true)
        {
            insertMenu.setVisible(true);
        }
        else
        {
            insertMenu.setVisible(false);
        }
        return true;
    }

    /**
     * Définiton des actions à effectuer lors du clique sur les icones du menu journal
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        /**
         * Mise en place du composant permettant de faire une recherche
         */
        switch(item.getItemId())
        {
            //Retour à l'acceuil
            case R.id.action_new_deal:
                startActivity(new Intent(ListActivity.this,MainActivity.class));
                finish();
                return true;

            case R.id.action_logout:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(ListActivity.this,"Deconnection",Toast.LENGTH_SHORT).show();
                                FirebaseUtil.attachListener();
                            }
                        });
                FirebaseUtil.detachListener();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUtil.detachListener();
    }

    @Override
    protected void onResume() {
        super.onResume();

        FirebaseUtil.openFbReference("traveldeals",this);
        RecyclerView rvDeals = findViewById(R.id.recycler_view);
        final  Adapter adapter = new Adapter(this);

        rvDeals.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        rvDeals.setLayoutManager(linearLayoutManager );
        rvDeals.setHasFixedSize(true);
        FirebaseUtil.attachListener();
    }



    public void showMenu()
    {
        invalidateOptionsMenu();
    }


}
