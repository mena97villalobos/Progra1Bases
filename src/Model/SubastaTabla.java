package Model;

public class SubastaTabla {
    public int idItem;
    public int idVendedor;
    public float precio;
    public String fechaFin;
    public String detallesEntrega;

    public SubastaTabla(int idItem, int idVendedor, float precio, String fechaFin, String detallesEntrega) {
        this.idItem = idItem;
        this.idVendedor = idVendedor;
        this.precio = precio;
        this.fechaFin = fechaFin;
        this.detallesEntrega = detallesEntrega;
    }
}
