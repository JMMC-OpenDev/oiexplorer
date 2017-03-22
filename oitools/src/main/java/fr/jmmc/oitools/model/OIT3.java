/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.oitools.OIFitsConstants;
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.DataRange;
import fr.jmmc.oitools.meta.Types;
import fr.jmmc.oitools.meta.Units;
import fr.jmmc.oitools.meta.WaveColumnMeta;
import fr.jmmc.oitools.util.MathUtils;

/**
 * Class for OI_T3 table.
 */
public final class OIT3 extends OIData {

    /* static descriptors */
    /** U1COORD column descriptor */
    private final static ColumnMeta COLUMN_U1COORD = new ColumnMeta(OIFitsConstants.COLUMN_U1COORD, "U coordinate of baseline AB of the triangle", Types.TYPE_DBL, Units.UNIT_METER);
    /** V1COORD column descriptor */
    private final static ColumnMeta COLUMN_V1COORD = new ColumnMeta(OIFitsConstants.COLUMN_V1COORD, "V coordinate of baseline AB of the triangle", Types.TYPE_DBL, Units.UNIT_METER);
    /** U2COORD column descriptor */
    private final static ColumnMeta COLUMN_U2COORD = new ColumnMeta(OIFitsConstants.COLUMN_U2COORD, "U coordinate of baseline BC of the triangle", Types.TYPE_DBL, Units.UNIT_METER);
    /** V2COORD column descriptor */
    private final static ColumnMeta COLUMN_V2COORD = new ColumnMeta(OIFitsConstants.COLUMN_V2COORD, "V coordinate of baseline BC of the triangle", Types.TYPE_DBL, Units.UNIT_METER);

