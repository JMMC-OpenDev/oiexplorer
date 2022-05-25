# Actions in OIExplorer

This page lists the actions available in OIExplorer. A one-line list, then a [detailed list](#detailed-list-of-actions). There is also explanations about what is an *action* in the third section of the present document.

## List of actions

Non-strictly grouped by theme:

### General

- `Quit`
- `Edit/Copy/Paste`: TODO: where is it used ?
- `Preferences`: Modifies general OIFitsExplorer settings
- `Cancel`: Cancels the current loading of OIFits files / collection

### Loading and adding

- `Add OIFits file`: Loads one or several OIFits file(s) from the disk and adds them to the collection
- `Add OIFits files from collection`: (Down)Loads all OIFits files listed in a collection file of the disk
- `Open OIFits Collection`: Loads a Collection file completely, with OIFits files, subsets and plots definitions
- `Open Recent`: Lists recently loaded collection and selects one of them to load it

### Removing

- `New OIFits Collection` [automatic]: Resets collection. Called at program start
- `Remove OIFits files`: Removes from collection all OIFits files concerned by the current SubsetDefinition

### Exporting

- `Save OIFits Collection`: Exports the collection to a collection file and saves it on the disk
- `Export to OIFits file`: Merges all OIFits files concerned by the current SubsetDefinition in one OIFits file and saves it on the disk
- `Export plot to PDF/PNG/JPG`: Exports plots of the current View to a PDF/PNG/JPG file and saves it on the disk
- `Export all plots to PDF/PNG/JPG`: Exports plots of every View to a PDF/PNG/JPG file and saves it on the disk

### SAMP

- `Register with Hub` [automatic]: Called at program start
- `Unregister from Hub`
- `Send OIFits data`: Calls `Export to OIFits file` but sends the file to SAMP instead of saving it on the disk
- `Receive OIFits data`: Calls `Add OIFits file` but receives the file from SAMP instead of loading if from the disk

### Granule Tree Panel

- `Toggle Target/Ins.Mode/Night/Files/Tables`: Disables or Enables the Target/Ins.Mode/etc nodes in the Granule Tree
- `Drag & Drop Target/Ins.Mode/Night Column`: Re-order the Target/Ins.Mode/Night nodes in the Granule Tree
- `Select Node in Granule Tree`: currently does nothing
- `Displays Node Info`: Displays information about the hovered node
- `Expand/Collapse Nodes`: Expands/Collapses all paths in the Granule Tree
- `List Concerned Files`: Lists all loaded files and highlights the ones concerned by the current SubsetDefinition
- `Browse OIFits file`: Displays information about the first of OIFits files concerned by the current SubsetDefinition

### Data Tree Panel

- `Select Node in Data Tree`: Uses the selected node as the current SubsetDefinition
- `Display Node Info`: Same action as the one in Granule Tree Panel

### View Panel

- `Select Tab`: Select the current tab among Overview, View tabs, Browser tabs
- `Add View`: Creates a new View and creates an associated tab
- `Close Tab`: Closes the designed tab
- `Square Selection`: Selects bounds in the two axes in one plots and applies these bounds to all plots
- `Reset Square Selection`: Resets bounds to minimum and maximum of original data
- `Select Point`: Finds the point closest to user's click and selects it
- `Unselect Point`
- `Copy Plot`: Copies the designed plot to clipboard
- `Show Data/Plot Tab`: Toggles between the Plot and the Data tabs
- `Set Viewport Range Mode`
- `Set Colors`: Sets the column on which the points in the plots are colored
- `Toggle Skip Flagged`: Toggles whether the skipped data points are shown/hidden in the plots
- `Toggle Drawn Lines`: Toggles whether plots displays data as points or as lines
- `Choose Predefined Axes`: Chooses between a set of predefined x and y axes
- `Set X Axis`: Selects the column to be used for X Axis
- `Set Y Axis`: Selects the column to be used for the designed Y Axis
- `Add Y Axis`: Adds another Y Axis, effectively adding another plot in the view
- `Remove Y Axis`: Removes the last Y Axis, effectively removing the last plot in the view
- `Edit User Column`: Edits a user specified column that will be dynamically added to the data

### Help

- `Dependencies`: Displays OIFitsExplorer software dependencies and licences
- `Report Feedback`
- `Show Log console`
- `Aknowledgement`: Copies latex string of JMMC aknowledgement message to the clipboard
- `Display Releases Notes`
- `About`: Displays version number and dependencies

## Detailed list of actions

Same list as above, with more informative content for each action.

TODO

## What is an action ?

An action is a useful goal, a useful task that can be achieved by the software during its execution.

It consists of a summary goal, a list of its steps, ordered or not, a list of the callers of the action, additional notes, and related source code.

It can be low-level enough to be realized by a single function in the source code, but it can also span over several functions. It goes the same for the GUI : the action can be fully realized by a single click on a button, or it can imply several interactions with the user, for example such as a dialog with an input text field and a validation button.\
However an action should not be too high-level, as a limit example "Use OIExplorer" is the most high-level possible action, and it is too broad too be relevant. The high-level tasks that span over a large sequence of actions will be described in the form of "scenarios of usage". For example "Merge all files of a collection about one target in one file" is one relevant scenario of usage that implies a sequence of actions in OIExplorer.

### Who call an action ?

- **User & graphical user interface**\
This is the most common source of action. Typically the user clicks on a button, for example the "Add an OIFits file" button. Almost every widget use leads to an OIExplorer action, for example selecting a node in the *DataTreePanel* triggers an action.
- **Asynchronous action**\
Some actions take a long time, for example the `Start Add OIFits file from collection` action can ask a lot of downloads.
These actions are split in two parts: the *start* part and the *end* part. The split reflects better the behaviour of the program, and also reflects the fact that the user can trigger other actions between *start* and *end*.

  By the technical design of OIExplorer, some basic actions are asynchronous. For example, adding an OIFits file from the disk actually spans in two asynchronous steps. In the first step the file is loaded, analysed and added to the collection, and then an event is fired to asynchronously call the second step. The second step analyses the full collection, and updates datas and widgets accordingly, thus eventually firing new events, for an additional step, until no new event is fired. Such a complexity is *not* reflected by this actions page, as it is considered too low-level. The asynchronous behaviour is merely an technical design way of realizing a high-level function that do not intrinsically stand on asynchronism.
- **SAMP & other JMMC softwares**\
*JMMC* softwares are able to communicate by the *SAMP* system. For example the user can trigger an action on another *JMMC* software that will send an image to OIexplorer by *SAMP*, this will trigger an OIExplorer action.
- **Composite actions**\
Some actions implies other actions. For example, the `---`(TODO) action implies the `---`(TODO) action to ---(TODO), and the `---`(TODO) to ---(TODO).\
Some actions are not directly available from the graphical interface and are solely called by other actions, these are marked as "[internal]".
- **Automatic**\
Some actions are triggered more or else automatically by the OIExplorer software. For example OIExplorer tries to connect to the *SAMP* hub by the action *Register SAMP*.
