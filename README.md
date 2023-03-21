## **Cloud Programming Project**  
### Author
**Name:** Amavi DOSSA  
**Email address:** amavi.dossa@um6p.ma  

### Compilation Instructions
The program is made of six (06) scala source files, as follows:
- MainProc.scala: contains the definition of Message objects, as well as the main execution thread code in charge of instantiating the differents class objects required.
- ConsoleSystem.scala: defines a ConsoleSystem class in charge of prompting the user and checking command structure before making calls to appropriate functions
- StoreSystem.scala: defines a class responsible for storing key-value pair in memory
- DurableStorage.scala: contains a class holding a persistent versions of entries in a file
- Orchest.scala: Defines an Orchestrator class responsible of load-balancing the key-value pairs between the different StoreSystem instances
- Cache.scala: implements a class that simulates a caching mechanism

The project folder following the **Akka Quickstart** structure and using the **sbt** build tool, the project can be compiled and run by simply running 
> sbt run

after having run 
> sbt update 

the first time for fixing dependencies.  
Note that the actual code does  not follow instructions given in Day4. I jumped to Day5 after Day3 test the execution on EC2.

### Run Instructions
At execution, the user will be prompted to choose an execution mode:
> **Please, choose an execution mode:  
> [1] Manual execution: Interactive command prompt  
> [2] Execute randomly generated instructions  
> \>_**

Any choice other than 1 and 2 will result in program termination.  
Choice 1 leads to an interactive execution, where commands typed by the user are executed one after another.  
Choice 2 jumps to the execution scenario described at Day3-Part2 where randomly picked keys are stored in first place, then randomly chosen operations are performed. At this level, the results of Lookup() operations are printed on the screen, but not that of Store() and Delete(). Those consequences of those two can be seen in live if the files used by the DurableStorage instances, storage_file1.txt, storage_file2.txt and storage_file3.txt are open in a text editor during the execution, in VSCode for instance.  
It is worth mentioning that the entries stored in each file are reloaded back to the StoreSystem instances at program restart. It is therefore important to flush those files between two consecutive executions in mode 2 to avoid memory saturation if the number of requests chosen is big enough, or just to start the system from ground.

### Design Choices
- As mentioned above, a reload mechanism has been implemented between DurableStorage and StoreSystem to load the persistent content if any from files back to memory at program restart.
- In the randomly picked-keys part in Day3, the picked keys are store with values same as the key, for the sake of simplicity.
- An orchestrator, defined in Orchest.scala, is in charge of load balancing the entries between the instances of StoreSystem. It is the link between them and the ConsoleSystem instance.