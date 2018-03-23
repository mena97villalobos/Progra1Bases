package Controller;

import GestoresDB.GestorDB;
import Model.Usuario;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import jdk.nashorn.internal.ir.GetSplitState;

import javax.xml.soap.Text;
import java.net.URL;
import java.util.ResourceBundle;

public class ControllerModificarUser implements Initializable {
    @FXML
    public TextField alias;
    @FXML
    public TextField nombre;
    @FXML
    public TextField apellido;
    @FXML
    public TextField cedula;
    @FXML
    public TextField correo;
    @FXML
    public PasswordField passActual;
    @FXML
    public PasswordField nuevoPass;
    @FXML
    public TextArea direccion;
    @FXML
    public Button guardar;
    @FXML
    public TitledPane contrasena;
    @FXML
    public Label calificacion;

    public Usuario modificando;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        guardar.setOnAction(event -> {
            if(!passActual.getText().equals("") && nuevoPass.getText().equals("")){
                int id = GestorDB.gestor.validate_user(alias.getText(), passActual.getText(), false);
                if(id != 0){
                    GestorDB.gestor.update_password(Integer.parseInt(modificando.getId()), nuevoPass.getText(), false);
                }
                else{
                    GestorDB.gestor.invocarAlerta("Password Actual no coincide", Alert.AlertType.ERROR);
                }
            }
            modificando.setNombre(nombre.getText());
            modificando.setAlias(alias.getText());
            modificando.correo = correo.getText();
            modificando.direccion = direccion.getText();
            modificando.cedula = cedula.getText();
            modificando.apellido = apellido.getText();
            GestorDB.gestor.update_usuario(this.modificando);
        });
    }

    public void cargarDatos(boolean modificable){
        if(!modificable){
            guardar.setVisible(false);
            contrasena.setVisible(false);
            alias.setEditable(false);
            nombre.setEditable(false);
            apellido.setEditable(false);
            cedula.setEditable(false);
            correo.setEditable(false);
            direccion.setEditable(false);
        }
        alias.setText(modificando.getAlias());
        nombre.setText(modificando.getNombre());
        apellido.setText(modificando.apellido);
        cedula.setText(modificando.cedula);
        correo.setText(modificando.correo);
        direccion.setText(modificando.direccion);
        calificacion.setText(modificando.calificacion);
    }
}
