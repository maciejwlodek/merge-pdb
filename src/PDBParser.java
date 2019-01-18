import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/*
Class to parse PDB files
 */
public class PDBParser {

    private static final String ATOM_RECORD = "ATOM";
    private static final String HETATM_RECORD = "HETATM";
    public static final String TEMPLATE_LINE = "TEMPLATE";

    /*
    read residues from a list of files and make them compatible
     */
    List<List<Residue>> readAllResidues(List<String> files) throws IOException {
        List<List<Residue>> residues = new ArrayList<>();
        for(String fileName:files) {
            List<String> currentLines = openPDBFile(fileName);
            List<Residue> currentResidues = getResidues(currentLines);
            sortResidues(currentResidues);
            residues.add(currentResidues);
        }
        makeListsCompatible(residues);
        return residues;
    }

    /*
    read the residues from a PDB file
     */
    List<Residue> getResidues(List<String> lines) {
        List<Residue> residues = new ArrayList<>();
        Residue currentResidue = null;
        int currentResidueNumber = -1;
        int counter = 0;
        for(int i=0; i<lines.size(); i++){
            String currentLine = lines.get(i);
            if(!isAtom(currentLine)){
                if(currentResidue!=null) residues.add(currentResidue);
            }
            else {
                //System.out.println(currentLine);
                Atom currentAtom = new Atom(currentLine);
                int residueNumber = currentAtom.getResidueSequenceNumber();
                if(residueNumber != currentResidueNumber) {
                    if(currentResidue!=null) residues.add(currentResidue);
                    counter = 0;
                    currentResidueNumber = residueNumber;
                    currentResidue = new Residue(residueNumber);
                }
                Atom atom = new Atom(lines.get(i));
                if (counter < 4) {
                    currentResidue.addMainChainAtom(atom);
                } else {
                    currentResidue.addSideChainAtom(atom);
                }

                counter++;
            }
        }
        return residues;
    }

    /*
    generate a template file, which consists of all non-atom records, and template lines for each residue
     */
    List<String> generateTemplateFile(String filename, List<Residue> sortedList) {
        try {
            List<String> file = openPDBFile(filename);
            return generateTemplateFile(file, sortedList);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    /*
    generate a template file, which consists of all non-atom records, and template lines for each residue
     */
    List<String> generateTemplateFile(List<String> originalFile, List<Residue> sortedList) {
        int currentResidueNumber = -1;
        int residueIndex = 0;
        List<String> templateFile = new ArrayList<>();
        boolean readingAtoms = false;
        for(int i=0; i<originalFile.size(); i++) {
            String currentLine = originalFile.get(i);
            if(!isAtom(currentLine)) {
                if(readingAtoms) {
                    for (Residue r : sortedList) {
                        if (r.getResidueNumber() == currentResidueNumber) {
                            templateFile.add(TEMPLATE_LINE + " " + currentResidueNumber);
                            break;
                        }
                    }
                }
                readingAtoms=false;
                templateFile.add(currentLine);
            }
            else {
                readingAtoms = true;
                int residueNumber = new Atom(currentLine).getResidueSequenceNumber();
                if(residueNumber != currentResidueNumber) {
                    for(Residue r: sortedList) {
                        if(r.getResidueNumber() == currentResidueNumber) {
                            templateFile.add(TEMPLATE_LINE + " " + currentResidueNumber);
                            break;
                        }
                    }
                    currentResidueNumber = residueNumber;
                }
            }
        }
        String lastLine = originalFile.get(originalFile.size()-1);
        if(isAtom(lastLine)) {
            for(Residue r: sortedList) {
                if(r.getResidueNumber() == currentResidueNumber) {
                    templateFile.add(TEMPLATE_LINE + " " + currentResidueNumber);
                    break;
                }
            }
        }
        return templateFile;
    }

    /*
    test whether a given line is a record of an atom
     */
    public static boolean isAtom(String record) {
        return record.contains(ATOM_RECORD) || record.contains(HETATM_RECORD);
    }

    /*
    sort the residues by residue number, in case they are not in order initially
     */
    void sortResidues(List<Residue> residues) {
        residues.sort(Comparator.comparingInt(Residue::getResidueNumber));
    }

    /*
    remove all incompatible residues from each list
    e.g. if list1 contains residues 5,6,8 and list2 contains 5,7,8 -> 6 removed from list1, 7 removed from list2
     */
    void makeListsCompatible(List<List<Residue>> lists) {
        List<Residue> intersection = new ArrayList<>(lists.get(0));
        for(List<Residue> residues : lists) {
            intersection.retainAll(residues);
        }
        for(List<Residue> residues : lists) {
            residues.retainAll(intersection);
        }
    }

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
