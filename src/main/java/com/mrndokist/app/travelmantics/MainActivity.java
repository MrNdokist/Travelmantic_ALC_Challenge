package com.mrndokist.app.travelmantics;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    EditText tv_title, tv_description, tv_price;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private static final int PICTURE_RESULT = 42;

    String title, description, price;
    TravelDeal deal;
    ImageView image;
    Button btn_upload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFirebaseDatabase = FirebaseUtil.firebaseDatabase;
        mDatabaseReference = FirebaseUtil.databaseReference;

        tv_title = findViewById(R.id.tv_title);
        tv_description = findViewById(R.id.tv_description);
        tv_price = findViewById(R.id.tv_price);
        image = findViewById(R.id.imageView);
        btn_upload = findViewById(R.id.btn_img);

        //Reception des datas

        Intent intent = getIntent();
        TravelDeal deal = (TravelDeal) intent.getSerializableExtra("Deal");

        if (deal == null) {
            deal = new TravelDeal();
            Log.d("MainActivity", "Deal est vide");
        }
        this.deal = deal;
        tv_title.setText(deal.getTitle());
        tv_description.setText(deal.getDescription());
        tv_price.setText(deal.getPrice());
        showImage(deal.getImageUrl());
        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    startActivityForResult(intent.createChooser(intent,"Insert Picture"),PICTURE_RESULT);
                }
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem deleteMenu = menu.findItem(R.id.action_delete);
        MenuItem saveMenu = menu.findItem(R.id.action_save);

        if(FirebaseUtil.isAdmin==true)
        {
            deleteMenu.setVisible(true);
            saveMenu.setVisible(true);
            enableEditext(true);
            btn_upload.setEnabled(true);
        }
        else
        {
            deleteMenu.setVisible(false);
            saveMenu.setVisible(false);
            enableEditext(false);
            btn_upload.setEnabled(false);
        }
        return true;
    }

    /**
     * Définiton des actions à effectuer lors du clique sur les icones du menu journal
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        /**
         * Mise en place du composant permettant de faire une recherche
         */
        switch (item.getItemId()) {
            //Retour à l'acceuil
            case R.id.action_save:

                saveDeal();
                Toast.makeText(this, "Deal saved", Toast.LENGTH_LONG).show();

                clearForm();

                return true;

            case R.id.action_delete:
                deleteDeal();
                Toast.makeText(this, "Deal Deleted", Toast.LENGTH_LONG).show();
                backToList();
                return true;
            //Retour à l'acceuil
            case android.R.id.home:
                startActivity(new Intent(MainActivity.this, ListActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void clearForm() {

        tv_title.setText("");
        tv_description.setText("");
        tv_price.setText("");
        tv_title.requestFocus();

    }

    private void saveDeal() {

        deal.setTitle(tv_title.getText().toString());
        deal.setDescription(tv_description.getText().toString());
        deal.setPrice(tv_price.getText().toString());

        if (deal.getId() == null) {
            mDatabaseReference.push().setValue(deal);
        } else {
            mDatabaseReference.child(deal.getId()).setValue(deal);
        }

    }

    private void deleteDeal() {

        if (deal == null) {
            Toast.makeText(this, "Please save the deal before  deleting", Toast.LENGTH_SHORT).show();
            return;
        }

        mDatabaseReference.child(deal.getId()).removeValue();
        if(deal.getImageName() !=null && deal.getImageName().isEmpty() == false)
        {

            StorageReference picRef  = FirebaseUtil.mStorage.getReference().child(deal.getImageName());
            picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("Delete Image","Image Successfully Deleted");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Log.d("Delete Image",e.getMessage());
                }
            });
        }

    }

    private void backToList() {
        startActivity(new Intent(MainActivity.this, ListActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backToList();
    }


    private void  enableEditext(Boolean isEnable)
    {
        tv_title.setEnabled(isEnable);
        tv_description.setEnabled(isEnable);
        tv_price.setEnabled(isEnable);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICTURE_RESULT && resultCode == RESULT_OK)
        {
            Uri imageUrl = data.getData();
            StorageReference ref = FirebaseUtil.mStorageReference.child(imageUrl.getLastPathSegment());
            ref.putFile(imageUrl).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String url = taskSnapshot.getDownloadUrl().toString();
                    String pictureName = taskSnapshot.getStorage().getPath();
                    deal.setImageUrl(url);
                    deal.setImageName(pictureName);

                    Log.d("Url",url);
                    Log.d("Name",pictureName);
                    showImage(url);
                }
            });
        }
    }

    private void showImage(String url)
    {
        if (url !=null && url.isEmpty() == false)
        {
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
            Picasso.with(this).load(url).resize(width,width*2/3).centerCrop().into(image);
        }
    }
}
