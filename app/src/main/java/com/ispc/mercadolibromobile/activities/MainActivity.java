package com.ispc.mercadolibromobile.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.ispc.mercadolibromobile.R;
import com.ispc.mercadolibromobile.api.ApiService;
import com.ispc.mercadolibromobile.api.RetrofitClient;
import com.ispc.mercadolibromobile.fragments.BooksFragment;
import com.ispc.mercadolibromobile.fragments.CarritoFragment;
import com.ispc.mercadolibromobile.fragments.ContactFragment;
import com.ispc.mercadolibromobile.fragments.DireccionFormFragment;
import com.ispc.mercadolibromobile.fragments.FeedbackFragment;
import com.ispc.mercadolibromobile.fragments.MyReviewsFragment;
//import com.ispc.mercadolibromobile.fragments.PedidosFragment;
import com.ispc.mercadolibromobile.fragments.PedidoFragment;
import com.ispc.mercadolibromobile.fragments.ProfileFragment;
import com.ispc.mercadolibromobile.models.ItemCarrito;
import com.ispc.mercadolibromobile.models.UserInfo;
import com.ispc.mercadolibromobile.utils.SessionUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        CarritoFragment.CartUpdateListener {
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private Toolbar toolbar;
    private TextView cartBadgeTextView;
    private ApiService apiService;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Verifico y agrego la expiracion del token antes de continuar
        String token = SessionUtils.getAuthToken(this);
        if (token == null || SessionUtils.isTokenExpired(token)) {
            SessionUtils.clearSession(this);
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return;
        }
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.beige_suave));
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setLogo(R.drawable.ic_logo_app);
            getSupportActionBar().setDisplayUseLogoEnabled(true);
            getSupportActionBar().setTitle("");
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
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new BooksFragment())
                    .commit();
            navigationView.setCheckedItem(R.id.nav_products);
        }

        apiService = RetrofitClient.getApiService(this);
        handleDeepLink(getIntent());
        obtenerNombre(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleDeepLink(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadgeCount();
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
        } else if (id == R.id.nav_feedback) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new FeedbackFragment())
                    .addToBackStack(null)
                    .commit();
        } else if (id == R.id.nav_profile) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment())
                    .addToBackStack(null)
                    .commit();
        } else if (id == R.id.nav_my_reviews) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new MyReviewsFragment())
                    .addToBackStack(null)
                    .commit();
        } else if (id == R.id.nav_my_addresses) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DireccionFormFragment())
                    .addToBackStack(null)
                    .commit();
        } else if (id == R.id.nav_my_orders) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new PedidoFragment())
                    .addToBackStack(null)
                    .commit();
        } else if (id == R.id.nav_logout) {
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

        // Obtener la referencia al TextView del badge
        MenuItem cartItem = menu.findItem(R.id.action_cart);
        View actionView = cartItem.getActionView();
        if (actionView != null) {
            cartBadgeTextView = actionView.findViewById(R.id.cart_badge);
            actionView.setOnClickListener(v -> onOptionsItemSelected(cartItem));
        }

        updateCartBadgeCount();
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

    private void handleDeepLink(Intent intent) {
        if (intent != null && intent.getData() != null) {
            Uri data = intent.getData();
            Log.d(TAG, "Deep Link data: " + data.toString()); // Usando TAG

            if ("mercadolibromobile".equals(data.getScheme()) && "checkout".equals(data.getHost())) {
                String path = data.getPath(); // Ej. "/success", "/failure", "/pending"
                String paymentId = data.getQueryParameter("payment_id");
                String statusMp = data.getQueryParameter("status");
                String externalReference = data.getQueryParameter("external_reference");

                Log.d(TAG, "Path: " + path + ", Payment ID: " + paymentId + ", Status: " + statusMp + ", External Ref: " + externalReference);

                if ("/success".equals(path)) {
                    Toast.makeText(this, "Pago Exitoso! Ref: " + externalReference, Toast.LENGTH_LONG).show();

                } else if ("/failure".equals(path)) {
                    Toast.makeText(this, "Pago Fallido! Ref: " + externalReference, Toast.LENGTH_LONG).show();
                } else if ("/pending".equals(path)) {
                    Toast.makeText(this, "Pago Pendiente! Ref: " + externalReference, Toast.LENGTH_LONG).show();
                }

                setIntent(null);
            }
        }
    }

    // Métodos para el Contador del Carrito
    @Override
    public void onCartUpdated() {
        Log.d(TAG, "onCartUpdated() llamado desde un fragmento. Actualizando badge.");
        updateCartBadgeCount();
    }
    public void updateCartBadgeCount() {
        String token = SessionUtils.getAuthToken(this);

        if (token == null || apiService == null) {
            if (cartBadgeTextView != null) {
                cartBadgeTextView.setVisibility(View.GONE);
                cartBadgeTextView.setText("0");
            }
            Log.d(TAG, "No hay sesión activa o apiService no inicializado. Ocultando badge.");
            return;
        }

        Call<List<ItemCarrito>> call = apiService.obtenerCarrito("Bearer " + token);
        call.enqueue(new Callback<List<ItemCarrito>>() {
            @Override
            public void onResponse(@NonNull Call<List<ItemCarrito>> call, @NonNull Response<List<ItemCarrito>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int totalQuantity = 0;
                    for (ItemCarrito item : response.body()) {
                        totalQuantity += item.getCantidad();
                    }
                    int itemCount = totalQuantity;

                    if (cartBadgeTextView != null) {
                        if (itemCount > 0) {
                            cartBadgeTextView.setText(String.valueOf(itemCount));
                            cartBadgeTextView.setVisibility(View.VISIBLE);
                            Log.d(TAG, "Badge actualizado con " + itemCount + " ítems.");
                        } else {
                            cartBadgeTextView.setVisibility(View.GONE);
                            Log.d(TAG, "Carrito vacío. Ocultando badge.");
                        }
                    }
                } else {
                    Log.e(TAG, "Error al obtener cantidad del carrito: " + response.code() + ", " + response.message());
                    if (cartBadgeTextView != null) {
                        cartBadgeTextView.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ItemCarrito>> call, @NonNull Throwable t) {
                Log.e(TAG, "Fallo de conexión al obtener cantidad del carrito: " + t.getMessage());
                if (cartBadgeTextView != null) {
                    cartBadgeTextView.setVisibility(View.GONE);
                }
            }
        });
    }
    public void obtenerNombre(Context context) {
        Call<List<UserInfo>> call = apiService.getUsers();

        call.enqueue(new Callback<List<UserInfo>>() {
            @Override
            public void onResponse(@NonNull Call<List<UserInfo>> call, @NonNull Response<List<UserInfo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<UserInfo> usuarios = response.body();
                    String emailGuardado = SessionUtils.getUserEmail(context);

                    // Filtrar usuarios por email que coincida con el guardado
                    List<UserInfo> filtrados = new ArrayList<>();
                    for (UserInfo user : usuarios) {
                        if (emailGuardado.equals(user.getEmail())) {
                            filtrados.add(user);
                        }
                    }

                    if (filtrados.size() == 1) {
                        String username = filtrados.get(0).getUsername();
                        SessionUtils.saveUserName(context, username);
                        Log.d("USERNAME", "Usuario autenticado: " + username);
                    } else {
                        Log.e("USERNAME", "Error: se esperaba 1 usuario con email " + emailGuardado +
                                ", pero se encontraron " + filtrados.size());
                    }

                } else {
                    Log.e("USERNAME", "Respuesta fallida del servidor: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<UserInfo>> call, @NonNull Throwable t) {
                Log.e("USERNAME", "Error de red: " + t.getMessage());
            }
        });
    }
}