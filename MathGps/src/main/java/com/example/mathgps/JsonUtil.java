package com.example.mathgps;

import com.google.gson.Gson;

public class JsonUtil {
    // Instância do Gson para realizar a serialização e deserialização
    private static final Gson gson = new Gson();

    // Método para serializar um objeto Java para uma String JSON
    public static String toJson(Object obj) {
        //converte um objeto Java em uma String JSON.
        return gson.toJson(obj);
    }

    // Método para deserializar uma String JSON para um objeto Java
    public static <T> T fromJson(String json, Class<T> classOfT) {
        //Converte uma String JSON de volta para um objeto Java.
        return gson.fromJson(json, classOfT);
    }
}

