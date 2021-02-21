package com.example.nuovo_pt;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.Menu;

import com.example.nuovo_pt.api.ExerciseRepository;
import com.example.nuovo_pt.api.Result;
import com.example.nuovo_pt.db.ClientViewModel;
import com.example.nuovo_pt.db.WorkoutViewModel;
import com.example.nuovo_pt.db.clients.Client;
import com.example.nuovo_pt.ui.AddClientFragment;
import com.example.nuovo_pt.ui.ClientWorkoutsFragment;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ClientsAdditionListener,NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;
    ClientsHolder clientsHolder;
    NavigationView navigationView;
    Menu navMenu;
    NavController navController;
    DrawerLayout drawer;
    List<Result> exercises;
    ClientViewModel clientViewModel;
    WorkoutViewModel workoutViewModel;
    boolean firstTimePopulated = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navMenu = navigationView.getMenu();
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_clients, R.id.nav_gallery, R.id.nav_slideshow, R.id.nav_add_client)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(this);

        clientsHolder = ClientsHolder.getInstance();

        exercises = new ArrayList<>();

        clientViewModel = new ViewModelProvider(this).get(ClientViewModel.class);
        clientViewModel.getAllClients().observe(this, new Observer<List<Client>>() {
            @Override
            public void onChanged(@Nullable final List<Client> clients) {
                if(firstTimePopulated) {
                    populateNavMenu(clients);
                    firstTimePopulated = false;
                } else {
                    navMenu.add(clients.get(clients.size()-1).getName());
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void addClient(Client client) {
        clientViewModel.insert(client);
    }

    public void populateNavMenu(List<Client> clients) {
        for(Client client:clients) {
            navMenu.add(client.getName());
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id != R.id.nav_add_client) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new ClientWorkoutsFragment(new Client((String) item.getTitle(),1)))
                    .commit();

        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new AddClientFragment())
                    .commit();
        }

        getSupportActionBar().setTitle(item.getTitle());
        drawer.close();
        return true;
    }

}