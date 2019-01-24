package com.master.joda.tradetrack;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class ViewRecordsFragment extends Fragment {

    private Date date;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mRecordsDatabaseReference;

    public ViewRecordsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_records, container, false);

        mRecordsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("records");
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        return view;
    }

}
