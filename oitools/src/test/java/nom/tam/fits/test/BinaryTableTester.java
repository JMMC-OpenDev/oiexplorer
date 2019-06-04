package nom.tam.fits.test;

import fr.nom.tam.fits.BasicHDU;
import fr.nom.tam.fits.BinaryTable;
import fr.nom.tam.fits.BinaryTableHDU;
import fr.nom.tam.fits.Fits;
import fr.nom.tam.fits.FitsFactory;
import fr.nom.tam.fits.FitsUtil;
import fr.nom.tam.fits.Header;
import fr.nom.tam.util.ArrayFuncs;
import fr.nom.tam.util.BufferedDataOutputStream;
import fr.nom.tam.util.BufferedFile;
import java.io.FileOutputStream;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/** This class tests the binary table classes for
 *  the Java FITS library, notably BinaryTableHDU,
 *  BinaryTable, FitsHeap and the utility class ColumnTable.
 *  Tests include:
 *  <pre>
 *     Reading and writing data of all valid types.
 *     Reading and writing variable length da
 *     Creating binary tables from:
 *        Object[][] array
 *        Object[] array
 *        ColumnTable
 *        Column x Column
 *        Row x Row
 *     Read binary table
 *        Row x row
 *        Element x element
 *     Modify
 *        Row, column, element
 *     Rewrite binary table in place
 * </pre>
 */
public class BinaryTableTester {

  byte[] bytes = new byte[50];
  byte[][] bits = new byte[50][2];
  boolean[] bools = new boolean[50];
  short[][] shorts = new short[50][3];
  int[] ints = new int[50];
  float[][][] floats = new float[50][4][4];
  double[] doubles = new double[50];
  long[] longs = new long[50];
  String[] strings = new String[50];
  float[][] vf = new float[50][];
  short[][] vs = new short[50][];
  double[][] vd = new double[50][];
  boolean[][] vbool = new boolean[50][];

  @Before
  public void initialize() {

    for (int i = 0; i < bytes.length; i += 1) {
      bytes[i] = (byte) (2 * i);
      bits[i][0] = bytes[i];
      bits[i][1] = (byte) (~bytes[i]);
      bools[i] = (bytes[i] % 8) == 0 ? true : false;

      shorts[i][0] = (short) (2 * i);
      shorts[i][1] = (short) (3 * i);
      shorts[i][2] = (short) (4 * i);

      ints[i] = i * i;
      for (int j = 0; j < 4; j += 1) {
        for (int k = 0; k < 4; k += 1) {
          floats[i][j][k] = (float) (i + j * Math.exp(k));
        }
      }
      doubles[i] = 3 * Math.sin(i);
      longs[i] = i * i * i * i;
      strings[i] = "abcdefghijklmnopqrstuvwxzy".substring(0, i % 20);

      vf[i] = new float[i + 1];
      vf[i][i / 2] = i * 3;
      vs[i] = new short[i / 10 + 1];
      vs[i][i / 10] = (short) -i;
      vd[i] = new double[i % 2 == 0 ? 1 : 2];
      vd[i][0] = 99.99;
      vbool[i] = new boolean[i / 10];
      if (i >= 10) {
        vbool[i][0] = i % 2 == 1;
      }
    }
  }

  @Test
  public void testSimpleIO() throws Exception {

    FitsFactory.setUseAsciiTables(false);

    Fits f = new Fits();
    Object[] data = new Object[]{bytes, bits, bools, shorts, ints,
                                 floats, doubles, longs, strings};
    f.addHDU(Fits.makeHDU(data));

    BinaryTableHDU bhdu = (BinaryTableHDU) f.getHDU(1);
    bhdu.setColumnName(0, "bytes", null);
    bhdu.setColumnName(1, "bits", "bits later on");
    bhdu.setColumnName(6, "doubles", null);
    bhdu.setColumnName(5, "floats", "4 x 4 array");

    BufferedFile bf = new BufferedFile("bt1.fits", "rw");
    f.write(bf);
    bf.flush();
    bf.close();


    f = new Fits("bt1.fits");
    f.read();

    assertEquals("NHDU", 2, f.getNumberOfHDUs());


    BinaryTableHDU thdu = (BinaryTableHDU) f.getHDU(1);
    Header hdr = thdu.getHeader();

    assertEquals("HDR1", 9, hdr.getIntValue("TFIELDS"));
    assertEquals("HDR2", 2, hdr.getIntValue("NAXIS"));
    assertEquals("HDR3", 8, hdr.getIntValue("BITPIX"));
    assertEquals("HDR4", "BINTABLE", hdr.getStringValue("XTENSION"));
    assertEquals("HDR5", "bytes", hdr.getStringValue("TTYPE1"));
    assertEquals("HDR6", "doubles", hdr.getStringValue("TTYPE7"));

    for (int i = 0; i < data.length; i += 1) {
      Object col = thdu.getColumn(i);
      if (i == 8) {
        String[] st = (String[]) col;

        for (int j = 0; j < st.length; j += 1) {
          st[j] = st[j].trim();
        }
      }
      assertEquals("Data" + i, true, ArrayFuncs.arrayEquals(data[i], col));
    }
  }

