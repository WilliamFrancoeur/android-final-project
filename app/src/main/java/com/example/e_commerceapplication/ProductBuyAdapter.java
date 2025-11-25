package com.example.e_commerceapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ProductBuyAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Product> productList;


    public ProductBuyAdapter(Context c,ArrayList<Product> list){
        context = c;
        productList = list;
    }


    @Override
    public int getCount() {
        return productList.size();
    }

    @Override
    public Object getItem(int i) {
        return productList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if (view == null){
            view = LayoutInflater.from(context).inflate(R.layout.item_product_buy,viewGroup,false);
        }
        Product product = productList.get(i);

        ImageView imageView = view.findViewById(R.id.imageViewProductItem);
        TextView nameText = view.findViewById(R.id.textViewProductName);
        TextView descriptionText = view.findViewById(R.id.textViewProductDescription);
        TextView priceText = view.findViewById(R.id.textViewProductPrice);


        nameText.setText(product.getName());
        descriptionText.setText(product.getDescription());
        priceText.setText("$ " + product.getPrice());

        if (product.getImageURL() !=null && !product.getImageURL().isEmpty()){
            new ProductAdapter.ImageLoadTask(product.getImageURL(),imageView).execute();
        }else {
            imageView.setImageResource(android.R.drawable.ic_menu_report_image);
        }


        return view;
    }

    private void deleteProductRecordDB(String productID){
        FirebaseDatabase.getInstance().getReference("products")
                .child(productID)
                .removeValue().addOnSuccessListener(aVoid ->
                        Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show());
    }


    static class ImageLoadTask extends AsyncTask<Void,Void, Bitmap> {
        private String url;
        private ImageView imageView;
        public ImageLoadTask(String url, ImageView imageView){
            this.url = url;
            this.imageView = imageView;
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
            }catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                imageView.setImageBitmap(result);
            }else{
                imageView.setImageResource(android.R.drawable.ic_menu_report_image);
            }
        }
    }
}

