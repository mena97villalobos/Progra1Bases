package Model;

import javafx.beans.property.SimpleStringProperty;

import java.io.File;

public class Subastas {
    private SimpleStringProperty id;
    private SimpleStringProperty vendedor;
    private SimpleStringProperty fechaFin;
    private SimpleStringProperty monto;
    private SimpleStringProperty detallesItem;
    private SimpleStringProperty montoFinal;
    private SimpleStringProperty comentarioVendedor;
    private SimpleStringProperty comentarioComprador;

    public Subastas(String _id, String _vendedor, String _fechaFin, String _monto){
        this.id = new SimpleStringProperty(_id);
        this.vendedor = new SimpleStringProperty(_vendedor);
        this.fechaFin = new SimpleStringProperty(_fechaFin);
        this.monto = new SimpleStringProperty(_monto);
    }

    public Subastas(String id, String vendedor, String fechaFin, String monto, String montoFinal,
                    String comentarioVendedor, String comentarioComprador) {
        this.id = new SimpleStringProperty(id);
        this.vendedor = new SimpleStringProperty(vendedor);
        this.fechaFin = new SimpleStringProperty(fechaFin);
        this.monto = new SimpleStringProperty(monto);
        this.montoFinal = new SimpleStringProperty(montoFinal);
        this.comentarioVendedor = new SimpleStringProperty(comentarioVendedor);
        this.comentarioComprador = new SimpleStringProperty(comentarioComprador);
    }

    public Subastas(String id, String vendedor, String fechaFin,
                    String monto, String detallesItem) {
        this.id = new SimpleStringProperty(id);
        this.vendedor = new SimpleStringProperty(vendedor);
        this.fechaFin = new SimpleStringProperty(fechaFin);
        this.monto = new SimpleStringProperty(monto);
        this.detallesItem = new SimpleStringProperty(detallesItem);
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

    public String getMonto() {
        return monto.get();
    }

    public void setMonto(String monto) {
        this.monto.set(monto);
    }

    public String getDetallesItem() {
        return detallesItem.get();
    }
}
