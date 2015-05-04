Exercise for Bulletproof - CSV updater based on Spring

First build the application using this command:
mvn package

Tests are the part of the build process but can be also run using:
mvn test

To update your own CSV file use its absolute path as the first argument, for example:
java -jar target/maja-exercise-0.0.1-SNAPSHOT.jar /path/to/your/file.csv

Updated CSV file can be then found in the same directory with "-out" suffix, eg. file-out.csv

Have a look at source code and read the comments :)