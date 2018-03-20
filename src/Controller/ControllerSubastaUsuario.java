package Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;

import java.awt.*;
import java.net.URL;
import java.util.ResourceBundle;

public class ControllerSubastaUsuario implements Initializable {
    @FXML
    public TextField alias;
    @FXML
    public TextField pujaActual;
    @FXML
    public TextArea detEntrega;
    @FXML
    public TextArea descrItem;
    @FXML
    public ImageView imagen;
    @FXML
    public Label fechaFin;
    @FXML
    public Button detallesVendedor;
    @FXML
    public TextField incrMin;
    @FXML
    public TextField puja;
    @FXML
    public Button pujar;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
