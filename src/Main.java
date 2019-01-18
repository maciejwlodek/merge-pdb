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
import java.util.List;
import java.util.stream.Collectors;

public class Main extends Application {
    /*
    launch the application
     */
    public static void main(String[] args) {
        launch(args);
    }

    /*
    display the application window
     */
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
        //disable the merge button if either the mainChainTolerance or the sideChainTolerance is not a number
        mergeButton.disableProperty().bind(new BooleanBinding() {
            {bind(mainTolerance.textProperty(), sideTolerance.textProperty());}
            @Override
            protected boolean computeValue() {
                return !isDouble(mainTolerance.getText()) || !isDouble(sideTolerance.getText());
            }
        });
    }
    /*
    prompt user to choose pdb files and merge them together
     */
    static void handleMergeButton (Stage primaryStage, double mainTolerance, double sideTolerance, boolean multipleConfs) {
        try {
            List<String> files = showOpenDialog(primaryStage).stream().map(File::toString).collect(Collectors.toList());
            PDBMerger mergeTool = new PDBMerger();
            //TODO: make finalFilename a user-inputted option
            mergeTool.mergeFilesAndWrite(files, mainTolerance, sideTolerance, multipleConfs, "merge.pdb");
            System.out.println(mergeTool.numberOfMergers);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Merged file created");
            alert.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /*
    show a file chooser dialog so the user may choose which files to merge
     */
    static List<File> showOpenDialog(Stage primaryStage) {
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File("."));
        fc.setTitle("Choose files to merge");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDB Files", "*.pdb"));
        return fc.showOpenMultipleDialog(primaryStage);
    }
    /*
    helper method to test if a string is a number
     */
    static boolean isDouble(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch(NumberFormatException e) {
            return false;
        }
    }
}
