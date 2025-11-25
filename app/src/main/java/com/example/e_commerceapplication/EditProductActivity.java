package com.example.e_commerceapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class EditProductActivity extends AppCompatActivity {

    EditText editTextName,editTextDesc,editTextPrice;
    ImageView imageView;
    Button btnChooseImg,btnSaveProd;
    DatabaseReference databaseProduct;
    StorageReference storageReference;
    String productId,oldImageURL;

    Uri selectedImageUri;
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_product);

        editTextName = findViewById(R.id.etNameEp);
        editTextDesc = findViewById(R.id.etDescEp);
        editTextPrice = findViewById(R.id.etPriceEp);

        imageView = findViewById(R.id.imageViewProductEdit);
        btnChooseImg = findViewById(R.id.btnchooseimage);
        btnSaveProd = findViewById(R.id.savechange);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(v -> finish());



        databaseProduct = FirebaseDatabase.getInstance().getReference("products");
        storageReference = FirebaseStorage.getInstance().getReference("product_images");

        Intent intent = getIntent();
        productId = intent.getStringExtra("productID");
        editTextName.setText(intent.getStringExtra("productName"));
        editTextDesc.setText(intent.getStringExtra("productDesc"));
        double price = intent.getDoubleExtra("productPrice",0.0);
        editTextPrice.setText(String.valueOf(price));
        oldImageURL = intent.getStringExtra("productImageURL");
        selectedImageUri = Uri.parse(oldImageURL);

        new Thread(() -> {
            Bitmap bitmap = null;
            try {
                InputStream input = new java.net.URL(oldImageURL).openStream();
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Bitmap finalBitmap = bitmap;
            runOnUiThread(() -> imageView.setImageBitmap(finalBitmap));
        }).start();

        if (oldImageURL !=null && !oldImageURL.isEmpty()){
            //create class to
            new ImageLoadingTask(oldImageURL,imageView).execute();
        }

        btnChooseImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                selectImageLauncher.launch("image/*");

            }
        });

        btnSaveProd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editTextName.getText().toString().trim();
                String description = editTextDesc.getText().toString().trim();
                String priceStr = editTextPrice.getText().toString().trim();


                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(description) || TextUtils.isEmpty(priceStr) || selectedImageUri == null) {
                    Toast.makeText(EditProductActivity.this, "All fields need to be filled", Toast.LENGTH_SHORT).show();
                    return;
                }

                double price = Double.parseDouble(priceStr);



                StorageReference fileRef = storageReference.child(productId + ".jpg");
                UploadTask uploadTask = fileRef.putFile(selectedImageUri);

                uploadTask.addOnCompleteListener(task ->
                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl;
                            if (uri == null){
                                imageUrl = oldImageURL;
                            }
                            else{
                                imageUrl = uri.toString();
                            }

                            //create my object
                            Product product = new Product(productId, name, description, price, imageUrl);
                            databaseProduct.child(productId).setValue(product).addOnSuccessListener(aVoid -> {
                                Toast.makeText(EditProductActivity.this, "Success Product updated", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                        })
                );
            }
        });
    }

    private final ActivityResultLauncher<String> selectImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    imageView.setImageURI(uri);
                }
            });

    //class for loading the Image

    private static class ImageLoadingTask extends AsyncTask<Void,Void, Bitmap> {

        private final String url;
        private final ImageView imageView;

        public ImageLoadingTask(String u, ImageView im){
            url = u;
            imageView = im;
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            try{
                URL urlConnection = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlConnection.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);


            }catch(Exception e){

            }

            return null;
        }
    }
}