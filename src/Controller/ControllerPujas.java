package Controller;

import GestoresDB.GestorDB;
import Model.Pujas;
import Model.Subastas;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ControllerPujas implements Initializable {
    @FXML
    public TableView tablaPujas;
    @FXML
    public TableColumn id;
    @FXML
    public TableColumn comprador;
    @FXML
    public TableColumn monto;
    @FXML
    public TableColumn fecha;

    public Subastas actual;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void configurarColumnas(){
        id.setCellValueFactory(new PropertyValueFactory<Pujas, String>("id"));
        comprador.setCellValueFactory(new PropertyValueFactory<Pujas, String>("comprador"));
        monto.setCellValueFactory(new PropertyValueFactory<Pujas, String>("monto"));
        fecha.setCellValueFactory(new PropertyValueFactory<Pujas, String>("fecha"));
    }

    public void iniciar(){
        ArrayList<Pujas> p = GestorDB.gestor.hist_pujas(actual.getId());
        tablaPujas.setItems(FXCollections.observableArrayList(p));
    }
}
