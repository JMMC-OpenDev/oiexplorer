# OIFits Explorer Overwiew Doc

## Principal features

OIExplorer is a tool that permits to load from one to hundreds of OI Fits files.

It does not present these files as a list, but rather presents a list of *Granules*. A granule is the triplet (Target, InstrumentMode, NightId).\
The user is meant to select some of these granules to display the associated data tables in plots, or in tables.

The set of the loaded files forms an "OIFits Collection" which can be saved to the disk and loaded from it.

## Interface Scheme

![general view of the oifits explorer interface](http://www.jmmc.fr/twiki/pub/Jmmc/Software/OImagingAntoineK/general-view.svg)

The Viewer Panel (VP) displays the plots and the data.
You can add some other views (tabs), but currently all views display the same datas. You can only tune the plot of each view (change the axis column, change colors etc). Each view has exactly one plot, but one plot have in fact several Y axes so you could say that it contains several subplots.

The Granule Tree Panel (GTP) displays the available tables, sorted in the following arborescence:
Target > Instrument mode > Night > File > Table
One characteristic of this tree is that you can flatten some levels (the Night level for example). You can also reorder the first three levels.

The Data Tree Panel (DTP) has a tree similar to the one in GTP, but you cannot flatten levels and the arborescence is always Target > Instrument mode > Table.
It is an action panel: by clicking on a node or leaf of the tree, it select the currently displayed data in the VP. Selecting a leaf selects one table, and selecting a node selects one or several tables.

## The OIFitsCollectionManager class

One of the main classes is the OIFitsCollectionManager (OCM) which stores all the loaded files, the structures computed from them (i.e the association map from granules to tables), and the current plots and views settings. 

OCM contains two main objects, in summary OIFitsCollection which stores the OIFitsFiles and some computed analysis, and OiDataCollection which contains everything that is exported to the disk as a collection.

![three of the data structures in OIFits Explorer](http://www.jmmc.fr/twiki/pub/Jmmc/Software/OImagingAntoineK/data-structures.svg)

### OIFitsCollection

The OIFitsCollection contains the list of opened files (OIFitsFile), in a Map with their filepath as key.\
OIFitsCollection is the "main" referencer of the loaded OIFitsFiles. Every loaded OIFitFile is mandatory referenced by OIFitsCollection.

It contains the association map from granules (Target, Instrument mode & Night) to OIData (superclass of OI_VIS, OI_VIS2 & OI_T3 tables).\
One Granule can be associated to several OIData tables: for example the OI_VIS2 & OI_T3 tables.
One OIData can be associated to several Granules: some tables contains rows of one night and rows of another night.

### OiDataCollection

OiDataCollection is the class that can be called "the collection". 
Note that OIFits Explorer has exactly one collection opened at any time. When it starts, there is a initial collection with no files. Loaded files are added to it. When a collection is loaded from disk it replaces the current one.

An OiDataCollection has an XML equivalence. The collection can be exported to XML and written to the disk, and later read and parsed from the disk.\
Note that the "heavy data" (OIFitsFile, OIDatas) are *not* exported to XML: only the filepaths. This makes the collection XML file pretty small but subject to unwanted changes or failures if the OIFitsFiles are moved or deleted or modified on the disk.\
Note that OIFits Explorer applies no "automatic synchronization" with the OIFitsFiles on the disk. When you load a file to your collection, modifying it directly on the disk will not modify it in OIExplorer.

#### OIDataFile 

The OIDataFile contains a `file:String` field containing the filepath to an OIFits file. It also contains a reference to the `OIFitsFile` object containing the parsed content at the filepath. Only the filepath is part of the XML export, not the OIFitsFile object itself.

The OIDataFile is also used as a part of TableUID which is an identifier for Tables. Indeed OIDataFile herits from Identifiable. Note that only the filename (not the full filepath) is used as identifier.

#### SubsetDefinition

SubsetDefinition define the subset of datas as source to the plot. Mainly, the user selects a Target or an Instrument Mode, but he can also select some tables directly.\
Every field need not to be defined. For example if Night is undefined, it means you want all nights. If Target is specified, it means you want only data for this target.

SubsetDefinition contains an `OIFitsFile` which is *not* one of the opened files. It is artificially built to reference the tables that correspond to the subset.

#### PlotDefinition

The PlotDefinition defines the axes of the plot. It also defines other plots settings such as `ColorMapping` which colors the data points.

The field `xAxis:Axis` stores the name of the column in the table to use as data in the X axis, along with parameters.
Note that the `yAxes` field is a list of `Axis` objects. Indeed one plot can have several subplots, all sharing the same X axis but having their own Y axis. These Y axes contain the column names to be plotted.

All of these settings are stored in the XML, so the user really saves its "plot display choices".

#### Plot

One Plot references one SubsetDefinition and one PlotDefinition. So one Plot consists of a selection of data, along with the settings for the subplots to display (the x column, the y columns, the colors groups etc).

For example, you can create two plots, with each its own PlotDefinition, but both sharing the same SubsetDefinition.

Currently in OIFits Explorer the interface has the concept of "views". A view is a Plot but all views share the same SubsetDefinition. Each view has its own PlotDefinition. This may be augmented in the future.


