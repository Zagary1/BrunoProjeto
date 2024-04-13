package com.example.brunoprojeto;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.example.mathgps.Cryptography;
import com.example.mathgps.JsonUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    // Código de permissão para acesso à localização
    private final int FINE_PERMISSION_CODE = 1;
    // Instância do GoogleMap para interagir com o mapa
    private GoogleMap myMap;
    // View para busca de localização no mapa
    private SearchView mapSearchView;
    // Variáveis para armazenar a latitude e longitude do marcador
    private double markerLatitude;
    private double markerLongitude;
    // Gerenciador de regiões para adicionar e gerenciar regiões no mapa
    private RegionManager regionManager;
    // Cliente para acessar a localização do dispositivo
    FusedLocationProviderClient fusedLocationProviderClient;
    // Objeto para armazenar a localização atual do usuário
    Location currentLocation;
    // Instância do Firebase Firestore para interagir com o banco de dados
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<Marker> markerList = new ArrayList<>();
    private List<Circle> circles = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Inicializa o gerenciador de regiões
        regionManager = new RegionManager();
        // Inicializa a SearchView para busca de localização
        mapSearchView = findViewById(R.id.mapSearch);
        // Botão para adicionar uma nova região
        Button btnAddRegion = findViewById(R.id.btnAddRegion);
        btnAddRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewRegion();
            }
        });
        // Botão para salvar regiões no banco de dados
        Button btnSaveToDatabase = findViewById(R.id.btnSaveToDatabase);
        btnSaveToDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveRegionsToDatabase();
            }
        });
        Button btnAddSubRegion = findViewById(R.id.btnAddSubRegion);
        btnAddSubRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSubRegion();
            }
        });

        Button btnAddRestrictedRegion = findViewById(R.id.btnAddRestrictedRegion);
        btnAddRestrictedRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addRestrictedRegion();
            }
        });

        // Inicializa o SupportMapFragment para exibir o mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Inicializa o cliente para acessar a localização do dispositivo
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();

        // Configura o listener para a SearchView
        mapSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            // Busca a localização pelo nome e adiciona um marcador no mapa
            // Atualiza a UI com a nova localização
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

                myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));

                // Atualizando as variáveis com as coordenadas do novo marcador


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

    // Método para obter a última localização do dispositivo
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


    // Método para atualizar a UI com a localização atual do usuário
    private void updateLocationUI() {
        runOnUiThread(new Runnable() { // Atualiza TextViews com a latitude e longitude da localização atual
            @Override
            public void run() {
                TextView tvLatitude = findViewById(R.id.tvLatitude);
                TextView tvLongitude = findViewById(R.id.tvLongitude);
                tvLatitude.setText("Latitude: " + currentLocation.getLatitude());
                tvLongitude.setText("Longitude: " + currentLocation.getLongitude());
            }
        });
    }
    // Método para atualizar a UI com a localização do marcador atual
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
        myMap.getUiSettings().setZoomGesturesEnabled(false);
        myMap.getUiSettings().setScrollGesturesEnabled(true);

        myMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                // Se já existe um marcador, remova-o antes de adicionar um novo
                if (!markerList.isEmpty()) {
                    Marker lastMarker = markerList.get(markerList.size() - 1);
                    lastMarker.remove();
                    markerList.remove(lastMarker);
                }

                // Adiciona um novo marcador na localização clicada
                Marker newMarker = myMap.addMarker(new MarkerOptions().position(latLng).title("Novo Marcador"));
                markerList.add(newMarker); // Adiciona o novo marcador à lista

                // Atualiza as variáveis com as coordenadas do novo marcador
                markerLatitude = latLng.latitude;
                markerLongitude = latLng.longitude;

                // Atualiza a UI com a nova localização
                updateMarkerLocationUI(latLng.latitude, latLng.longitude);

                Toast.makeText(MainActivity.this, "Marcador adicionado", Toast.LENGTH_SHORT).show();
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

    // Método chamado quando o usuário responde à solicitação de permissão
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

    // Método para adicionar uma nova região ao gerenciador de regiões
    private void addNewRegion() {
        // Supondo que você tenha as coordenadas da nova região em markerLatitude e markerLongitude
        LatLng newRegion = new LatLng(markerLatitude, markerLongitude);
        // Supondo que você tenha um código de usuário, por exemplo, userId
        int userId = 1; // Substitua 1 pelo código do usuário real
        regionManager.addNewRegion(userId, newRegion);
        addCircleToMap(newRegion,30,Color.TRANSPARENT);
    }

    private void addSubRegion() {
        // Supondo que você tenha as coordenadas da nova região em markerLatitude e markerLongitude
        LatLng newRegion = new LatLng(markerLatitude, markerLongitude);
        // Supondo que você tenha um código de usuário, por exemplo, userId
        int userId = 1; // Substitua 1 pelo código do usuário real
        SubRegion.addSubRegion(userId,newRegion);

        addCircleToMap(newRegion,5,Color.CYAN);
    }

    private void addRestrictedRegion() {
        // Supondo que você tenha as coordenadas da nova região em markerLatitude e markerLongitude
        LatLng newRegion = new LatLng(markerLatitude, markerLongitude);
        // Supondo que você tenha um código de usuário, por exemplo, userId
        int userId = 1; // Substitua 1 pelo código do usuário real
        RestrictedRegion.addRestrictedRegion(userId,newRegion);

        addCircleToMap(newRegion,5,Color.MAGENTA);
    }



    // Método para salvar regiões no banco de dados Firestore
    private void saveRegionsToDatabase() {
        new Thread(() -> {
            for (String encryptedRegion : regionManager.getRegionQueue()) {
                try {
                    // Descriptografa os dados antes de verificar se a região já existe no Firestore
                    String decryptedData = Cryptography.decrypt(encryptedRegion);
                    LatLng region = JsonUtil.fromJson(decryptedData, LatLng.class);

                    // Verificar se a região já existe no Firestore
                    db.collection("regions")
                            .whereEqualTo("latitude", region.latitude)
                            .whereEqualTo("longitude", region.longitude)
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    if (task.getResult().isEmpty()) {
                                        // A região não existe no Firestore, então adicione-a
                                        Map<String, Object> regionData = new HashMap<>();
                                        regionData.put("encryptedData", encryptedRegion); // Adiciona os dados criptografados ao mapa

                                        db.collection("regions")
                                                .add(regionData)
                                                .addOnSuccessListener(documentReference -> Log.d("Firestore", "DocumentSnapshot added with ID: " + documentReference.getId()))
                                                .addOnFailureListener(e -> Log.w("Firestore", "Error adding document", e));
                                    } else {
                                        Log.d("Firestore", "Region already exists in Firestore");
                                    }
                                } else {
                                    Log.w("Firestore", "Error getting documents.", task.getException());
                                }
                            });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void addCircleToMap(LatLng center, double radius,int fillColor) {
        CircleOptions circleOptions = new CircleOptions()
                .center(center)
                .radius(radius)
                .fillColor(fillColor)
                .strokeColor(Color.BLACK)
                .strokeWidth(2);

        Circle circle = myMap.addCircle(circleOptions);
        circles.add(circle); // Adiciona o círculo à lista de círculos
    }

}










