package com.example.brunoprojeto;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.calculosgpsandroid.MathGps;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Semaphore;



public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private final int FINE_PERMISSION_CODE = 1;
    private GoogleMap myMap;
    private SearchView mapSearchView;
    private double markerLatitude;
    private double markerLongitude;
    private Handler handler = new Handler();
    private long lastClickTime = 0;
    private RegionManager regionManager;
    private Polyline routeLine;
    private List<LatLng> routePoints = new ArrayList<>();
    FusedLocationProviderClient fusedLocationProviderClient;
    Location currentLocation;

    private Marker currentMarker = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        regionManager = new RegionManager();

        mapSearchView = findViewById(R.id.mapSearch);
        Button btnAddRegion = findViewById(R.id.btnAddRegion);
        btnAddRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewRegion();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();

        mapSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = mapSearchView.getQuery().toString();
                List<Address> addressList = null;
                Geocoder geocoder = new Geocoder(MainActivity.this);

                try {
                    addressList = geocoder.getFromLocationName(location, 1);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                assert addressList != null;
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                myMap.addMarker(new MarkerOptions().position(latLng).title(location));
                myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));

                // Atualizando as variáveis com as coordenadas do novo marcador
                markerLatitude = latLng.latitude;
                markerLongitude = latLng.longitude;

                // Atualizando as labels com as novas coordenadas
                updateMarkerLocationUI();

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    updateLocationUI();
                }
            }
        });
    }

    private void updateLocationUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tvLatitude = findViewById(R.id.tvLatitude);
                TextView tvLongitude = findViewById(R.id.tvLongitude);
                tvLatitude.setText("Latitude: " + currentLocation.getLatitude());
                tvLongitude.setText("Longitude: " + currentLocation.getLongitude());
            }
        });
    }

    private void updateMarkerLocationUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tvLatitude = findViewById(R.id.tvLatitude);
                TextView tvLongitude = findViewById(R.id.tvLongitude);
                tvLatitude.setText("Latitude: " + markerLatitude);
                tvLongitude.setText("Longitude: " + markerLongitude);
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;
        myMap.getUiSettings().setZoomControlsEnabled(true);
        myMap.getUiSettings().setCompassEnabled(true);
        myMap.getUiSettings().setZoomGesturesEnabled(true);
        myMap.getUiSettings().setScrollGesturesEnabled(true);

        myMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                long currentClickTime = System.currentTimeMillis();
                long elapsedTime = currentClickTime - lastClickTime;

                if (elapsedTime < 500) { // 500ms é o intervalo para considerar um duplo clique
                    // Se já existe um marcador, remova-o antes de adicionar um novo

                    if (currentMarker != null) {
                        currentMarker.remove();
                    }

                    // Adiciona um novo marcador na localização clicada
                    currentMarker = myMap.addMarker(new MarkerOptions().position(latLng).title("Novo Marcador"));

                    // Atualiza as labels com as novas coordenadas
                    updateMarkerLocationUI(latLng.latitude, latLng.longitude);

                    Toast.makeText(MainActivity.this, "Marcador adicionado", Toast.LENGTH_SHORT).show();
                }

                lastClickTime = currentClickTime;
            }
        });

    }



    private void updateMarkerLocationUI(double latitude, double longitude) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tvLatitude = findViewById(R.id.tvLatitude);
                TextView tvLongitude = findViewById(R.id.tvLongitude);
                tvLatitude.setText("Latitude: " + latitude);
                tvLongitude.setText("Longitude: " + longitude);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, "Location permission is denied, please allow the permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addNewRegion() {
        // Supondo que você tenha as coordenadas da nova região em markerLatitude e markerLongitude
        LatLng newRegion = new LatLng(markerLatitude, markerLongitude);
        // Supondo que você tenha um código de usuário, por exemplo, userId
        int userId = 1; // Substitua 1 pelo código do usuário real
        regionManager.addNewRegion(userId, newRegion);
    }

}









