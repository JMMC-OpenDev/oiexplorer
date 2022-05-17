- The images for dia diagrams are stored in the dia folder (here), because it permits Dia software to use only the image filename as a path.
  This avoids using absolute path dependant on the machine and relative paths dependant on the OS.
- When exporting to SVG the path will be absolute to the images. So you should rather export to PNG.
- You can place the exported svg in the svg folder and the exported png in the img folder.
- You should save Dia files as "uncompressed" (see Save As), so it fits better in the git repository.
