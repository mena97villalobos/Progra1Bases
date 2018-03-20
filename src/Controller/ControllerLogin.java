package Controller;

import GestoresDB.GestorDB;
import Model.USER_TYPES;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ControllerLogin implements Initializable {
    @FXML
    public TextField user;
    @FXML
    public PasswordField password;
    @FXML
    public CheckBox admin;
    @FXML
    public Button entrar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        entrar.setOnAction(event -> {
            String u = user.getText();
            String p = password.getText();
            boolean a = admin.isSelected();
            int userID = GestorDB.gestor.validate_user(u, p, a);
            if (userID != 0){
                abrir_ventana_user(a ? USER_TYPES.admin : USER_TYPES.user, userID);
            }
            else{
                GestorDB.gestor.invocarAlerta("Usuario o Contraseña Incorrectos", Alert.AlertType.WARNING);
                password.clear();
            }
        });
    }

    private void abrir_ventana_user(USER_TYPES type, int id){
        try {
            FXMLLoader loader = new FXMLLoader();
            Parent root = loader.load(getClass().getResource("../View/"+ type.toString() +".fxml").openStream());
            Stage escenario = new Stage();
            if(type.equals(USER_TYPES.admin)){
                ControllerAdmin c = loader.getController();
                c.setAdminLogged(id);
            }
            else{
                ControllerUser c = loader.getController();
                c.setUserID(id);
                c.iniciar();
            }
            escenario.setTitle(type.toString().toUpperCase());
            int width = type.equals(USER_TYPES.admin) ? 460 : 600;
            int height = type.equals(USER_TYPES.admin) ? 600 : 500;
            escenario.setScene(new Scene(root, width, height));
            escenario.show();
            Stage actual = (Stage) entrar.getScene().getWindow();
            actual.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
