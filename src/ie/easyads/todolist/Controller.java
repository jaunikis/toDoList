package ie.easyads.todolist;


import ie.easyads.todolist.datamodel.TodoData;
import ie.easyads.todolist.datamodel.Todoitem;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;


public class Controller {

    private List<Todoitem> todoItems;

    @FXML
    private ListView<Todoitem> todoListas;
    @FXML
    private TextArea itemDetailsTextArea;
    @FXML
    private Label deadLineLabel;

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private ContextMenu listContextMenu;

    @FXML
    private ToggleButton filterToggleButton;

    private FilteredList<Todoitem> filteredList;
    private Predicate<Todoitem>  wantAllItems;
    private Predicate<Todoitem> wantTodaysItems;


    public void initialize(){
//        System.out.println("initialize.");
//        Todoitem item1 = new Todoitem("Mail Birthday card",
//                "buy a 30th card for Johny", LocalDate.of(2018,03,06));
//        Todoitem item2 = new Todoitem("get bread",
//                "Get bread from shop", LocalDate.of(2018,06,01));
//        Todoitem item3 = new Todoitem("bla",
//                "bla bla bla", LocalDate.of(2018,07,26));
//        Todoitem item4 = new Todoitem("gimtadienis",
//                "mano gimtadienis", LocalDate.of(2019,07,27));
//        Todoitem item5 = new Todoitem("gimtadienis K",
//                "Kristes gimtadienis", LocalDate.of(2019,9,8));
//
//        todoItems = new ArrayList<Todoitem>();
//        todoItems.add(item1);
//        todoItems.add(item2);
//        todoItems.add(item3);
//        todoItems.add(item4);
//        todoItems.add(item5);
//
//        TodoData.getInstance().setTodoItems(todoItems);

        listContextMenu = new ContextMenu();
        MenuItem deleteMenuItem = new MenuItem("Delete");
        deleteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Todoitem item = todoListas.getSelectionModel().getSelectedItem();
                deleteItem(item);
            }
        });

        listContextMenu.getItems().addAll(deleteMenuItem);
        todoListas.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Todoitem>() {
            @Override
            public void changed(ObservableValue<? extends Todoitem> observable, Todoitem oldValue, Todoitem newValue) {
                if(newValue != null){
                    Todoitem item = todoListas.getSelectionModel().getSelectedItem();
                    itemDetailsTextArea.setText(item.getDetails());
                    DateTimeFormatter df = DateTimeFormatter.ofPattern("MMMM d, yyyy");
                    deadLineLabel.setText(df.format(item.getDeadLine()));
                }
            }
        });

        wantAllItems = new Predicate<Todoitem>() {
            @Override
            public boolean test(Todoitem todoitem) {
                return true;
            }
        };

        wantTodaysItems = new Predicate<Todoitem>() {
            @Override
            public boolean test(Todoitem todoitem) {
                return (todoitem.getDeadLine().equals(LocalDate.now()));
            }
        };

        filteredList = new FilteredList<Todoitem>(TodoData.getInstance().getTodoItems(),wantAllItems);

        SortedList<Todoitem> sortedList = new SortedList<Todoitem>(filteredList,
                new Comparator<Todoitem>() {
                    @Override
                    public int compare(Todoitem o1, Todoitem o2) {
                        return o1.getDeadLine().compareTo(o2.getDeadLine());
                    }
                });

//        todoListas.setItems(TodoData.getInstance().getTodoItems());
        todoListas.setItems(sortedList);
        todoListas.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        todoListas.getSelectionModel().selectFirst();

        todoListas.setCellFactory(new Callback<ListView<Todoitem>, ListCell<Todoitem>>() {
            @Override
            public ListCell<Todoitem> call(ListView<Todoitem> param) {
                ListCell<Todoitem> cell = new ListCell<Todoitem>(){
                    @Override
                    protected void updateItem(Todoitem item, boolean empty) {
                        super.updateItem(item, empty);
                        if(empty){setText(null);}
                            else{
                                setText(item.getShortDescription());
                                if(item.getDeadLine().equals(LocalDate.now())){
                                    setTextFill(Color.RED);
                                }else if(item.getDeadLine().equals(LocalDate.now().plusDays(1))){
                                    setTextFill(Color.ORANGE);
                                }else if(item.getDeadLine().isBefore(LocalDate.now())){
                                    setTextFill(Color.PURPLE);
                                }

                        }

                    }
                };

                cell.emptyProperty().addListener(
                        (obs, wasEmpty, isNowEmpty) -> {
                            if(isNowEmpty){
                                cell.setContextMenu(null);
                            }else{
                                cell.setContextMenu(listContextMenu);
                            }
                        });

                return cell;
            }
        });
    }

    @FXML
    public void showNewItemDialog(){
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("Add new item");
        dialog.setHeaderText("Use this dialog to create new item.");
        FXMLLoader fxmlloader = new FXMLLoader();
        fxmlloader.setLocation(getClass().getResource("todoItemDialog.fxml"));
        try{
            dialog.getDialogPane().setContent(fxmlloader.load());
        }catch(IOException e){
            System.out.println("couldnt load the dialog");
            e.printStackTrace();
            return;
        }
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK){
            System.out.println("OK pressed.");
            dialogController controller = fxmlloader.getController();
            Todoitem newItem = controller.processResults();
//            todoListas.getItems().setAll(TodoData.getInstance().getTodoItems());
            todoListas.getSelectionModel().select(newItem);

        }
    }

    public void handleKeyPress(javafx.scene.input.KeyEvent keyEvent){
        Todoitem selectedItem = todoListas.getSelectionModel().getSelectedItem();
        if(selectedItem != null){
            if(keyEvent.getCode().equals(KeyCode.DELETE)){
                deleteItem(selectedItem);
            }
        }
    }

    @FXML
    public void handleClickListView(){
        Todoitem item=todoListas.getSelectionModel().getSelectedItem();
        itemDetailsTextArea.setText(item.getDetails());
        deadLineLabel.setText(item.getDeadLine().toString());
//        System.out.println("The selected item is "+item);
//        itemDetailsTextArea.setText(item.getDetails());
//        StringBuilder sb = new StringBuilder(item.getDetails());
//        sb.append("\n\n\n\n");
//        sb.append(item.getDeadLine().toString());
//        itemDetailsTextArea.setText(sb.toString());
    }

    public void deleteItem(Todoitem item){
        System.out.println("delete item");
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete todo item");
        alert.setHeaderText("Delete item: "+item.getShortDescription());
        alert.setContentText("Are you sure? Press OK or CANCEL");
        Optional<ButtonType> result = alert.showAndWait();

        if(result.isPresent() && (result.get()== ButtonType.OK)){
            TodoData.getInstance().deleteTodoItem(item);
        }
    }

    public void menuShow(){
        System.out.println("menu Show");
    }

    public void pressFileSettings(){
        System.out.println("settings pressed.");
    }

    public void handleFilterButton(){
        if(filterToggleButton.isSelected()){
            System.out.println("on");
            filteredList.setPredicate(wantTodaysItems);
        }else{
            System.out.println("off");
            filteredList.setPredicate(wantAllItems);
        }
    }

    @FXML
    public void handleExit(){
        Platform.exit();
    }

}
