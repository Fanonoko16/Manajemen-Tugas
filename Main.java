import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MainApp extends Application {
    @FXML
    private TextField itemNameTextField;

    @FXML
    private ListView<Item> itemListView;

    private final ObservableList<Item> items = FXCollections.observableArrayList();

    public static void main(String[] args) {
        DatabaseHandler.createTable();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("MainView.fxml"));

        Scene scene = new Scene(root, 400, 300);

        primaryStage.setTitle("CRUD App");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void initialize() {
        loadItems();
        itemListView.setItems(items);
        itemListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                itemNameTextField.setText(newValue.getName());
            }
        });
    }

    public void addItem() {
        String itemName = itemNameTextField.getText();
        DatabaseHandler.insertItem(itemName);
        loadItems();
        itemNameTextField.clear();
    }

    public void updateItem() {
        Item selectedItem = itemListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            int selectedItemId = selectedItem.getId();
            String newName = itemNameTextField.getText();
            DatabaseHandler.updateItem(selectedItemId, newName);
            loadItems();
            itemNameTextField.clear();
        }
    }

    public void deleteItem() {
        Item selectedItem = itemListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            int selectedItemId = selectedItem.getId();
            DatabaseHandler.deleteItem(selectedItemId);
            loadItems();
            itemNameTextField.clear();
        }
    }

    private void loadItems() {
        List<Item> itemList = DatabaseHandler.getAllItems();
        items.setAll(itemList);
    }
}

class Item {
    private int id;
    private String name;

    public Item(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}

class DatabaseHandler {
    private static final String URL = "jdbc:sqlite:items.db";

    public static void createTable() {
        try (Connection connection = DriverManager.getConnection(URL);
             Statement statement = connection.createStatement()) {

            String query = "CREATE TABLE IF NOT EXISTS items (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT)";
            statement.execute(query);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertItem(String name) {
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO items (name) VALUES (?)")) {

            preparedStatement.setString(1, name);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(URL);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM items")) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                items.add(new Item(id, name));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return items;
    }

    public static void updateItem(int id, String newName) {
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement preparedStatement = connection.prepareStatement("UPDATE items SET name = ? WHERE id = ?")) {

            preparedStatement.setString(1, newName);
            preparedStatement.setInt(2, id);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteItem(int id) {
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM items WHERE id = ?")) {

            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
