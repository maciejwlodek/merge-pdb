import java.util.ArrayList;
import java.util.List;

public class PDBMerger {

    private final static String ATOM_RECORD = "ATOM";
    private static final String ALT_STRING = "AA";

    List<String> merge(double mainChainTolerance, double sideChainTolerance, boolean multipleConformations, List<List<String>> lists) {
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
                    if(counter<4) {
                        currentResidues[j].addMainChainAtom(atom);
                    } else {
                        currentResidues[j].addSideChainAtom(atom);
                    }
                }
                counter++;
            }
        }
        return mergedList;
    }
    List<String> merge(double mainChainTolerance, double sideChainTolerance, boolean multipleConformations, List<String>... lists) {
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

    int mergeSingleResidue(List<String> mergedList, Residue[] currentResidues, int serialID, double sharedOccupancy, double mainChainTolerance, double sideChainTolerance, boolean multipleConformations) {
        Residue meanResidue = meanResidue(currentResidues);
        boolean merging = testResidues(mainChainTolerance, sideChainTolerance, currentResidues, meanResidue);
        if (merging) {
            System.out.println("Merging residue " + meanResidue.getResidueNumber());
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
    char getAltLocKey(int index) {
        return (index<26)? (char) (index+65) : (char) (index+71);
    }
    boolean testResidues(double mainChainTolerance, double sideChainTolerance, Residue[] residues, Residue meanResidue) {
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
            sumOfSideMeans+=meanDistance;
        }
        double averageOfSideMeans = numSideAtoms==0? 0 : sumOfSideMeans / ((double) numSideAtoms);
        if(averageOfSideMeans>sideChainTolerance) return false;
        return true;
    }
    Residue meanResidue(Residue[] residues) {
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
    Atom meanAtom(Residue[] residues, int index, boolean mainChain) {
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
        meanAtom.setChainID('A');
        //meanAtom.setTempFactor(something)??
        return meanAtom;
    }
}
