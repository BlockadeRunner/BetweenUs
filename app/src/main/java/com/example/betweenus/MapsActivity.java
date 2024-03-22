//////////////////////////////////////////////////////
//       MapsActivity: Identify Midpoint Page       //
//                                                  //
// Co-Developers: Grishma Baruah and Alex Longo     //
//                                                  //
// Description: This activity handles instantiating //
// a google maps plugin api that allows the user to //
// view their location, their friends location, and //
// the midpoint between the two, so that they can   //
// identify a good place to meet up that will be    //
// equidistant between them. It loads in location   //
// data from the database and handles the           //
// calculations for the midpoint between the two.   //
//////////////////////////////////////////////////////
package com.example.betweenus;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.betweenus.databinding.ActivityMapsBinding;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback
{
    // Declare variables for Google Maps
    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    // Grab the intent for access to currentUser data
    Intent intent;

    // Declare variables for midpoint calculations
    private String currentUser;
    private double currentUserLat;
    private double currentUserLon;
    private String otherUser;
    private double otherUserLat;
    private double otherUserLon;
    private double midPointLat;
    private double midPointLon;

    // Declare variable for accessing database
    private DBHelper DB;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        intent = getIntent();
        currentUser = intent.getStringExtra("CURRENT_USER");
        otherUser = intent.getStringExtra("OTHER_USER");
        DB = new DBHelper(this);

        // Update User Coordinates
        updateAllCoords(currentUser, otherUser);

        // inflate maps api fragment
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where the midpoint, user, and friend's locations are shown.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        // grab map
        mMap = googleMap;

        // Prepare coordinates for each marker
        LatLng curUserLatLng = new LatLng(currentUserLat, currentUserLon);
        LatLng othUserLatLng = new LatLng(otherUserLat, otherUserLon);
        LatLng midPointLatLng = new LatLng(midPointLat, midPointLon);

        // User's Location Marker
        Marker userLocMarker = mMap.addMarker(new MarkerOptions()
                .position(curUserLatLng)
                .title("YOU"));
        // Friend's Locatoin Marker
        Marker otherLocMarker = mMap.addMarker(new MarkerOptions()
                .position(othUserLatLng)
                .title(otherUser));
        // Midpoint Location Marker
        Marker midPointMarker = mMap.addMarker(new MarkerOptions()
                .position(midPointLatLng)
                .title("MIDPOINT"));

        // Label Midpoint Marker on Map
        midPointMarker.showInfoWindow();
        // Zoom to Midpoint
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(midPointLatLng, 9));
    }

    // This method calculates and updates all the coordinates for each location on the map
    private void updateAllCoords(String curUsername, String othUsername)
    {
        // Locations from the database
        User current = DB.getUserByUsername(curUsername);
        User other = DB.getUserByUsername(othUsername);
        String curLoc = current.getLocation();
        String othLoc = other.getLocation();

        // Split the location string at the "B" character
        String[] currentCoords = curLoc.split("B");
        String[] otherCoords = othLoc.split("B");

        // Extract latitude and longitude from coordinates
        String curLat = currentCoords[0];
        String curLon = currentCoords[1];
        String othLat = otherCoords[0];
        String othLon = otherCoords[1];

        // Convert latitude and longitude to double
        currentUserLat = Double.parseDouble(curLat);
        currentUserLon = Double.parseDouble(curLon);
        otherUserLat = Double.parseDouble(othLat);
        otherUserLon = Double.parseDouble(othLon);

        // Calculate midpoint coordinates
        midPointLat = (currentUserLat + otherUserLat) / 2;
        midPointLon = (currentUserLon + otherUserLon) / 2;
    }
}