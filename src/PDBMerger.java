import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
Class that merges pdb files based on proximity of corresponding residues
 */
public class PDBMerger {

    //global variable that counts the number of times residues have been merged together
    int numberOfMergers = 0;

    //TODO: make this into a user input variable
    int tempTolerance = 10;

    /*
    read the files in filenames, merge them together according to main and side tolerance, and write the merged file to finalFilename
     */
    void mergeFilesAndWrite(List<String> filenames, double mainChainTolerance, double sideChainTolerance, boolean multipleConformations, String finalFilename) throws IOException {
        PDBParser parser = new PDBParser();
        List<List<Residue>> allResidues = parser.readAllResidues(filenames);
        System.out.println(allResidues.get(0).size());
        List<String> templateFile = parser.generateTemplateFile(filenames.get(0), allResidues.get(0));
        List<Residue> mergedResidues = mergeResidues(mainChainTolerance, sideChainTolerance, multipleConformations, allResidues);
        List<String> mergedFile = generateMergedFile(mergedResidues, templateFile);
        parser.writePDBFile(mergedFile, finalFilename);
    }

    /*
    merge a list of lists of residues - each list comes from a separate file
    must be preprocessed by PDBParser to ensure compatibility
     */
    private List<Residue> mergeResidues(double mainChainTolerance, double sideChainTolerance, boolean multipleConformations, List<List<Residue>> residues) {
        int size = residues.get(0).size();
        double sharedOccupancy = 1.0 / ((double) residues.size());
        List<Residue> mergedList = new ArrayList<>();
        List<Boolean> tests = new ArrayList<>();
        List<Residue> meanResidues = new ArrayList<>();

        for(int i=0; i<size; i++) {
            Residue[] currentResidues = new Residue[residues.size()];
            for(int j=0; j<residues.size(); j++) {
                currentResidues[j] = residues.get(j).get(i);
            }
            Residue mean = meanResidue(currentResidues);
            meanResidues.add(mean);
            tests.add(testResidues(mainChainTolerance, sideChainTolerance, currentResidues, mean));
        }

        tests = smooth(tests, tempTolerance);

        for(int i=0; i< size; i++) {
            boolean currentMerge = tests.get(i);
            if(currentMerge) {
                numberOfMergers++;
                mergedList.add(meanResidues.get(i));
                System.out.println("MERGE");
            }
            else {
                if(multipleConformations) {
                    System.out.println();
                    for (int j = 0; j < residues.size(); j++) {
                        Residue currentResidue = residues.get(j).get(i);
                        currentResidue.setAltLoc(getAltLocKey(j));
                        currentResidue.setOccupancy(sharedOccupancy);
                        mergedList.add(currentResidue);
                    }
                }
            }
        }
        return mergedList;
    }

    /*
    combine the template file with the merged residues
     */
    private List<String> generateMergedFile(List<Residue> mergedResidues, List<String> templateFile) {
        List<String> mergedFile = new ArrayList<>();
        int residueCounter = 0;
        int serialID = 1;
        for(String line : templateFile) {
            if(!isTemplateLine(line)) mergedFile.add(line);
            else {
                int residueNumber = Integer.parseInt(line.split(" ")[1]);
                while(mergedResidues.size() > residueCounter && mergedResidues.get(residueCounter).getResidueNumber() == residueNumber) {
                    serialID = writeResidue(mergedFile, mergedResidues.get(residueCounter), serialID);
                    residueCounter++;
                }
            }
        }
        return mergedFile;
    }
    /*
    test if a given line will need to be replaced by one or more residues
     */
    private boolean isTemplateLine(String line) {
        return line.contains(PDBParser.TEMPLATE_LINE);
    }

