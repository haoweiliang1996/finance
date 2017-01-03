package EXP;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import util.FileWriter;


/**
 * Created by haowei on 16-12-21.
 */
public class Analysis {
    private static String[] resultList;
    public static Vector<String> resultVes = new Vector<>();
    public static LinkedHashMap<String, String> model_compete_Map = new LinkedHashMap<>();
    public static LinkedHashMap<String, LinkedHashSet<String[]>> model_shadow_Map = new LinkedHashMap<String, LinkedHashSet<String[]>>();

    /**
     * 分析模式竞争
     * 输出格式
     * 短文本《tab》(模式1,class1)<空格>(模式2,class2)
     */
    public static void Anylysis_model_compete() throws IOException {
        FileWriter fw = new FileWriter("Data/Analysis_model_compete.txt", "GBK", false, true);
        /*Stream.of(resultList).
                map(line -> line.split("\t", -1)).
                map(strs -> Arrays.asList(strs[2] + "\t" + strs[4], strs[3] + "\t" + strs[5])).
                flatMap(List::stream).
                distinct().
                forEach(fw::write);
        */
        model_compete_Map.entrySet().forEach(x -> fw.write(x.getKey() + "\t" + x.getValue() + "\t" + x.getValue().split("\\) \\(").length + "\n"));
        fw.close();
    }

    /**
     * 分析模式含混
     * 输入格式：
     * line,type
     * 输出格式
     * 模式<tab>[短句1@class1]<空格>[短句2@class2]...<tab>"kind:"类别数
     */
    public static void Analysis_model_shadow() throws IOException {
        FileWriter fw = new FileWriter("Data/Analysis_model_shadow.txt", "GBK", false, true);
        FileWriter fw1 = new FileWriter("Data/Analysis_model_shadow_greater_than_one.txt", "GBK", false, true);
        for (Map.Entry<String, LinkedHashSet<String[]>> x : model_shadow_Map.entrySet()) {
            StringBuilder sb = new StringBuilder();
            List<String> classList = x.getValue().stream().map(strings -> strings[1]).distinct().collect(Collectors.toList());
            long classCount = classList.size();
            sb.append(x.getKey() + "\t");
            x.getValue().forEach(strings -> sb.append("[" + strings[0] + "@" + strings[1] + "] "));
            sb.deleteCharAt(sb.length() - 1);
            sb.append("\t" + classCount + "\t" + classList.toString() + "\n");
            fw.write(sb.toString());
            if (classCount > 1)
                fw1.write(sb.toString());
        }
        fw.close();
        fw1.close();
    }

    public static void Analysis_model_shadow_all(HashMap<String,String> patternMap) throws  IOException{
        String [] keys=patternMap.keySet().toArray(new String[patternMap.size()]);
        List<List<String>> valuesList = Stream.of(keys).map(str -> Arrays.asList(patternMap.get(str).split("@"))).collect(Collectors.toList());
        Map<String,String> modelToClass = new HashMap<>();
        valuesList.forEach(l -> l.forEach(s -> modelToClass.put(s,"")));
        IntStream.range(0,keys.length)
                .forEach(i -> valuesList.get(i)
                        .forEach(str -> modelToClass.put(str,modelToClass.get(str)+"\t"+keys[i])));
        modelToClass.entrySet().stream().filter(x -> x.getValue().trim().split("\t").length>1).forEach(es -> System.out.println(es.getKey()+"\t"+es.getValue()));
    }

}
