package Model;

import javafx.beans.property.SimpleStringProperty;
import java.io.InputStream;

public class Subastas {
    private SimpleStringProperty id;
    private SimpleStringProperty vendedor;
    private SimpleStringProperty fechaFin;
    private SimpleStringProperty monto;
    private SimpleStringProperty detallesItem;
    private SimpleStringProperty montoFinal;
    private SimpleStringProperty detEntrega;
    private SimpleStringProperty incrMin;
    private SimpleStringProperty comentarioVendedor;
    private SimpleStringProperty comentarioComprador;
    private SimpleStringProperty calificacionUsuario;
    public InputStream imagen;

    public Subastas(String id, String vendedor, String fechaFin, String detallesItem, String montoFinal,
                    String detEntrega, String incrMin, InputStream imagen) {
        this.id = new SimpleStringProperty(id);
        this.vendedor = new SimpleStringProperty(vendedor);
        this.fechaFin = new SimpleStringProperty(fechaFin);
        this.detallesItem = new SimpleStringProperty(detallesItem);
        this.montoFinal = new SimpleStringProperty(montoFinal);
        this.detEntrega = new SimpleStringProperty(detEntrega);
        this.incrMin = new SimpleStringProperty(incrMin);
        this.imagen = imagen;
    }

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

    public Subastas(int id, String fechaFin, String monto, String calificacion, String alias){
        this.id = new SimpleStringProperty(String.valueOf(id));
        this.fechaFin = new SimpleStringProperty(fechaFin);
        this.monto = new SimpleStringProperty(monto);
        this.calificacionUsuario = new SimpleStringProperty(calificacion);
        this.vendedor = new SimpleStringProperty(alias);
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

    public String getFechaFin() {
        return fechaFin.get();
    }

    public void setFechaFin(String fechaFin) {
        this.fechaFin.set(fechaFin);
    }

    public String getMonto() {
        return monto.get();
    }

    public String getDetallesItem() {
        return detallesItem.get();
    }

    public String getMontoFinal() {
        return montoFinal.get();
    }

    public String getDetEntrega() {
        return detEntrega.get();
    }

    public String getIncrMin() {
        return incrMin.get();
    }

    public String getComentarioVendedor() {
        return comentarioVendedor.get();
    }

    public String getComentarioComprador() {
        return comentarioComprador.get();
    }

    public String getCalificacionUsuario() {
        return calificacionUsuario.get();
    }
}
