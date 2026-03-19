package cn.a114.klassmaster.gui.main;

import cn.a114.klassmaster.gui.impl.GUILicense;
import cn.a114.klassmaster.gui.impl.MainController;
import cn.a114.klassmaster.gui.util.ResourceUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import me.iris.ambien.obfuscator.Ambien;

import java.io.IOException;

public class KlassMasterGUIApp extends Application {

    public static void main(String... args) {
        // Initialize
        Ambien.get.initializeTransformers();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        // 先显示 License 窗口
        showLicDialog();

        // 如果同意，加载主界面
        FXMLLoader loader = new FXMLLoader(ResourceUtil.res(this.getClass(),"/cn/a114/klassmaster/gui/MainController.fxml"));
        Parent root = loader.load();

        MainController controller = loader.getController();
        controller.setStage(primaryStage);


//        image by bzym2
        primaryStage.getIcons().add(new Image(ResourceUtil.streamRes(this.getClass(),"/cn/a114/klassmaster/gui/aaa.png")));
        Scene scene = new Scene(root);
        primaryStage.setTitle("a114 Klassmaster");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showLicDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(ResourceUtil.res(this.getClass(),"/cn/a114/klassmaster/gui/GUILicense.fxml"));
            Parent root = loader.load();

            Stage dialog = new Stage(StageStyle.DECORATED);
            dialog.setTitle("a114 KlassMaster");
            dialog.initModality(Modality.APPLICATION_MODAL); // 模态，必须处理完才能继续
            dialog.setResizable(false);
            dialog.setScene(new Scene(root));

            // 拿到控制器
            GUILicense controller = loader.getController();
            // 设置回调
            controller.setOnAgree(dialog::close);
            controller.setOnDisagree(() -> {
                // Halt on disagree
                Runtime.getRuntime().halt(0);
            });

            dialog.showAndWait(); // 阻塞，直到用户操作
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}