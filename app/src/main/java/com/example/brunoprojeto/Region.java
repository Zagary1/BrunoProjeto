package com.example.brunoprojeto;

import com.example.mathgps.MathGps;
import com.google.android.gms.maps.model.LatLng;

    public class Region {

        protected String name;
        protected double latitude;
        protected double longitude;
        protected int user;
        protected long timestamp;
        protected LatLng location;

        public Region() {
        }

        // Construtor da classe
        public Region(String name, double latitude, double longitude, int usuario, long timestamp,LatLng location) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
            this.user = usuario;
            this.timestamp = timestamp;
            this.location = location;
        }



        // MÉTODOS GETS //

        // Método getter para obter o nome da região
        public String getNome() {
            return name;
        }

        // Método getter para obter a latitude da região
        public double getLatitude() {
            return latitude;
        }

        // Método getter para obter a longitude da região
        public double getLongitude() {
            return longitude;
        }

        // Método getter para obter o número de usuário
        public int getUsuario() {
            return user;
        }/////////////

        // Método getter para obter o timeStamp
        public long getTimestamp() {
            return timestamp;
        }

        public Region getRegiaoPrincipal() {
            return null;
        }


        // MÉTODOS SETS //

        // Método setter para atualizar o nome da região
        public void setNome(String name) {
            this.name = name;
        }

        // Método setter para atualizar a latitude da região
        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        // Método setter para atualizar a longitude da região
        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        // Método setter para atualizar o numero de usuário
        public void setUsuario(int usuario) {
            this.user = usuario;
        }

        // Método setter para atualizar o timeStamp
        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public void setRegiaoPrincipal(Region regiaoMaisProxima) {
        }

        public double calculateDistance(LatLng newRegion) {
            // Implementação específica para SubRegion
            return MathGps.calculateDistance(location.latitude, location.longitude, newRegion.latitude, newRegion.longitude);
        }
    }

