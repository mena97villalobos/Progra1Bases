package Controller;

import GestoresDB.GestorDB;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

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


    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
    }
}
