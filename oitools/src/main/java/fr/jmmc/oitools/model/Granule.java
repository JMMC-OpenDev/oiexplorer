/* 
 * Copyright (C) 2018 CNRS - JMMC project ( http://www.jmmc.fr )
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

/**
 * A value-type representing a granule = INSNAME (backend) + TARGET + NIGHT
 *
 * @author bourgesl
 */
public final class Granule {

    public enum GranuleField {
        TARGET, INS_MODE, NIGHT;
    }

    private Target target;
    private InstrumentMode insMode;
    private NightId night;

    public Granule() {
        this(null, null, null);
    }

    public Granule(final Target target, final InstrumentMode insMode, final NightId night) {
        if (target == null && (insMode != null || night != null)) {
            throw new IllegalStateException("trying to build a new granule with target = null");
        }
        set(target, insMode, night);
    }

    public void set(final Target target, final InstrumentMode insMode, final NightId night) {
        this.target = target;
        this.insMode = insMode;
        this.night = night;
    }

    public Target getTarget() {
        return target;
    }

    public InstrumentMode getInsMode() {
        return insMode;
    }

    public NightId getNight() {
        return night;
    }

    public Object getField(GranuleField field) {
        switch (field) {
            case TARGET:
                return getTarget();
            case INS_MODE:
                return getInsMode();
            case NIGHT:
                return getNight();
            default:
                return null;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.target != null ? this.target.hashCode() : 0);
        hash = 67 * hash + (this.insMode != null ? this.insMode.hashCode() : 0);
        hash = 67 * hash + (this.night != null ? this.night.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Granule other = (Granule) obj;
        if (this.target != other.getTarget() && (this.target == null || !this.target.equals(other.getTarget()))) {
            return false;
        }
        if (this.insMode != other.getInsMode() && (this.insMode == null || !this.insMode.equals(other.getInsMode()))) {
            return false;
        }
        if (this.night != other.getNight() && (this.night == null || !this.night.equals(other.getNight()))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Granule{" + "target=" + target + ", insMode=" + insMode + ", night=" + night + '}';
    }

}
