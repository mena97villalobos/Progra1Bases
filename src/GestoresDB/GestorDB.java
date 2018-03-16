package GestoresDB;

import javafx.scene.control.Alert;

import java.sql.*;

public class GestorDB {
    private Connection conexion;
    private Statement estado;
    private final String url;
    private final String user;
    private final String password;
    private Connection conecction;
    public static GestorDB gestor;

    public GestorDB(String connectionString, String user, String password) {
        conexion = null;
        estado = null;
        this.url = connectionString;
        this.user = user;
        this.password = password;
        conecction = connect();
    }

    public Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public int validate_user(String user, String password, boolean admin){
        String SQL;
        if(admin)
            SQL = "SELECT * FROM validar_admin (?, ?)";
        else
            SQL = "SELECT * FROM validar_login (?, ?)";
        try {
            PreparedStatement pstmt = conecction.prepareStatement(SQL);
            pstmt.setString(1, user);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            int idUser = 0;
            if(rs.next())
                 idUser = rs.getInt("validar_admin");
            System.out.println(idUser);
            return idUser;
        } catch (SQLException e) {
            invocarAlerta("Error al iniciar sesión", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
        return 0;
    }

    public void crear_admin(String nombre, String alias, String password){
        String SQL = "SELECT * FROM crear_admin(?, ?, ?);";
        try {
            PreparedStatement pstmt = conecction.prepareStatement(SQL);
            pstmt.setString(1, nombre);
            pstmt.setString(2, alias);
            pstmt.setString(3, password);
            pstmt.execute();
            invocarAlerta("Administrador Creado", Alert.AlertType.INFORMATION);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int crear_usuario
            (String cedula, String nombre, String apellido, String alias, String password, String direccion, String correo){
        String SQL = "SELECT * FROM create_user(?, ?, ?, ?, ?, ?, ?);";
        try {
            PreparedStatement pstmt = conecction.prepareStatement(SQL);
            pstmt.setString(1, cedula);
            pstmt.setString(2, nombre);
            pstmt.setString(3, apellido);
            pstmt.setString(4, alias);
            pstmt.setString(5, password);
            pstmt.setString(6, direccion);
            pstmt.setString(7, correo);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next())
                return rs.getInt("create_user");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void crear_telefono(int idUsuario, String tel){
        String SQL = "SELECT * FROM crear_telefono(?, ?);";
        try {
            PreparedStatement pstmt = conecction.prepareStatement(SQL);
            pstmt.setInt(1, idUsuario);
            pstmt.setString(2, tel);
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void invocarAlerta(String mensaje, Alert.AlertType tipo) {
        Alert nuevaAlerta = new Alert(tipo);
        nuevaAlerta.setTitle("");
        nuevaAlerta.setContentText(mensaje);
        nuevaAlerta.showAndWait();
    }

}
