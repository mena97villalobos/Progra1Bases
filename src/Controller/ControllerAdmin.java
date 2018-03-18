package Controller;

import GestoresDB.GestorDB;
import Model.Persona;
import Model.Subastas;
import Model.Usuario;
import Model.VariablesSistema;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.apache.commons.io.IOUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ControllerAdmin implements Initializable {
    @FXML
    public TextField user;
    @FXML
    public TextField password;
    @FXML
    public TextField password1;
    @FXML
    public TextField apellido;
    @FXML
    public TextField name;
    @FXML
    public TextField email;
    @FXML
    public TextField celAdd;
    @FXML
    public TextField ced;
    @FXML
    public TextArea addr;
    @FXML
    public CheckBox admin;
    @FXML
    public Button addCel;
    @FXML
    public Button reset;
    @FXML
    public Button registrar;
    @FXML
    public ComboBox<String> celRegister;
    @FXML
    public Rectangle rectang;
    @FXML
    public Button save;
    @FXML
    public Button loadImage;
    @FXML
    public TextField inc;
    @FXML
    public TextField percent;
    @FXML
    public TextField imagePath;
    @FXML
    public ImageView imagenDef;
    @FXML
    public Button load;
    @FXML
    public Button modificar;
    @FXML
    public Button update;
    @FXML
    public TableView tableAdmin;
    @FXML
    public TableView tableUser;
    @FXML
    public TableColumn idUser;
    @FXML
    public TableColumn nombreUser;
    @FXML
    public TableColumn aliasUser;
    @FXML
    public TableColumn idAdmin;
    @FXML
    public TableColumn nombreAdmin;
    @FXML
    public TableColumn aliasAdmin;
    @FXML
    public TableView listaSubastas;
    @FXML
    public TableView listaUsuarios;
    @FXML
    public TableColumn idSubasta;
    @FXML
    public TableColumn vendedor;
    @FXML
    public TableColumn fechaFin;
    @FXML
    public TableColumn idUserLista;
    @FXML
    public TableColumn aliasLista;
    @FXML
    public TableColumn nombreLista;
    @FXML
    public ComboBox catPrimaria;
    @FXML
    public ComboBox catSecundaria;
    @FXML
    public Button filtrar;
    @FXML
    public Button mostrarPujas;
    @FXML
    public Button histSubastas;
    @FXML
    public Button histGanadas;

    private int adminLogged;
    private File imagenActual = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        /*Tab Registrar Usuarios*/
        admin.setOnAction(event -> {
            email.setDisable(!email.isDisable());
            celAdd.setDisable(!celAdd.isDisable());
            ced.setDisable(!ced.isDisable());
            apellido.setDisable(!apellido.isDisable());
            addr.setDisable(!addr.isDisable());
            addCel.setDisable(!addCel.isDisable());
            celRegister.setDisable(!celRegister.isDisable());
        });
        addCel.setOnAction(event -> {
            String tel = celAdd.getText();
            ArrayList<String> aux = new ArrayList<>(celRegister.getItems());
            aux.add(tel);
            celRegister.setItems(FXCollections.observableArrayList(aux));
            celAdd.clear();
        });
        registrar.setOnAction(event -> {
            if(password.getText().equals(password1.getText())){
                if(admin.isSelected()){
                    String u = user.getText();
                    String n = name.getText();
                    String p = password.getText();
                    GestorDB.gestor.crear_admin(n, u, p);
                    rectang.setFill(Color.LIME);
                }
                else{
                    String userName = user.getText();
                    String pass = password.getText();
                    String nombre = name.getText();
                    String apellidos = apellido.getText();
                    String correo = email.getText();
                    String addrs = addr.getText();
                    String cedula = ced.getText();
                    int insertado = GestorDB.gestor.crear_usuario(cedula, nombre, apellidos, userName, pass, addrs, correo);
                    if(insertado != 0) {
                        ArrayList<String> aux = new ArrayList<>(celRegister.getItems());
                        for (String tel : aux) {
                            GestorDB.gestor.crear_telefono(insertado, tel);
                        }
                        rectang.setFill(Color.LIME);
                        GestorDB.gestor.invocarAlerta("Usuario Creado", Alert.AlertType.INFORMATION);
                    }
                    else {
                        rectang.setFill(Color.RED);
                        GestorDB.gestor.invocarAlerta("Error al registrar usuario", Alert.AlertType.ERROR);
                    }
                }
            }
            else
                GestorDB.gestor.invocarAlerta("Las contraseñas deben coincidir", Alert.AlertType.ERROR);
        });
        reset.setOnAction(event -> {
            rectang.setFill(Color.WHITE);
            celRegister.setItems(FXCollections.observableArrayList(new ArrayList<>()));
            user.clear();
            addr.clear();
            password.clear();
            password1.clear();
            name.clear();
            ced.clear();
            email.clear();
            apellido.clear();
            celAdd.clear();
        });
        /****************************/
        /*Tab Variables del Sistema*/
        loadImage.setOnAction(event -> {
            String path = imagePath.getText();
            if(!path.equals("")){
                File file = new File(path);
                Image image = new Image(file.toURI().toString());
                imagenDef.setImage(image);
                imagenActual = file;
            }
            else
                GestorDB.gestor.invocarAlerta("Path invalido", Alert.AlertType.ERROR);
        });
        save.setOnAction(event -> {
            if(imagenActual == null)
                GestorDB.gestor.invocarAlerta("Imagen no seleccionada", Alert.AlertType.ERROR);
            else {
                Float incremento = Float.parseFloat(inc.getText());
                Float porcentaje = Float.parseFloat(percent.getText());
                GestorDB.gestor.update_variables(incremento, porcentaje, imagenActual);
            }

        });
        load.setOnAction(event -> {
            VariablesSistema vs = GestorDB.gestor.read_variables();
            inc.setText(Float.toString(vs.inc));
            percent.setText(Float.toString(vs.percent));
            imagenActual = GestorDB.gestor.cargarImagen(imagenDef);
        });
        load.fire();
        /****************************/
        /*Tab Modificar Usuarios*/
        configurarColumnas();
        cargarUsuarios(); //Llenar las tablas cuando inicia la app
        update.setOnAction(event -> {
            cargarUsuarios();
        });
        modificar.setOnAction(event -> {
            Persona admin = (Persona) tableAdmin.getSelectionModel().getSelectedItem();
            Persona user = (Persona) tableUser.getSelectionModel().getSelectedItem();
            if(admin == null && user == null)
                GestorDB.gestor.invocarAlerta("Seleccione un usuario para modificar", Alert.AlertType.INFORMATION);
            else if(admin == null){
                modificarUsuario(user);
            }
            else if(user == null){
                modificarAdmin(admin);
            }
            else{
                modificarUsuario(user);
                modificarAdmin(admin);
            }
            tableUser.getSelectionModel().clearSelection();
            tableAdmin.getSelectionModel().clearSelection();
        });
        /****************************/
        /*Tab listas*/
        crearTask();
        catPrimaria.setItems(FXCollections.observableArrayList(GestorDB.gestor.read_categoria_primaria()));
        ArrayList<Subastas> subastas = GestorDB.gestor.read_subastas("", "", false);
        listaSubastas.setItems(FXCollections.observableArrayList(subastas));
        filtrar.setOnAction(event -> {
            String primariaSeleccionada = (String) catPrimaria.getSelectionModel().getSelectedItem();
            String secundariaSeleccionada = (String) catSecundaria.getSelectionModel().getSelectedItem();
            ArrayList<Subastas> s = GestorDB.gestor.read_subastas(primariaSeleccionada, secundariaSeleccionada, true);
            listaSubastas.setItems(FXCollections.observableArrayList(s));
            catPrimaria.getSelectionModel().clearSelection();
            catSecundaria.setItems(FXCollections.observableArrayList(new ArrayList<String>()));
            //Esto va al final para limpiar
            catPrimaria.setDisable(false);
            filtrar.setDisable(true);
            catSecundaria.setDisable(true);
            crearTask();
        });
        mostrarPujas.setOnAction(event -> {
            Subastas s = (Subastas) listaSubastas.getSelectionModel().getSelectedItem();
            if(s == null)
                GestorDB.gestor.invocarAlerta("Seleccione una subasta", Alert.AlertType.INFORMATION);
            else {
                try {
                    FXMLLoader loader = new FXMLLoader();
                    Parent root = loader.load(getClass().getResource("../View/pujas.fxml").openStream());
                    Stage escenario = new Stage();
                    ControllerPujas c = loader.getController();
                    c.configurarColumnas();
                    c.actual = s;
                    c.iniciar();
                    escenario.setTitle("Pujas para ID: " + s.getId());
                    escenario.setScene(new Scene(root, 600, 400));
                    escenario.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        histSubastas.setOnAction(event -> {
            Persona usuario = (Persona) listaUsuarios.getSelectionModel().getSelectedItem();
            if(usuario == null)
                GestorDB.gestor.invocarAlerta("Seleccione un usuario", Alert.AlertType.INFORMATION);
            else {
                try {
                    FXMLLoader loader = new FXMLLoader();
                    Parent root = loader.load(getClass().getResource("../View/subastas.fxml").openStream());
                    Stage escenario = new Stage();
                    ControllerSubastas c = loader.getController();
                    c.revisando = usuario;
                    c.configurarColumnas();
                    c.iniciar("SELECT * FROM hist_subastas_usuario(?);");
                    escenario.setTitle("Subastas Creadas para ID: " + usuario.getId());
                    escenario.setScene(new Scene(root, 950, 400));
                    escenario.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        histGanadas.setOnAction(event -> {
            Persona usuario = (Persona) listaUsuarios.getSelectionModel().getSelectedItem();
            if(usuario == null)
                GestorDB.gestor.invocarAlerta("Seleccione un usuario", Alert.AlertType.INFORMATION);
            else {
                try {
                    FXMLLoader loader = new FXMLLoader();
                    Parent root = loader.load(getClass().getResource("../View/subastas.fxml").openStream());
                    Stage escenario = new Stage();
                    ControllerSubastas c = loader.getController();
                    c.revisando = usuario;
                    c.configurarColumnas();
                    c.iniciar("SELECT * FROM hist_subastas_ganadas(?);");
                    escenario.setTitle("Subastas Ganadas para ID: " + usuario.getId());
                    escenario.setScene(new Scene(root, 950, 400));
                    escenario.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        /****************************/
    }

    public void setAdminLogged(int adminLogged) {
        this.adminLogged = adminLogged;
    }

    public void configurarColumnas(){
        idUser.setCellValueFactory(new PropertyValueFactory<Persona, String>("id"));
        nombreUser.setCellValueFactory(new PropertyValueFactory<Persona, String>("nombre"));
        aliasUser.setCellValueFactory(new PropertyValueFactory<Persona, String>("alias"));
        idAdmin.setCellValueFactory(new PropertyValueFactory<Persona, String>("id"));
        nombreAdmin.setCellValueFactory(new PropertyValueFactory<Persona, String>("nombre"));
        aliasAdmin.setCellValueFactory(new PropertyValueFactory<Persona, String>("alias"));
        idUserLista.setCellValueFactory(new PropertyValueFactory<Persona, String>("id"));
        nombreLista.setCellValueFactory(new PropertyValueFactory<Persona, String>("nombre"));
        aliasLista.setCellValueFactory(new PropertyValueFactory<Persona, String>("alias"));
        idSubasta.setCellValueFactory(new PropertyValueFactory<Subastas, String>("id"));
        vendedor.setCellValueFactory(new PropertyValueFactory<Subastas, String>("vendedor"));
        fechaFin.setCellValueFactory(new PropertyValueFactory<Subastas, String>("fechaFin"));
    }

    public void cargarUsuarios(){
        ArrayList<Persona> usuarios = GestorDB.gestor.read_users_admins(false);
        ObservableList<Persona> listaUsers = FXCollections.observableArrayList(usuarios);
        tableUser.setItems(listaUsers);
        ArrayList<Persona> admins = GestorDB.gestor.read_users_admins(true);
        ObservableList<Persona> listaAdmins = FXCollections.observableArrayList(admins);
        tableAdmin.setItems(listaAdmins);
        listaUsuarios.setItems(listaUsers);
    }

    public void modificarUsuario(Persona persona){
        Usuario user = GestorDB.gestor.get_user_info(Integer.parseInt(persona.getId()));
        try {
            FXMLLoader loader = new FXMLLoader();
            Parent root = loader.load(getClass().getResource("../View/modificar_user.fxml").openStream());
            Stage escenario = new Stage();
            ControllerModificarUser c = loader.getController();
            c.modificando = user;
            c.cargarDatos();
            escenario.setTitle("Modificando ID: " + persona.getId());
            escenario.setScene(new Scene(root, 600, 300));
            escenario.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void modificarAdmin(Persona persona){
        try {
            FXMLLoader loader = new FXMLLoader();
            Parent root = loader.load(getClass().getResource("../View/modificar_admin.fxml").openStream());
            Stage escenario = new Stage();
            ControllerModificarAdmin c = loader.getController();
            c.modificando = persona;
            c.iniciar();
            escenario.setTitle("Modificando ID: " + persona.getId());
            escenario.setScene(new Scene(root, 600, 160));
            escenario.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void crearTask(){
        //Thread para dejar bloqueado categoria secundaria y el boton de filtrar
        Task task = new Task() {
            @Override
            protected Void call() {
                while(catPrimaria.getSelectionModel().isEmpty()){}
                catPrimaria.setDisable(true);
                catSecundaria.setDisable(false);
                String primariaSeleccionada = (String) catPrimaria.getSelectionModel().getSelectedItem();
                ArrayList<String> secundarias = GestorDB.gestor.read_categoria_secundaria(primariaSeleccionada);
                catSecundaria.setItems(FXCollections.observableArrayList(secundarias));
                while(catSecundaria.getSelectionModel().isEmpty()){}
                catSecundaria.setDisable(true);
                filtrar.setDisable(false);
                return null;
            }
        };
        Thread t = new Thread(task);
        t.start();
    }

}
