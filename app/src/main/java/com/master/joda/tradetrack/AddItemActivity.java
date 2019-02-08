package com.master.joda.tradetrack;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
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
    private InterstitialAd mInterstitialAd;

    private String mItemId;
    private final int[] mIds = new int[]{
            R.id.edit_item_name,
            R.id.edit_quantity,
            R.id.edit_cost_price,
            R.id.edit_selling_price
    };
    private boolean mIsEditTextEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        ButterKnife.bind(this);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("items");

        Intent intent = getIntent();
        if (intent.hasExtra(getString(R.string.item_id_extra))) {
            mItemId = intent.getStringExtra(getString(R.string.item_id_extra));
        } else {
            mItemId = generateId();
        }
        initialiseEditTexts(intent);

        fabAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                validateEditText(mIds);
                if (!mIsEditTextEmpty) {
                    Item item = new Item(
                            mItemId,
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
                                .child(mItemId)
                                .setValue(item);
                        showInterstitialAd();
//                        finish();
                    }
                }
            }
        });

        MobileAds.initialize(this,
                getString(R.string.admob_app_id));
        initInterstitialAd();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        showInterstitialAd();
    }

    @NonNull
    private String generateId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Initialises the EditTexts if an intent starts this activity
     *
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

    private void validateEditText(int... ids) {

        for (int id : ids) {

            EditText editText = findViewById(id);
            if (TextUtils.isEmpty(editText.getText().toString())) {
                editText.setError("Value Cannot be empty. Please enter a value");
                mIsEditTextEmpty = true;
            } else {
                mIsEditTextEmpty = false;
            }
        }
    }

    private void initInterstitialAd() {
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                finish();
            }
        });
    }

    private void loadInterstitial() {
        if (!mInterstitialAd.isLoading() && !mInterstitialAd.isLoaded()) {
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice("18110811144C3B76421A1668CF660726")
                    .build();
            mInterstitialAd.loadAd(adRequest);
        }
    }

    private void showInterstitialAd() {
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            loadInterstitial();
        }
    }
}
