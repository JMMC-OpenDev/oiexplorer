# Data Selection Mechanism

![schéma de la sélection des données jusqu'à leur affichage](http://www.jmmc.fr/twiki/pub/Jmmc/Software/OImagingAntoineK/data-selection-mechanism.png)

## Interface

The users selects the data for the SubsetDefinition in the DataTreePanel. The tree is ordered as Target > Instrument Mode > Table. Since it is a tree, selecting a node will select all its descendant leaves (Tables). 
The selection is thus limited but is planned to be augmented in the future. The underlying core structures present more possibilities than the interface.

## SubsetFilter

The user selection is placed in a SubFilter object.\
A SubsetFilter has four fields: Target, Instrument Mode, Night, Tables.\
The "selection" of a SubsetFilter consist of any Table that respect the *four* fields. A Selected Table must have the specified Target, the specified Instrument Mode, the specified Night, and must belong to the specified list of Tables.\
A field can be set to `null`, which has the effect of accepting anything. A `null` Night field accepts any Night.

## SubsetDefinition

A SubsetDefinition contains several SubsetFilters.\
The selection of a SubsetDefinition consist of the union of the selection of each SubsetFilters.

## Selector

For the computing of the selection of a SubsetDefinition, a Selector is created for every SubsetFilter.

You can consider the Selector as a more powerful structure than SubsetFilter: it has more fields to filter the Tables.

## Granule

The Granule is a Triplet of a Target, an Instrument Mode, and a Night. For each Selector, the list of corresponding Granules is computed.\
The list of Granules is known because it is computed when you add an OIFits file to the collection. There is also a structure in OIFitsCollection that associates Granules to Tables.

## SelectorResult

For a Selector, we have a list of Granules and for each Granule, we have a list of Tables. That gives us a list of resulting Tables for the Selector.

Remember that SubsetFilter (and so Selector) has a list of Table field. Thus we remove every resulting Table that do not belong to that list.

We put the final Tables in the SelectorResult object, and we repeat this for every Selector.

## OIFitsFile

The tables from the SelectorResult are put in a newly created OIFitsFile object.

## Final filtering in PlotCharPanel

There is still a final work to do. We have an OIFitsFile whose Tables correspond to the SubsetFilters defined in the SubsetDefinition.\
But some Tables actually contains data from several Granules: for example some rows can concern one night, and other rows can concern another night, that makes two different Granules.\
Thus in PlotCharPanel, in the function `updatePlot`, when it loops over the rows of the Table to add it to the Plot, it skips the rows that do not correspond to the SubsetDefinition.