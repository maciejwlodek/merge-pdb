import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
Class for a residue in a pdb file
 */
public class Residue {

    private int residueNumber;
    private List<Atom> mainChainAtoms;
    private List<Atom> sideChainAtoms;

    public Residue(int residueNumber) {
        this.residueNumber = residueNumber;
        mainChainAtoms = new ArrayList<>();
        sideChainAtoms = new ArrayList<>();
    }

    public int getResidueNumber() {
        return residueNumber;
    }
    public void addMainChainAtom(Atom atom) {
        mainChainAtoms.add(atom);
    }
    public void addSideChainAtom(Atom atom) {
        sideChainAtoms.add(atom);
    }
    public List<Atom> getMainChainAtoms() {
        return mainChainAtoms;
    }
    public Atom getMainChainAtom(int i) {
        return mainChainAtoms.get(i);
    }
    public int numMainChainAtoms() {
        return mainChainAtoms.size();
    }
    public List<Atom> getSideChainAtoms() {
        return sideChainAtoms;
    }
    public Atom getSideChainAtom(int i) {
        return sideChainAtoms.get(i);
    }
    public int numSideChainAtoms() {
        return sideChainAtoms.size();
    }
    public void setAltLoc(char c) {
        mainChainAtoms.forEach(e -> e.setAltLoc(c));
        sideChainAtoms.forEach(e -> e.setAltLoc(c));
    }
    public void setOccupancy(double d) {
        mainChainAtoms.forEach(e -> e.setOccupancy(d));
        sideChainAtoms.forEach(e -> e.setOccupancy(d));
    }

    @Override
    public String toString() {
        return "Residue{" +
                "residueNumber = " + residueNumber +
                ", mainChainAtoms = " + mainChainAtoms +
                ", sideChainAtoms = " + sideChainAtoms +
                "}";
    }

    /*
    two residues considered equal iff they have an equivalent residue number and equal number of main and side chain atoms
    this is only used for purposes of testing compatibility of residues (PDBParser.makeListsCompatible(...))
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Residue residue = (Residue) o;
        return (residueNumber == residue.residueNumber) &&
                (mainChainAtoms.size()==residue.mainChainAtoms.size()) &&
                (sideChainAtoms.size()==residue.sideChainAtoms.size());
    }

    @Override
    public int hashCode() {
        return Objects.hash(residueNumber, mainChainAtoms, sideChainAtoms);
    }
}
