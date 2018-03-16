package Model;

import javafx.beans.property.SimpleStringProperty;

public class Persona {
    SimpleStringProperty id;
    SimpleStringProperty nombre;
    SimpleStringProperty alias;

    public Persona(int _id, String _nombre, String _alias){
        this.id = new SimpleStringProperty(Integer.toString(_id));
        this.nombre = new SimpleStringProperty(_nombre);
        this.alias = new SimpleStringProperty(_alias);
    }

    public String getId() {
        return id.get();
    }

    public String getNombre() {
        return nombre.get();
    }

    public String getAlias() {
        return alias.get();
    }
}
