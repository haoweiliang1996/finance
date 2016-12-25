package EXP;

import util.FileWriter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.io.BufferedReader;
import java.io.FileInputStream;

/**
 * Created by haowei on 2016/12/24.
 */
public class Pick {
    private List<String> patternList;
    private String patternFilename, inputFilename, outputFilename;

    private Pick(String patternFilename, String inputFilename, String outputFilename) {
        this.patternFilename = patternFilename;
        this.inputFilename = inputFilename;
        this.outputFilename = outputFilename;
    }

    private void loadPattern() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(patternFilename), "GBK"));
        patternList = br.
                lines().
                map(line -> line.replaceAll("\\.\\*", "(?<keySentence>.*)")).
                collect(Collectors.toList());
        br.close();
        System.out.print("debug" + patternList.toString());
    }

    private String isKeyType(String regex, String line) {
        Matcher pa = Pattern.compile(regex).matcher(line);
        if (!pa.find())
            return "";
        if (pa.group("keySentence").length() == 0)
            return pa.group(0);
        System.out.println("debug: " + pa.group(0));
        return Pattern.compile(pa.group("keySentence"), Pattern.LITERAL).matcher(pa.group(0)).replaceAll("\t" + pa.group("keySentence") + "\t");
    }

    private String type(String line) {
        String result = patternList.
                stream().
                map(strings -> isKeyType(strings, line)).
                distinct().
                filter(x -> x.length() != 0).
                reduce("", (a, b) -> a + "|" + b);
        if (result.length() > 0)
            result = result.substring(1);
        return result;
    }

    private void processInput(String fileName, String outFileName) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "GBK"));
        FileWriter fw = new FileWriter(outFileName, "GBK", false, true);
        br.lines().
                map(x -> x + "\t" + type(x)).
                forEach(x -> fw.write(x + "\n"));
        fw.close();
        br.close();
    }

    private void solve() throws IOException {
        loadPattern();
        processInput(this.inputFilename, this.outputFilename);
    }

    public static void main(String args[]) throws IOException {
        Pick pick = new Pick("Data/���ұ߽������ȡ_ģʽ.txt", "Data/shortTxt.in", "Data/Pick_out.txt");
        pick.solve();
    }
}
