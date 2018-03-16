package Controller;

import GestoresDB.GestorDB;
import Model.Persona;
import Model.VariablesSistema;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.apache.commons.io.IOUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
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
            try {
                File tempFile = File.createTempFile("img_def", ".png");
                tempFile.deleteOnExit();
                FileOutputStream out = new FileOutputStream(tempFile);
                IOUtils.copy(vs.imagen, out);
                Image image = new Image(tempFile.toURI().toString());
                imagenDef.setImage(image);
                imagenActual = tempFile;
            }
            catch (FileNotFoundException e){
                GestorDB.gestor.invocarAlerta("Error de archivo", Alert.AlertType.ERROR);
            }
            catch (IOException e){
                GestorDB.gestor.invocarAlerta("Error de IO", Alert.AlertType.ERROR);
            }
        });
        /****************************/
        /*Tab Modificar Usuarios*/
        configurarColumnas();
        cargarUsuarios(); //Llenar las tablas cuando inicia la app
        update.setOnAction(event -> {
            cargarUsuarios();
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
    }

    public void cargarUsuarios(){
        ArrayList<Persona> usuarios = GestorDB.gestor.read_users_admins(false);
        ObservableList<Persona> listaUsuarios = FXCollections.observableArrayList(usuarios);
        tableUser.setItems(listaUsuarios);

        ArrayList<Persona> admins = GestorDB.gestor.read_users_admins(true);
        ObservableList<Persona> listaAdmins = FXCollections.observableArrayList(admins);
        tableAdmin.setItems(listaAdmins);
    }

}
