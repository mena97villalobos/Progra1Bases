package Controller;

import GestoresDB.GestorDB;
import Model.Subastas;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

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

    public Subastas revisando;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void iniciar(int idSubasta){
        this.revisando = GestorDB.gestor.read_subastas_item(idSubasta);
        alias.setText(revisando.getVendedor());
        pujaActual.setText(revisando.getMontoFinal());
        detEntrega.setText(revisando.getDetEntrega());
        descrItem.setText(revisando.getDetallesItem());
        GestorDB.gestor.cargarImagen(imagen, revisando.imagen);
        fechaFin.setText(revisando.getFechaFin());
        incrMin.setText(revisando.getIncrMin());
    }
}
