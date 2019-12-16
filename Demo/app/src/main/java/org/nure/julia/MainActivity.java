package org.nure.julia;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.pusher.pushnotifications.PushNotifications;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.nure.julia.auth.AuthenticationService;
import org.nure.julia.database.PersistenceContext;
import org.nure.julia.database.entity.Notification;

public class MainActivity extends AppCompatActivity {
    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton add = findViewById(R.id.add);
        add.setOnClickListener(view -> {
            Intent intent = new Intent(this, DeviceActivity.class);

            if (this.getIntent() != null && this.getIntent().getExtras() != null) {
                intent.putExtras(this.getIntent().getExtras());
            }

            startActivity(intent);
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow, R.id.nav_send)
                .setDrawerLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        if (this.getIntent().hasExtra("afterDeviceAdding")
                && this.getIntent().hasExtra("deviceWasAdded")
                && this.getIntent().getBooleanExtra("afterDeviceAdding", false)) {

            Toast.makeText(this, this.getIntent().getBooleanExtra("deviceWasAdded", false)
                            ? "Device was added"
                            : "Device was previously added",
                    Toast.LENGTH_SHORT).show();
        }

        setAppBarUserData(drawer);

        PushNotifications.start(getApplicationContext(), "9f9f0337-33b7-4ce6-afb3-0585c253506d");
        PushNotifications.addDeviceInterest(AuthenticationService.INSTANCE.getAccountDto().getEmail());
    }

    private void setAppBarUserData(DrawerLayout drawer) {
        final View header = ((NavigationView) drawer.findViewById(R.id.nav_view)).getHeaderView(0);

        ImageView userPhoto = header.findViewById(R.id.userPhoto);
        Picasso.get().load(AuthenticationService.INSTANCE.getAccountDto().getPhotoUri())
                .resize(200, 200).into(userPhoto);

        TextView username = header.findViewById(R.id.username);
        username.setText(AuthenticationService.INSTANCE.getAccountDto().getName());

        TextView userEmail = header.findViewById(R.id.userEmail);
        userEmail.setText(AuthenticationService.INSTANCE.getAccountDto().getEmail());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
