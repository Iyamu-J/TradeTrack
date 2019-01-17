package com.master.joda.tradetrack;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.master.joda.tradetrack.model.Item;

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

    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        ButterKnife.bind(this);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("items");

        fabAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Item item = new Item(
                        1,
                        editItemName.getText().toString(),
                        editQuantity.getText().toString(),
                        Double.valueOf(editSellingPrice.getText().toString()),
                        Double.valueOf(editCostPrice.getText().toString())
                );
                mDatabaseReference.child("items").setValue(item);
                finish();
            }
        });
    }
}
