package GestoresDB;

import Model.*;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;

public class GestorDB {
    private final String url;
    private String user;
    private String password;
    private Connection connection;
    public static GestorDB gestor;

    public GestorDB(String connectionString, String user, String password) {
        this.url = connectionString;
        //TODO Cambiar usuarios
        this.user = "postgres";
        this.password = "9545";
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
        connection = connect();
        if(admin)
            SQL = "SELECT * FROM validar_admin (?, ?)";
        else
            SQL = "SELECT * FROM validar_login (?, ?)";
        try {
            PreparedStatement pstmt = connection.prepareStatement(SQL);
            pstmt.setString(1, user);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            int idUser = 0;
            if(rs.next())
                 idUser = rs.getInt(1);
            pstmt.close();
            connection.close();
            if(idUser != 0) {
                if (admin) {
                    this.user = "postgres";
                    connection = connect();
                } else {
                    this.user = "postgres";
                    connection = connect();
                }
            }
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
            PreparedStatement pstmt = connection.prepareStatement(SQL);
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
            PreparedStatement pstmt = connection.prepareStatement(SQL);
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
            PreparedStatement pstmt = connection.prepareStatement(SQL);
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
            PreparedStatement pstmt = connection.prepareStatement(SQL);
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
            PreparedStatement ps = connection.prepareStatement(SQL);

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

    public ArrayList<Persona> read_users_admins(boolean admin){
        String SQL;
        if(admin)
            SQL = "SELECT * FROM read_admins();";
        else
            SQL = "SELECT * FROM read_users();";
        try {
            ArrayList<Persona> arrayPersonas = new ArrayList<>();
            PreparedStatement ps = connection.prepareStatement(SQL);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                String alias = rs.getString("alias");
                Persona p = new Persona(id, nombre, alias);
                arrayPersonas.add(p);
            }
            return arrayPersonas;
        } catch (SQLException e) {
            e.printStackTrace();
            invocarAlerta("Error al recuperar datos", Alert.AlertType.ERROR);
        }
        return null;
    }

    public Usuario get_user_info(int id){
        String SQL = "SELECT * FROM get_user_info(?);";
        try {
            Usuario user = null;
            PreparedStatement ps = connection.prepareStatement(SQL);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                int _id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                String apellido = rs.getString("apellido");
                String cedula = rs.getString("cedula");
                String alias = rs.getString("alias");
                String direccion = rs.getString("direccion");
                String correo = rs.getString("correo");
                String calificacion = rs.getString("calificacion");
                user = new Usuario(_id, nombre, apellido, cedula, alias, correo, direccion, calificacion);
            }
            else{
                invocarAlerta("Error al recuperar información", Alert.AlertType.ERROR);
            }
            return user;
        } catch (SQLException e) {
            e.printStackTrace();
            invocarAlerta("Error al recuperar datos", Alert.AlertType.ERROR);
        }
        return null;
    }

    public void update_password(int id, String password, boolean isAdmin){
        String SQL = "SELECT * FROM update_password(?, ?, ?);";
        try {
            PreparedStatement pstmt = connection.prepareStatement(SQL);
            pstmt.setInt(1, id);
            pstmt.setString(2, password);
            pstmt.setBoolean(3, isAdmin);
            pstmt.execute();
            pstmt.close();
            invocarAlerta("Password para " + String.valueOf(id) + " ha cambiado", Alert.AlertType.INFORMATION);
        }
        catch (SQLException e){
            e.printStackTrace();
            invocarAlerta("Error al actualizar", Alert.AlertType.ERROR);
        }
    }

    public void update_admin(int id, String nombre, String alias){
        String SQL = "SELECT * FROM update_admin(?, ?, ?);";
        try {
            PreparedStatement pstmt = connection.prepareStatement(SQL);
            pstmt.setInt(1, id);
            pstmt.setString(2, nombre);
            pstmt.setString(3, alias);
            pstmt.execute();
            pstmt.close();
            invocarAlerta("Datos de: " + String.valueOf(id) + " actualizados", Alert.AlertType.INFORMATION);
        }
        catch (SQLException e){
            e.printStackTrace();
            invocarAlerta("Error al actualizar", Alert.AlertType.ERROR);
        }
    }

