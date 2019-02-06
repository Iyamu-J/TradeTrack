package com.master.joda.tradetrack;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
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
import com.master.joda.tradetrack.model.Record;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class ViewRecordsFragment extends Fragment {

    private FirebaseUser mFirebaseUser;
    private DatabaseReference mRecordsDatabaseReference;
    private FirebaseRecyclerAdapter mAdapter;

    @BindView(R.id.record_list)
    RecyclerView mRecyclerView;
    @BindView(R.id.records_progress_bar)
    ProgressBar mProgressBar;

    private Unbinder unbinder;

    public ViewRecordsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_records_list, container, false);

        unbinder = ButterKnife.bind(this, view);

        mRecordsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("records");
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        setupRecyclerView();

        mProgressBar.setVisibility(View.VISIBLE);

        return view;
    }

    private void setupRecyclerView() {
        final Query query = mRecordsDatabaseReference.child(mFirebaseUser.getUid());

        FirebaseRecyclerOptions<Record> options = new FirebaseRecyclerOptions.Builder<Record>()
                .setQuery(query, Record.class)
                .build();

        mAdapter = new FirebaseRecyclerAdapter<Record, ViewHolder>(options) {

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.fragment_record, viewGroup, false);
                return new ViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Record model) {
                holder.bind(model);

                final String profit = model.getProfit();
                holder.mShareButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_TEXT,
                                getString(R.string.share_profit, profit));
                        startActivity(Intent.createChooser(shareIntent, getString(R.string.intent_chooser_title)));
                    }
                });
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
                Toast.makeText(getContext(), getString(R.string.records_error_message), Toast.LENGTH_LONG).show();
            }
        };

        mRecyclerView.setAdapter(mAdapter);
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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.date)
        TextView mSalesDate;
        @BindView(R.id.profit)
        TextView mSalesProfit;
        @BindView(R.id.share_img_button)
        ImageButton mShareButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(Record record) {
            mSalesDate.setText(record.getDate());
            mSalesProfit.setText(record.getProfit());
        }
    }
}
