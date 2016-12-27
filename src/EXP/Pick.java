package EXP;

import util.FileWriter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by haowei on 2016/12/24.
 */
public class Pick {
    private List<List<String>> patternList;
    private String patternFilename, inputFilename, outputFilename, expanFilename;
    private HashMap<String, Integer> expandMap;
    private final String divideCharacter = " ";

    private Pick(String patternFilename, String inputFilename, String outputFilename, String expanFilename) {
        this.patternFilename = patternFilename;
        this.inputFilename = inputFilename;
        this.outputFilename = outputFilename;
        this.expanFilename = expanFilename;
    }

    private void loadPattern() throws IOException {
        patternList = new ArrayList<List<String>>();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(patternFilename), "GBK"));
        List<String> lines = br.lines().collect(Collectors.toList());
        for (int i = 0; i < lines.size() / 3; i++) {
            List<String> regexes = new ArrayList<String>();
            String line1, line2, line3;
            line1 = lines.get(3 * i);
            line2 = lines.get(3 * i + 1);
            line3 = lines.get(3 * i + 2);
            for (String left : line1.split("\t"))
                for (String right : line2.split("\t"))
                    regexes.add("(" + left + ")" + ".*" + "(" + right + ")");
            Arrays.stream(line3.split("\t")).filter(string -> string.length() != 0).forEach(regex -> regexes.add(regex));
            patternList.add(regexes.stream().map(regex -> regex.replaceAll("\\.\\*", "(?<keySentence>.*?)")).
                    collect(Collectors.toList()));
        }
        br.close();
        patternList.forEach(regexs -> regexs.forEach(System.out::println));
    }

    private void loadExpandMap() throws IOException {
        expandMap = new HashMap<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(expanFilename), "GBK"));
        br.lines().
                map(line -> line.split("\t")).
                forEach(line -> expandMap.put(line[0], Integer.parseInt(line[1])));
        System.out.println(expandMap);
    }

    private String isKeyType(String regex, String line) {
        Matcher pa = Pattern.compile(regex).matcher(line);
        if (!pa.find())
            return "";
        if (pa.group("keySentence").length() == 0) {
            StringBuilder sb = new StringBuilder(pa.group(0));
            sb.insert(pa.start("keySentence") - pa.start(0), divideCharacter + divideCharacter);
            return sb.toString();
        }
        System.out.println("debug: " + pa.group(0));
        return Pattern.compile(pa.group("keySentence"), Pattern.LITERAL).
                matcher(pa.group(0)).
                replaceAll(" " + pa.group("keySentence") + divideCharacter);
    }

    private String type(String line) {
        String result = patternList.
                stream().
                map(regexes -> regexes.stream().map(regex -> isKeyType(regex, line)).
                        filter(strings -> strings.length() != 0).
                        findFirst().orElse("")).
                map(string -> string.split(" ")).
                map(stringList -> expandMap.containsKey(stringList[0]) ?
                        stringList[0].substring(0, stringList[0].length() - expandMap.get(stringList[0])) + divideCharacter
                                + stringList[0].substring(stringList[0].length()-expandMap.get(stringList[0]))
                                + String.join(divideCharacter,Arrays.asList(stringList).subList(1,stringList.length))
                        : String.join(divideCharacter, Arrays.asList(stringList))).
                reduce("", (a, b) -> a + "\t" + b);
        if (result.length() > 0)
            result = result.substring(1);
        return result;
    }

    private void processInput() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFilename), "GBK"));
        FileWriter fw = new FileWriter(outputFilename, "GBK", false, true);
        br.lines().
                map(x -> x + "\t" + type(x)).
                forEach(x -> fw.write(x + "\n"));
        fw.close();
        br.close();
    }

    private void solve() throws IOException {
        loadExpandMap();
        loadPattern();
        processInput();
    }

    public static void main(String args[]) throws IOException {
        Pick pick = new Pick("Data/左右边界术语抽取_模式.txt", "Data/shortTxt.in", "Data/Pick_out.txt", "Data/边界到主体_模式.txt");
        pick.solve();
    }
}
