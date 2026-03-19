package cn.a114.klassmaster.gui.impl;

import cn.a114.commonutil.wtf.CrazyStuff;
import cn.a114.klassmaster.gui.util.ResourceUtil;
import com.google.gson.JsonArray;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.entry.Entrypoint;
import me.iris.ambien.obfuscator.settings.SettingsManager;
import me.iris.ambien.obfuscator.transformers.data.Transformer;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Objects;
import java.util.ResourceBundle;

@SuppressWarnings("unused")
public class MainController implements Initializable {

    @FXML
    // Bottom text
    public TextField text;


    // 还是搞拥有图形化界面的settings.json编辑器吧，操
    @FXML
    private ListView<Transformer> transformerListView;


    @Setter
    private Stage stage; // 用于访问窗口，如打开文件对话框

    private IOData dat;

    // 这个方法由 FXMLLoader 在加载 FXML 文件后自动调用@FXML
    public void initialize(URL loc, ResourceBundle sb) {
        // Zelix Klassmaster
        text.setText("Status");
        dat = new IOData(null, null);
        Ambien.INSTANCE.initializeTransformers();
        transformerListView.getItems().addAll(Ambien.INSTANCE.transformerManager.getTransformers());

        // 给 transformerListView 添加双击事件
        transformerListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // 双击
                configureSelectedTransformer();
            }
        });
        transformerListView.setOnKeyPressed(it -> {
            if (it.getCode().equals(KeyCode.ENTER)) {
                configureSelectedTransformer();
            }
        });
    }

    private void configureSelectedTransformer() {
        Transformer selected = transformerListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openTransformerConfig(selected);
        }
    }


    public void openTransformerConfig(Transformer transformer) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    ResourceUtil.res(
                            "/cn/a114/klassmaster/gui/TransformerConfig.fxml"
                    )
            );
            Parent root = loader.load();

            TransformerConfigController controller = loader.getController();
            controller.setTransformer(transformer);

            Stage configStage = new Stage();
            configStage.setTitle("Configure - " + transformer.getName());
            Scene scene = new Scene(root);
            configStage.setScene(scene);
            scene.setOnKeyPressed(it -> {
                if (it.getCode().equals(KeyCode.ESCAPE)) {
                    configStage.close();
                }
            });

            configStage.initOwner(stage);
            configStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handler$handleOpenFile(ActionEvent e) {
        dat.in = apiShit(false);
    }

    public void handler$handleSaveFile(ActionEvent e) {
        dat.out = apiShit(true);
    }

    public void handler$obfuscate(ActionEvent e) {
        if (dat.in == null || dat.out == null) {
            JOptionPane.showMessageDialog(null, "Select input & output first!");
            return;
        }
        try {
            File tempJson = Files.createTempFile("blah", ".tmp").toFile();
            SettingsManager.createMethodForAPIS(
                    tempJson,
                    dat.in,
                    dat.out,
                    new JsonArray(),
                    new JsonArray(),
                    false
            );
            Entrypoint.main(new String[]{"-c", tempJson.getAbsolutePath()});
        } catch (IOException ex) {
            text.setText("Failed to create configuration file for obfuscation, see stderr for more details.");
            ex.printStackTrace();
        }
    }

    public void handler$help_about(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    ResourceUtil.res(
                            "/cn/a114/klassmaster/gui/AboutScreen.fxml"
                    )
            );
            Parent root = loader.load();

            Stage configStage = new Stage();
            configStage.setTitle("About a114 klassmaster");
            Scene scene = new Scene(root);
            AboutScreen.stage = configStage;
            configStage.setScene(scene);
            scene.setOnKeyPressed(it -> {
                if (it.getCode().equals(KeyCode.ESCAPE)) {
                    configStage.close();
                }
            });

            configStage.initOwner(stage);
            configStage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void exit(ActionEvent e) {
        CrazyStuff.halt(0);
        System.exit(0);
    }

    public void handler$gc() {
        // Zelix Klassmaster LMAO
        long before = Runtime.getRuntime().freeMemory();
        String parsed = Long.toString(before / 1024) + "K";
        System.gc();
        text.setText(parsed + " in use before garbage collection");
    }

    private String apiShit(boolean save) {
        FileChooser fileChooser = new FileChooser();
        File saveFile = save ? fileChooser.showSaveDialog(stage)
                :
                fileChooser.showOpenDialog(stage);
        if (saveFile != null) {
            return saveFile.getAbsolutePath();
        }
        return null;
    }


    @Setter
    @Getter
    private static final class IOData {

        private String in;
        private String out;

        private IOData(String in, String out) {
            this.in = in;
            this.out = out;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            IOData that = (IOData) obj;
            return Objects.equals(this.in, that.in) &&
                    Objects.equals(this.out, that.out);
        }

        @Override
        public String toString() {
            return "IOData[" +
                    "in=" + in + ", " +
                    "out=" + out + ']';
        }
    }
}