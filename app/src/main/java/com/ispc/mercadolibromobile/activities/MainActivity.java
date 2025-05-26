package com.ispc.mercadolibromobile.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.TextView;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.ispc.mercadolibromobile.R;

import com.ispc.mercadolibromobile.fragments.BooksFragment;
import com.ispc.mercadolibromobile.fragments.ContactFragment;
import com.ispc.mercadolibromobile.fragments.ProfileFragment;
import com.ispc.mercadolibromobile.fragments.CarritoFragment;
import com.ispc.mercadolibromobile.utils.SessionUtils;
import com.google.android.material.navigation.NavigationView;
import com.ispc.mercadolibromobile.fragments.MyReviewsFragment;
import com.ispc.mercadolibromobile.fragments.DireccionFragment;
import com.ispc.mercadolibromobile.fragments.PagoFragment;
import com.ispc.mercadolibromobile.fragments.PedidosFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private Toolbar toolbar;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.beige_suave));
        }

        // Configure the ActionBar (Toolbar)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setLogo(R.drawable.ic_logo_app);
            getSupportActionBar().setDisplayUseLogoEnabled(true);
            getSupportActionBar().setTitle(R.string.app_name);
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        toggle.getDrawerArrowDrawable().setColor(ContextCompat.getColor(this, R.color.sunstone_neutral_7));

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);

        if (headerView != null) {
            TextView userNameTextView = headerView.findViewById(R.id.textViewUserName);

            String userEmail = SessionUtils.getUserEmail(this);

            if (userEmail != null && !userEmail.isEmpty()) {
                userNameTextView.setText(userEmail);
            } else {
                userNameTextView.setText(getString(R.string.nav_header_title_default));
            }
        } else {
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new BooksFragment())
                    .commit();
            navigationView.setCheckedItem(R.id.nav_products);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_products) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new BooksFragment())
                    .addToBackStack(null)
                    .commit();
        } else if (id == R.id.nav_contact) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ContactFragment())
                    .addToBackStack(null)
                    .commit();
        } else if (id == R.id.nav_profile) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment())
                    .addToBackStack(null)
                    .commit();
        } else if (id == R.id.nav_my_reviews) { // NEW ITEM
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new MyReviewsFragment())
                    .addToBackStack(null)
                    .commit();
        } else if (id == R.id.nav_my_addresses) { // NEW ITEM
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DireccionFragment()) // Using DireccionFragment
                    .addToBackStack(null)
                    .commit();
        } else if (id == R.id.nav_my_payments) { // NEW ITEM
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new PagoFragment()) // Using PagoFragment
                    .addToBackStack(null)
                    .commit();
        } else if (id == R.id.nav_my_orders) { // NEW ITEM
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new PedidosFragment()) // Using PedidosFragment
                    .addToBackStack(null)
                    .commit();
        }else if (id == R.id.nav_logout) {
            Toast.makeText(this, getString(R.string.logout_message), Toast.LENGTH_SHORT).show();
            SessionUtils.clearSession(this);
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_cart) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CarritoFragment())
                    .addToBackStack(null)
                    .commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}