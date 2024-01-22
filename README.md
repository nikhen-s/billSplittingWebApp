# BillSplittingApp
This Program is a personal project made by me as an extension the class for Software Construction during my exchange in EPFL

In this Project, I created a program that would allow any amount of users to split a Bill. The way it works is that users would add bill items and their associated price. Once all of the bill items has been added, the user can select which of the bill items they bought. Once all the users select their individual bill items, the program would display the price that they have to pay based on their input. To do this, I implemented a state machine, where the program kept the Bill as the state. I also created events that the user would send to the server, and views which the server would project to the users. These events and views are serialized and deserialized such that they can be sent back and forth through the server.

# The Classes below contain the code that I have written:

* BillSplittingLogic.Scala
* BillSplittingWire.Scala
* BillSplittingTypes.Scala
* BillSplittingUI.Scala
* BillSplittingTest.Scala

Everything else is the work of EPFL's teaching staff.

