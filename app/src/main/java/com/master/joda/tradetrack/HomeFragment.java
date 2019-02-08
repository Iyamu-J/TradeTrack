package com.master.joda.tradetrack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.master.joda.tradetrack.model.Item;
import com.master.joda.tradetrack.model.Record;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A fragment representing a list of Items.
 */
public class HomeFragment extends Fragment implements ValueEventListener {

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mItemsDatabaseReference;
    private DatabaseReference mRecordsDatabaseReference;
    private FirebaseUser mFirebaseUser;
    private FirebaseRecyclerAdapter mAdapter;

    private SharedPreferences mSharedPreferences;
    private MediaPlayer mMediaPlayer;

    @BindView(R.id.list)
    RecyclerView mRecyclerView;
    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;

    private Unbinder unbinder;
    private boolean isDatePresent;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public HomeFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        unbinder = ButterKnife.bind(this, view);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mMediaPlayer = MediaPlayer.create(getContext(), R.raw.click);

        // initialise FirebaseDatabase
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mItemsDatabaseReference = mFirebaseDatabase.getReference().child("items");
        mRecordsDatabaseReference = mFirebaseDatabase.getReference().child("records").child(mFirebaseUser.getUid());

        mProgressBar.setVisibility(View.VISIBLE);

        setupRecyclerView();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter.startListening();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAdapter.stopListening();
        mMediaPlayer.release();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        mMediaPlayer.release();
    }

    private void setupRecyclerView() {
        Query query = mItemsDatabaseReference.child(mFirebaseUser.getUid()).orderByChild("name");

        FirebaseRecyclerOptions<Item> options =
                new FirebaseRecyclerOptions.Builder<Item>()
                        .setQuery(query, Item.class)
                        .build();

        mAdapter = new FirebaseRecyclerAdapter<Item, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Item model) {
                holder.bind(model);

                final String itemId = model.getId();
                final String itemName = model.getName();
                final String itemQuantity = model.getQuantity();
                final double itemCostPrice = model.getCostPrice();
                final double itemSellingPrice = model.getSellingPrice();

                holder.mEditItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), AddItemActivity.class);
                        intent.putExtra(getString(R.string.item_id_extra), itemId);
                        intent.putExtra(getString(R.string.item_name_extra), itemName);
                        intent.putExtra(getString(R.string.item_quantity_extra), itemQuantity);
                        intent.putExtra(getString(R.string.item_cost_price_extra), itemCostPrice);
                        intent.putExtra(getString(R.string.item_selling_price_extra), itemSellingPrice);
                        startActivity(intent);
                    }
                });

                isNewDay();
                holder.mRecordSale.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        int itemQuantityInt = Integer.valueOf(itemQuantity);
                        if (itemQuantityInt >= 1) {
                            itemQuantityInt--;

                            boolean isChecked = mSharedPreferences
                                    .getBoolean(getString(R.string.is_checked_key), true);
                            if (isChecked) {
                                mMediaPlayer.start();
                            }

                            // Update Quantity
                            updateQuantity(itemQuantityInt, itemId);

                            // get new profit
                            final double profit = itemSellingPrice - itemCostPrice;

                            if (isDatePresent) {
                                updateProfit(profit);
                                Log.d(HomeFragment.class.getSimpleName(), "Record updated");
                            } else {
                                createNewRecordInstance(profit);
                                Log.d(HomeFragment.class.getSimpleName(), "New Record created");
                            }
                        } else {
                            makeToast();
                        }
                    }
                });
            }

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_item, parent, false);
                return new ViewHolder(view);
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();

                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(@NonNull DatabaseError error) {
                super.onError(error);
                mProgressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), getString(R.string.error_message), Toast.LENGTH_LONG).show();
            }
        };

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void updateProfit(double profit) {
        // get saved profit
        int storedProfit = mSharedPreferences.getInt(getString(R.string.key_stored_profit), 0);

        // get saved push key
        String savedKey = mSharedPreferences.getString(getString(R.string.key_saved_push_key), "");

        // add new profit to previous profit
        storedProfit = (int) (storedProfit + profit);

        assert savedKey != null;
        mRecordsDatabaseReference.child(savedKey).child("profit").setValue(String.valueOf(storedProfit));

        // temporarily store profit till new day
        mSharedPreferences.edit()
                .putInt(getString(R.string.key_stored_profit), storedProfit)
                .apply();
    }

    private void createNewRecordInstance(double profit) {
        // remove previous profit
        mSharedPreferences.edit()
                .remove(getString(R.string.key_stored_profit))
                .apply();

        // get push key
        String key = mRecordsDatabaseReference.push().getKey();

        // create new Date instance
        String dateString = new DateTime().toString(DateTimeFormat.fullDate());

        // create new instance of Record
        Record record = new Record();
        record.setProfit(String.valueOf(profit));
        record.setDate(dateString);

        // upload to database
        assert key != null;
        mRecordsDatabaseReference.child(key).setValue(record);

        // store new profit
        mSharedPreferences.edit()
                .putInt(getString(R.string.key_stored_profit), (int) profit)
                .apply();

        // store new push key
        mSharedPreferences.edit()
                .putString(getString(R.string.key_saved_push_key), key)
                .apply();

    }

    private void updateQuantity(int itemQuantityInt, String itemId) {
        mItemsDatabaseReference.child(mFirebaseUser.getUid())
                .child(itemId)
                .child("quantity")
                .setValue(String.valueOf(itemQuantityInt));
    }

    private void makeToast() {
        Toast.makeText(getContext(), getString(R.string.zero_quantity_message), Toast.LENGTH_SHORT)
                .show();
    }

    private void isNewDay() {

        String key = mSharedPreferences.getString(getString(R.string.key_saved_push_key), "");
        if (TextUtils.isEmpty(key)) {
            isDatePresent = false;
        } else {
            assert key != null;
            mRecordsDatabaseReference.child(key).child("date")
                    .addListenerForSingleValueEvent(this);
        }
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        String dateString = new DateTime().toString(DateTimeFormat.fullDate());
        Log.d(HomeFragment.class.getSimpleName(), "DataSnapshot: " + dataSnapshot.toString());
        isDatePresent = dateString.equals(dataSnapshot.getValue());
        Log.d(HomeFragment.class.getSimpleName(), "Is Date present? " + isDatePresent);
        Log.d(HomeFragment.class.getSimpleName(), "Date: " + dateString);
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_name)
        TextView mItemName;
        @BindView(R.id.item_price)
        TextView mItemPrice;
        @BindView(R.id.item_quantity)
        TextView mItemQuantity;
        @BindView(R.id.edit_item)
        ImageButton mEditItem;
        @BindView(R.id.record_sale)
        ImageButton mRecordSale;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        void bind(Item item) {
            mItemName.setText(item.getName());
            mItemPrice.setText(getString(R.string.item_price_text, item.getSellingPrice()));
            mItemQuantity.setText(getString(R.string.item_quantity_text, item.getQuantity()));
        }
    }
}
