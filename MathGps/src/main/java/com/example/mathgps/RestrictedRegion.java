package com.example.mathgps;

import com.google.android.gms.maps.model.LatLng;

public class RestrictedRegion extends RegionManager {
    private LatLng mainRegion; // Supondo que LatLng represente a localização da região principal
    private boolean restricted;

    public RestrictedRegion(LatLng mainRegion, boolean restricted) {
        this.mainRegion = mainRegion;
        this.restricted = restricted;
    }

    // Métodos específicos de RestrictedRegion
    public LatLng getMainRegion() {
        return mainRegion;
    }

    public void setMainRegion(LatLng mainRegion) {
        this.mainRegion = mainRegion;
    }

    public boolean isRestricted() {
        return restricted;
    }

    public void setRestricted(boolean restricted) {
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

    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Converte as coordenadas de latitude e longitude para radianos
        lat1 = Math.toRadians(lat1);
        lon1 = Math.toRadians(lon1);
        lat2 = Math.toRadians(lat2);
        lon2 = Math.toRadians(lon2);

        // Distância entre latitudes e longitudes
        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        // Aplica a fórmula de Haversine
        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Raio da Terra em quilômetros
        double R = 6371;

        // Distância em quilômetros
        return R * c;
    }
}

