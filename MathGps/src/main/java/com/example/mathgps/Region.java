package com.example.mathgps;

import static com.example.mathgps.RegionManager.regionQueue;

import com.google.android.gms.maps.model.LatLng;

public class Region extends RegionManager {
    protected LatLng location;

    public Region(LatLng location) {
        this.location = location;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
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
    public static boolean isRegionWithin30Meters(LatLng newRegion) {
        for (String encryptedRegion : regionQueue) {
            try {
                // Descriptografa os dados antes de comparar
                String decryptedData = com.example.mathgps.Cryptography.decrypt(encryptedRegion);
                LatLng existingRegion = com.example.mathgps.JsonUtil.fromJson(decryptedData, LatLng.class);

                // Calcula a distância entre a nova região e uma região existente
                double distance = MathGps.calculateDistance(newRegion.latitude, newRegion.longitude, existingRegion.latitude, existingRegion.longitude);

                // Verifica se a distância é menor ou igual a 30 metros
                if (distance <= 0.03) {
                    return true; // Retorna true se a nova região estiver dentro de 30 metros de uma região existente
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false; // Retorna false se a nova região não estiver dentro de 30 metros de nenhuma região existente
    }
    public static boolean isWithin5MetersOfAnyRegion(LatLng newRegion) {
        for (String encryptedRegion : regionQueue) {
            try {
                // Descriptografa os dados antes de comparar
                String decryptedData = Cryptography.decrypt(encryptedRegion);
                LatLng existingRegion = JsonUtil.fromJson(decryptedData, LatLng.class);

                // Calcula a distância entre a nova região e uma região existente
                double distance = MathGps.calculateDistance(newRegion.latitude, newRegion.longitude, existingRegion.latitude, existingRegion.longitude);

                // Verifica se a distância é menor ou igual a 5 metros
                if (distance <= 0.005) { // 0.005 km é aproximadamente 5 metros
                    return true; // Retorna true se a nova região estiver dentro de 5 metros de uma região existente
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false; // Retorna false se a nova região não estiver dentro de 5 metros de nenhuma região existente
    }
}
