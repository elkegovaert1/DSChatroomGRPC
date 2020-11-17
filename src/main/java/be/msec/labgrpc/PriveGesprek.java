package be.msec.labgrpc;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class PriveGesprek {
    private ObservableList<String> berichten;
    private String partner;
    private ChatroomClient client;

    public PriveGesprek(ChatroomClient c, String p) {
        client = c;
        partner = p;
        berichten = FXCollections.observableArrayList();
    }
    public String toString() {
        return partner;
    }
    public ObservableList<String> getBerichten() {
        return berichten;
    }
    public void setBerichten(ObservableList<String> berichten) {
        this.berichten = berichten;
    }
    public String getPartner() {
        return partner;
    }
    public void setPartner(String partner) {
        this.partner = partner;
    }
    public ChatroomClient getClient() {
        return client;
    }
    public void setClient(ChatroomClient client) {
        this.client = client;
    }
    public void addBericht(String s) {
        berichten.add(s);
    }
}
