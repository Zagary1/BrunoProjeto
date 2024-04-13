package com.example.mathgps;

public class MathGps {
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
