package cn.a114.klassmaster.gui.impl;

import cn.a114.klassmaster.gui.data.CopyToClipboardJsonDTO;
import com.google.gson.Gson;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import me.iris.ambien.obfuscator.Ambien;

import java.net.URL;
import java.util.ResourceBundle;

public class AboutScreen implements Initializable {

    public static Stage stage;

    @FXML
    public Text version;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (version != null) {
            version.setText("version " + Ambien.VERSION);
        } else {
            version = new Text("null version wtfk");
        }
    }

    @FXML
    private void handler$copy() {
        ClipboardContent content = new ClipboardContent();
        CopyToClipboardJsonDTO data = new CopyToClipboardJsonDTO(
                System.getProperty("os.name"),
                System.getProperty("os.arch"),
                System.getProperty("os.version"),
                Ambien.VERSION
        );
        Gson g = new Gson();
        String json = g.toJson(data, CopyToClipboardJsonDTO.class);

        content.putString(json);
        Clipboard.getSystemClipboard().setContent(content);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText("Copied!");
        alert.showAndWait();
    }

    @FXML
    private void handler$close() {
        stage.close();
    }
}
