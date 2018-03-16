package Controller;

import GestoresDB.GestorDB;
import Model.Persona;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class ControllerModificarAdmin implements Initializable {
    @FXML
    public TextField nombre;
    @FXML
    public TextField alias;
    @FXML
    public PasswordField passActual;
    @FXML
    public PasswordField nuevoPass;
    @FXML
    public Button guardar;

    public Persona modificando;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        guardar.setOnAction(event -> {
            //Revisar cambio de password
            if (!passActual.getText().equals("") && nuevoPass.getText().equals("")) {
                int id = GestorDB.gestor.validate_user(alias.getText(), passActual.getText(), true);
                if (id != 0) {
                    GestorDB.gestor.update_password(Integer.parseInt(modificando.getId()), nuevoPass.getText(), true);
                } else {
                    GestorDB.gestor.invocarAlerta("Password Actual no coincide", Alert.AlertType.ERROR);
                }
            }
            modificando.setAlias(alias.getText());
            modificando.setNombre(nombre.getText());
            GestorDB.gestor.update_admin(Integer.parseInt(modificando.getId()), nombre.getText(), alias.getText());
        });
    }

    public void iniciar(){
        nombre.setText(modificando.getNombre());
        alias.setText(modificando.getAlias());
    }
}
