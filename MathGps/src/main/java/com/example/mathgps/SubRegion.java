package com.example.mathgps;

import static com.example.mathgps.RegionManager.isRegionWithin30Meters;
import static com.example.mathgps.RegionManager.isWithin5MetersOfAnyRegion;
import static com.example.mathgps.RegionManager.regionQueue;

import com.google.android.gms.maps.model.LatLng;

public class SubRegion extends Region{
    private Region mainRegion; // LatLng representa a localização da região principal

    public SubRegion(Region mainRegion) {
        super();
        this.mainRegion = mainRegion;
    }

    public static void addSubRegion(int user, LatLng newRegion) {
        Thread addSubRegionThread = new Thread(() -> {
            try {
                if (isRegionWithin30Meters(newRegion)) {
                    if (!isWithin5MetersOfAnyRegion(newRegion)) {
                        String jsonData = JsonUtil.toJson(newRegion);
                        String encryptedData = Cryptography.encrypt(jsonData);
                        regionQueue.add(encryptedData);
                        System.out.println("Sub Região adicionada: " + newRegion.latitude + ", " + newRegion.longitude);
                    } else {
                        System.out.println("Não é possível adicionar SubRegion. Está muito próximo de uma RestrictedRegion/subRegion.");
                    }
                } else {
                    System.out.println("Não é possível adicionar SubRegion. Não está dentro de um raio de 30 metros de uma região existente.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        addSubRegionThread.start();
        try {
            addSubRegionThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    public double calculateDistance(LatLng newRegion) {

        return MathGps.calculateDistance(location.latitude, location.longitude, newRegion.latitude, newRegion.longitude);
    }
}

