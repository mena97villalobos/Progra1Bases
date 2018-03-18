package Controller;

import GestoresDB.GestorDB;
import Model.Item;
import Model.VariablesSistema;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ControllerUser implements Initializable {
    @FXML
    public ComboBox catPrimaria;
    @FXML
    public ComboBox catSecundaria;
    @FXML
    public TextArea descripcion;
    @FXML
    public Button cargarImagen;
    @FXML
    public CheckBox selectDefault;
    @FXML
    public ImageView imagen;
    @FXML
    public TitledPane crearItem;
    @FXML
    public TitledPane crearSubasta;
    @FXML
    public DatePicker fechaCierre;
    @FXML
    public TextField horaCierre;
    @FXML
    public TextField precio;
    @FXML
    public TextArea detallesEntrega;
    @FXML
    public Button crear;

    private int userID;
    private File imagenSeleccionada;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        crearTask();
        /*Tab Subastar*/
        catPrimaria.setItems(FXCollections.observableArrayList(GestorDB.gestor.read_categoria_primaria()));
        catPrimaria.setOnAction(event -> {
            catSecundaria.setItems(FXCollections.observableArrayList(new ArrayList<String>()));
            catSecundaria.setDisable(true);
            crearTask();
        });
        cargarImagen.setOnAction(event -> {
            Stage actual = (Stage) cargarImagen.getScene().getWindow();
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Resource File");
            File file = fileChooser.showOpenDialog(actual);
            if(file != null){
                Image image = new Image(file.toURI().toString());
                imagen.setImage(image);
                imagenSeleccionada = file;
                selectDefault.setSelected(false);
                selectDefault.setDisable(true);
            }
        });
        selectDefault.setOnAction(event -> {
            imagenSeleccionada = GestorDB.gestor.cargarImagen(imagen);
        });
        crear.setOnAction(event -> {
            //cargar Datos Item
            String primaria = (String) catPrimaria.getSelectionModel().getSelectedItem();
            String secundaria = (String) catSecundaria.getSelectionModel().getSelectedItem();
            int categoria = GestorDB.gestor.get_id_categoria(primaria, secundaria);
            if(categoria == 0)
                GestorDB.gestor.invocarAlerta("Error al seleccionar Categoria", Alert.AlertType.ERROR);
            String desc = descripcion.getText();
            if(imagenSeleccionada == null)
                selectDefault.fire();
            Item nuevo = new Item(categoria, desc, imagenSeleccionada);
            //TODO insertar item y sacar el id que se le asignó

        });
        /*******************************************/
    }

    public void crearTask(){
        //Thread para dejar bloqueado categoria secundaria y el boton de filtrar
        Task task = new Task() {
            @Override
            protected Void call() {
                while(catPrimaria.getSelectionModel().isEmpty()){}
                catSecundaria.setDisable(false);
                String primariaSeleccionada = (String) catPrimaria.getSelectionModel().getSelectedItem();
                ArrayList<String> secundarias = GestorDB.gestor.read_categoria_secundaria(primariaSeleccionada);
                catSecundaria.setItems(FXCollections.observableArrayList(secundarias));
                while(catSecundaria.getSelectionModel().isEmpty()){}
                System.out.println("Secundaria Seleccionada");
                crearSubasta.setDisable(false);
                return null;
            }
        };
        Thread t = new Thread(task);
        t.start();
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }
}
