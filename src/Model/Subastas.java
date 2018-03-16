package Model;

import javafx.beans.property.SimpleStringProperty;

public class Subastas {
    private SimpleStringProperty id;
    private SimpleStringProperty vendedor;
    private SimpleStringProperty fechaFin;

    public Subastas(String _id, String _vendedor, String _fechaFin){
        this.id = new SimpleStringProperty(_id);
        this.vendedor = new SimpleStringProperty(_vendedor);
        this.fechaFin = new SimpleStringProperty(_fechaFin);
    }

    public String getId() {
        return id.get();
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public String getVendedor() {
        return vendedor.get();
    }

    public void setVendedor(String vendedor) {
        this.vendedor.set(vendedor);
    }

    public String getFechaFin() {
        return fechaFin.get();
    }

    public void setFechaFin(String fechaFin) {
        this.fechaFin.set(fechaFin);
    }
}
