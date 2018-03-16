package Controller;

import GestoresDB.GestorDB;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ControllerInicial implements Initializable {
    @FXML
    public RadioButton oracle;
    @FXML
    public RadioButton postgres;
    @FXML
    public Button entrar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        oracle.setOnAction(event -> {
            if(postgres.isDisable())
                postgres.setDisable(false);
            else
                postgres.setDisable(true);
        });
        postgres.setOnAction(event -> {
            if(oracle.isDisable())
                oracle.setDisable(false);
            else
                oracle.setDisable(true);
        });
        entrar.setOnAction(event -> {
            if (oracle.selectedProperty().getValue()){

            }
            if (postgres.selectedProperty().getValue()){
                //TODO El usuario y la contraseña hay que cambiarlos
                GestorDB.gestor = new GestorDB("jdbc:postgresql://localhost:5432/Progra1", "postgres", "9545");
            }
            abrir_login();
        });
    }

    public void abrir_login(){
        try {
            FXMLLoader loader = new FXMLLoader();
            Parent root = loader.load(getClass().getResource("../View/login.fxml").openStream());
            Stage escenario = new Stage();
            escenario.setTitle("Login");
            escenario.setScene(new Scene(root, 314, 197));
            escenario.show();
            Stage actual = (Stage) entrar.getScene().getWindow();
            actual.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
