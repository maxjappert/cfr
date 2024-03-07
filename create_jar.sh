echo Compiling the code...
cd src || exit
javac Main.java
echo Creating jar file...
jar cfm run.jar ../MANIFEST.MF *.class && echo .jar file successfully created!
cp run.jar ..
rm run.jar
rm *.class
