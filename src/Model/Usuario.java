package Model;

public class Usuario extends Persona {
    public String apellido;
    public String cedula;
    public String direccion;
    public String correo;
    public String calificacion;

    public Usuario(int _id, String _nombre, String _apellido, String _cedula, String _alias, String _correo, String _direccion, String _calificacion) {
        super(_id, _nombre, _alias);
        this.apellido = _apellido;
        this.cedula = _cedula;
        this.direccion = _direccion;
        this.correo = _correo;
        this.calificacion = _calificacion;
    }
}