  @Test
  public void testRowDelete() throws Exception {
    Fits f = new Fits("bt1.fits");
    f.read();

    BinaryTableHDU thdu = (BinaryTableHDU) f.getHDU(1);

    assertEquals("Del1", 50, thdu.getNRows());
    thdu.deleteRows(10, 20);
    assertEquals("Del2", 30, thdu.getNRows());

    double[] dbl = (double[]) thdu.getColumn(6);
    assertEquals("del3", dbl[9], doubles[9]);
    assertEquals("del4", dbl[10], doubles[30]);

    BufferedFile bf = new BufferedFile("bt1x.fits", "rw");
    f.write(bf);
    bf.close();

    f = new Fits("bt1x.fits");
    f.read();
    thdu = (BinaryTableHDU) f.getHDU(1);
    dbl = (double[]) thdu.getColumn(6);
    assertEquals("del5", 30, thdu.getNRows());
    assertEquals("del6", 9, thdu.getNCols());
    assertEquals("del7", dbl[9], doubles[9]);
    assertEquals("del8", dbl[10], doubles[30]);

    thdu.deleteRows(20);
    assertEquals("del9", 20, thdu.getNRows());
    dbl = (double[]) thdu.getColumn(6);
    assertEquals("del10", 20, dbl.length);
    assertEquals("del11", dbl[0], doubles[0]);
    assertEquals("del12", dbl[19], doubles[39]);
  }

  @Test
  public void testVar() throws Exception {

    Object[] data = new Object[]{floats, vf, vs, vd, shorts, vbool};
    BasicHDU hdu = Fits.makeHDU(new Object[]{floats, vf, vs, vd, shorts, vbool});
    Fits f = new Fits();
    f.addHDU(hdu);
    BufferedDataOutputStream bdos = new BufferedDataOutputStream(new FileOutputStream("bt2.fits"));
    f.write(bdos);
    bdos.close();

    f = new Fits("bt2.fits");
    f.read();
    BinaryTableHDU bhdu = (BinaryTableHDU) f.getHDU(1);
    Header hdr = bhdu.getHeader();

    assertEquals("var1", true, hdr.getIntValue("PCOUNT") > 0);
    assertEquals("var2", 6, hdr.getIntValue("TFIELDS"));

    for (int i = 0; i < data.length; i += 1) {
      assertEquals("vardata" + i, true, ArrayFuncs.arrayEquals(data[i], bhdu.getColumn(i)));
    }
  }

