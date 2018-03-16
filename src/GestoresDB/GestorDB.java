package GestoresDB;

import Model.VariablesSistema;
import javafx.scene.control.Alert;

import java.io.*;
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
            pstmt.close();
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
            pstmt.close();
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
            pstmt.close();
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
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update_variables(float incremento, float porcentaje, File image){
        String SQL = "SELECT * FROM update_variables(?, ?, ?);";
        try {
            FileInputStream fis = new FileInputStream(image);
            PreparedStatement pstmt = conecction.prepareStatement(SQL);
            pstmt.setFloat(1, incremento);
            pstmt.setFloat(2, porcentaje);
            pstmt.setBinaryStream(3, fis, image.length());
            pstmt.execute();
            pstmt.close();
            fis.close();
            invocarAlerta("Variables Actualizadas", Alert.AlertType.INFORMATION);
        }
        catch(FileNotFoundException e){
            invocarAlerta("Archivo no encontrado", Alert.AlertType.ERROR);
        }
        catch (SQLException e){
            e.printStackTrace();
            invocarAlerta("Error al actualizar", Alert.AlertType.ERROR);
        }
        catch (IOException e){
            invocarAlerta("Error de Archivo", Alert.AlertType.ERROR);
        }
    }

    public VariablesSistema read_variables(){
        String SQL = "SELECT * FROM read_variables();";
        try {
            PreparedStatement ps = conecction.prepareStatement(SQL);

            ResultSet rs = ps.executeQuery();
            VariablesSistema vs = null;
            if (rs.next()) {
                byte[] imgBytes = rs.getBytes("image");
                float inc = rs.getFloat("incr");
                float percent = rs.getFloat("percent");
                InputStream i = new ByteArrayInputStream(imgBytes);
                rs.close();
                vs = new VariablesSistema(inc, percent, i);
            }
            ps.close();
            return vs;
        }
        catch (SQLException e){
            e.printStackTrace();
            invocarAlerta("Error SQL", Alert.AlertType.ERROR);
        }
        return null;
    }

    public void invocarAlerta(String mensaje, Alert.AlertType tipo) {
        Alert nuevaAlerta = new Alert(tipo);
        nuevaAlerta.setTitle("");
        nuevaAlerta.setContentText(mensaje);
        nuevaAlerta.showAndWait();
    }

}