    public void update_usuario(Usuario usuario){
        String SQL = "SELECT * FROM update_usuario(?, ?, ?, ?, ?, ?, ?);";
        try {
            PreparedStatement pstmt = connection.prepareStatement(SQL);
            pstmt.setInt(1, Integer.valueOf(usuario.getId()));
            pstmt.setString(3, usuario.getNombre());
            pstmt.setString(5, usuario.getAlias());
            pstmt.setString(2, usuario.cedula);
            pstmt.setString(4, usuario.apellido);
            pstmt.setString(6, usuario.direccion);
            pstmt.setString(7, usuario.correo);
            pstmt.execute();
            pstmt.close();
            invocarAlerta("Datos de: " + usuario.getId() + " actualizados", Alert.AlertType.INFORMATION);
        }
        catch (SQLException e){
            e.printStackTrace();
            invocarAlerta("Error al actualizar", Alert.AlertType.ERROR);
        }
    }

    public ArrayList<String> read_categoria_primaria(){
        String SQL = "SELECT * FROM read_categoria_primaria()";
        try {
            ArrayList<String> arrayPersonas = new ArrayList<>();
            PreparedStatement ps = connection.prepareStatement(SQL);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                String nombre = rs.getString("categoria_primaria");
                arrayPersonas.add(nombre);
            }
            return arrayPersonas;
        } catch (SQLException e) {
            e.printStackTrace();
            invocarAlerta("Error al recuperar datos", Alert.AlertType.ERROR);
        }
        return null;
    }

    public ArrayList<String> read_categoria_secundaria(String primaria){
        String SQL = "SELECT * FROM read_categoria_secundaria(?)";
        try {
            ArrayList<String> arrayPersonas = new ArrayList<>();
            PreparedStatement ps = connection.prepareStatement(SQL);
            ps.setString(1, primaria);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                String nombre = rs.getString("categoria_secundaria");
                arrayPersonas.add(nombre);
            }
            return arrayPersonas;
        } catch (SQLException e) {
            e.printStackTrace();
            invocarAlerta("Error al recuperar datos", Alert.AlertType.ERROR);
        }
        return null;
    }

    public ArrayList<Subastas> read_subastas(String primaria, String secundaria, boolean filtro){
        String SQL = "SELECT * FROM read_subastas(?, ?, ?);";
        try {
            ArrayList<Subastas> subastas = new ArrayList<>();
            Subastas subasta;
            PreparedStatement ps = connection.prepareStatement(SQL);
            ps.setString(1, primaria);
            ps.setString(2, secundaria);
            ps.setBoolean(3, filtro);
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                String id = String.valueOf(rs.getInt("id"));
                String vendedor = rs.getString("nombreVendedor");
                String fechaFin = rs.getString("fechaFin");
                subasta = new Subastas(id, vendedor, fechaFin, "");
                subastas.add(subasta);
            }
            return subastas;
        } catch (SQLException e) {
            e.printStackTrace();
            invocarAlerta("Error al recuperar datos", Alert.AlertType.ERROR);
        }
        return null;
    }

    public ArrayList<Subastas> hist_subastas(String idUser, String SQL){
        try {
            ArrayList<Subastas> subastas = new ArrayList<>();
            Subastas subasta;
            PreparedStatement ps = connection.prepareStatement(SQL);
            ps.setInt(1, Integer.valueOf(idUser));
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                String id = String.valueOf(rs.getInt("idSubasta"));
                String vendedor = rs.getString("alias");
                String fechaFin = rs.getString("fecha_Fin");
                String inicial = String.valueOf(rs.getFloat("inicial"));
                String actual = String.valueOf(rs.getFloat("actual"));
                String commentarioV = rs.getString("comentarioVendedor");
                String comentarioC = rs.getString("comentarioComprador");
                subasta = new Subastas(id, vendedor, fechaFin, inicial, actual, commentarioV, comentarioC);
                subastas.add(subasta);
            }
            return subastas;
        } catch (SQLException e) {
            e.printStackTrace();
            invocarAlerta("Error al recuperar datos", Alert.AlertType.ERROR);
        }
        return null;
    }

    public ArrayList<Pujas> hist_pujas(String idSubasta){
        String SQL = "SELECT * FROM historial_pujas(?);";
        try {
            ArrayList<Pujas> pujas = new ArrayList<>();
            Pujas puja;
            PreparedStatement ps = connection.prepareStatement(SQL);
            ps.setInt(1, Integer.parseInt(idSubasta));
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                String id = String.valueOf(rs.getInt("id"));
                String comprador = rs.getString("comprador");
                String fechaFin = rs.getString("fecha");
                String monto = String.valueOf(rs.getFloat("monto"));
                puja = new Pujas(id, comprador, monto, fechaFin);
                pujas.add(puja);
            }
            return pujas;
        } catch (SQLException e) {
            e.printStackTrace();
            invocarAlerta("Error al recuperar datos", Alert.AlertType.ERROR);
        }
        return null;
    }

    public void invocarAlerta(String mensaje, Alert.AlertType tipo) {
        Alert nuevaAlerta = new Alert(tipo);
        nuevaAlerta.setTitle("");
        nuevaAlerta.setContentText(mensaje);
        nuevaAlerta.showAndWait();
    }

    public int get_id_categoria(String primaria, String secundaria){
        String SQL = "SELECT * FROM get_id_categoria(?, ?);";
        try {
            PreparedStatement pstmt = connection.prepareStatement(SQL);
            pstmt.setString(1, primaria);
            pstmt.setString(2, secundaria);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int crear_item(int idCategoria, String desc, File imagen){
        String SQL = "SELECT * FROM crear_item(?, ?, ?);";
        try {
            FileInputStream fis = new FileInputStream(imagen);
            PreparedStatement pstmt = connection.prepareStatement(SQL);
            pstmt.setString(1, desc);
            pstmt.setInt(2, idCategoria);
            pstmt.setBinaryStream(3, fis, imagen.length());
            ResultSet rs = pstmt.executeQuery();
            fis.close();
            invocarAlerta("Item insertado", Alert.AlertType.INFORMATION);
            if(rs.next())
                return rs.getInt(1);
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
        return 0;
    }

    public int crear_subasta(int idVendedor, int idItem, float precio, Timestamp ts, String dEntrega){
        String SQL = "SELECT * FROM crear_subasta(?, ?, ?, ?, ?);";
        try {
            PreparedStatement pstmt = connection.prepareStatement(SQL);
            pstmt.setInt(1, idVendedor);
            pstmt.setInt(2, idItem);
            pstmt.setFloat(3, precio);
            pstmt.setTimestamp(4, ts);
            pstmt.setString(5, dEntrega);
            ResultSet rs = pstmt.executeQuery();
            invocarAlerta("Subasta Creada", Alert.AlertType.INFORMATION);
            if(rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public ArrayList<Subastas> read_subastas_usuario(int idCategoria, boolean filtrar){
        String SQL = "SELECT * FROM read_subastas_usuario(?, ?);";
        try {
            ArrayList<Subastas> subastas = new ArrayList<>();
            Subastas subasta;
            PreparedStatement ps = connection.prepareStatement(SQL);
            ps.setInt(1, idCategoria);
            ps.setBoolean(2, filtrar);
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                String id = String.valueOf(rs.getInt("id"));
                String vendedor = rs.getString("alias");
                String fechaFin = rs.getString("fecha_Fin");
                String descripcionItem = rs.getString("detallesItem");
                subasta = new Subastas(id, vendedor, fechaFin, "", descripcionItem);
                subastas.add(subasta);
            }
            return subastas;
        } catch (SQLException e) {
            e.printStackTrace();
            invocarAlerta("Error al recuperar datos", Alert.AlertType.ERROR);
        }
        return null;
    }

    public Subastas read_subastas_item(int idSubasta){
        String SQL = "SELECT * FROM read_subasta_item(?);";
        try {
            Subastas subasta = null;
            PreparedStatement ps = connection.prepareStatement(SQL);
            ps.setInt(1, idSubasta);
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                String id = String.valueOf(rs.getInt("id"));
                String vendedor = rs.getString("alias");
                String pujaActual = String.valueOf(rs.getFloat("pujaActual"));
                String detEntrega = rs.getString("detEntrega");
                String descripcionItem = rs.getString("descrItem");
                String fechaFin = rs.getString("fechaFin");
                String incrMin = rs.getString("incrMin");
                byte[] imgBytes = rs.getBytes("imagen");
                InputStream i = new ByteArrayInputStream(imgBytes);
                subasta = new Subastas(id, vendedor, fechaFin, descripcionItem, pujaActual, detEntrega, incrMin, i);
            }
            return subasta;
        } catch (SQLException e) {
            e.printStackTrace();
            invocarAlerta("Error al recuperar datos", Alert.AlertType.ERROR);
        }
        return null;
    }

    public int pujar(int idSubasta, int idComprador, float monto){
        String SQL = "SELECT * FROM crear_puja(?, ?, ?)";
        try {
            PreparedStatement pstmt = connection.prepareStatement(SQL);
            pstmt.setInt(1, idSubasta);
            pstmt.setInt(2, idComprador);
            pstmt.setFloat(3, monto);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            invocarAlerta("Error al pujar", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
        return 0;
    }

    public ArrayList<Subastas> get_subastas_usuario_ganadas(int idUser, boolean ganadas){
        String SQL;
        if(ganadas)
            SQL = "SELECT * FROM get_subastas_ganadas(?);";
        else
            SQL = "SELECT * FROM get_subastas_usuario(?);";
        try {
            ArrayList<Subastas> subastas = new ArrayList<>();
            Subastas subasta;
            PreparedStatement ps = connection.prepareStatement(SQL);
            ps.setInt(1, idUser);
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                int id = rs.getInt("id");
                String fechaFin = rs.getString("fechafin");
                String pujaActual = String.valueOf(rs.getFloat("montoActual"));
                String calificacion = rs.getString("calificacion");
                String vendedor = rs.getString("alias");
                subasta = new Subastas(id, fechaFin, pujaActual, calificacion, vendedor);
                subastas.add(subasta);
            }
            return subastas;
        } catch (SQLException e) {
            e.printStackTrace();
            invocarAlerta("Error al recuperar datos", Alert.AlertType.ERROR);
        }
        return null;
    }

    public int get_fk_vendedor(int idSubasta){
        String SQL = "SELECT * FROM get_fk_vendedor(?);";
        try {
            PreparedStatement pstmt = connection.prepareStatement(SQL);
            pstmt.setInt(1, idSubasta);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public ArrayList<String> get_telefonos(int idUser){
        String SQL = "SELECT * FROM get_tel_usuario(?);";
        ArrayList<String> tel = new ArrayList<>();
        try {
            PreparedStatement pstmt = connection.prepareStatement(SQL);
            pstmt.setInt(1, idUser);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()){
                tel.add(rs.getString(1));
            }
            return tel;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public File cargarImagen(ImageView imageView, InputStream imagen){
        try {
            File tempFile = File.createTempFile("img_def", ".png");
            tempFile.deleteOnExit();
            FileOutputStream out = new FileOutputStream(tempFile);
            IOUtils.copy(imagen, out);
            Image image = new Image(tempFile.toURI().toString());
            imageView.setImage(image);
            return tempFile;
        }
        catch (FileNotFoundException e){
            GestorDB.gestor.invocarAlerta("Error de archivo", Alert.AlertType.ERROR);
        }
        catch (IOException e){
            GestorDB.gestor.invocarAlerta("Error de IO", Alert.AlertType.ERROR);
        }
        return null;
    }
}
