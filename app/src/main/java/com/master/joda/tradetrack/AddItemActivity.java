package com.master.joda.tradetrack;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
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

public class AddItemActivity extends AppCompatActivity implements TextWatcher {

    @BindView(R.id.edit_item_name)
    EditText editItemName;
    @BindView(R.id.edit_quantity)
    EditText editQuantity;
    @BindView(R.id.edit_cost_price)
    EditText editCostPrice;
    @BindView(R.id.edit_selling_price)
    EditText editSellingPrice;
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

        editItemName.addTextChangedListener(this);
        editQuantity.addTextChangedListener(this);
        editCostPrice.addTextChangedListener(this);
        editSellingPrice.addTextChangedListener(this);

        Intent intent = getIntent();
        if (intent.hasExtra(getString(R.string.item_id_extra))) {
            itemId = intent.getStringExtra(getString(R.string.item_id_extra));
        } else {
            itemId = generateId();
        }
        initialiseEditTexts(intent);

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

    /**
     * Initialises the EditTexts if an intent starts this activity
     * @param intent intent that starts the activity
     */
    private void initialiseEditTexts(Intent intent) {

        // initialise the Item name
        if (intent.hasExtra(getString(R.string.item_name_extra))) {
            editItemName.setText(intent.getStringExtra(getString(R.string.item_name_extra)));
        }

        // initialise the Item quantity
        if (intent.hasExtra(getString(R.string.item_quantity_extra))) {
            editQuantity.setText(intent.getStringExtra(getString(R.string.item_quantity_extra)));
        }

        // initialise the Item cost price
        if (intent.hasExtra(getString(R.string.item_cost_price_extra))) {
            editCostPrice.setText(
                    String.valueOf(
                            intent.getDoubleExtra(getString(R.string.item_cost_price_extra),
                                    0)
                    ));
        }

        // initialise the Item selling price
        if (intent.hasExtra(getString(R.string.item_selling_price_extra))) {
            editSellingPrice.setText(
                    String.valueOf(
                            intent.getDoubleExtra(getString(R.string.item_selling_price_extra),
                                    0)
                    ));
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.toString().trim().length() > 0) {
            fabAddItem.setEnabled(true);
        } else {
            fabAddItem.setEnabled(false);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }
}
