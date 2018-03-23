package Controller;

import GestoresDB.GestorDB;
import Model.Subastas;
import Model.Usuario;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
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
    public int idUsuario;

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
        pujar.setOnAction(event -> {
            float monto = Float.valueOf(puja.getText());
            int id = Integer.valueOf(revisando.getId());
            GestorDB.gestor.pujar(id, idUsuario, monto);
        });
        detallesVendedor.setOnAction(event -> {
            int fk_vendedor = GestorDB.gestor.get_fk_vendedor(Integer.valueOf(revisando.getId()));
            Usuario user = GestorDB.gestor.get_user_info(fk_vendedor);
            try {
                FXMLLoader loader = new FXMLLoader();
                Parent root = loader.load(getClass().getResource("../View/modificar_user.fxml").openStream());
                Stage escenario = new Stage();
                ControllerModificarUser c = loader.getController();
                c.modificando = user;
                c.cargarDatos(false);
                escenario.setTitle("Detalles Vendedor");
                escenario.setScene(new Scene(root, 600, 300));
                escenario.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
