package Controller;

import GestoresDB.GestorDB;
import Model.Subastas;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
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
    @FXML
    public TableView listaSubastas;
    @FXML
    public TableColumn idSubasta;
    @FXML
    public TableColumn vendedor;
    @FXML
    public TableColumn fechaFin;
    @FXML
    public TableColumn descrItem;
    @FXML
    public ComboBox catPrimaria1;
    @FXML
    public ComboBox catSecundaria1;
    @FXML
    public Button filtrar;
    @FXML
    public Button pujar;

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
            else {
                String desc = descripcion.getText();
                if (imagenSeleccionada == null)
                    selectDefault.fire();
                int idItem = GestorDB.gestor.crear_item(categoria, desc, imagenSeleccionada);
                if(idItem != 0) {
                    //cargar datos subasta
                    String fechaFin = fechaCierre.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    String horaFin = horaCierre.getText();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
                    Date parsedDate = null;
                    try {
                        parsedDate = dateFormat.parse(fechaFin + " " + horaFin);
                    } catch (ParseException e) {
                        GestorDB.gestor.invocarAlerta("Asegurese que el formato de la hora sea correcto", Alert.AlertType.ERROR);
                    }
                    if (parsedDate != null) {
                        Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
                        float precioInit = Float.parseFloat(precio.getText());
                        String detallesEnt = detallesEntrega.getText();
                        int idSubasta = GestorDB.gestor.crear_subasta(this.userID, idItem, precioInit, timestamp, detallesEnt);
                        if(idSubasta == 0)
                            GestorDB.gestor.invocarAlerta("Error al crear subasta", Alert.AlertType.ERROR);
                    }
                }
            }
        });
        /*******************************************/
        /*Tab Comprar*/
        crearTaskTabComprar();
        filtrar.setOnAction(event -> {
            String primariaSeleccionada = (String) catPrimaria1.getSelectionModel().getSelectedItem();
            String secundariaSeleccionada = (String) catSecundaria1.getSelectionModel().getSelectedItem();
            int idCategoria = GestorDB.gestor.get_id_categoria(primariaSeleccionada, secundariaSeleccionada);
            ArrayList<Subastas> s = GestorDB.gestor.read_subastas_usuario(idCategoria, true);
            listaSubastas.setItems(FXCollections.observableArrayList(s));
        });
        /******************************************/
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

    public void crearTaskTabComprar(){
        Task task = new Task() {
            @Override
            protected Void call() {
                catPrimaria1.setItems(FXCollections.observableArrayList(GestorDB.gestor.read_categoria_primaria()));
                catSecundaria1.setDisable(true);
                filtrar.setDisable(true);
                while(catPrimaria1.getSelectionModel().isEmpty()){}
                catPrimaria1.setDisable(true);
                catSecundaria1.setDisable(false);
                String primariaSeleccionada = (String) catPrimaria1.getSelectionModel().getSelectedItem();
                ArrayList<String> secundarias = GestorDB.gestor.read_categoria_secundaria(primariaSeleccionada);
                catSecundaria1.setItems(FXCollections.observableArrayList(secundarias));
                while(catSecundaria1.getSelectionModel().isEmpty()){}
                catSecundaria1.setDisable(true);
                filtrar.setDisable(false);
                return null;
            }
        };
        Thread t = new Thread(task);
        t.start();
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public void configurarColumnas(){
        idSubasta.setCellValueFactory(new PropertyValueFactory<Subastas, String>("id"));
        vendedor.setCellValueFactory(new PropertyValueFactory<Subastas, String>("vendedor"));
        fechaFin.setCellValueFactory(new PropertyValueFactory<Subastas, String>("fechaFin"));
        descrItem.setCellValueFactory(new PropertyValueFactory<Subastas, String>("detallesItem"));
    }

    public void iniciar(){
        configurarColumnas();
        ArrayList<Subastas> s = GestorDB.gestor.read_subastas_usuario(0, false);
        listaSubastas.setItems(FXCollections.observableArrayList(s));
    }
}
