import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

/*
Class to parse PDB files
 */

public class PDBParser {

    /*
    read a PDB file and save its contents to a list of strings, each string contains a single line(record)
     */
    List<String> openPDBFile(String pdbFileName) throws IOException {
        File pdbFile = new File(pdbFileName);
        FileReader fileReader = new FileReader(pdbFile);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        List<String> lines = bufferedReader.lines().collect(Collectors.toList());
        bufferedReader.close();
        fileReader.close();
        return lines;
    }
    /*
    write a pdb file line by line
     */
    void writePDBFile(List<String> lines, String pdbFileName) throws IOException {
        File pdbFile = new File(pdbFileName);
        FileWriter fileWriter = new FileWriter(pdbFile);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        lines.forEach(printWriter::println);
        printWriter.close();
        fileWriter.close();
    }


}
