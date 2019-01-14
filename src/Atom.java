/*
Class for a single atom record in a pdb file
 */
public class Atom {

    private final int[] splittingIndices = {0, 6, 11, 12, 16, 17, 20, 21, 22, 26, 27, 30, 38, 46, 54, 60, 66, 76, 78, 79};
    /*
    Parameters are read from a single atom record in a pdb file
    [0] = Record name, always "ATOM  "
    [1] = Serial number, #####
    [2] = blank
    [3] = Atom name, 4 chars padded with spaces e.g. " CD2"
    [4] = AltLoc, 1 char
    [5] = Residue name, 3 chars e.g. "LEU"
    [6] = blank
    [7] = ChainID, 1 char
    [8] = Residue sequence number, ####
    [9] = iCode, 1 char
    [10] = blank
    [11] = X coordinate, ####.### e.g. "  -2.320"
    [12] = Y coordinate
    [13] = Z coordinate
    [14] = Occupancy, ###.##, e.g. "  1.00"
    [15] = TempFactor, ###.##
    [16] = blank
    [17] = "AA" if alternate, blank otherwise
    [18] = blank
    [19] = Element

     */
    private String[] parameters;

    public Atom(String line) {
        parameters = splitString(line, splittingIndices);
    }

    public Atom(Atom anotherAtom) {
        this(anotherAtom.toLine());
    }

    /*
    Read parameters from single ATOM record in PDB file
     */
    private String[] splitString(String lineToSplit, int... indices) {
        String[] split = new String[indices.length - 1];
        for (int i = 0; i < split.length; i++) {
            split[i] = lineToSplit.substring(indices[i], indices[i + 1]);
        }
        return split;
    }

    double getDistance(Atom anotherAtom) {
        double dx = anotherAtom.getX() - getX();
        double dy = anotherAtom.getY() - getY();
        double dz = anotherAtom.getZ() - getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    @Override
    public String toString() {
        return "Atom{" +
                "x=" + getX() +
                ", y=" + getY() +
                ", z=" + getZ() +
                ", residueSequenceNumber=" + getResidueSequenceNumber() +
                '}';
    }
    //Convert the parameters back to an ATOM pdb record
    public String toLine() {
        String line = "";
        StringBuilder stringBuilder = new StringBuilder(line);
        for (int i = 0; i < parameters.length; i++) {
            stringBuilder.append(parameters[i]);
        }
        return stringBuilder.toString();
    }
    /*
    format a double to have exact number of leading and trailing digits
    Leading digits are padded with spaces, trailing digits padded with zeros
     */
    private static String formatDouble(double d, int leadingDigits, int trailingDigits) {
        StringBuilder stringBuilder = new StringBuilder();
        int digits = (d + "").indexOf('.');
        for (int i = 0; i < leadingDigits - digits; i++) {
            stringBuilder.append(" ");
        }
        String formattingString = "%." + trailingDigits + "f";
        stringBuilder.append(String.format(formattingString, d));
        return stringBuilder.toString();
    }

    public double getOccupancy() {
        return Double.parseDouble(parameters[14].trim());
    }
    public void setOccupancy(double occupancy) {
        parameters[14] = formatDouble(occupancy, 3, 2);
    }
    public double getX() {
        return Double.parseDouble(parameters[11].trim());
    }
    public double getY() {
        return Double.parseDouble(parameters[12].trim());
    }
    public double getZ() {
        return Double.parseDouble(parameters[13].trim());
    }
    public void setX(double x) {
        parameters[11] = formatDouble(x, 4, 3);
    }
    public void setY(double y) {
        parameters[12] = formatDouble(y, 4, 3);
    }
    public void setZ(double z) {
        parameters[13] = formatDouble(z, 4, 3);
    }
    public int getResidueSequenceNumber() {
        return Integer.parseInt(parameters[8].trim());
    }
    public char getAltLoc() {
        return parameters[4].charAt(0);
    }
    public void setAltLoc(char c) {
        parameters[4] = c + "";
    }
    public int getSerialID() {
        return Integer.parseInt(parameters[1].trim());
    }
    public void setSerialID(int id) {
        parameters[1] = formatDouble(id, 5, 0);
    }
    public char getChainID() {
        return parameters[7].charAt(0);
    }
    public void setChainID(char id) {
        parameters[7] = id+"";
    }
    public String getResidueCode() {
        return parameters[5];
    }

}
