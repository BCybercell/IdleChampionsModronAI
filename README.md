# IdleChampionsModronAI
An AI developed to optimize modron core layouts for idle champions

The latest version can be found at https://github.com/BCybercell/IdleChampionsModronAI/
## **NOTE: THIS IS NOT THE FINAL PRODUCT. IT SILL CONTAINS BUGS. IMPROVEMENTS NEED TO STILL BE MADE**

Export.json contains a more user friendly output of all 3 cores similar to what the game does. This will be formatted in a way that can be uploaded to https://ic.byteglow.com

![columns](https://user-images.githubusercontent.com/44996531/143289204-073a64ce-63e5-448a-9bac-42f382f568f5.png)

The output csv columns mean as follows:

  Gen - The current generation
  
  Size - The size of the generation
  
  Best - The score of the best individual
  
  DNA - This relates to the ids in the pipe.csv file
  
  DNA (pipe ids) - This is a list of actual pipe ids starting from left to right on the core
  
Below is an example list of ids
These have been separated by the 2 cores used for testing to help explain.
![ids](https://user-images.githubusercontent.com/44996531/143289834-797b7f70-5df6-49c5-91d6-254d3c5d7254.png)

To fill your cores start from the first core (by id)[moddest, strong, fast]
Start on the top left and fill up all the empty slots row by row, once all the empty slots have been filled continue with the next core
In the example below start at the X
![Example 1](https://user-images.githubusercontent.com/44996531/143290639-39a570aa-23a3-48a6-a4c8-97bc8d0b5ea7.png)

In the list of ids above, the last id before the space is 4, which is the straight horisontal pipe
The last slot used on the modest core
![Example 2](https://user-images.githubusercontent.com/44996531/143290948-48582682-9315-4caf-bc88-3eba0c0b57e3.png)

You then start the next core with id 119 in this case.

