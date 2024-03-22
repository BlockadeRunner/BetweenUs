//////////////////////////////////////////////////////
//             MainActivity: Login Page             //
//                                                  //
// Lead Developer: Alex Longo                       //
// Assisting Developer: Daniel Han                  //
//                                                  //
// Description: This activity handles prompting the //
// user for location permissions, starting location //
// services, obtaining the user's actual location,  //
// and authenticating users via inputted login      //
// credentials.                                     //
//////////////////////////////////////////////////////
package com.example.betweenus;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class MainActivity extends AppCompatActivity
{

    // Declare variables for location services
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private LocationManager mylocationManager;

    // Create variable to access internal database
    DBHelper DB;

    // Create a variable to hold the application's instance of the Password Manager
    private PasswordManager passwordManager;

    // Create Location variables
    double myLat;
    double myLon;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Link the application's PasswordManager instance to a variable
        passwordManager = PasswordManager.getInstance(getApplicationContext());

        // Link the text input fields to variables
        TextView username = (TextView) findViewById(R.id.username);
        TextView password = (TextView) findViewById(R.id.password);

        // Link the on-screen buttons to variables
        MaterialButton loginbtn = (MaterialButton) findViewById(R.id.loginbtn);
        MaterialButton signupbtn = (MaterialButton) findViewById(R.id.signupbtn);

        // Initialize Database access
        DB = new DBHelper(this);

        // Request Necessary Permissions
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

        // ACTIVATE LOCATION SERVICES ON DEVICE AND OBTAIN USER LOCATION
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        mylocationManager = (LocationManager) getSystemService(MainActivity.this.LOCATION_SERVICE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Create the location request
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000); // 10 seconds
        // Create a location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Handle location updates
                    // For example, update UI with the new location
                    startLocationUpdates();
                }
            }
        };
        // Double Check if the app has permission to access device location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permissions
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted
            startLocationUpdates();
        }
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


        // Load user's existing friends!
        loadFriends();

        // Handle when "login button" is pressed
        loginbtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // ACTIVATE LOCATION SERVICES ON DEVICE AND OBTAIN USER LOCATION
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
                // Create the location request
                locationRequest = LocationRequest.create();
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                locationRequest.setInterval(10000); // 10 seconds
                // Create a location callback
                locationCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        if (locationResult == null) {
                            return;
                        }
                        for (Location location : locationResult.getLocations()) {
                            // Handle location updates
                            // For example, update UI with the new location
                            startLocationUpdates();
                        }
                    }
                };
                // Double Check if the app has permission to access device location
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // Request location permissions
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                } else {
                    // Permission already granted
                    startLocationUpdates();
                }
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                // Location accessed, get last known location
                getLastKnownLocation();

                // Retrieve password and check credentials
                try {
                    // Retrieve password from database
                    String retrievedPassword = passwordManager.getPassword(username.getText().toString());

                    // Check password inputted with retrieved password
                    if (password.getText().toString().equals(retrievedPassword)) {
                        // correct

                        // Check that location was obtained
                        if (!(myLon == 999.9) && !(myLat == 999.9)) {
                            // Bundle the location coordinates into a String
                            String location = String.valueOf(myLat) + "B" + String.valueOf(myLon);

                            // Create a new user object for database editing
                            User user = new User(0, username.getText().toString(), location);

                            // Update user's info in the database
                            DB.updateUserByUsername(user.getUsername(), user);

                            // Display Success Message and switch to next screen
                            Toast.makeText(MainActivity.this, "LOGIN SUCCESSFUL", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainActivity.this, FindUsersActivity.class);
                            intent.putExtra("CURRENT_USER", user.getUsername());
                            startActivity(intent);
                        } else {
                            Toast.makeText(MainActivity.this, "Please Make Sure Location is ON and permissions are granted", Toast.LENGTH_LONG).show();
                        }

                    } else
                    {
                        // incorrect
                        Toast.makeText(MainActivity.this, "WRONG USERNAME OR PASSWORD", Toast.LENGTH_SHORT).show();
                    }
                }
                // Catch and handle errors
                catch (KeyStoreException | CertificateException | NoSuchAlgorithmException |
                       IOException | NoSuchPaddingException | InvalidKeyException |
                       InvalidAlgorithmParameterException | UnrecoverableKeyException |
                       IllegalBlockSizeException | BadPaddingException e) {
                    // Show an error message to the user
                    Toast.makeText(MainActivity.this, "Error occurred: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }   // end onClick
        });   // end onClickListener


        // Handle when "sign-up button" is pressed
        signupbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                startLocationUpdates();
                // Switch to Sign Up activity
                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    // This method get's the device's last known location
    private void getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Get last known location
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (lastKnownLocation != null) {
                double latitude = lastKnownLocation.getLatitude();
                double longitude = lastKnownLocation.getLongitude();
                myLat = latitude;
                myLon = longitude;
            } else {
                // No last known location available
                //Toast.makeText(MainActivity.this, "Make Sure this Device's Location is ON and permissions are GRANTED", Toast.LENGTH_LONG).show();
                Toast.makeText(MainActivity.this, "YOUR DEVICE'S LOCATION IS UNAVAILABLE", Toast.LENGTH_LONG).show();
                Toast.makeText(MainActivity.this, "PLEASE WAIT 10 SECONDS AND TRY AGAIN, OR:", Toast.LENGTH_LONG).show();
                Toast.makeText(MainActivity.this, "Please Double Check Your Location Through Your OS", Toast.LENGTH_LONG).show();
                Toast.makeText(MainActivity.this, "If using an android emulator, start loc services by opening google maps", Toast.LENGTH_LONG).show();
                myLat = 999.9;
                myLon = 999.9;
            }
        }
    }

    // This method handles response to Permissions Access
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                startLocationUpdates();
            } else {
                // Permission denied, handle accordingly
                Toast.makeText(MainActivity.this, "APP WILL NOT WORK WITHOUT LOCATION", Toast.LENGTH_LONG).show();
            }
        }
    }

    // This method activates device/emulator's Location Services if not currently active
    private void startLocationUpdates()
    {
        // check permissions before starting
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // check GPS system online
        if (!mylocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Location services are not enabled, prompt user to enable them
            // You can show a dialog or prompt the user to enable location services here
            Toast.makeText(MainActivity.this, "LOC SERVICES NOT ENABLED ON THIS DEVICE", Toast.LENGTH_LONG).show();
            Toast.makeText(MainActivity.this, "PLEASE ENABLE LOC SERVICES ON THIS DEVICE", Toast.LENGTH_LONG).show();
        } else {
            // Location services are enabled, start listening for location updates
            // You can use requestLocationUpdates method to start receiving location updates
            // Example:
            LocationListener locationListener = new LocationListener()
            {
                @Override
                public void onLocationChanged(@NonNull Location location)
                {

                }
            };
            mylocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

        // Check for last known location
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>()
                {
                    @Override
                    public void onSuccess(Location location)
                    {
                        if (location != null)
                        {
                            onPause(); // Call onPause once location is retrieved
                        }
                    }
                });
    }

    // This method stops location updates when they are no longer needed
    @Override
    protected void onPause()
    {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    // This method loads some existing demo friends into the database
    private void loadFriends()
    {
        // Create local friends
        User alice = new User(96, "AliceHayward", "37.449515B-77.038172");
        User ruby = new User(95, "Ruby_Reynolds", "37.276465B-76.714484");
        User dan = new User(97, "Dan_Kuso_77", "37.540775B-77.416665");
        User julie = new User(98, "JulieISCOOL", "37.415914B-76.525863");
        User marty = new User(99, "Marty269", "38.036042B-78.474152");

        // Update Database with new Friends
        DB.updateUserByUsername("AliceHayward", alice);
        DB.updateUserByUsername("Ruby_Reynolds", ruby);
        DB.updateUserByUsername("Dan_Kuso_77", dan);
        DB.updateUserByUsername("JulieISCOOL", julie);
        DB.updateUserByUsername("Marty269", marty);

        // Try to Load in Friends' Credentials
        try
        {
            // Check that credentials are not already in use
            boolean success1 = passwordManager.savePassword("AliceHayward", "n934hg0284hyq809ewrhjg");
            boolean success2 = passwordManager.savePassword("Ruby_Reynolds", "n934hg0284hyq809ewrhjg");
            boolean success3 = passwordManager.savePassword("Dan_Kuso_77", "n934hg0284hyq809ewrhjg");
            boolean success4 = passwordManager.savePassword("JulieISCOOL", "n934hg0284hyq809ewrhjg");
            boolean success5 = passwordManager.savePassword("Marty269", "n934hg0284hyq809ewrhjg");
        }
        // Catch and handle errors
        catch (KeyStoreException | CertificateException | NoSuchAlgorithmException |
               IOException | NoSuchPaddingException | InvalidKeyException |
               InvalidAlgorithmParameterException | NoSuchProviderException |
               UnrecoverableKeyException | IllegalBlockSizeException | BadPaddingException e)
        {
            // Show an error message to the user
            Toast.makeText(MainActivity.this, "Error occurred: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

}   // end MainActivity class