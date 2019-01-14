import javafx.application.Application;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage)  {

        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10,10,10,10));
        vbox.setSpacing(20);
        vbox.setAlignment(Pos.CENTER);
        Button mergeButton = new Button("Choose files to merge");
        TextField mainTolerance = new TextField();
        mainTolerance.setPromptText("Enter main chain cutoff");
        TextField sideTolerance = new TextField();
        sideTolerance.setPromptText("Enter side chain cutoff");
        CheckBox multipleConformation = new CheckBox("Include multiple conformations");
        vbox.getChildren().addAll(mainTolerance, sideTolerance, multipleConformation, mergeButton);
        Scene scene = new Scene(vbox);
        vbox.requestFocus();
        primaryStage.setScene(scene);
        primaryStage.setTitle("Merge PDB files");
        primaryStage.setWidth(300);
        primaryStage.setHeight(300);
        primaryStage.show();

        mergeButton.setOnAction(e -> handleMergeButton(primaryStage, Double.parseDouble(mainTolerance.getText()), Double.parseDouble(sideTolerance.getText()), multipleConformation.isSelected()));
        mergeButton.disableProperty().bind(new BooleanBinding() {
            {bind(mainTolerance.textProperty(), sideTolerance.textProperty());}
            @Override
            protected boolean computeValue() {
                return !isDouble(mainTolerance.getText()) || !isDouble(sideTolerance.getText());
            }
        });
    }
    static void handleMergeButton (Stage primaryStage, double mainTolerance, double sideTolerance, boolean multipleConfs) {
        try {
            PDBParser parser = new PDBParser();
            List<String> files = showOpenDialog(primaryStage).stream().map(File::toString).collect(Collectors.toList());
            List<List<String>> allLines = new ArrayList<>();
            for(String fileName : files) {
                List<String> currentFile = parser.openPDBFile(fileName);
                allLines.add(currentFile);
            }
            PDBMerger mergeTool = new PDBMerger();
            List<String> mergedFile = mergeTool.merge(mainTolerance, sideTolerance, multipleConfs, allLines);
            parser.writePDBFile(mergedFile, "merge.pdb");
            System.out.println(mergeTool.numberOfMergers);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Merged file created");
            alert.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    static List<File> showOpenDialog(Stage primaryStage) {
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File("."));
        fc.setTitle("Choose files to merge");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDB Files", "*.pdb"));
        return fc.showOpenMultipleDialog(primaryStage);
    }
    static boolean isDouble(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch(NumberFormatException e) {
            return false;
        }
    }
}
