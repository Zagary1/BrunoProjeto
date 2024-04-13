package com.example.mathgps;

import com.example.mathgps.Cryptography;
import com.example.mathgps.JsonUtil;
import com.example.mathgps.MathGps;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;



//Neste código, a classe RegionManager agora inclui campos para o código do usuário (user) e o timestamp (timestamp).
// O código do usuário é atualizado cada vez que uma nova região é adicionada, e o timestamp é atualizado para o momento
// atual em nanosegundos usando System.nanoTime().

public class RegionManager {
    // Fila para armazenar as regiões adicionadas, usando LinkedList para implementar a interface Queue
    static Queue<String> regionQueue = new LinkedList<>();

    // Semáforo para controlar o acesso à fila de regiões, garantindo que apenas uma thread possa modificar a fila de cada vez
    private Semaphore semaphore = new Semaphore(1);

    // Instância do Firebase Firestore para interagir com o banco de dados
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Adicionando campos para código do usuário e timestamp
    private int user;
    private long timestamp;
    protected LatLng location;


    // Método para adicionar uma nova região à fila
    public void addNewRegion(int user, LatLng newRegion) {
        this.user = user; // Atualiza o código do usuário
        this.timestamp = System.nanoTime(); // Atualiza o timestamp

        // Inicia uma nova thread para adicionar a região à fila
        Thread addRegionThread = new Thread(() -> {
            try {
                // Aguarda a permissão do semáforo para acessar a fila
                semaphore.acquire();
                // Verifica se a nova região está dentro de 30 metros de uma região existente
                if (!isRegionWithin30Meters(newRegion)) {
                    // Se não estiver, adiciona a nova região à fila
                    // Criptografa os dados antes de adicionar à fila
                    String encryptedData = com.example.mathgps.Cryptography.encrypt(com.example.mathgps.JsonUtil.toJson(newRegion));
                    regionQueue.add(encryptedData);
                    System.out.println("Nova região adicionada: " + newRegion.latitude + ", " + newRegion.longitude);
                } else {
                    System.out.println("Nova região não adicionada. Está dentro de um raio 30 metros de uma região existente.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // Libera a permissão do semáforo após a operação
                semaphore.release();
            }
        });
        addRegionThread.start();
        try {
            addRegionThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    // Método para verificar se uma nova região está dentro de 30 metros de uma região existente
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


    public Queue<String> getRegionQueue() {
        return regionQueue;
    }

    public LatLng getLatLng() {
        return this.location;
    }

}
