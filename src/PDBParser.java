import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

public class PDBParser {

    public PDBParser() {

    }

    List<String> openPDBFile(String pdbFileName) throws IOException {
        File pdbFile = new File(pdbFileName);
        FileReader fileReader = new FileReader(pdbFile);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        List<String> lines = bufferedReader.lines().collect(Collectors.toList());
        bufferedReader.close();
        fileReader.close();
        return lines;
    }
    void writePDBFile(List<String> lines, String pdbFileName) throws IOException {
        File pdbFile = new File(pdbFileName);
        FileWriter fileWriter = new FileWriter(pdbFile);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        lines.forEach(printWriter::println);
        printWriter.close();
        fileWriter.close();
    }


}
