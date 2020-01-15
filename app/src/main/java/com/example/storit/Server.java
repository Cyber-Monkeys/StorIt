package com.example.storit;

public class Server {

    String serverName;
    double storageAmount;

    public Server(String serverName, double storageAmount) {
        this.serverName = serverName;
        this.storageAmount = storageAmount;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public double getStorageAmount() {
        return storageAmount;
    }

    public void setStorageAmount(double storageAmount) {
        this.storageAmount = storageAmount;
    }
}
