package nom.tam.fits;

/*
 * Copyright: Thomas McGlynn 1997-1998.
 * This code may be used for any purpose, non-commercial
 * or commercial so long as this copyright notice is retained
 * in the source code or included in or referred to in any
 * derived software.
 *
 * Many thanks to David Glowacki (U. Wisconsin) for substantial
 * improvements, enhancements and bug fixes.
 */
import nom.tam.util.*;
import java.lang.reflect.Array;
import java.util.Arrays;

/** FITS binary table header/data unit */
public class BinaryTableHDU
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
   * @param table the array used to build the binary table.
   * @exception FitsException if there was a problem with the data.
   */
  public static Header manufactureHeader(Data data) throws FitsException {
    Header hdr = new Header();
    data.fillHeader(hdr);
    return hdr;
  }

  /** Encapsulate data in a BinaryTable data type */
  public static Data encapsulate(Object o) throws FitsException {

    if (o instanceof nom.tam.util.ColumnTable) {
      return new BinaryTable((nom.tam.util.ColumnTable) o);
    } else if (o instanceof Object[][]) {
      return new BinaryTable((Object[][]) o);
    } else if (o instanceof Object[]) {
      return new BinaryTable((Object[]) o);
    } else {
      throw new FitsException("Unable to encapsulate object of type:" +
              o.getClass().getName() + " as BinaryTable");
    }
  }

  /** Check that this is a valid binary table header.
   * @param header to validate.
   * @return <CODE>true</CODE> if this is a binary table header.
   */
  public static boolean isHeader(final Header header) {
    // LAURENT : use getTrimmedStringValue instead of getStringValue :
    final String xten = header.getTrimmedStringValue("XTENSION");
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

    if (o instanceof nom.tam.util.ColumnTable || o instanceof Object[][] ||
            o instanceof Object[]) {
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
      myHeader.addValue("PCOUNT", table.getHeapSize(), "Includes Heap");
    }

    if (myHeader.getIntValue("PCOUNT") == 0) {
      myHeader.deleteKey("THEAP");
    } else {
      myHeader.getIntValue("TFIELDS");
      int offset = myHeader.getIntValue("NAXIS1") *
              myHeader.getIntValue("NAXIS2") +
              table.getHeapOffset();
      myHeader.addValue("THEAP", offset, "");
    }

    super.write(ado);
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

    for (int i = 1; i <= nhcol; i += 1) {
      System.out.print("           " + i + ":");
      prtField("Name", "TTYPE" + i);
      prtField("Format", "TFORM" + i);
      prtField("Dimens", "TDIM" + i);
      System.out.println("");
    }

    System.out.println("      Data Information:");
    if (myData == null ||
            table.getNRows() == 0 || table.getNCols() == 0) {
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
   */
  public final char getColumnType(final int index) {

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
   */
  public final char getColumnVarType(final int index) {

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
   */
  public final int getColumnLength(final int index) {

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
    final String tdims = myHeader.getTrimmedStringValue("TDIM" + (index + 1));
    if (tdims != null) {
      return BinaryTable.getTDims(tdims);
    }
    return null;
  }

  /**
   * Set complex type for the column if the column is already a 2D float column
   *
   * // LAURENT : added to change the TFORM / TDIM keywords for complex data
   *
   * @param index The 0-based column index.
   * @throws FitsException
   */
  public void setColumnComplex(final int index)
          throws FitsException {
    if (getNCols() > index && index >= 0) {
      final char type = getColumnType(index);

      char cType = 0;

      // float type
      if (type == 'E') {
        cType = 'C';
      } else if (type == 'D') {
        cType = 'M';
      }

      if (cType != 0) {
        final int[] dims = getColumnDimensions(index);

        if (dims != null && dims.length == 2) {
          // check if the value dimension is 2 :
          final int nDim = dims[1];

          if (nDim == 2) {
            final int nRows = dims[0];

            final String tform = "" + nRows + cType;
            // replace previous TFORM keyword value 'aF' by 'bC' :
            myHeader.addValue("TFORM" + (index + 1), tform, null);
            // remove TDIM keyword :
            myHeader.removeCard("TDIM" + (index + 1));
          }
        }
      }
    }
  }
}
