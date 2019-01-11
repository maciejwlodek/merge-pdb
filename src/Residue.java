import java.util.ArrayList;
import java.util.List;

public class Residue {

    int residueNumber;
    List<Atom> mainChainAtoms;
    List<Atom> sideChainAtoms;

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
    public List<Atom> getSideChainAtoms() {
        return sideChainAtoms;
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
