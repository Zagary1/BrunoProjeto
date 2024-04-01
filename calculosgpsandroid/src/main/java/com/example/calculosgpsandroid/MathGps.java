package com.example.calculosgpsandroid;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.LinkedList;
import java.util.Queue;

public class MathGps {
    private Queue<LatLng> regionQueue = new LinkedList<>();
    private boolean isRegionWithin30Meters(LatLng newRegion) {
        for (LatLng existingRegion : regionQueue) {
            double distance = calculateDistance(newRegion, existingRegion);
            if (distance <= 0.000269) { // Verifica se a distância é menor ou igual a 30 metros
                return true;
            }
        }
        return false;
    }

    public static double calculateDistance(LatLng start, LatLng end) {
        try {
            Location location1 = new Location("locationA");
            location1.setLatitude(start.latitude);
            location1.setLongitude(start.longitude);
            Location location2 = new Location("locationB");
            location2.setLatitude(end.latitude);
            location2.setLongitude(end.longitude);
            return location1.distanceTo(location2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
