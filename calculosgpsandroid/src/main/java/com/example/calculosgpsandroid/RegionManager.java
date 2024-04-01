package com.example.calculosgpsandroid;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

//Neste código, a classe RegionManager agora inclui campos para o código do usuário (user) e o timestamp (timestamp).
// O código do usuário é atualizado cada vez que uma nova região é adicionada, e o timestamp é atualizado para o momento
// atual em nanosegundos usando System.nanoTime().

public class RegionManager {
    private Queue<LatLng> regionQueue = new LinkedList<>();
    private Semaphore semaphore = new Semaphore(1);

    // Adicionando campos para código do usuário e timestamp
    private int user;
    private long timestamp;

    public void addNewRegion(int user, LatLng newRegion) {
        this.user = user; // Atualiza o código do usuário
        this.timestamp = System.nanoTime(); // Atualiza o timestamp

        new Thread(() -> {
            try {
                semaphore.acquire();
                if (!isRegionWithin30Meters(newRegion)) {
                    regionQueue.add(newRegion);
                    System.out.println("Nova região adicionada: " + newRegion.latitude + ", " + newRegion.longitude);
                } else {
                    System.out.println("Nova região não adicionada. Está dentro de 30 metros de uma região existente.");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                semaphore.release();
            }
        }).start();
    }

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
