::  USER-SPECIFIC VARIABLES, PLEASE SET THESE

::  OUTPUT_DIRECTORY
::  The directory where all your *.class files end up
::  In IntelliJ, the default is ${YOUR_PROJECT_FOLDER}/out/production/${YOUR_PROJECT_NAME}
set OUTPUT_DIRECTORY=C:\Users\Thomas\Dropbox\IdeaProjects\System_Y\out\production\System_Y

::  CODEBASE
::  The directory where all source code is stored. (If we ever decide to separate client and server codebases, we need to review this!)

::  POLICY_FILE
::  The full path to the policy file to be used by the application
set POLICY_FILE=C:\Users\Thomas\Dropbox\IdeaProjects\System_Y\src\Misc\Server.policy

::  END OF USER-SPECIFIC VARIABLES, PLEASE DO NOT TOUCH ANYTHING BEYOND THIS LINE

javaw rmiregistry

set currentDir=%cd%

cd %OUTPUT_DIRECTORY%

java -cp %OUTPUT_DIRECTORY% ^
-Djava.security.policy=%POLICY_FILE% ^
NameserverMain

cd %currentDir%
