package Controller;

import GestoresDB.GestorDB;
import Model.Persona;
import Model.Subastas;
import Model.Usuario;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ControllerSubastas implements Initializable {
    @FXML
    public TableView tablaSubastas;
    @FXML
    public TableColumn id;
    @FXML
    public TableColumn vendedor;
    @FXML
    public TableColumn fechaFin;
    @FXML
    public TableColumn precioInicial;
    @FXML
    public TableColumn precioFinal;
    @FXML
    public TableColumn comentarioVendedor;
    @FXML
    public TableColumn comentarioComprador;

    public Persona revisando;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void configurarColumnas(){
        id.setCellValueFactory(new PropertyValueFactory<Subastas, String>("id"));
        vendedor.setCellValueFactory(new PropertyValueFactory<Subastas, String>("vendedor"));
        fechaFin.setCellValueFactory(new PropertyValueFactory<Subastas, String>("fechaFin"));
        precioInicial.setCellValueFactory(new PropertyValueFactory<Subastas, String>("monto"));
        precioFinal.setCellValueFactory(new PropertyValueFactory<Subastas, String>("montoFinal"));
        comentarioVendedor.setCellValueFactory(new PropertyValueFactory<Subastas, String>("comentarioVendedor"));
        comentarioComprador.setCellValueFactory(new PropertyValueFactory<Subastas, String>("comentarioComprador"));
    }

    public void iniciar(String SQL){
        ArrayList<Subastas> s = GestorDB.gestor.hist_subastas(revisando.getId(), SQL);
        tablaSubastas.setItems(FXCollections.observableArrayList(s));
    }
}
