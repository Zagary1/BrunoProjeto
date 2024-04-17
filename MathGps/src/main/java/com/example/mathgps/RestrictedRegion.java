package com.example.mathgps;

import static com.example.mathgps.RegionManager.isRegionWithin30Meters;
import static com.example.mathgps.RegionManager.isWithin5MetersOfAnyRegion;
import static com.example.mathgps.RegionManager.regionQueue;

import com.google.android.gms.maps.model.LatLng;

public class RestrictedRegion extends Region {
    private Region mainRegion; // Supondo que LatLng represente a localização da região principal
    private boolean restricted;

    public RestrictedRegion(Region mainRegion, boolean restricted) {
        this.mainRegion = mainRegion;
        this.restricted = restricted;
    }
    public static void addRestrictedRegion(int user, LatLng newRegion) {
        Thread addRestrictedRegionThread = new Thread(() -> {
            try {
                if (isRegionWithin30Meters(newRegion)) {
                    if (!isWithin5MetersOfAnyRegion(newRegion)) {
                        String jsonData = JsonUtil.toJson(newRegion);
                        String encryptedData = Cryptography.encrypt(jsonData);
                        regionQueue.add(encryptedData);
                        System.out.println("Região Restrita adicionada: " + newRegion.latitude + ", " + newRegion.longitude);
                    } else {
                        System.out.println("Não é possível adicionar uma Região restrita. Está muito próximo de uma SubRegion/RestrictedRegion.");
                    }
                } else {
                    System.out.println("Não é possível adicionar SubRegion. Não está dentro de um raio de 30 metros de uma região existente.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        addRestrictedRegionThread.start();
        try {
            addRestrictedRegionThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public double calculateDistance(LatLng newRegion) {
        // Implementação específica para RestrictedRegion
        return MathGps.calculateDistance(location.latitude, location.longitude, newRegion.latitude, newRegion.longitude);
    }
}

