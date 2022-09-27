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

    private static final String GAMMA2_VELORUM_FILENAME
                                = "PRODUCT_Gamma2_Velorum_2.02-2.09micron_2007-03-31T01_39_00.5372.fits";
    private static final String GAM_VIC_FILENAME = "2008-Contest_Binary.fits";

    private final OIFitsCollectionManager ocm = OIFitsCollectionManager.getInstance();

    /**
     * Adds one file, removes it, checks if eveything is clean.
     */
    @Test
    @GUITest
    public void m1_addOneFileRemoveIt() {
        newCollection(); // reset app

        addOIFitsFile(RESOURCE_TEST_FOLDER, GAMMA2_VELORUM_FILENAME);

        remove();

        // saveScreenshot(window, "OIXP-addOneFile-remove.png");
        // assert that there is no files remaining in OIFitsCollection
        Assert.assertEquals(0, ocm.getOIFitsCollection().getOIFitsFiles().size());
        // assert that there is no files remaining in OIDataCollection
        Assert.assertNull(ocm.getOIDataFile(GAMMA2_VELORUM_FILENAME));
        // assert that the filter of SubsetDefinition has no target
        Assert.assertNull(ocm.getCurrentSubsetDefinition().getFilter().getTargetUID());
    }

    /**
     * Add two files with two targets, remove one target, check that the other target remains.
     */
    @Test
    @GUITest
    public void m2_addTwoFilesTwoTargetRemoveOneTarget() {
        newCollection(); // reset app

        // load and select Gamma2_Velorum target:
        addOIFitsFile(RESOURCE_TEST_FOLDER, GAMMA2_VELORUM_FILENAME);

        addOIFitsFile(RESOURCE_TEST_FOLDER, GAM_VIC_FILENAME);

        // memorize sorted targets: GAM_VIC, GAMMA2_VELORUM
        List<Target> targets = ocm.getOIFitsCollection().getTargetManager().getGlobals();
        // logger.info("targets: " + targets);

        remove(); // will remove the selected one: Gamma2_Velorum

        // saveScreenshot(window, "OIXP-addTwoFiles-remove.png");
        // logger.info("global targets:  " + OCM.getOIFitsCollection().getTargetManager().getGlobals());
        // assert that only one granule remains
        Assert.assertEquals(1, ocm.getOIFitsCollection().getSortedGranules().size());

        // assert that there is one target less (GAM_VIC, UNDEFINED)
        Assert.assertEquals(targets.size() - 1, ocm.getOIFitsCollection().getTargetManager().getGlobals().size());

        // assert that the remaining target is GAM_VIC
        Assert.assertEquals(targets.get(0), ocm.getOIFitsCollection().getTargetManager().getGlobals().get(0));
    }

}
