import java.util.ArrayList;
import java.util.List;

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

    @Override
    public String toString() {
        return "Residue{" +
                "residueNumber = " + residueNumber +
                "mainChainAtoms = " + mainChainAtoms +
                "sideChainAtoms = " + sideChainAtoms +
                "}";
    }


}
