package cn.a114.klassmaster.gui.impl;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import me.iris.ambien.obfuscator.settings.data.implementations.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import me.iris.ambien.obfuscator.settings.data.*;
import me.iris.ambien.obfuscator.transformers.data.Transformer;

import java.util.Arrays;
import java.util.List;

public class TransformerConfigController {
    @FXML
    public VBox settingsContainer;

    public Transformer transformer;

    public void setTransformer(Transformer transformer) {
        this.transformer = transformer;
        doInit();
    }

    public void doInit() {
        settingsContainer.getChildren().clear();
        settingsContainer.getChildren().add(new Text(transformer.getDescription()));

        for (Setting<?> setting : transformer.getSettings()) {
            HBox hbox = new HBox(10);
            Label label = new Label(setting.getName() + ": ");
            hbox.getChildren().add(label);

            if (setting instanceof BooleanSetting) {
                CheckBox checkBox = new CheckBox();
                checkBox.setSelected(((BooleanSetting) setting).isEnabled());
                checkBox.selectedProperty().addListener((obs, oldVal, newVal) ->
                        ((BooleanSetting) setting).setEnabled(newVal));
                hbox.getChildren().add(checkBox);
            } else if (setting instanceof StringSetting) {
                TextField textField = new TextField(((StringSetting) setting).getValue());
                textField.textProperty().addListener((obs, oldVal, newVal) ->
                        ((StringSetting) setting).setValue(newVal));
                hbox.getChildren().add(textField);
            } else if (setting instanceof NumberSetting) {
                TextField numberField = new TextField(((NumberSetting) setting).getValue().toString());
                numberField.textProperty().addListener((obs, oldVal, newVal) -> {
                    try {
                        if (!newVal.isEmpty()) {
                            ((NumberSetting) setting).setValue(Integer.parseInt(newVal));
                        }
                    } catch (NumberFormatException e) {
                        numberField.setText(oldVal);
                    }
                });
                hbox.getChildren().add(numberField);
            } else if (setting instanceof StringListSetting) {
                // ChatGPT moment
                StringListSetting listSetting = (StringListSetting) setting;
                List<String> backingList = listSetting.getOptions();

                VBox listBox = new VBox(5);
                listBox.setFillWidth(true);

                // Helper to add a GUI row for an item
                Runnable refreshList = new Runnable() {
                    @Override
                    public void run() {
                        listBox.getChildren().clear();
                        for (int i = 0; i < backingList.size(); i++) {
                            final int index = i;
                            String value = backingList.get(i);

                            HBox row = new HBox(5);
                            TextField field = new TextField(value);

                            // Sync field → underlying list
                            field.textProperty().addListener((obs, oldVal, newVal) -> {
                                backingList.set(index, newVal.trim());
                            });

                            Button removeBtn = new Button("-");
                            removeBtn.setOnAction(evt -> {
                                backingList.remove(index);
                                run();
                            });

                            row.getChildren().addAll(field, removeBtn);
                            listBox.getChildren().add(row);
                        }
                    }
                };

                // Add button for new entries
                Button addBtn = new Button("+");
                addBtn.setOnAction(evt -> {
                    backingList.add("");
                    refreshList.run();
                });

                // Initial population
                refreshList.run();

                VBox wrapper = new VBox(5, listBox, addBtn);
                hbox.getChildren().add(wrapper);
            }


            settingsContainer.getChildren().add(hbox);
        }
    }

    @FXML
    public void onClose() {
        Stage stage = (Stage) settingsContainer.getScene().getWindow();
        stage.close();
    }
}