    /*
    write each atom in a single residue to the file
     */
    private int writeResidue(List<String> file, Residue resToWrite, int serialNumber) {
        for(int i=0; i<resToWrite.numMainChainAtoms(); i++) {
            Atom currentAtom = resToWrite.getMainChainAtom(i);
            currentAtom.setSerialID(serialNumber);
            file.add(currentAtom.toLine());
            serialNumber++;
        }
        for(int i=0; i<resToWrite.numSideChainAtoms(); i++) {
            Atom currentAtom = resToWrite.getSideChainAtom(i);
            currentAtom.setSerialID(serialNumber);
            file.add(currentAtom.toLine());
            serialNumber++;
        }
        return serialNumber;
    }
    /*
    returns the alternate conformation character - 'A', 'B', 'C', ..., 'Z', 'a', 'b', ...
     */
    private char getAltLocKey(int index) {
        return (index<26)? (char) (index+65) : (char) (index+71);
    }
    /*
    test whether to merge group of corresponding residues based on proximity
     */
    private boolean testResidues(double mainChainTolerance, double sideChainTolerance, Residue[] residues, Residue meanResidue) {
        double sumOfMeans=0;
        int numMainAtoms = meanResidue.numMainChainAtoms();
        for(int i=0; i<numMainAtoms; i++) {
            double sumOfDistances=0;
            Atom meanAtom = meanResidue.getMainChainAtom(i);
            for(int j=0; j<residues.length; j++) {
                Atom currentAtom = residues[j].getMainChainAtom(i);
                sumOfDistances+=meanAtom.getDistance(currentAtom);
            }
            double meanDistance = sumOfDistances / ((double) residues.length);
            //if(meanDistance>mainChainTolerance) return false; //test each atom separately rather than average
            sumOfMeans+=meanDistance;
        }
        double averageOfMeans = numMainAtoms==0? 0 : sumOfMeans / ((double) numMainAtoms);
        if(averageOfMeans>mainChainTolerance) return false;

        double sumOfSideMeans=0;
        int numSideAtoms = meanResidue.numSideChainAtoms();
        for(int i=0; i<numSideAtoms; i++) {
            double sumOfDistances=0;
            Atom meanAtom = meanResidue.getSideChainAtom(i);
            for(int j=0; j<residues.length; j++) {
                Atom currentAtom = residues[j].getSideChainAtom(i);
                sumOfDistances+=meanAtom.getDistance(currentAtom);
            }
            double meanDistance = sumOfDistances / ((double) residues.length);
            //if(meanDistance>sideChainTolerance) return false;
            sumOfSideMeans+=meanDistance;
        }
        double averageOfSideMeans = numSideAtoms==0? 0 : sumOfSideMeans / ((double) numSideAtoms);
        if(averageOfSideMeans>sideChainTolerance) return false;
        return true;
    }
    /*
    Get the centroid of a group of residues
     */
    private Residue meanResidue(Residue[] residues) {
        Residue meanResidue = new Residue( residues[0].getResidueNumber());
        for(int i=0; i< residues[0].numMainChainAtoms(); i++) {
            Atom meanAtom = meanAtom(residues, i, true);
            meanResidue.addMainChainAtom(meanAtom);
        }
        for(int i=0; i< residues[0].numSideChainAtoms(); i++) {
            Atom meanAtom = meanAtom(residues, i, false);
            meanResidue.addSideChainAtom(meanAtom);
        }
        return meanResidue;
    }
    /*
    Get a centroid atom
     */
    private Atom meanAtom(Residue[] residues, int index, boolean mainChain) {
        double sumX=0;
        double sumY=0;
        double sumZ=0;
        for(int j=0; j< residues.length; j++) {
            Atom atom;
            if(mainChain) atom = residues[j].getMainChainAtom(index);
            else atom = residues[j].getSideChainAtom(index);
            sumX+=atom.getX();
            sumY+=atom.getY();
            sumZ+=atom.getZ();
        }
        double meanX = sumX/((double) residues.length);
        double meanY = sumY/((double) residues.length);
        double meanZ = sumZ/((double) residues.length);
        Atom meanAtom;
        if(mainChain) meanAtom = new Atom(residues[0].getMainChainAtom(index));
        else meanAtom = new Atom(residues[0].getSideChainAtom(index));
        meanAtom.setX(meanX);
        meanAtom.setY(meanY);
        meanAtom.setZ(meanZ);
        //meanAtom.setAltLoc('A');
        meanAtom.setOccupancy(1.0);
        //meanAtom.setChainID('A');
        //meanAtom.setTempFactor(something)??
        return meanAtom;
    }

    /*
    smooth a list of booleans by replacing value at index i with the mode of the sublist of radius=tolerance around i 
     */
    private static List<Boolean> smooth(List<Boolean> list, int tolerance) {

        List<Boolean> smoothList = new ArrayList<>();
        for(int i=0; i<list.size(); i++) {
            List<Boolean> subList = getSubList(list, i, tolerance);
            boolean mode =getMode(subList);
            smoothList.add(mode);
        }
        return smoothList;
    }
    /*
    get a sublist of list with radius=tolerance, centered around index i
     */
    private static List<Boolean> getSubList(List<Boolean> list, int i, int tolerance) {
        int lowerBound = (i<tolerance)? 0: i-tolerance;
        int upperBound = (i+tolerance>list.size())? list.size() : i+tolerance;
        return list.subList(lowerBound, upperBound);
    }
    /*
    get the mode of a list of booleans
     */
    private static boolean getMode(List<Boolean> list) {
        int counter=0;
        for(boolean b: list) counter+=b? 1:-1;
        return counter>=0;
    }
}
