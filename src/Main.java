import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    final static String ATOM_RECORD = "ATOM";
    final static double mainTolerance = 1.2; //max standard deviation of main chain atoms in residues
    final static double sideTolerance = 4.0; //max standard deviation of side chain atoms in residues
    final static String[] filenames = { "ChainA_fit.pdb", "ChainB_fit.pdb", "ChainC_fit.pdb", "ChainD_fit.pdb", "ChainE_fit.pdb", "ChainF_fit.pdb"};
    private static final String ALT_STRING = "AA";
    static int total=0;
    static boolean multipleConfs = false;
    public static void main(String[] args) {
        try {
            List<List<String>> allFiles = new ArrayList<>();
            for(int i=1; i<26; i++) {
                String fileName = "testData/" + i + ".pdb";
                List<String> currentFile = openPDBFile(fileName);
                allFiles.add(currentFile);
            }

            List<String> mergedFile = merge(mainTolerance, sideTolerance, multipleConfs, allFiles);
            writePDBFile(mergedFile, "testData/merged.pdb");

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(total);

    }
    static List<String> merge(double mainChainTolerance, double sideChainTolerance, boolean multipleConformations, List<List<String>> lists) {
        int size = lists.get(0).size();
        double sharedOccupancy = 1.0 / ((double) lists.size());
        List<String> mergedList = new ArrayList<>();
        int currentResidueNumber = -1;
        Residue[] currentResidues = new Residue[lists.size()];
        int counter = 0;
        int serialID = 1;
        for(int i=0; i<size; i++){
            String currentLine = lists.get(0).get(i);
            if(!currentLine.contains(ATOM_RECORD)){
                if(currentResidues[0]!=null) serialID = mergeSingleResidue(mergedList, currentResidues, serialID, sharedOccupancy, mainChainTolerance, sideChainTolerance, multipleConformations);
                mergedList.add(currentLine);
            }
            else {
                Atom currentAtom = new Atom(currentLine);
                int residueNumber = currentAtom.getResidueSequenceNumber();
                if(residueNumber != currentResidueNumber) {
                    if(currentResidues[0]!=null) serialID = mergeSingleResidue(mergedList, currentResidues, serialID, sharedOccupancy, mainChainTolerance, sideChainTolerance, multipleConformations);
                    counter = 0;
                    currentResidueNumber = residueNumber;
                    for(int j=0; j<lists.size(); j++) {
                        currentResidues[j] = new Residue(currentResidueNumber);
                    }
                }
                for(int j=0; j<lists.size(); j++) {
                    Atom atom = new Atom(lists.get(j).get(i));
                    if(counter<4) { //atom is main chain
                        currentResidues[j].addMainChainAtom(atom);
                    } else { //atom is side chain
                        currentResidues[j].addSideChainAtom(atom);
                    }
                }
                counter++;
            }
        }
        return mergedList;
    }
    static List<String> merge(double mainChainTolerance, double sideChainTolerance, boolean multipleConformations, List<String>... lists) {
        int size = lists[0].size();
        double sharedOccupancy = 1.0 / ((double) lists.length);
        List<String> mergedList = new ArrayList<>();
        int currentResidueNumber = -1;
        Residue[] currentResidues = new Residue[lists.length];
        int counter = 0;
        int serialID = 1;
        for(int i=0; i<size; i++){
            String currentLine = lists[0].get(i);
            if(!currentLine.contains(ATOM_RECORD)){
                if(currentResidues[0]!=null) serialID = mergeSingleResidue(mergedList, currentResidues, serialID, sharedOccupancy, mainChainTolerance, sideChainTolerance, multipleConformations);
                mergedList.add(currentLine);
            }
            else {
                Atom currentAtom = new Atom(currentLine);
                int residueNumber = currentAtom.getResidueSequenceNumber();
                if(residueNumber != currentResidueNumber) {
                    if(currentResidues[0]!=null) serialID = mergeSingleResidue(mergedList, currentResidues, serialID, sharedOccupancy, mainChainTolerance, sideChainTolerance, multipleConformations);
                    counter = 0;
                    currentResidueNumber = residueNumber;
                    for(int j=0; j<lists.length; j++) {
                        currentResidues[j] = new Residue(currentResidueNumber);
                    }
                }
                for(int j=0; j<lists.length; j++) {
                    Atom atom = new Atom(lists[j].get(i));
                    if(counter<4) { //atom is main chain
                        currentResidues[j].addMainChainAtom(atom);
                    } else { //atom is side chain
                        currentResidues[j].addSideChainAtom(atom);
                    }
                }
                counter++;
            }
        }
        return mergedList;
    }

    static int mergeSingleResidue(List<String> mergedList, Residue[] currentResidues, int serialID, double sharedOccupancy, double mainChainTolerance, double sideChainTolerance, boolean multipleConformations) {
        Residue meanResidue = meanResidue(currentResidues);
        boolean merging = testResidues(mainChainTolerance, sideChainTolerance, currentResidues, meanResidue);
        if (merging) {
            System.out.println("Merging residue " + meanResidue.getResidueNumber());
            total++;
            for (Atom mainChainAtom : meanResidue.getMainChainAtoms()) {
                mainChainAtom.setSerialID(serialID);
                serialID++;
                mergedList.add(mainChainAtom.toLine());
            }
            for (Atom sideChainAtom : meanResidue.getSideChainAtoms()) {
                sideChainAtom.setSerialID(serialID);
                serialID++;
                mergedList.add(sideChainAtom.toLine());
            }
        } else {
            if(multipleConformations) {
                for (int k = 0; k < currentResidues.length; k++) {
                    Residue r = currentResidues[k];
                    for (Atom mainAtom : r.getMainChainAtoms()) {
                        mainAtom.setAltLoc(getAltLocKey(k));
                        mainAtom.setSerialID(serialID);
                        serialID++;
                        mainAtom.setOccupancy(sharedOccupancy);
                        mainAtom.setChainID('A');
                        StringBuilder stringBuilder = new StringBuilder(mainAtom.toLine());
                        stringBuilder.replace(72, 74, ALT_STRING);
                        mergedList.add(stringBuilder.toString());
                    }
                    for (Atom sideAtom : r.getSideChainAtoms()) {
                        sideAtom.setAltLoc(getAltLocKey(k));
                        sideAtom.setSerialID(serialID);
                        serialID++;
                        sideAtom.setOccupancy(sharedOccupancy);
                        sideAtom.setChainID('A');
                        StringBuilder stringBuilder = new StringBuilder(sideAtom.toLine());
                        stringBuilder.replace(72, 74, ALT_STRING);
                        mergedList.add(stringBuilder.toString());
                    }
                }
            }
        }
        return serialID;
    }
    static char getAltLocKey(int index) {
        return (index<26)? (char) (index+65) : (char) (index+71);
    }
    static boolean testResidues(double mainChainTolerance, double sideChainTolerance, Residue[] residues, Residue meanResidue) {
        double sumOfMeans=0;
        int numMainAtoms = meanResidue.getMainChainAtoms().size();
        for(int i=0; i<numMainAtoms; i++) {
            double sumOfDistances=0;
            Atom meanAtom = meanResidue.getMainChainAtoms().get(i);
            for(int j=0; j<residues.length; j++) {
                Atom currentAtom = residues[j].getMainChainAtoms().get(i);
                sumOfDistances+=meanAtom.getDistance(currentAtom);
            }
            double meanDistance = sumOfDistances / ((double) residues.length);
            sumOfMeans+=meanDistance;
        }
        double averageOfMeans = numMainAtoms==0? 0 : sumOfMeans / ((double) numMainAtoms);
        if(averageOfMeans>mainChainTolerance) return false;

        double sumOfSideMeans=0;
        int numSideAtoms = meanResidue.getSideChainAtoms().size();
        for(int i=0; i<numSideAtoms; i++) {
            double sumOfDistances=0;
            Atom meanAtom = meanResidue.getSideChainAtoms().get(i);
            for(int j=0; j<residues.length; j++) {
                Atom currentAtom = residues[j].getSideChainAtoms().get(i);
                sumOfDistances+=meanAtom.getDistance(currentAtom);
            }
            double meanDistance = sumOfDistances / ((double) residues.length);
            sumOfSideMeans+=meanDistance;
        }
        double averageOfSideMeans = numSideAtoms==0? 0 : sumOfSideMeans / ((double) numSideAtoms);
        if(averageOfSideMeans>sideChainTolerance) return false;
        return true;
    }
    static Residue meanResidue(Residue[] residues) {
        Residue meanResidue = new Residue( residues[0].getResidueNumber());
        for(int i=0; i< residues[0].getMainChainAtoms().size(); i++) {
            Atom meanAtom = meanAtom(residues, i, true);
            meanResidue.addMainChainAtom(meanAtom);
        }
        for(int i=0; i< residues[0].getSideChainAtoms().size(); i++) {
            Atom meanAtom = meanAtom(residues, i, false);
            meanResidue.addSideChainAtom(meanAtom);
        }
        return meanResidue;
    }
    static Atom meanAtom(Residue[] residues, int index, boolean mainChain) {
        double sumX=0;
        double sumY=0;
        double sumZ=0;
        for(int j=0; j< residues.length; j++) {
            Atom atom;
            if(mainChain) atom = residues[j].getMainChainAtoms().get(index);
            else atom = residues[j].getSideChainAtoms().get(index);
            sumX+=atom.getX();
            sumY+=atom.getY();
            sumZ+=atom.getZ();
        }
        double meanX = sumX/((double) residues.length);
        double meanY = sumY/((double) residues.length);
        double meanZ = sumZ/((double) residues.length);
        Atom meanAtom;
        if(mainChain) meanAtom = new Atom(residues[0].getMainChainAtoms().get(index));
        else meanAtom = new Atom(residues[0].getSideChainAtoms().get(index));
        meanAtom.setX(meanX);
        meanAtom.setY(meanY);
        meanAtom.setZ(meanZ);
        //meanAtom.setAltLoc('A');
        meanAtom.setOccupancy(1.0);
        meanAtom.setChainID('A');
        //meanAtom.setTempFactor(something)??
        return meanAtom;
    }

    static List<String> openPDBFile(String pdbFileName) throws IOException {
        File pdbFile = new File(pdbFileName);
        FileReader fileReader = new FileReader(pdbFile);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        List<String> lines = bufferedReader.lines().collect(Collectors.toList());
        bufferedReader.close();
        fileReader.close();
        return lines;
    }
    static void writePDBFile(List<String> lines, String pdbFileName) throws IOException {
        File pdbFile = new File(pdbFileName);
        FileWriter fileWriter = new FileWriter(pdbFile);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        lines.forEach(printWriter::println);
        printWriter.close();
        fileWriter.close();
    }

}
