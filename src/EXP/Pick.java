package EXP;

import util.FileWriter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
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
    private final String divideCharacter = "";
    private HashMap<String, String> resultCache = new HashMap<>();

    private Pick(String patternFilename, String inputFilename, String outputFilename, String expanFilename) {
        this.patternFilename = patternFilename;
        this.inputFilename = inputFilename;
        this.outputFilename = outputFilename;
        this.expanFilename = expanFilename;
    }

    private void loadPattern() throws IOException {
        patternList = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(patternFilename), "GBK"));
        List<String> lines = br.lines().collect(Collectors.toList());
        for (int i = 0; i < lines.size() / 3; i++) {
            List<String> regexes = new ArrayList<>();
            String line1, line2, line3;
            line1 = lines.get(3 * i);
            line2 = lines.get(3 * i + 1);
            line3 = lines.get(3 * i + 2);
            for (String left : line1.split("\t"))
                for (String right : line2.split("\t"))
                    regexes.add("(" + left + ")" + "(?<keySentence>[^，。,]*?)" + "(" + right + ")");
            Arrays.stream(line3.split("\t")).filter(string -> string.length() != 0).forEach(regex -> regexes.add(regex));
            patternList.add(regexes);
        }
        br.close();
        patternList.forEach(regexs -> regexs.forEach(System.out::println));
    }

    private void loadExpandMap() throws IOException {
        expandMap = new HashMap<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(expanFilename), "GBK"));
        br.lines().
                map(line -> line.split("\t")).
                forEach(line -> expandMap.put(line[0], Integer.parseInt(line[1]) > 0 ? Integer.parseInt(line[1]) + 1 : Integer.parseInt(line[1])));
        System.out.println(expandMap);
    }

    private String praseMatchResult(String p1, String p2, String p3) {
        StringBuilder result = new StringBuilder();
        Function<String, Integer> getDivideIndex = p -> (p.length() - expandMap.get(p) + 1) % (p.length() + 1);
        Consumer<String> putGroup = p -> result.append(p.substring(0, getDivideIndex.apply(p)) + divideCharacter + p.substring(getDivideIndex.apply(p)));
        if (expandMap.containsKey(p1))
            putGroup.accept(p1);
        else
            result.append(p1 + divideCharacter);
        result.append(p2);
        if (expandMap.containsKey(p3))
            putGroup.accept(p3);
        else
            result.append(divideCharacter + p3);
        return result.toString();
    }

    private String isKeyType(String regex, String line) {
        Matcher pa = Pattern.compile(regex).matcher(line);
        if (!pa.find())
            return "";
        //System.out.println("debug: " + line + "\n\t" + pa.group(0));
        return praseMatchResult(line.substring(pa.start(), pa.start("keySentence"))
                , pa.group("keySentence")
                , line.substring(pa.end("keySentence"), pa.end()));
    }

    private String type(String line) {
        if (resultCache.containsKey(line))
            return resultCache.get(line);
        else {
            String result = patternList.
                    stream().
                    map(regexes -> regexes.stream().map(regex -> isKeyType(regex, line)).
                            filter(strings -> strings.length() != 0).
                            findFirst().orElse("")).
                    reduce((a, b) -> a + "\t" + b).orElse("Bug report");
            resultCache.put(line, result);
            return result;
        }
    }

    private void processInput() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFilename), "GBK"));
        FileWriter fw = new FileWriter(outputFilename, "GBK", false, true);
        br.lines().
                map(x -> "\t" + x + "\t" + type(x)).
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
