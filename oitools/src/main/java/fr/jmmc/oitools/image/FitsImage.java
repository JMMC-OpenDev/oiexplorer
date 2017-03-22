/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.image;

import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.logging.Level;

/**
 * This class describes an astronomical image (2D) with its coordinates, orientation, scale ...
 * 
 * @author bourgesl
 */
public final class FitsImage {

    /** Logger associated to image classes */
    private final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FitsImage.class.getName());
    /* members */
    /** FITS image HDU */
    private FitsImageHDU fitsImageHDU = null;
    /** index of this image plane in the data cube (1 for an image) */
    private int imageIndex = 1;
    /** image identifier */
    private String imageIdentifier = null;
    /** wavelength position of the reference pixel (real starting from 1.0) */
    private double pixRefWL = 1d;
    /** wavelength value at the reference pixel column (meter) */
    private double valRefWL = 0d;
    /** wavelength increment along the wavelength axis (meter per pixel) */
    private double incWL = 1d;
    /** image wavelength in meters (NaN if undefined) */
    private Double wavelength = null;
    /* image related information (may changed during processing) */
    /** number of columns */
    private int nbCols = 0;
    /** number of rows */
    private int nbRows = 0;
    /** column position of the reference pixel (real starting from 1.0) */
    private double pixRefCol = 1d;
    /** row position of the reference pixel (real starting from 1.0) */
    private double pixRefRow = 1d;
    /** coordinate value at the reference pixel column (radians) */
    private double valRefCol = 0d;
    /** coordinate value at the reference pixel row (radians) */
    private double valRefRow = 0d;
    /** sign flag of the coordinate increment along the column axis (true means positive or false negative) */
    private boolean incColPositive = true;
    /** absolute coordinate increment along the column axis (radians per pixel) */
    private double incCol = 1d;
    /** sign flag of the coordinate increment along the row axis (true means positive or false negative) */
    private boolean incRowPositive = true;
    /** absolute coordinate increment along the row axis (radians per pixel) */
    private double incRow = 1d;
    /** image area coordinates (radians) */
    private Rectangle2D.Double area = null;
    /** image data as float[nbRows][nbCols] ie [Y][X] */
    private float[][] data = null;
    /** minimum value in data */
    private double dataMin = Double.NaN;
    /** maximum value in data */
    private double dataMax = Double.NaN;
    /** number of data */
    private int nData = 0;
    /** sum of data values */
    private double sum = 0d;

    /** 
     * Public FitsImage class constructor
     */
    public FitsImage() {
        super();
    }

    /* image meta data */
    /**
     * Return the FITS image HDU
     * @return FITS image HDU
     */
    public FitsImageHDU getFitsImageHDU() {
        return this.fitsImageHDU;
    }

    /**
     * Define the FITS image HDU
     * @param fitsImageHDU FITS image HDU
     */
    public void setFitsImageHDU(final FitsImageHDU fitsImageHDU) {
        this.fitsImageHDU = fitsImageHDU;
    }

    /**
     * Return the index of this image plane in the data cube (1 for an image)
     * @return index of this image plane in the data cube (1 for an image)
     */
    public int getImageIndex() {
        return this.imageIndex;
    }

    /**
     * Define the index of this image plane in the data cube (1 for an image)
     * @param imageIndex index of this image plane in the data cube (1 for an image)
     */
    public void setImageIndex(final int imageIndex) {
        this.imageIndex = imageIndex;
    }

    /**
     * Return the number of image planes in the data cube (1 for an image)
     * @return number of image planes in the data cube (1 for an image) or -1 if the FITS image HDU is undefined
     */
    public int getImageCount() {
        if (this.fitsImageHDU == null) {
            return -1;
        }
        return this.fitsImageHDU.getImageCount();
    }

    /**
     * Define the fits image identifier = 'FileName#ExtNb'
     * @param imageIdentifier image identifier 
     */
    public void setFitsImageIdentifier(final String imageIdentifier) {
        this.imageIdentifier = imageIdentifier;
    }

    /**
     * Return the fits image identifier = 'FileName#ExtNb'
     * @return fits image identifier or null if undefined
     */
    public String getFitsImageIdentifier() {
        return this.imageIdentifier;
    }

    /**
     * Return the wavelength position of the reference pixel (real starting from 1.0)
     * @return wavelength position of the reference pixel (real starting from 1.0)
     */
    public double getPixRefWL() {
        return this.pixRefWL;
    }

    /**
     * Define the wavelength position of the reference pixel (real starting from 1.0)
     * @param pixRefWL wavelength position of the reference pixel (real starting from 1.0)
     */
    public void setPixRefWL(final double pixRefWL) {
        this.pixRefWL = pixRefWL;
        this.wavelength = null; // reset wavelength
    }

    /**
     * Return the wavelength value at the reference pixel column (meter)
     * @return wavelength value at the reference pixel column (meter)
     */
    public double getValRefWL() {
        return this.valRefWL;
    }

    /**
     * Define the wavelength value at the reference pixel column (meter)
     * @param valRefWL wavelength value at the reference pixel column (meter)
     */
    public void setValRefWL(final double valRefWL) {
        this.valRefWL = valRefWL;
        this.wavelength = null; // reset wavelength
    }

    /**
     * Return the wavelength increment along the wavelength axis (meter per pixel)
     * @return wavelength increment along the wavelength axis (meter per pixel)
     */
    public double getIncWL() {
        return this.incWL;
    }

    /**
     * Define the wavelength increment along the wavelength axis (meter per pixel)
     * @param incWL wavelength increment along the wavelength axis (meter per pixel)
     */
    public void setIncWL(final double incWL) {
        if (incWL != 0.0) {
            this.incWL = incWL;
        }
        this.wavelength = null; // reset wavelength
    }

    /**
     * Return the image wavelength in meters or Double.NaN if undefined
     * @return image wavelength in meters or Double.NaN if undefined
     */
    public double getWaveLength() {
        if (this.wavelength == null) {
            // If increment is undefined (NaN):
            if (Double.isNaN(this.incWL)) {
                if (this.imageIndex > 1 || this.pixRefWL > 1d) {
                    // wavelength is undefined:
                    this.wavelength = Double.valueOf(Double.NaN);
                } else {
                    this.wavelength = Double.valueOf(this.valRefWL);
                }
            } else {
                this.wavelength = Double.valueOf(this.valRefWL + ((this.imageIndex - 1) - (this.pixRefWL - 1d)) * this.incWL);
            }
        }
        return this.wavelength.doubleValue();
    }

    /**
     * Return the lower image wavelength in meters = wavelength - 1/2 increment_wavelength or Double.NaN if wavelength is undefined
     * @return lower image wavelength in meters = wavelength - 1/2 increment_wavelength or Double.NaN if wavelength is undefined
     */
    public double getWaveLengthMin() {
        final double wl = getWaveLength();
        if (Double.isNaN(wl)) {
            return Double.NaN;
        }
        return wl - 0.5d * getIncWL();
    }

    /**
     * Return the upper image wavelength in meters = wavelength + 1/2 increment_wavelength or Double.NaN if wavelength is undefined
     * @return upper image wavelength in meters = wavelength + 1/2 increment_wavelength or Double.NaN if wavelength is undefined
     */
    public double getWaveLengthMax() {
        final double wl = getWaveLength();
        if (Double.isNaN(wl)) {
            return Double.NaN;
        }
        return wl + 0.5d * getIncWL();
    }

    /* image related information (may changed during processing) */
    /**
     * Return the number of columns i.e. the Fits NAXIS1 keyword value
     * @return the number of columns i.e. the Fits NAXIS1 keyword value
     */
    public int getNbCols() {
        return this.nbCols;
    }

    /**
     * Define the number of columns i.e. the Fits NAXIS1 keyword value
     * @param nbCols number of columns i.e. the Fits NAXIS1 keyword value
     */
    void setNbCols(final int nbCols) {
        this.nbCols = nbCols;
        this.area = null; // reset area
    }

    /**
     * Return the number of rows i.e. the Fits NAXIS2 keyword value
     * @return the number of rows i.e. the Fits NAXIS2 keyword value
     */
    public int getNbRows() {
        return this.nbRows;
    }

    /**
     * Define the number of rows i.e. the Fits NAXIS2 keyword value
     * @param nbRows number of rows i.e. the Fits NAXIS2 keyword value
     */
    void setNbRows(final int nbRows) {
        this.nbRows = nbRows;
        this.area = null; // reset area
    }

    /**
     * Return the column position of the reference pixel (real starting from 1.0)
     * @return column position of the reference pixel (real starting from 1.0)
     */
    public double getPixRefCol() {
        return this.pixRefCol;
    }

    /**
     * Define the column position of the reference pixel (real starting from 1.0)
     * @param pixRefCol column position of the reference pixel (real starting from 1.0)
     */
    public void setPixRefCol(final double pixRefCol) {
        this.pixRefCol = pixRefCol;
        this.area = null; // reset area
    }

    /**
     * Return the row position of the reference pixel (real starting from 1.0)
     * @return row position of the reference pixel (real starting from 1.0)
     */
    public double getPixRefRow() {
        return this.pixRefRow;
    }

    /**
     * Define the row index of the reference pixel (real starting from 1.0)
     * @param pixRefRow row index of the reference pixel (real starting from 1.0)
     */
    public void setPixRefRow(final double pixRefRow) {
        this.pixRefRow = pixRefRow;
        this.area = null; // reset area
    }

    /**
     * Return the coordinate value at the reference pixel column in radians
     * @return coordinate value at the reference pixel column in radians
     */
    public double getValRefCol() {
        return this.valRefCol;
    }

    /**
     * Define the coordinate value at the reference pixel column in radians
     * @param valRefCol coordinate value at the reference pixel column in radians
     */
    public void setValRefCol(final double valRefCol) {
        this.valRefCol = valRefCol;
        this.area = null; // reset area
    }

    /**
     * Return the coordinate value at the reference pixel row in radians
     * @return coordinate value at the reference pixel row in radians
     */
    public double getValRefRow() {
        return this.valRefRow;
    }

    /**
     * Define the coordinate value at the reference pixel row in radians
     * @param valRefRow coordinate value at the reference pixel row in radians
     */
    public void setValRefRow(final double valRefRow) {
        this.valRefRow = valRefRow;
        this.area = null; // reset area
    }

    /**
     * Return true if the coordinate increment along the column axis is defined
     * @return true if the coordinate increment along the column axis is defined
     */
    public boolean isIncColDefined() {
        return (this.incCol != FitsImageConstants.DEFAULT_CDELT);
    }

    /**
     * Return the sign flag of the coordinate increment along the column axis (true means positive or false negative)
     * @return sign flag of the coordinate increment along the column axis (true means positive or false negative)
     */
    public boolean isIncColPositive() {
        return this.incColPositive;
    }

    /**
     * Return the signed coordinate increment along the column axis in radians
     * @return signed coordinate increment along the column axis in radians
     */
    public double getSignedIncCol() {
        return (this.incColPositive) ? this.incCol : -this.incCol;
    }

    /**
     * Define the absolute and sign flag of the coordinate increment along the column axis
     * @param incCol signed coordinate increment along the column axis in radians
     */
    public void setSignedIncCol(final double incCol) {
        if (incCol != 0.0) {
            this.incColPositive = (incCol >= 0d);
            this.incCol = (this.incColPositive) ? incCol : -incCol;
        }
        this.area = null; // reset area
    }

    /**
     * Return the absolute coordinate increment along the column axis in radians
     * @return absolute coordinate increment along the column axis in radians
     */
    public double getIncCol() {
        return this.incCol;
    }

    /**
     * Return true if the coordinate increment along the row axis is defined
     * @return true if the coordinate increment along the row axis is defined
     */
    public boolean isIncRowDefined() {
        return (this.incRow != FitsImageConstants.DEFAULT_CDELT);
    }

    /**
     * Return the sign flag of the coordinate increment along the row axis (true means positive or false negative)
     * @return sign flag of the coordinate increment along the row axis (true means positive or false negative)
     */
    public boolean isIncRowPositive() {
        return this.incRowPositive;
    }

    /**
     * Return the signed coordinate increment along the row axis
     * @return signed coordinate increment along the row axis in radians
     */
    public double getSignedIncRow() {
        return (this.incRowPositive) ? this.incRow : -this.incRow;
    }

    /**
     * Define the absolute and sign flag of the coordinate increment along the row axis
     * @param incRow signed coordinate increment along the row axis in radians
     */
    public void setSignedIncRow(final double incRow) {
        if (incRow != 0.0) {
            this.incRowPositive = (incRow >= 0d);
            this.incRow = (this.incRowPositive) ? incRow : -incRow;
        }
        this.area = null; // reset area
    }

    /**
     * Return the absolute coordinate increment along the row axis in radians
     * @return absolute coordinate increment along the row axis in radians
     */
    public double getIncRow() {
        return this.incRow;
    }

    /**
     * Return the image area coordinates in radians
     * @return image area coordinates in radians
     */
    public Rectangle2D.Double getArea() {
        if (this.area == null) {
            updateArea();
        }
        return this.area;
    }

    /**
     * Return the image data as float[nbRows][nbCols] ie [Y][X]
     * @return image data as float[nbRows][nbCols] ie [Y][X]
     */
    public float[][] getData() {
        return this.data;
    }

    /**
     * Define the image data as float[nbRows][nbCols] ie [Y][X].
     * Note: no array copy is performed so do not modify the given array afterwards.
     * 
     * @param data image data as float[nbRows][nbCols] ie [Y][X]
     * 
     * @throws IllegalStateException if the given data is null
     */
    public void setData(final float[][] data) throws IllegalStateException {
        if (data == null) {
            throw new IllegalStateException("Empty data array !");
        }
        this.data = data;

        // update nbRows / nbCols:
        final int length = data.length;
        setNbRows(length);
        setNbCols((length > 0) ? data[0].length : 0);

        // reset data min/max:
        setDataMin(Double.NaN);
        setDataMax(Double.NaN);
    }

    /**
     * Return true if the minimum and maximum value in data are defined
     * @return true if the minimum and maximum value in data are defined 
     */
    public boolean isDataRangeDefined() {
        return (!Double.isNaN(this.dataMin) && !Double.isNaN(this.dataMax));
    }

    /**
     * Return the minimum value in data
     * @return minimum value in data or Double.NaN if undefined
     */
    public double getDataMin() {
        return this.dataMin;
    }

    /**
     * Define the minimum value in data
     * @param dataMin minimum value in data
     */
    public void setDataMin(final double dataMin) {
        this.dataMin = dataMin;
    }

    /**
     * Return the maximum value in data
     * @return maximum value in data or Double.NaN if undefined
     */
    public double getDataMax() {
        return this.dataMax;
    }

    /**
     * Define the maximum value in data
     * @param dataMax maximum value in data
     */
    public void setDataMax(final double dataMax) {
        this.dataMax = dataMax;
    }

    /**
     * Return the number of data
     * @return number of data
     */
    public int getNData() {
        return this.nData;
    }

    /**
     * Define the number of data
     * @param nData number of data
     */
    public void setNData(final int nData) {
        this.nData = nData;
    }

    /**
     * Return the sum of data values
     * @return sum of data values
     */
    public double getSum() {
        return this.sum;
    }

    /**
     * Define the sum of data values
     * @param sum sum of data values
     */
    public void setSum(final double sum) {
        this.sum = sum;
    }

    // utility methods:
    /**
     * Return the viewed angle along column axis in radians
     * @return viewed angle along column axis in radians
     */
    public double getAngleCol() {
        return this.nbCols * this.incCol;
    }

    /**
     * Return the viewed angle along row axis in radians
     * @return viewed angle along row axis in radians
     */
    public double getAngleRow() {
        return this.nbRows * this.incRow;
    }

    /**
     * Return the minimum view angle in radians
     * @return minimum view angle in radians
     */
    public double getMinAngle() {
        return Math.min(getAngleCol(), getAngleRow());
    }

    /**
     * Return the maximum view angle in radians
     * @return maximum view angle in radians
     */
    public double getMaxAngle() {
        return Math.max(getAngleCol(), getAngleRow());
    }

    /**
     * Update image area coordinates in radians
     */
    private void updateArea() {
        this.area = new Rectangle2D.Double(
                this.valRefCol - (this.pixRefCol - 1d) * this.incCol,
                this.valRefRow - (this.pixRefRow - 1d) * this.incRow,
                this.nbCols * this.incCol,
                this.nbRows * this.incRow);

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("updateArea: " + area);
            logger.fine("updateArea: (" + getAngleAsString(area.getX()) + ", " + getAngleAsString(area.getY()) + ") ["
                    + getAngleAsString(area.getWidth()) + ", " + getAngleAsString(area.getHeight()) + ']');
        }
    }

    // toString helpers:
    /**
     * Return a string representation of the given angle in degrees using appropriate unit (deg/arcmin/arcsec/milli arcsec)
     * @param angle angle in radians
     * @return string representation of the given angle
     */
    public static String getAngleAsString(final double angle) {
        return getAngleAsString(angle, null);
    }

    /**
     * Return a string representation of the given angle in radians using the appropriate unit (deg/arcmin/arcsec/milli arcsec)
     * @param angle angle in radians
     * @param df optional decimal formatter
     * @return string representation of the given angle
     */
    public static String getAngleAsString(final double angle, final DecimalFormat df) {
        if (Double.isNaN(angle)) {
            return "NaN";
        }
        double tmp = Math.toDegrees(angle);
        if (tmp > 1e-1d) {
            return ((df != null) ? df.format(tmp) : tmp) + " deg";
        }
        tmp *= 60d;
        if (tmp > 1e-1d) {
            return ((df != null) ? df.format(tmp) : tmp) + " arcmin";
        }
        tmp *= 60d;
        if (tmp > 1e-1d) {
            return ((df != null) ? df.format(tmp) : tmp) + " arcsec";
        }
        tmp *= 1000d;
        return ((df != null) ? df.format(tmp) : tmp) + " mas";
    }

    /**
     * Returns a string representation of this Fits image
     * @return a string representation of this Fits image
     */
    @Override
    public String toString() {
        return toString(true);
    }

    /**
     * Returns a string representation of this Fits image
     * @param dumpHeaderCards true to dump also header cards
     * @return a string representation of this Fits image
     */
    public String toString(final boolean dumpHeaderCards) {
        return "FitsImage[" + getFitsImageIdentifier() + "][" + getImageIndex() + '/' + getImageCount()
                + "][" + getNbCols() + " x " + getNbRows() + ']'
                + " RefPix (" + getPixRefCol() + ", " + getPixRefRow() + ')'
                + " RefVal (" + getValRefCol() + ", " + getValRefRow() + ')'
                + " Increments (" + getSignedIncCol() + ", " + getSignedIncRow() + ')'
                + " Max view angle (" + getAngleAsString(getMaxAngle()) + ')'
                + " Area " + getArea()
                + " Lambda { RefPix " + getPixRefWL() + " RefVal " + getValRefWL()
                + " Increment " + getIncWL() + "} = " + getWaveLength() + " m.";
    }
}