    /**
     * Public OIT3 class constructor
     * @param oifitsFile main OifitsFile
     */
    public OIT3(final OIFitsFile oifitsFile) {
        super(oifitsFile);

        // T3AMP  column definition
        addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_T3AMP, "triple product amplitude", Types.TYPE_DBL, OIFitsConstants.COLUMN_T3AMPERR, DataRange.RANGE_POSITIVE, this));

        // T3AMPERR  column definition
        addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_T3AMPERR, "error in triple product amplitude", Types.TYPE_DBL, DataRange.RANGE_POSITIVE, this));

        // T3PHI  column definition
        addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_T3PHI, "triple product phase", Types.TYPE_DBL, Units.UNIT_DEGREE, OIFitsConstants.COLUMN_T3PHIERR, RANGE_ANGLE, this));

        // T3PHIERR  column definition
        addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_T3PHIERR, "error in triple product phase", Types.TYPE_DBL, Units.UNIT_DEGREE, DataRange.RANGE_POSITIVE, this));

        // if OI support is enabled
        if (DataModel.hasOiModelColumnsSupport()) {

            // T3 MODEL columns definition (optional)
            addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_NS_MODEL_T3AMP, "model of the triple product amplitude", Types.TYPE_DBL, Units.NO_UNIT, null, DataRange.RANGE_POSITIVE, true, this));
            addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_NS_MODEL_T3PHI, "model of the triple product phase", Types.TYPE_DBL, Units.UNIT_DEGREE, null, RANGE_ANGLE, true, this));

        }

        // U1COORD  column definition
        addColumnMeta(COLUMN_U1COORD);

        // V1COORD  column definition
        addColumnMeta(COLUMN_V1COORD);

        // U2COORD  column definition
        addColumnMeta(COLUMN_U2COORD);

        // V2COORD  column definition
        addColumnMeta(COLUMN_V2COORD);

        // STA_INDEX  column definition
        addColumnMeta(new ColumnMeta(OIFitsConstants.COLUMN_STA_INDEX, "station numbers contributing to the data", Types.TYPE_INT, 3) {
            @Override
            public short[] getIntAcceptedValues() {
                return getAcceptedStaIndexes();
            }
        });

        // FLAG  column definition
        addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_FLAG, "flag", Types.TYPE_LOGICAL, this));

        // Derived SPATIAL_U1_FREQ column definition
        addDerivedColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_U1COORD_SPATIAL, "spatial U1 frequency", Types.TYPE_DBL, this));

        // Derived SPATIAL_V1_FREQ column definition
        addDerivedColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_V1COORD_SPATIAL, "spatial V1 frequency", Types.TYPE_DBL, this));

        // Derived SPATIAL_U2_FREQ column definition
        addDerivedColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_U2COORD_SPATIAL, "spatial U2 frequency", Types.TYPE_DBL, this));

        // Derived SPATIAL_V2_FREQ column definition
        addDerivedColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_V2COORD_SPATIAL, "spatial V2 frequency", Types.TYPE_DBL, this));
    }

    /**
     * Public OIT3 class constructor to create a new table
     * @param oifitsFile main OifitsFile
     * @param insName value of INSNAME keyword
     * @param nbRows number of rows i.e. the Fits NAXIS2 keyword value
     */
    public OIT3(final OIFitsFile oifitsFile, final String insName, final int nbRows) {
        this(oifitsFile);

        setInsName(insName);

        this.initializeTable(nbRows);
    }

    /* --- Columns --- */
    /**
     * Return the T3AMP column.
     * @return the T3AMP column.
     */
    public double[][] getT3Amp() {
        return this.getColumnDoubles(OIFitsConstants.COLUMN_T3AMP);
    }

    /**
     * Return the T3AMPERR column.
     * @return the T3AMPERR column.
     */
    public double[][] getT3AmpErr() {
        return this.getColumnDoubles(OIFitsConstants.COLUMN_T3AMPERR);
    }

    /**
     * Return the T3PHI column.
     * @return the T3PHI column.
     */
    public double[][] getT3Phi() {
        return this.getColumnDoubles(OIFitsConstants.COLUMN_T3PHI);
    }

    /**
     * Return the T3PHIERR column.
     * @return the T3PHIERR column.
     */
    public double[][] getT3PhiErr() {
        return this.getColumnDoubles(OIFitsConstants.COLUMN_T3PHIERR);
    }

    /**
     * Return the U1COORD column.
     * @return the U1COORD column.
     */
    public double[] getU1Coord() {
        return this.getColumnDouble(OIFitsConstants.COLUMN_U1COORD);
    }

    /**
     * Return the V1COORD column.
     * @return the V1COORD column.
     */
    public double[] getV1Coord() {
        return this.getColumnDouble(OIFitsConstants.COLUMN_V1COORD);
    }

    /**
     * Return the U2COORD column.
     * @return the U2COORD column.
     */
    public double[] getU2Coord() {
        return this.getColumnDouble(OIFitsConstants.COLUMN_U2COORD);
    }

    /**
     * Return the V2COORD column.
     * @return the V2COORD column.
     */
    public double[] getV2Coord() {
        return this.getColumnDouble(OIFitsConstants.COLUMN_V2COORD);
    }

    /*
     * --- public data access ---------------------------------------------------------
     */
    /**
     * Return the derived column data as double arrays (2D) for the given column name
     * To be overriden in child classes for lazy computed columns
     * @param name any column name
     * @return column data as double arrays (2D) or null if undefined or wrong type
     */
    @Override
    protected double[][] getDerivedColumnAsDoubles(final String name) {
        if (OIFitsConstants.COLUMN_U1COORD_SPATIAL.equals(name)) {
            return getSpatialU1Coord();
        }
        if (OIFitsConstants.COLUMN_V1COORD_SPATIAL.equals(name)) {
            return getSpatialV1Coord();
        }
        if (OIFitsConstants.COLUMN_U2COORD_SPATIAL.equals(name)) {
            return getSpatialU2Coord();
        }
        if (OIFitsConstants.COLUMN_V2COORD_SPATIAL.equals(name)) {
            return getSpatialV2Coord();
        }
        return super.getDerivedColumnAsDoubles(name);
    }

    /* --- Alternate data representation methods --- */
    /**
     * Return the spatial frequencies column. The computation is based
     * on the maximum distance of u1,v1 (AB), u2,v2 (BC) and -(u1+u2), - (v1+v2) (CA) vectors.
     *
     * @return the computed spatial frequencies.
     */
    @Override
    public double[][] getSpatialFreq() {
        // lazy:
        double[][] spatialFreq = this.getColumnDerivedDoubles(OIFitsConstants.COLUMN_SPATIAL_FREQ);

        if (spatialFreq == null) {
            final int nRows = getNbRows();
            final int nWaves = getNWave();
            spatialFreq = new double[nRows][nWaves];

            if (nWaves != 0) {
                final double[] effWaves = getOiWavelength().getEffWaveAsDouble();
                final double[] u1coord = getU1Coord();
                final double[] v1coord = getV1Coord();
                final double[] u2coord = getU2Coord();
                final double[] v2coord = getV2Coord();

                double dist1, dist2, dist3;
                double[] row;
                double c;
                for (int i = 0, j; i < nRows; i++) {
                    row = spatialFreq[i];

                    // mimic OIlib/yorick/oidata.i cridx3
                    dist1 = MathUtils.carthesianNorm(u1coord[i], v1coord[i]);
                    dist2 = MathUtils.carthesianNorm(u2coord[i], v2coord[i]);

                    // (u3, v3) = (u1, v1) + (u2, v2)
                    dist3 = MathUtils.carthesianNorm(u1coord[i] + u2coord[i], v1coord[i] + v2coord[i]);

                    c = Math.max(Math.max(dist1, dist2), dist3);

                    for (j = 0; j < nWaves; j++) {
                        row[j] = c / effWaves[j];
                    }
                }
            }
            this.setColumnDerivedValue(OIFitsConstants.COLUMN_SPATIAL_FREQ, spatialFreq);
        }

        return spatialFreq;
    }

    /**
     * Return the spatial u1coord.
     * u1coord/effWave
     *
     * @return the computed spatial coords r[x][y] (x,y for coordIndex,effWaveIndex) .
     */
    public double[][] getSpatialU1Coord() {
        return getSpatialCoord(OIFitsConstants.COLUMN_U1COORD_SPATIAL, OIFitsConstants.COLUMN_U1COORD);
    }

    /**
     * Return the spatial v1coord.
     * v1coord/effWave
     *
     * @return the computed spatial coords r[x][y] (x,y for coordIndex,effWaveIndex) .
     */
    public double[][] getSpatialV1Coord() {
        return getSpatialCoord(OIFitsConstants.COLUMN_V1COORD_SPATIAL, OIFitsConstants.COLUMN_V1COORD);
    }

    /**
     * Return the spatial u2coord.
     * u2coord/effWave
     *
     * @return the computed spatial coords r[x][y] (x,y for coordIndex,effWaveIndex) .
     */
    public double[][] getSpatialU2Coord() {
        return getSpatialCoord(OIFitsConstants.COLUMN_U2COORD_SPATIAL, OIFitsConstants.COLUMN_U2COORD);
    }

    /**
     * Return the spatial v2coord.
     * v2coord/effWave
     *
     * @return the computed spatial coords r[x][y] (x,y for coordIndex,effWaveIndex) .
     */
    public double[][] getSpatialV2Coord() {
        return getSpatialCoord(OIFitsConstants.COLUMN_V2COORD_SPATIAL, OIFitsConstants.COLUMN_V2COORD);
    }

    /**
     * Return the radius column i.e. projected base line (m).
     *
     * @return the computed radius r[x] (x for coordIndex)
     */
    public double[] getRadius() {
        // lazy:
        double[] radius = this.getColumnDerivedDouble(OIFitsConstants.COLUMN_RADIUS);

        if (radius == null) {
            final int nRows = getNbRows();
            radius = new double[nRows];

            final double[] u1coord = getU1Coord();
            final double[] v1coord = getV1Coord();
            final double[] u2coord = getU2Coord();
            final double[] v2coord = getV2Coord();

            double r1, r2, r3;

            for (int i = 0; i < nRows; i++) {
                r1 = MathUtils.carthesianNorm(u1coord[i], v1coord[i]);
                r2 = MathUtils.carthesianNorm(u2coord[i], v2coord[i]);

                // (u3, v3) = (u1, v1) + (u2, v2)
                r3 = MathUtils.carthesianNorm(u1coord[i] + u2coord[i], v1coord[i] + v2coord[i]);

                radius[i] = Math.max(Math.max(r1, r2), r3);
            }

            this.setColumnDerivedValue(OIFitsConstants.COLUMN_RADIUS, radius);
        }

        return radius;
    }

    /**
     * Return the position angle column i.e. position angle of the projected base line (deg).
     *
     * @return the computed position angle r[x] (x for coordIndex)
     */
    public double[] getPosAngle() {
        // lazy:
        double[] angle = this.getColumnDerivedDouble(OIFitsConstants.COLUMN_POS_ANGLE);

        if (angle == null) {
            final int nRows = getNbRows();
            angle = new double[nRows];

            final double[] u1coord = getU1Coord();
            final double[] v1coord = getV1Coord();
            final double[] u2coord = getU2Coord();
            final double[] v2coord = getV2Coord();

            double u3, v3;
            double r1, r2, r3;

            for (int i = 0, j; i < nRows; i++) {
                r1 = MathUtils.carthesianNorm(u1coord[i], v1coord[i]);
                r2 = MathUtils.carthesianNorm(u2coord[i], v2coord[i]);

                // (u3, v3) = (u1, v1) + (u2, v2)
                u3 = u1coord[i] + u2coord[i];
                v3 = v1coord[i] + v2coord[i];
                r3 = MathUtils.carthesianNorm(u3, v3);

                j = (r1 >= r2) ? ((r1 >= r3) ? (1) : (3)) : ((r2 >= r3) ? (2) : (3));

                switch (j) {
                    case 1: // r1
                        angle[i] = Math.atan2(u1coord[i], v1coord[i]);
                        break;
                    case 2: // r2
                        angle[i] = Math.atan2(u2coord[i], v2coord[i]);
                        break;
                    case 3: // r3
                        angle[i] = Math.atan2(u3, v3);
                        break;
                    default:
                        angle[i] = 0d;
                }

                angle[i] = Math.toDegrees(angle[i]);
            }

            this.setColumnDerivedValue(OIFitsConstants.COLUMN_POS_ANGLE, angle);
        }

        return angle;
    }

    /* --- Other methods --- */
    /**
     * Do syntactical analysis.
     * @param checker checker component
     */
    @Override
    public void checkSyntax(final OIFitsChecker checker) {
        super.checkSyntax(checker);

        final int nRows = getNbRows();
        // TODO: use T3 dimensions (Nwave = 0 if missing table)
        final int nWaves = getNWave();

        final boolean[][] flags = getFlag();
        final double[][] t3AmpErr = getT3AmpErr();
        final double[][] t3PhiErr = getT3PhiErr();

        boolean[] rowFlag;
        double[] rowT3ampErr, rowT3PhiErr;

        for (int i = 0, j; i < nRows; i++) {
            rowFlag = flags[i];
            rowT3ampErr = t3AmpErr[i];
            rowT3PhiErr = t3PhiErr[i];

            for (j = 0; j < nWaves; j++) {
                if (!rowFlag[j]) {
                    // Not flagged:
                    if (!isErrorValid(rowT3ampErr[j])) {
                        checker.severe("Invalid value at index " + j + " for column '" + OIFitsConstants.COLUMN_T3AMPERR
                                + "' line " + i + ", found '" + rowT3ampErr[j] + "' should be >= 0 or NaN or flagged out");
                    }
                    if (!isErrorValid(rowT3PhiErr[j])) {
                        checker.severe("Invalid value at index " + j + " for column '" + OIFitsConstants.COLUMN_T3PHIERR
                                + "' line " + i + ", found '" + rowT3PhiErr[j] + "' should be >= 0 or NaN or flagged out");
                    }
                }
            }
        }
    }
}
/*___oOo___*/