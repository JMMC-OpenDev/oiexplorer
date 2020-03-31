/*
 * This code is part of the Java FITS library developed 1996-2012 by T.A. McGlynn (NASA/GSFC)
 * The code is available in the public domain and may be copied, modified and used
 * by anyone in any fashion for any purpose without restriction. 
 * 
 * No warranty regarding correctness or performance of this code is given or implied.
 * Users may contact the author if they have questions or concerns.
 * 
 * The author would like to thank many who have contributed suggestions, 
 * enhancements and bug fixes including:
 * David Glowacki, R.J. Mathar, Laurent Michel, Guillaume Belanger,
 * Laurent Bourges, Rose Early, Fred Romelfanger, Jorgo Baker, A. Kovacs, V. Forchi, J.C. Segovia,
 * Booth Hartley and Jason Weiss.  
 * I apologize to any contributors whose names may have been inadvertently omitted.
 * 
 *      Tom McGlynn
 */
package fr.nom.tam.fits;

import fr.nom.tam.util.ArrayDataOutput;
import fr.nom.tam.util.ArrayFuncs;
import fr.nom.tam.util.ColumnTable;

/** FITS binary table header/data unit */
public final class BinaryTableHDU
        extends TableHDU {

    private BinaryTable table;
    /** The standard column keywords for a binary table. */
    private String[] keyStems = {"TTYPE", "TFORM", "TUNIT", "TNULL", "TSCAL", "TZERO", "TDISP", "TDIM"};

    public BinaryTableHDU(Header hdr, Data datum) {

        super((TableData) datum);
        myHeader = hdr;
        myData = datum;
        table = (BinaryTable) datum;

    }

    /** Create data from a binary table header.
     * @param header the template specifying the binary table.
     * @exception FitsException if there was a problem with the header.
     */
    public static Data manufactureData(Header header) throws FitsException {
        return new BinaryTable(header);
    }

    public Data manufactureData() throws FitsException {
        return manufactureData(myHeader);
    }

    /** Build a binary table HDU from the supplied data.
     * @param data   the data used to build the binary table.  This is typically
     *               some kind of array of objects.
     * @exception FitsException if there was a problem with the data.
     */
    public static Header manufactureHeader(Data data) throws FitsException {
        Header hdr = new Header();
        data.fillHeader(hdr);
        return hdr;
    }

    /** Encapsulate data in a BinaryTable data type */
    public static Data encapsulate(Object o) throws FitsException {

        if (o instanceof ColumnTable) {
            return new BinaryTable((ColumnTable) o);
        } else if (o instanceof Object[][]) {
            return new BinaryTable((Object[][]) o);
        } else if (o instanceof Object[]) {
            return new BinaryTable((Object[]) o);
        } else {
            throw new FitsException("Unable to encapsulate object of type:"
                    + o.getClass().getName() + " as BinaryTable");
        }
    }

    /** Check that this is a valid binary table header.
     * @param header to validate.
     * @return <CODE>true</CODE> if this is a binary table header.
     */
    public static boolean isHeader(Header header) {
        // LAURENT : use getTrimmedStringValue instead of getStringValue :
        String xten = header.getTrimmedStringValue("XTENSION");
        if (xten == null) {
            return false;
        }
        if (xten.equals("BINTABLE") || xten.equals("A3DTABLE")) {
            return true;
        }
        return false;
    }

    /** Check that this HDU has a valid header.
     * @return <CODE>true</CODE> if this HDU has a valid header.
     */
    public boolean isHeader() {
        return isHeader(myHeader);
    }

    /* Check if this data object is consistent with a binary table.  There
     * are three options:  a column table object, an Object[][], or an Object[].
     * This routine doesn't check that the dimensions of arrays are properly
     * consistent.
     */
    public static boolean isData(Object o) {

        if (o instanceof ColumnTable || o instanceof Object[][]
                || o instanceof Object[]) {
            return true;
        } else {
            return false;
        }
    }

    /** Add a column without any associated header information.
     *
     * @param data The column data to be added.  Data should be an Object[] where
     *             type of all of the constituents is identical.  The length
     *             of data should match the other columns.  <b> Note:</b> It is
     *             valid for data to be a 2 or higher dimensionality primitive
     *             array.  In this case the column index is the first (in Java speak)
     *             index of the array.  E.g., if called with int[30][20][10], the
     *             number of rows in the table should be 30 and this column
     *             will have elements which are 2-d integer arrays with TDIM = (10,20).
     * @exception FitsException the column could not be added.
     */
    public int addColumn(Object data) throws FitsException {

        int col = table.addColumn(data);
        table.pointToColumn(getNCols() - 1, myHeader);
        return col;
    }

    // Need to tell header about the Heap before writing.
    public void write(ArrayDataOutput ado) throws FitsException {

        int oldSize = myHeader.getIntValue("PCOUNT");
        if (oldSize != table.getHeapSize()) {
            myHeader.addValue("PCOUNT", table.getHeapSize(), "ntf::binarytablehdu:pcount:1");
        }

        if (myHeader.getIntValue("PCOUNT") == 0) {
            myHeader.deleteKey("THEAP");
        } else {
            myHeader.getIntValue("TFIELDS");
            int offset = myHeader.getIntValue("NAXIS1")
                    * myHeader.getIntValue("NAXIS2")
                    + table.getHeapOffset();
            myHeader.addValue("THEAP", offset, "ntf::binarytablehdu:theap:1");
        }

        super.write(ado);
    }

    /**
     * Convert a column in the table to complex. Only tables with appropriate
     * types and dimensionalities can be converted. It is legal to call this on
     * a column that is already complex.
     *
     * @param index  The 0-based index of the column to be converted.
     * @return Whether the column can be converted
     * @throws FitsException
     */
    public boolean setComplexColumn(int index) throws FitsException {
        boolean status = false;
        if (table.setComplexColumn(index)) {

            // No problem with the data.  Make sure the header
            // is right.
            int[] dimens = table.getDimens()[index];
            Class base = table.getBases()[index];

            int dim = 1;
            String tdim = "";
            String sep = "";
            // Don't loop over all values.
            // The last is the [2] for the complex data.
            for (int i = 0; i < dimens.length - 1; i += 1) {
                dim *= dimens[i];
                tdim = dimens[i] + sep + tdim;
                sep = ",";
            }
            String suffix = "C";  // For complex
            // Update the TFORMn keyword.
            if (base == double.class) {
                suffix = "M";
            }

            // Worry about variable length columns.
            String prefix = "";
            if (table.isVarCol(index)) {
                prefix = "P";
                dim = 1;
                if (table.isLongVary(index)) {
                    prefix = "Q";
                }
            }

            // Now update the header.
            myHeader.findCard("TFORM" + (index + 1));
            HeaderCard hc = myHeader.nextCard();
            String oldComment = hc.getComment();
            if (oldComment == null) {
                oldComment = "Column converted to complex";
            }
            myHeader.addValue("TFORM" + (index + 1), dim + prefix + suffix, oldComment);
            if (tdim.length() > 0) {
                myHeader.addValue("TDIM" + (index + 1), "(" + tdim + ")", "ntf::binarytablehdu:tdimN:1");
            } else {
                // Just in case there used to be a TDIM card that's no longer needed.
                myHeader.removeCard("TDIM" + (index + 1));
            }
            status = true;
        }
        return status;
    }

    private void prtField(String type, String field) {
        String val = myHeader.getStringValue(field);
        if (val != null) {
            System.out.print(type + '=' + val + "; ");
        }
    }

    /** Print out some information about this HDU.
     */
    public void info() {

        BinaryTable myData = (BinaryTable) this.myData;

        System.out.println("  Binary Table");
        System.out.println("      Header Information:");

        int nhcol = myHeader.getIntValue("TFIELDS", -1);
        int nrow = myHeader.getIntValue("NAXIS2", -1);
        int rowsize = myHeader.getIntValue("NAXIS1", -1);

        System.out.print("          " + nhcol + " fields");
        System.out.println(", " + nrow + " rows of length " + rowsize);

        for (int i = 1; i <= nhcol; i++) {
            System.out.print("           " + i + ":");
            prtField("Name", "TTYPE" + i);
            prtField("Format", "TFORM" + i);
            prtField("Dimens", "TDIM" + i);
            System.out.println("");
        }

        System.out.println("      Data Information:");
        if (myData == null
                || table.getNRows() == 0 || table.getNCols() == 0) {
            System.out.println("         No data present");
            if (table.getHeapSize() > 0) {
                System.out.println("         Heap size is: " + table.getHeapSize() + " bytes");
            }
        } else {

            System.out.println("          Number of rows=" + table.getNRows());
            System.out.println("          Number of columns=" + table.getNCols());
            if (table.getHeapSize() > 0) {
                System.out.println("          Heap size is: " + table.getHeapSize() + " bytes");
            }
            Object[] cols = table.getFlatColumns();
            for (int i = 0; i < cols.length; i += 1) {
                System.out.println("           " + i + ":" + ArrayFuncs.arrayDescription(cols[i]));
            }
        }
    }

    /** What are the standard column stems for a binary table?
     */
    public String[] columnKeyStems() {
        return keyStems;
    }

    /**
     * Get the type of a column in the table.
     *
     * // LAURENT : added method
     *
     * @param index The 0-based column index.
     * @return The type char representing the FITS type or 0 if undefined or an invalid index was requested.
     * @exception FitsException if an invalid index was requested.
     */
    public final char getColumnType(final int index)
            throws FitsException {

        final String tform = getColumnFormat(index);
        if (tform != null) {
            return table.getTFORMType(tform);
        }
        return 0;
    }

    /**
     * Get the type of a varying length column in the table.
     *
     * // LAURENT : added method
     *
     * @param index The 0-based column index.
     * @return The type char representing the FITS type or 0 if undefined or an invalid index was requested.
     * @exception FitsException if an invalid index was requested.
     */
    public final char getColumnVarType(final int index)
            throws FitsException {

        final String tform = getColumnFormat(index);
        if (tform != null) {
            return table.getTFORMVarType(tform);
        }
        return 0;
    }

    /**
     * Get the explicit or implied length of a column in the table.
     *
     * // LAURENT : added method
     *
     * @param index The 0-based column index.
     * @return The explicit or implied length or 0 if undefined or an invalid index was requested.
     * @exception FitsException if an invalid index was requested.
     */
    public final int getColumnLength(final int index)
            throws FitsException {

        final String tform = getColumnFormat(index);
        if (tform != null) {
            return table.getTFORMLength(tform);
        }
        return 0;
    }

    /**
     * Get the dimensions of a column in the table or null if TDIM keyword is not present
     *
     * // LAURENT : added method
     *
     * @param index The 0-based column index.
     * @return the dimensions of a column of null
     */
    public final int[] getColumnDimensions(final int index) {
        final String tdims = getColumnMeta(index, "TDIM");
        if (tdims != null) {
            return BinaryTable.getTDims(tdims);
        }
        return null;
    }
}