  @Test
  public void testSet() throws Exception {

    Fits f = new Fits("bt2.fits");
    f.read();
    BinaryTableHDU bhdu = (BinaryTableHDU) f.getHDU(1);
    Header hdr = bhdu.getHeader();

    // Check the various set methods on variable length data.
    float[] dta = (float[]) bhdu.getElement(4, 1);
    dta = new float[]{22, 21, 20};
    bhdu.setElement(4, 1, dta);

    BufferedDataOutputStream bdos = new BufferedDataOutputStream(new FileOutputStream("bt2a.fits"));
    f.write(bdos);
    bdos.close();

    f = new Fits("bt2a.fits");
    bhdu = (BinaryTableHDU) f.getHDU(1);
    float[] xdta = (float[]) bhdu.getElement(4, 1);

    assertEquals("ts1", true, ArrayFuncs.arrayEquals(dta, xdta));
    assertEquals("ts2", true, ArrayFuncs.arrayEquals(bhdu.getElement(3, 1), vf[3]));
    assertEquals("ts4", true, ArrayFuncs.arrayEquals(bhdu.getElement(5, 1), vf[5]));

    assertEquals("ts5", true, ArrayFuncs.arrayEquals(bhdu.getElement(4, 1), dta));

    float tvf[] = new float[]{101, 102, 103, 104};
    vf[4] = tvf;

    bhdu.setColumn(1, vf);
    assertEquals("ts6", true, ArrayFuncs.arrayEquals(bhdu.getElement(3, 1), vf[3]));
    assertEquals("ts7", true, ArrayFuncs.arrayEquals(bhdu.getElement(4, 1), vf[4]));
    assertEquals("ts8", true, ArrayFuncs.arrayEquals(bhdu.getElement(5, 1), vf[5]));

    bdos = new BufferedDataOutputStream(new FileOutputStream("bt2b.fits"));
    f.write(bdos);
    bdos.close();

    f = new Fits("bt2b.fits");
    bhdu = (BinaryTableHDU) f.getHDU(1);
    assertEquals("ts9", true, ArrayFuncs.arrayEquals(bhdu.getElement(3, 1), vf[3]));
    assertEquals("ts10", true, ArrayFuncs.arrayEquals(bhdu.getElement(4, 1), vf[4]));
    assertEquals("ts11", true, ArrayFuncs.arrayEquals(bhdu.getElement(5, 1), vf[5]));

    Object[] rw = bhdu.getRow(4);

    float[] trw = new float[]{-1, -2, -3, -4, -5, -6};
    rw[1] = trw;

    bhdu.setRow(4, rw);
    assertEquals("ts12", true, ArrayFuncs.arrayEquals(bhdu.getElement(3, 1), vf[3]));
    assertEquals("ts13", false, ArrayFuncs.arrayEquals(bhdu.getElement(4, 1), vf[4]));
    assertEquals("ts14", true, ArrayFuncs.arrayEquals(bhdu.getElement(4, 1), trw));
    assertEquals("ts15", true, ArrayFuncs.arrayEquals(bhdu.getElement(5, 1), vf[5]));

    bdos = new BufferedDataOutputStream(new FileOutputStream("bt2c.fits"));
    f.write(bdos);
    bdos.close();

    f = new Fits("bt2c.fits");
    bhdu = (BinaryTableHDU) f.getHDU(1);
    assertEquals("ts16", true, ArrayFuncs.arrayEquals(bhdu.getElement(3, 1), vf[3]));
    assertEquals("ts17", false, ArrayFuncs.arrayEquals(bhdu.getElement(4, 1), vf[4]));
    assertEquals("ts18", true, ArrayFuncs.arrayEquals(bhdu.getElement(4, 1), trw));
    assertEquals("ts19", true, ArrayFuncs.arrayEquals(bhdu.getElement(5, 1), vf[5]));
  }

  @Test
  public void buildByColumn() throws Exception {

    BinaryTable btab = new BinaryTable();

    btab.addColumn(floats);
    btab.addColumn(vf);
    btab.addColumn(strings);
    btab.addColumn(vbool);
    btab.addColumn(ints);

    Fits f = new Fits();
    f.addHDU(Fits.makeHDU(btab));

    BufferedDataOutputStream bdos = new BufferedDataOutputStream(new FileOutputStream("bt3.fits"));
    f.write(bdos);

    f = new Fits("bt3.fits");
    BinaryTableHDU bhdu = (BinaryTableHDU) f.getHDU(1);
    btab = (BinaryTable) bhdu.getData();

    assertEquals("col1", true, ArrayFuncs.arrayEquals(floats, bhdu.getColumn(0)));
    assertEquals("col2", true, ArrayFuncs.arrayEquals(vf, bhdu.getColumn(1)));

    String[] col = (String[]) bhdu.getColumn(2);
    for (int i = 0; i < col.length; i += 1) {
      col[i] = col[i].trim();
    }
    assertEquals("coi3", true, ArrayFuncs.arrayEquals(strings, col));

    assertEquals("col4", true, ArrayFuncs.arrayEquals(vbool, bhdu.getColumn(3)));
    assertEquals("col5", true, ArrayFuncs.arrayEquals(ints, bhdu.getColumn(4)));
  }

