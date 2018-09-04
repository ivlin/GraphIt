# GraphIt
GraphIt - An Interactive Graphing Utility

by Ivan Lin

## Description

GraphIt is an interactive graphing utility developed in Java. The user can click on the graph and its nodes to add, remove, or move points. There are toggle switches to show the mean, standard deviation, regression line, and other statistical measures about the dataset. There are also options to textually modify the data points so that changes will be reflected in the graph. Batch modifications can be made by applying functions to all the points in the set. 

The user can also import their own data from a csv file or output the current dataset to a new csv.

## Motivation
The idea is to develop a graphing application that is easy to use and particularly aimed at touchscreen or Smartboard applications for classroom use. The original motivation was to allow teachers to display class data in a visually appealling way. Visualizing statistical measures will allow students to more intuitively understand their performance relative to others. Students with "what-if" questions who are curious how things may change if they had done better or worse can instantly make that changes and see the effects relative to means and averages. 

## Development

GraphIt is a web application done in Java using Canvas and JavaFX.

## Download

Download it by running:

HTTPS

`git clone https://github.com/ivlin/GraphIt.git`

## Usage

Run the program from the command line with:

`make all run`

or do it manually with:

```
javac GraphIt.java
java GraphIt
```

### Graph View Functionality

* Add point - click an empty point on the canvas to generate a new point
* Move point - click on an existing point on the canvas and you can drag-and-drop it in its new location
* Delete point - activate the deletion mode making sure the button is highlighted red and click on a point to delete it
* Move view - fill in the boundary values for the new viewframe in the minX, maxX, minY, and maxY boxes and hit set
* Empty graph - click the clear button
* Snap to grid - click the "snap to grid" button to place all new points at the nearest grid intersection
* Show/Hide Statistics - click on the appropriate button making sure it's highlighted red to visualize the corresponding statistical measure

### Options Functionality
* Add point - type the x and y coordinate values of the new point to create a new point
* Transform all data points - write an expression that can include numerals, "x", and "y" in the function box to set all point's coordinate values
* Export dataset - type a filename in the box next to the "export to:" button and click the button
* Import dataset - click the "import a csv" button to select a file where each line is formatted as "x,y" representing the x and y coordinates of each point
