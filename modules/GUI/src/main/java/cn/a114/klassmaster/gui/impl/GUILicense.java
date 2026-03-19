package cn.a114.klassmaster.gui.impl;


import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import lombok.Getter;
import lombok.Setter;
import me.iris.ambien.obfuscator.Ambien;

import java.net.URL;
import java.util.ResourceBundle;

public class GUILicense implements Initializable {


    // Constructor: No FXML-related initialization here.
    public GUILicense() {
        // This constructor is called by the FXMLLoader *before* @FXML fields are injected.
        // Any initialization related to @FXML fields should happen in initialize().
    }

    // The initialize method is called by the FXMLLoader *after* all @FXML fields
    // have been injected. This is the perfect place to set the text for 'version'.
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (version != null) {
            version.setText("version " + Ambien.VERSION);
        }
    }

    @FXML
    public Text version;

    @FXML
    private Button agreeButton;

    @FXML
    private Button disagreeButton;

    @Getter
    private boolean agreed = false;

    @Setter
    private Runnable onAgree;
    @Setter
    private Runnable onDisagree;

    @FXML
    private void onAgree() {
        agreed = true;
        if (onAgree != null) onAgree.run();
    }

    @FXML
    private void onDisagree() {
        agreed = false;
        if (onDisagree != null) onDisagree.run();
    }

}