  @Test
  public void buildByRow() throws Exception {

    Fits f = new Fits("bt2.fits");
    f.read();
    BinaryTableHDU bhdu = (BinaryTableHDU) f.getHDU(1);
    Header hdr = bhdu.getHeader();
    BinaryTable btab = (BinaryTable) bhdu.getData();
    for (int i = 0; i < 50; i += 1) {

      Object[] row = btab.getRow(i);
      float[] qx = (float[]) row[1];
      float[][] p = (float[][]) row[0];
      p[0][0] = (float) (i * Math.sin(i));
      btab.addRow(row);
    }

    f = new Fits();
    f.addHDU(Fits.makeHDU(btab));
    BufferedFile bf = new BufferedFile("bt4.fits", "rw");
    f.write(bf);
    bf.flush();
    bf.close();

    f = new Fits("bt4.fits");

    btab = (BinaryTable) f.getHDU(1).getData();
    assertEquals("row1", 100, btab.getNRows());


    // Try getting data before we read in the table.

    float[][][] xf = (float[][][]) btab.getColumn(0);
    assertEquals("row2", (float) 0., xf[50][0][0]);
    assertEquals("row3", (float) (49 * Math.sin(49)), xf[99][0][0]);

    for (int i = 0; i < xf.length; i += 3) {

      boolean[] ba = (boolean[]) btab.getElement(i, 5);
      float[] fx = (float[]) btab.getElement(i, 1);

      int trow = i % 50;

      assertEquals("row4", true, ArrayFuncs.arrayEquals(ba, vbool[trow]));
      assertEquals("row6", true, ArrayFuncs.arrayEquals(fx, vf[trow]));

    }
    // Fill the table.
    f.getHDU(1).getData();

    xf = (float[][][]) btab.getColumn(0);
    assertEquals("row7", 0.F, xf[50][0][0]);
    assertEquals("row8", (float) (49 * Math.sin(49)), xf[99][0][0]);

    for (int i = 0; i < xf.length; i += 3) {

      boolean[] ba = (boolean[]) btab.getElement(i, 5);
      float[] fx = (float[]) btab.getElement(i, 1);

      int trow = i % 50;

      assertEquals("row9", true, ArrayFuncs.arrayEquals(ba, vbool[trow]));
      assertEquals("row11", true, ArrayFuncs.arrayEquals(fx, vf[trow]));

    }
  }

  @Test
  public void testObj() throws Exception {

    /*** Create a binary table from an Object[][] array */
    Object[][] x = new Object[5][3];
    for (int i = 0; i < 5; i += 1) {
      x[i][0] = new float[]{i};
      x[i][1] = new String("AString" + i);
      x[i][2] = new int[][]{{i, 2 * i}, {3 * i, 4 * i}};
    }

    Fits f = new Fits();
    BasicHDU hdu = Fits.makeHDU(x);
    f.addHDU(hdu);
    BufferedFile bf = new BufferedFile("bt5.fits", "rw");
    f.write(bf);
    bf.close();

    /** Now get rid of some columns */
    BinaryTableHDU xhdu = (BinaryTableHDU) hdu;


    // First column
    assertEquals("delcol1", 3, xhdu.getNCols());
    xhdu.deleteColumnsIndexOne(1, 1);
    assertEquals("delcol2", 2, xhdu.getNCols());

    xhdu.deleteColumnsIndexZero(1, 1);
    assertEquals("delcol3", 1, xhdu.getNCols());

    bf = new BufferedFile("bt6.fits", "rw");
    f.write(bf);

    f = new Fits("bt6.fits");

    xhdu = (BinaryTableHDU) f.getHDU(1);
    assertEquals("delcol4", 1, xhdu.getNCols());
  }

  @Test
  public void testDegenerate() throws Exception {

    String[] sa = new String[10];
    int[][] ia = new int[10][0];
    Fits f = new Fits();

    for (int i = 0; i < sa.length; i += 1) {
      sa[i] = "";
    }

    Object[] data = new Object[]{sa, ia};
    BinaryTableHDU bhdu = (BinaryTableHDU) Fits.makeHDU(data);
    Header hdr = bhdu.getHeader();
    f.addHDU(bhdu);
    BufferedFile bf = new BufferedFile("bt7.fits", "rw");
    f.write(bf);
    bf.close();

    assertEquals("degen1", 2, hdr.getIntValue("TFIELDS"));
    assertEquals("degen2", 10, hdr.getIntValue("NAXIS2"));
    assertEquals("degen3", 0, hdr.getIntValue("NAXIS1"));

    f = new Fits("bt7.fits");
    bhdu = (BinaryTableHDU) f.getHDU(1);

    hdr = bhdu.getHeader();
    assertEquals("degen4", 2, hdr.getIntValue("TFIELDS"));
    assertEquals("degen5", 10, hdr.getIntValue("NAXIS2"));
    assertEquals("degen6", 0, hdr.getIntValue("NAXIS1"));
  }

