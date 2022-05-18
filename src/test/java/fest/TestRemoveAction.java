/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/

package fest;

import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManager;
import fr.jmmc.oitools.model.Target;
import java.util.List;
import org.fest.swing.annotation.GUITest;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestRemoveAction extends OIExplorerFestBase {

    private final OIFitsCollectionManager OCM = OIFitsCollectionManager.getInstance();

    /**
     * Adds one file, removes it, checks if eveything is clean.
     */
    @Test
    @GUITest
    public void m1_addOneFileRemoveIt() {

        addOIFitsFile(RESOURCE_TEST_FOLDER, GAMMA2_VELORUM_FILENAME);
        remove();

        pauseShort();
        // assert that there is no files remaining in OIFitsCollection
        Assert.assertEquals(0, OCM.getOIFitsCollection().getOIFitsFiles().size());
        // assert that there is no files remaining in OIDataCollection
        Assert.assertNull(OCM.getOIDataFile(GAMMA2_VELORUM_FILENAME));
        // assert that the filter of SubsetDefinition has no target
        Assert.assertNull(OCM.getCurrentSubsetDefinition().getFilter().getTargetUID());
    }

    /**
     * Add two files with two targets, remove one target, check that the other target remains.
     */
    @Test
    @GUITest
    public void m2_addTwoFilesTwoTargetRemoveOneTarget() {
        newCollection(); // reboot app (a complete reboot would be better)

        addOIFitsFile(RESOURCE_TEST_FOLDER, GAMMA2_VELORUM_FILENAME);
        addOIFitsFile(RESOURCE_TEST_FOLDER, GAM_VIC_FILENAME);

        // memorize targets: GAMMA2_VELORUM, GAM_VIC
        List<Target> targets = OCM.getOIFitsCollection().getTargetManager().getGlobals();

        remove(); // will remove the first one: GAMMA2_VELORUM

        pauseShort();
        // assert that only one granule remains
        Assert.assertEquals(1, OCM.getOIFitsCollection().getSortedGranules().size());
        // assert that there is only one target remaining
        Assert.assertEquals(1, OCM.getOIFitsCollection().getTargetManager().getGlobals().size());
        // assert that the remaining target is the one of GAM_VIC
        Assert.assertEquals(targets.get(1), OCM.getOIFitsCollection().getTargetManager().getGlobals().get(0));
    }

}
