package Model;

import java.io.File;

public class Item {
    public int categoria;
    public String descripcion;
    public File imagen;

    public Item(int _categoria, String _descripcion, File _imagen){
        this.categoria = _categoria;
        this.descripcion = _descripcion;
        this.imagen = _imagen;
    }
}
