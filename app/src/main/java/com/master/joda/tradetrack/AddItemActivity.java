package com.master.joda.tradetrack;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.master.joda.tradetrack.model.Item;

import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddItemActivity extends AppCompatActivity {

    @BindView(R.id.edit_item_name)
    EditText editItemName;
    @BindView(R.id.edit_cost_price)
    EditText editCostPrice;
    @BindView(R.id.edit_selling_price)
    EditText editSellingPrice;
    @BindView(R.id.edit_quantity)
    EditText editQuantity;
    @BindView(R.id.fab_add_item)
    FloatingActionButton fabAddItem;

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseReference;

    String itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        ButterKnife.bind(this);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("items");

        Intent intent = getIntent();
        if (intent.hasExtra(getString(R.string.item_id_extra))) {
            itemId = intent.getStringExtra(getString(R.string.item_id_extra));
        } else {
            itemId = generateId();
        }

        fabAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Item item = new Item(
                        itemId,
                        editItemName.getText().toString(),
                        editQuantity.getText().toString(),
                        Double.valueOf(editSellingPrice.getText().toString()),
                        Double.valueOf(editCostPrice.getText().toString())
                );
                FirebaseUser user = mFirebaseAuth.getCurrentUser();
                String userId;
                if (user != null) {
                    userId = user.getUid();
                    mDatabaseReference.child(userId)
                            .child(itemId)
                            .setValue(item);
                    finish();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    private String generateId() {
        return UUID.randomUUID().toString();
    }
}
