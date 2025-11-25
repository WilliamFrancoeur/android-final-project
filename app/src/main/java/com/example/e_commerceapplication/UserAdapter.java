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

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class UserAdapter extends BaseAdapter{

    private Context context;
    private ArrayList<User> userList;


    public UserAdapter(Context c,ArrayList<User> list){
        context = c;
        userList = list;
    }


    @Override
    public int getCount() {
        return userList.size();
    }

    @Override
    public Object getItem(int i) {
        return userList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if (view == null){
            view = LayoutInflater.from(context).inflate(R.layout.item_user,viewGroup,false);
        }
        User user = userList.get(i);

        TextView emailText = view.findViewById(R.id.textViewUserEmail);
        TextView roleText = view.findViewById(R.id.textViewUserRole);

        Button viewUserbtn = view.findViewById(R.id.btnViewUser);
        Button deleteUserbtn = view.findViewById(R.id.btnDeleteUser);

        emailText.setText(user.getEmail());
        roleText.setText(user.getRole());


        viewUserbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AdminViewActivity.class);
                intent.putExtra("UserID",user.getUID());
                intent.putExtra("email",user.getEmail());
                context.startActivity(intent);
            }
        });
        deleteUserbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteUserRecordDB(user.getUID());
            }

        });

        return view;
    }

    private void deleteUserRecordDB(String userID){
        ArrayList<Product> products = new ArrayList<>();
        DatabaseReference databaseProducts = FirebaseDatabase.getInstance().getReference("products");

        databaseProducts.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                products.clear();
                for (DataSnapshot possnapshot : snapshot.getChildren()) {
                    Product product = possnapshot.getValue(Product.class);
                    if (product != null && userID.equals(product.getSellerId())) {
                        FirebaseDatabase.getInstance().getReference("products")
                                .child(product.getId())
                                .removeValue();
                        StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(product.getImageURL());

                        imageRef.delete();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        FirebaseDatabase.getInstance().getReference("users")
                .child(userID)
                .removeValue().addOnSuccessListener(avoid ->
                        Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show());
    }

}
