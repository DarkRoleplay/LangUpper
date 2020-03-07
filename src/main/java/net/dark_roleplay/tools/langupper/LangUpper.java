package net.dark_roleplay.tools.langupper;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LangUpper {

    public static Pattern langPattern;
    public static Pattern jsonPattern;

    public static Map<String, String> changes = new HashMap<String, String>();

    public static Map<File, Map<String, String>> langFiles;

    public static void main(String[] args) {
        executeStep("RegEx Pattern", LangUpper::initializePatterns);
        executeStep("Loading Changes", LangUpper::readChanges);
        executeStep("Detecting Lang Files", () -> LangUpper.detectLangFiles(args));
        executeStep("Loading Lang Files", LangUpper::loadLangFiles);
        executeStep("Updating Lang Files", LangUpper::updateLangFiles);
        executeStep("Writing Json Files", LangUpper::writeJsonFiles);
    }

    public static void initializePatterns() {
        langPattern = Pattern.compile("(?:^(.*?)=(.*))", Pattern.MULTILINE);
        jsonPattern = Pattern.compile("(?:^\\s*?\"(.*?)\"\\s*?:\\s*\"(.*?)\")", Pattern.MULTILINE);
    }

    public static void readChanges(){
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(LangUpper.class.getClassLoader().getResourceAsStream("changes.json")));
            StringBuffer buffer = new StringBuffer();
            String str;
            while((str = reader.readLine()) != null) buffer.append(str);

            Matcher initMatcher = null;
            initMatcher = jsonPattern.matcher(buffer.toString());
            while (initMatcher.find()) {
                changes.put(initMatcher.group(1), initMatcher.group(2));
                initMatcher.end();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void detectLangFiles(String[] args) {
        if (args.length > 0) {
            langFiles = Arrays.stream(args)
                    .map(arg -> new File("./" + arg + ".lang"))
                    .filter(file -> file.exists() && file.isFile())
                    .collect(Collectors.toMap(b -> b, b -> new HashMap<String, String>()));
        } else {
            File local = new File("./");
            langFiles = Arrays.stream(local.listFiles())
                    .filter(file -> !file.isDirectory() && !file.isHidden() && file.getName().endsWith(".lang"))
                    .collect(Collectors.toMap(b -> b, b -> new HashMap<String, String>()));

        }
    }

    public static void loadLangFiles(){
        for(Map.Entry<File, Map<String, String>> entry : langFiles.entrySet()){
            try {
                Map<String, String> langEntries = entry.getValue();
                Matcher m = langPattern.matcher(new String(Files.readAllBytes(entry.getKey().toPath()), StandardCharsets.UTF_8).replaceAll("\\\"", "\\\\\""));
                while (m.find()) langEntries.put(m.group(1), m.group(2));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void updateLangFiles(){
        Map<String, String> addQueue = new HashMap();
        for(Map.Entry<File, Map<String, String>> entry : langFiles.entrySet()){
            Map<String, String> langEntries = entry.getValue();
            for(Iterator<Map.Entry<String, String>> it = langEntries.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, String> langEntry = it.next();
                if(changes.containsKey(langEntry.getKey())) {
                    addQueue.put(changes.get(langEntry.getKey()), langEntry.getValue());
                    it.remove();
                }
            }
            langEntries.putAll(addQueue);
            addQueue.clear();
        }
    }

    public static void writeJsonFiles(){
        for(Map.Entry<File, Map<String, String>> entry : langFiles.entrySet()) {
            File oldFile = entry.getKey();
            Map<String, String> content = entry.getValue();

            File newFile = new File(oldFile.getParentFile(), oldFile.getName().replace(".lang", ".json"));
            try {
                newFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try(BufferedWriter writer = new BufferedWriter(new FileWriter(newFile))){
                writer.write("{\n");
                Iterator<Map.Entry<String, String>> iter = entry.getValue().entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, String> mapEntry = iter.next();
                    writer.write("  \"" + mapEntry.getKey() + "\":\"" + mapEntry.getValue() + (iter.hasNext() ? "\",\n" : "\"\n"));
                }
                writer.write('}');
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void executeStep(String stepName, Runnable run) {
        long start = System.currentTimeMillis();
        System.out.println("Starting: " + stepName);
        run.run();
        System.out.println("Finished: " + stepName + " in " + (System.currentTimeMillis() - start) + "ms");
    }
}
