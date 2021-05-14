######### PROJECT- 2 ############
The code is in the src folder.
The files needed to run the code can be found in the same directory as the README file.
Keep one set of files on your local machine and another on the dc machine 
Step by Step Commnands are as follows :::

##############STEP BY STEP ########

1) Copy the src folder to the destination on DC machine.
2) Change directory path in ServerExtended and ClientExtended to the path of the src directory.
   Below are the line number where the changes need to be made along with file names.
	Line 19 --> ServerExtended.java
	Line 16 --> ClientExtended.java
3) Go inside the src folder and compile to generate java classes :
	--> javac *.java
4) In your local machine, change the PROJDIR in launcher_on.sh to the path of the src directory on dc machine.
5) Also, Change CONFIGLOCAL to the path of the project folder.
6) In clean_up.sh, change the path in CONFIGCLEANUP to the project folder path.
7) Further to run the code, open terminal and run launcher_on.sh using : 
	"./launcher_on.sh"
8) After the code runs successfully, run clean_up.sh to close all sockets on all the dc machines.
9) Close all the terminals after this.

P.S. : If desired, password login for dc machines can be setup for easier execution.



