package com.master.joda.tradetrack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.master.joda.tradetrack.widget.WidgetService;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.fab)
    FloatingActionButton mFloatingActionButton;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.nav_view)
    NavigationView mNavigationView;

    private static final int RC_SIGN_IN = 1;
    private FirebaseUser mFirebaseUser;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        setSupportActionBar(mToolbar);

//        enableStrictMode();

        MobileAds.initialize(this,
                getString(R.string.admob_app_id));
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("18110811144C3B76421A1668CF660726")
                .build();
        mAdView.loadAd(adRequest);

        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddItemActivity.class);
                startActivity(intent);
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView.setNavigationItemSelectedListener(this);

        SwitchCompat drawerSwitch = (SwitchCompat) mNavigationView.getMenu().findItem(R.id.nav_enable_sound).getActionView();
        drawerSwitch.setEnabled(true);
        drawerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                preferences.edit()
                        .putBoolean(getString(R.string.is_checked_key), isChecked)
                        .apply();
            }
        });

        setNavHeaderValues();
        openDrawer();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setTheme(R.style.AppTheme_FirebaseUI)
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.EmailBuilder().build(),
                                            new AuthUI.IdpConfig.GoogleBuilder().build()
                                    ))
                                    .build(),
                            RC_SIGN_IN
                    );
                } else {
                    displayHomeFragment();
                }
            }
        };
    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    private void enableStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    private void setNavHeaderValues() {
        View headerView = mNavigationView.getHeaderView(0);
        TextView usernameTextView = headerView.findViewById(R.id.username);
        TextView emailTextView = headerView.findViewById(R.id.user_email);
        if (mFirebaseUser != null) {
            usernameTextView.setText(mFirebaseUser.getDisplayName());
            emailTextView.setText(mFirebaseUser.getEmail());
        }
    }

    @Override
    protected void onResume() {
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
        if (mAdView != null) {
            mAdView.resume();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                FirebaseUser user = mFirebaseAuth.getCurrentUser();
                if (user != null) {
                    signInMessage(user);
                }
            } else if (resultCode == RESULT_CANCELED) {
//                Toast.makeText(this, getString(R.string.sign_in_cancelled_message), Toast.LENGTH_SHORT).show();
//                finish();
                if (!isConnected()) {
                    setContentView(R.layout.activity_main_no_internet);
                } else {
                    Toast.makeText(this, getString(R.string.sign_in_cancelled_message), Toast.LENGTH_SHORT).show();

                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sign_out:
                // sign out
                AuthUI.getInstance()
                        .signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        displaySelectedNavItem(id);

        return true;
    }

    private void openDrawer() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.openDrawer(GravityCompat.START);
            }
        }, 1000);
    }

    private void signInMessage(FirebaseUser user) {
        String displayName = user.getDisplayName();
        Snackbar.make(findViewById(R.id.coordinator_layout),
                getString(R.string.sign_in_message, displayName),
                Snackbar.LENGTH_SHORT)
                .show();
    }

    private void selectNavigationMenuItem(int id) {
        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        menu.findItem(id).setChecked(true);
    }

    private void displaySelectedNavItem(int navItemId) {
        switch (navItemId) {
            case R.id.nav_home:
                displayHomeFragment();
                break;
            case R.id.nav_sales_record:
                displayViewRecordsFragment();
                break;
            case R.id.nav_about:
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    private void displayViewRecordsFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(getString(R.string.view_records_fragment_tag));
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container_frame, fragment)
                    .commit();
        } else {
            fragment = new ViewRecordsFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container_frame, fragment, getString(R.string.view_records_fragment_tag))
                    .commit();
        }

        selectNavigationMenuItem(R.id.nav_sales_record);
    }

    private void displayHomeFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(getString(R.string.home_fragment_tag));
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container_frame, fragment)
                    .commit();
        } else {
            fragment = new HomeFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container_frame, fragment, getString(R.string.home_fragment_tag))
                    .commit();
        }
        selectNavigationMenuItem(R.id.nav_home);
    }
}
