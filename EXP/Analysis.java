package EXP;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;
import java.util.LinkedHashSet;

import util.FileWriter;


/**
 * Created by haowei on 16-12-21.
 */
public class Analysis {
    private static String[] resultList;
    public static Vector<String> resultVes = new Vector<>();
    public static LinkedHashMap<String,String> model_compete_Map= new LinkedHashMap<>();
    public static LinkedHashMap<String,LinkedHashSet<String []>> model_shadow_Map= new LinkedHashMap<String,LinkedHashSet<String[]>> ();

    /**
     * 分析模式竞争
     * 输出格式
     * 短文本《tab》(模式1,class1)<空格>(模式2,class2)
     */
    public static void Anylysis_model_compete() throws IOException{
        FileWriter fw = new FileWriter("Data/Analysis_model_compete.txt", "GBK", false,true);
        /*Stream.of(resultList).
                map(line -> line.split("\t", -1)).
                map(strs -> Arrays.asList(strs[2] + "\t" + strs[4], strs[3] + "\t" + strs[5])).
                flatMap(List::stream).
                distinct().
                forEach(fw::write);
        */
        model_compete_Map.entrySet().forEach(x -> fw.write( x.getKey()+"\t"+x.getValue()+"\n"));
        fw.close();
    }

    /**
     * 分析模式含混
     * 输入格式：
     *         line,type
     * 输出格式
     * 模式<tab>[短句1@class1]<空格>[短句2@class2]...<tab>"kind:"类别数
     */
    public static void Analysis_model_shadow() throws IOException {
        FileWriter fw = new FileWriter("Data/Analysis_model_shadow.txt", "GBK", false,true);
        FileWriter fw1 =  new FileWriter("Data/Analysis_model_shadow_greater_than_one.txt", "GBK", false,true);
        for(Map.Entry<String,LinkedHashSet<String[]>> x:model_shadow_Map.entrySet()){
            StringBuilder sb =new StringBuilder();
            long classCount=x.getValue().stream().map(strings -> strings[1]).distinct().count();
            sb.append(x.getKey()+"\t");
            x.getValue().forEach(strings -> sb.append("["+strings[0]+"@"+strings[1]+"] "));
            String w= sb.substring(0,sb.length()-1)+"\tkind:"+classCount+"\n";
            fw.write(w);
            if (classCount > 1)
                fw1.write(w);
        }
        fw.close();
        fw1.close();
    }

}