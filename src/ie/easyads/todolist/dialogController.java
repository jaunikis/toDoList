package ie.easyads.todolist;

import ie.easyads.todolist.datamodel.TodoData;
import ie.easyads.todolist.datamodel.Todoitem;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDate;

public class dialogController {

    @FXML
    private TextField shortDescriptionField;

    @FXML
    private TextArea detailsArea;

    @FXML
    private DatePicker deadLinePicker;

    public Todoitem processResults(){
        String shortDescription = shortDescriptionField.getText().trim();
        String details = detailsArea.getText().trim();
        LocalDate deadLineValue= deadLinePicker.getValue();

        Todoitem newItem = new Todoitem(shortDescription,details,deadLineValue);
        TodoData.getInstance().addTodoItem(newItem);
        return newItem;
    }

}
