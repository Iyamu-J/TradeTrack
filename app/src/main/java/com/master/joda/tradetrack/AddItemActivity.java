package com.master.joda.tradetrack;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        ButterKnife.bind(this);
    }
}
