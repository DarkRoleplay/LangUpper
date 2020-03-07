# Lang Upper
Lang Upper is a new tool made by the Dark Roleplay team.  
The main purpose of this tool is to update lang files from the 1.12 .lang format,  
to the new .json format.
At the same time it'll update the language keys to newer version keys.  

## Usage
Put the jar file into the same Folder as your .lang files.  
If you want to update all .lang files at once, just execute the jar file without any arguments.  
But to select single files you can use this command ``java -jar <filename.jar> [lang_file]``  
E.g. to update English and german:  
``java -jar LangUpper.jar "en_us" "de_de"``

The updated files will be saved in the same directory, with the .json extension.
