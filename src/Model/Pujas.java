package Model;

import javafx.beans.property.SimpleStringProperty;

public class Pujas {
    private SimpleStringProperty id;
    private SimpleStringProperty comprador;
    private SimpleStringProperty monto;
    private SimpleStringProperty fecha;

    public Pujas(String id, String comprador, String monto, String fecha){
        this.id = new SimpleStringProperty(id);
        this.comprador = new SimpleStringProperty(comprador);
        this.monto = new SimpleStringProperty(monto);
        this.fecha = new SimpleStringProperty(fecha);
    }

    public String getId() {
        return id.get();
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public String getComprador() {
        return comprador.get();
    }

    public void setComprador(String comprador) {
        this.comprador.set(comprador);
    }

    public String getMonto() {
        return monto.get();
    }

    public void setMonto(String monto) {
        this.monto.set(monto);
    }

    public String getFecha() {
        return fecha.get();
    }

    public void setFecha(String fecha) {
        this.fecha.set(fecha);
    }
}