  @Test
  public void testDegen2() throws Exception {
    FitsFactory.setUseAsciiTables(false);

    Object[] data = new Object[]{
      new String[]{"a", "b", "c", "d", "e", "f"},
      new int[]{1, 2, 3, 4, 5, 6},
      new float[]{1.f, 2.f, 3.f, 4.f, 5.f, 6.f},
      new String[]{"", "", "", "", "", ""},
      new String[]{"a", "", "c", "", "e", "f"},
      new String[]{"", "b", "c", "d", "e", "f"},
      new String[]{"a", "b", "c", "d", "e", ""},
      new String[]{null, null, null, null, null, null},
      new String[]{"a", null, "c", null, "e", "f"},
      new String[]{null, "b", "c", "d", "e", "f"},
      new String[]{"a", "b", "c", "d", "e", null}
    };

    Fits f = new Fits();
    f.addHDU(Fits.makeHDU(data));
    BufferedFile ff = new BufferedFile("bt8.fits", "rw");
    f.write(ff);

    f = new Fits("bt8.fits");
    BinaryTableHDU bhdu = (BinaryTableHDU) f.getHDU(1);

    assertEquals("deg21", "e", bhdu.getElement(4, data.length - 1));
    assertEquals("deg22", "", bhdu.getElement(5, data.length - 1));

    String[] col = (String[]) bhdu.getColumn(0);
    assertEquals("deg23", "a", col[0]);
    assertEquals("deg24", "f", col[5]);

    col = (String[]) bhdu.getColumn(3);
    assertEquals("deg25", "", col[0]);
    assertEquals("deg26", "", col[5]);

    col = (String[]) bhdu.getColumn(7);  // All nulls
    assertEquals("deg27", "", col[0]);
    assertEquals("deg28", "", col[5]);

    col = (String[]) bhdu.getColumn(8);

    assertEquals("deg29", "a", col[0]);
    assertEquals("deg210", "", col[1]);
  }

  @Test
  public void testMultHDU() throws Exception {
    BufferedFile ff = new BufferedFile("bt9.fits", "rw");
    Object[] data = new Object[]{bytes, bits, bools, shorts, ints,
                                 floats, doubles, longs, strings};

    Fits f = new Fits();

    // Add two identical HDUs
    f.addHDU(Fits.makeHDU(data));
    f.addHDU(Fits.makeHDU(data));
    f.write(ff);
    ff.close();

    f = new Fits("bt9.fits");

    f.readHDU();
    BinaryTableHDU hdu;
    // This would fail before...
    int count = 0;
    while ((hdu = (BinaryTableHDU) f.readHDU()) != null) {
      int nrow = hdu.getHeader().getIntValue("NAXIS2");
      count += 1;
      assertEquals(nrow, 50);
      for (int i = 0; i < nrow; i += 1) {
        Object o = hdu.getRow(i);
      }
    }
    assertEquals(count, 2);
  }

  @Test
  public void testByteArray() {
    String[] sarr = {"abc", " de", "f"};
    byte[] barr = {'a', 'b', 'c', ' ', 'b', 'c', 'a', 'b', ' '};

    byte[] obytes = FitsUtil.stringsToByteArray(sarr, 3);
    assertEquals("blen", obytes.length, 9);
    assertEquals("b1", obytes[0], (byte) 'a');
    assertEquals("b1", obytes[1], (byte) 'b');
    assertEquals("b1", obytes[2], (byte) 'c');
    assertEquals("b1", obytes[3], (byte) ' ');
    assertEquals("b1", obytes[4], (byte) 'd');
    assertEquals("b1", obytes[5], (byte) 'e');
    assertEquals("b1", obytes[6], (byte) 'f');
    assertEquals("b1", obytes[7], (byte) ' ');
    assertEquals("b1", obytes[8], (byte) ' ');

    String[] ostrings = FitsUtil.byteArrayToStrings(barr, 3);
    assertEquals("slen", ostrings.length, 3);
    assertEquals("s1", ostrings[0], "abc");
    assertEquals("s2", ostrings[1], "bc");
    assertEquals("s3", ostrings[2], "ab");
  }
}
