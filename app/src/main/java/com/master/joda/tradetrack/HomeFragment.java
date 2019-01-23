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
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.master.joda.tradetrack.model.Item;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A fragment representing a list of Items.
 */
public class HomeFragment extends Fragment {

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mItemsDatabaseReference;
    private FirebaseUser mFirebaseUser;
    private FirebaseRecyclerAdapter mAdapter;

    private SharedPreferences mSharedPreferences;
    private MediaPlayer mMediaPlayer;

    @BindView(R.id.list)
    RecyclerView mRecyclerView;
    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;

    private Unbinder unbinder;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public HomeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        mItemsDatabaseReference = mFirebaseDatabase.getReference().child("items");
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

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
    public void onStop() {
        super.onStop();
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
        Query query = mItemsDatabaseReference.child(mFirebaseUser.getUid());

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

                final int itemQuantityInt = Integer.valueOf(itemQuantity);

                if (itemQuantityInt <= 0) {
                    holder.mRecordSale.setEnabled(false);
                }

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

                holder.mRecordSale.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean isChecked = mSharedPreferences.getBoolean(getString(R.string.is_checked_key), true);
                        if (isChecked) {
                            mMediaPlayer.start();
                        }
                        int itemQuantityInt = Integer.valueOf(itemQuantity);
                        if (itemQuantityInt >= 1) {
                            itemQuantityInt--;
                            int storedProfit = mSharedPreferences.getInt("KEY_STORED_PROFIT", 0);
                            double profit = itemSellingPrice - itemCostPrice;
                            storedProfit = storedProfit + (int) profit;
                            mSharedPreferences.edit()
                                    .putInt("KEY_STORED_PROFIT", storedProfit)
                                    .apply();

                            mItemsDatabaseReference.child(mFirebaseUser.getUid())
                                    .child(itemId)
                                    .child("quantity")
                                    .setValue(String.valueOf(itemQuantityInt));
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

    private void makeToast() {
        Toast.makeText(getContext(), getString(R.string.zero_quantity_message), Toast.LENGTH_SHORT)
                .show();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_name)
        TextView mItemName;
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
        }
    }
}
