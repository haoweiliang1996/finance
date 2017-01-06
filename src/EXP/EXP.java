/**
 * Created by haowei on 16-11-23.
 */
package EXP;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Map;

import main.Main;
import tree.Tree;

public class EXP {
    public static HashMap<String, String> patternMap = new HashMap<>();
    public static HashMap<String, Integer> count2 = new HashMap<String, Integer>(); //统计贷款人所在行业
    public static HashMap<String, Integer> count3 = new HashMap<String, Integer>();//统计贷款流向行业
    public static HashMap<String, String> sentenceToClassMap = new HashMap<>();
    public static HashMap<String, Vector<String>> pattern_to_deny_pattern = new HashMap<>();
    public static Vector<String> keyList = new Vector<>();
    public static String NoPattern = ""; //问题类别模式的最后一行为NoPattern的名字
    public static myTreeKount treeCount;
    public static int line_count = 0;

    //前向树，用来找到class的所有父class 以便统计
    public static class myTreeKount {
        private static class flag {
            public int ROOT = -2; //pre[i]为-2时，节点是树根
            public int UN_PRASE = -1;
        }

        private flag FLAG = new flag();
        private int pre[];
        private HashMap<String, Integer> key_to_keyid = new HashMap<>();

        /**
         * @param s
         * @return 串的开头tab的个数
         */
        private int countHeadTab(String s) {
            int kount = 0;
            for (char c : s.toCharArray()) {
                if (c == '\t')
                    kount++;
                else
                    break;
            }
            return kount;
        }

        /**
         * @param keyId
         * @param keyIdOfFather
         */
        private void buildTree(int keyId, int keyIdOfFather) {
            if (keyId >= keyList.size()) {
                System.out.println("error " + "建树时key数组越界 keyId: " + keyId);
                return;
            }

            pre[keyId] = keyIdOfFather;
            for (int i = keyId + 1; i < keyList.size(); i++) {
                if (countHeadTab(keyList.get(keyId)) >= countHeadTab(keyList.get(i))) {
                    return;
                }
                if (pre[i] == FLAG.UN_PRASE)
                    buildTree(i, keyId);
            }
        }

        /**
         * 建立问题类别模式的树形结构，获得所有class存储在keylist中，class前面的tab保留，这样输出时依然会有空格，从而保证输出呈现为树型
         *
         * @param strFile 问题类别模式的文件名
         * @throws IOException
         */
        public myTreeKount(String strFile) throws IOException {
            File file = new File(strFile);
            if (file.isFile() && file.exists()) {
                InputStreamReader read = new InputStreamReader(new FileInputStream(
                        file), "GBK");
                BufferedReader br = new BufferedReader(read);
                String line = null;
                while ((line = br.readLine()) != null) {
                    int splitIndex = line.lastIndexOf('\t');
                    String key;
                    if (splitIndex == -1 || !line.trim().contains("\t"))
                        key = line;
                    else
                        key = line.substring(0, splitIndex);
                    key_to_keyid.put(key.trim(), keyList.size());
                    keyList.add(key);
                }
            } else
                System.out.println("info " + "这行的class无效");
            pre = new int[keyList.size()];
            for (int i = 0; i < pre.length; i++)
                pre[i] = FLAG.UN_PRASE;//根节点
            for (int i = 0; i < pre.length; i++)
                if (pre[i] == FLAG.UN_PRASE) {
                    buildTree(i, FLAG.ROOT);
                }
        }

        public int getFatherKeyId(int keyid) {
            return pre[keyid];
        }

        public int getKeyId(String key) {
            //System.out.println("debug getKeyId:"+key);
            if (!key_to_keyid.containsKey(key))
                return FLAG.ROOT;
            return key_to_keyid.get(key);
        }

        public String getKeyById(int id) {
            // System.out.println("debug getKeyById id:"+id);
            if (id >= keyList.size())
                System.out.println("KeyList下标越界：" + id + " " + keyList.size());
            return keyList.get(id);
        }
    }

    /**
     * 载入问题类别模式,建立class(去掉了前面的tab)与模式的对应关系，获得模式与-模式的对应关系
     *
     * @param strFile 问题类别模式所在的文件
     * @throws IOException 打不开文件
     */
    public static void loadPattern(String strFile)
            throws IOException {
        File file = new File(strFile);

        if (file.isFile() && file.exists()) {
            InputStreamReader read = new InputStreamReader(new FileInputStream(
                    file), "GBK");
            BufferedReader br = new BufferedReader(read);
            String line = null;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    if (!line.contains("\t")) {
                        continue;
                    }

                    int index = line.indexOf("\t");
                    String key = line.substring(0, index);
                    String value = line.substring(index + 1).trim();
                    value = value.toUpperCase();
                    value = value.replaceAll("\\+", "");
                    value = value.replaceAll("\\*", ".*");

                    //添加以处理 W1|W2*W3|W4的输入
                    String[] vauleArray = value.split("\\s+");
                    //System.out.println("debug"+vauleArray.length);
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < vauleArray.length; i++) {
                        //System.out.print("debug"+vauleArray[i]);

                        //《处理要去除的模式的输入
                        int throwBeginIndex = vauleArray[i].indexOf("^");

                        Vector<String> temp = new Vector<>();
                        if (throwBeginIndex != -1) {
                            String[] throwStrList = vauleArray[i].substring(throwBeginIndex + 1).split(",");//to-do
                            vauleArray[i] = vauleArray[i].substring(0, throwBeginIndex);
                            for (String str : throwStrList) {
                                if (!str.contains("|"))
                                    temp.add(str);
                                else {
                                    StringBuilder sbuilder = new StringBuilder();
                                    String[] strTempList = str.split("\\.\\*");
                                    for (String strTemp : strTempList) {
                                        if (strTemp.contains("|"))
                                            sbuilder.append("(" + strTemp + ")" + ".*");
                                        else
                                            sbuilder.append(strTemp + ".*");
                                    }
                                    if (sbuilder.length() > 0)
                                        temp.add(sbuilder.delete(sbuilder.length() - 2, sbuilder.length()).toString());
                                }
                            }
                        }
                        //处理要去除的模式的输入》

                        //处理模式,给诸如 W1|W2*w3加括号->(W1|W2)*W3
                        if (vauleArray[i].contains("|")) {
                            String[] phaseArrary = vauleArray[i].split("\\.\\*");
                            //System.out.println("debug"+phaseArrary.length);
                            for (String s : phaseArrary) {
                                if (s.contains("|"))
                                    s = "(" + s + ")";
                                //System.out.println("debug"+s);
                                sb.append(s + ".*");
                            }
                            sb.delete(sb.length() - 2, sb.length());
                        } else
                            sb.append(vauleArray[i]);

                        String nowPrasePattern = sb.substring(sb.lastIndexOf("@") + 1);
                        //System.out.println("debug nowPrasePattern:"+nowPrasePattern);
                        if (!pattern_to_deny_pattern.containsKey(nowPrasePattern))
                            pattern_to_deny_pattern.put(nowPrasePattern, temp);

                        if (i != vauleArray.length - 1)
                            sb.append("@");                     //不同的模式间的间隔符是@
                    }
                    if (sb.length() > 0) {
                        value = sb.toString();
                        //System.out.println("debug"+value);
                    }
                    //添加以处理 W1|W2*W3|W4的输入

                    if (patternMap.containsKey(key))
                        value = patternMap.get(key) + "@" + value;
                    patternMap.put(key, value);
                }
            }
            br.close();
            System.out.println(patternMap);
            System.out.println("debug " + "pattern_to_deny_pattern " + pattern_to_deny_pattern.toString());
        } else {
            System.out.println("找不到指定的文件");
        }
    }

    /**
     * 判断输入文件中的sentence所属class
     *
     * @param inFile  希望处理的输入文件
     *                模式库中的格式：每个模式前面可能有0个或多个Tab键，表示类别之间的层次关系。模式库的BNF定义如下：
     *                <模式库>::=<Line>{<Line>}*
     *                <Line>::=<Tabs><Class><Tab><Pat1>{<Space><Pat2>}*
     *                |<TABs><Class>
     *                <TABs>::={<Tab>}*
     *                示例：
     *                Class1        Pat11  Pat12  Pat13...
     *                Class11       Pat111  Pat112  ...
     *                Class12       Pat121  Pat122  ...
     *                Class2        Pat21  Pat22  ...
     *                Class21       Pat211  Pat212  ...
     *                Class22       Pat221  Pat222  ...
     *                Class23       Pat231  Pat232  ...
     * @param outFile 在内存中输出结果为：
     *                ID《tab》正文《tab》所在行业短句《tab》流向行业短句《tab》所在行业class《tab》流向行业class
     * @throws IOException
     */
    public static void processCluster(String inFile, String outFile)
            throws IOException {
        ArrayList<String> rList = new ArrayList<>();
        File fileIn = new File(inFile);
        File fileOut = new File(outFile);
        fileOut.createNewFile();
        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(fileOut));
        BufferedWriter bw = new BufferedWriter(osw);

        File fileOutClient = new File(outFile.split("\\.")[0] + "_client" + ".txt");
        fileOutClient.createNewFile();
        OutputStreamWriter oswC = new OutputStreamWriter(new FileOutputStream(fileOutClient));
        BufferedWriter bwC = new BufferedWriter(oswC);

        if (fileIn.isFile() && fileIn.exists()) {
            InputStreamReader read = new InputStreamReader(new FileInputStream(
                    fileIn), "GBK");
            BufferedReader br = new BufferedReader(read);
            String line = null;
            Tree tree = Main.load_tree();

            System.out.println("debug start process");
            br.lines().parallel()
                    .filter(str -> !str.isEmpty())
                    .map(str -> str.split("\\t", -1))
                    .map(strList -> Stream.of(strList[2].toUpperCase(), strList[3].toUpperCase())
                            .map(str -> str.trim().replaceAll("\\s+", "，").replaceAll("，，", "，").replaceAll("，，，", "，").trim())
                            .map(str -> type(str))
                            .filter(str -> str.length() != 0)
                            .peek(System.out::println)   //to-do 将统计写成consumer
                            .collect(Collectors.toList()))
                    .
            while ((line = br.readLine()) != null) {
                line_count++;
                if (line_count % 1000 == 0)
                    System.out.println("处理到了：" + line_count);
                if (!line.isEmpty()) {
                    String[] line_c = line.split("\\t", -1);//不忽略尾部的/t
                    if (line_c.length < 4) {
                        System.out.println("drop" + line);
                        continue;
                    }

                    String resultTemp = "";
                    for (int i = 2; i < 4; i++) {
                        String line_cs = line_c[i];
                        if (line_cs.length() == 0)
                            continue;
                        line_cs = line_cs.toUpperCase();
                        //System.out.println("debug"+line_cs);
                        line_cs = line_cs.trim();
                        line_cs = line_cs.replaceAll("\\s+", "，");
                        line_cs = line_cs.replaceAll("，，", "，");
                        line_cs = line_cs.replaceAll("，，，", "，");
                        String result = type(line_cs).trim();

                        //<<统计行业
                        /**
                         * 由于引入了^机制，可能连“其他”类别都没有
                         */
                        if (result.length() == 0) {
                            System.out.println("debug + line_cs: " + line_cs + " result:" + result);
                            System.out.println("因为某种情况 删除了所有找到的class");
                            result = "待处理";
                        }

                        //为了统计，需要得到class的所有父class
                        String[] result_split = result.split("\\|");
                        HashSet<String> keyIdSet = new HashSet<>();
                        for (String str : result_split) {
                            final int FLAG_ROOT = -2;
                            for (int id = treeCount.getKeyId(str); id != FLAG_ROOT; id = treeCount.getFatherKeyId(id)) {
                                keyIdSet.add(treeCount.getKeyById(id));
                            }
                        }
                        for (String str : keyIdSet) {
                            if (i == 2) {
                                if (count2.containsKey(str))
                                    count2.put(str, count2.get(str) + 1);
                                else
                                    count2.put(str, 1);
                            } else if (i == 3) {
                                if (count3.containsKey(str))
                                    count3.put(str, count3.get(str) + 1);
                                else
                                    count3.put(str, 1);
                            }
                        }
                        //>>统计行业

                        resultTemp += result + "\t";//贷款人所在行业<tab>贷款流向行业
                    }
                    resultTemp = resultTemp.trim();

                    //<<get tree
                    boolean ifGetTree = true;
                    if (ifGetTree) {
                        StringBuilder strTemp = new StringBuilder();
                        if (resultTemp.length() > 0) {
                            for (String str : resultTemp.split("\t")) {
                                String[] strArray = str.split("\\|");
                                for (String al : strArray)
                                    strTemp.append(tree.getAllParentsAndItself(al).toString() + "|");
                                if (strTemp.length() > 0)
                                    strTemp.deleteCharAt(strTemp.length() - 1);
                                strTemp.append('\t');
                            }
                        }
                        if (strTemp.length() > 0)
                            rList.add(line + '\t' + strTemp.substring(0, strTemp.length() - 1));
                        else
                            rList.add(line);
                    } else
                        rList.add(line + '\t' + resultTemp);
                    //get tree>>
                }
            }

            //将处理结果按指定格式写入文件
            for (String str : rList) {
                String[] strSplit = str.split("\t", -1);
                StringBuilder sbt = new StringBuilder();
                String selectSecond = "[" + NoPattern + "]";
                for (int i = 0; i < strSplit.length; i++) {
                    if (i == 2 || i == 3)
                        continue;
                    if (i <= 1)
                        sbt.append(strSplit[i] + "\t");
                    else if (i > 3) {
                        if (!strSplit[i].equals("[" + NoPattern + "]"))
                            selectSecond = strSplit[i];
                    }
                }
                sbt.append(selectSecond + "\t");
                bwC.write(sbt.substring(0, sbt.length() - 1) + '\n');
                bw.write(str + "\n");
            }
            br.close();
            bw.close();
            bwC.close();
        } else {
            System.out.println("找不到指定的文件");
        }
    }

    /**
     * 判断一个sentence所属class
     *
     * @param line sentence
     * @return 匹配到的classs 格式形如：class1|class2|class3
     */
    private static String type(String line) {
        if (sentenceToClassMap.containsKey(line)) {
            return sentenceToClassMap.get(line);
        }

        StringBuilder sb = new StringBuilder();
        Iterator<Entry<String, String>> iter = patternMap.entrySet().iterator();

        while (iter.hasNext()) {
            Entry<String, String> entry = iter.next();
            String key = entry.getKey();
            String regex = entry.getValue();

            String matchedPattern = isKeyType(regex, line);
            if (matchedPattern.length() == 0)
                continue;
            matchedPattern += "\t" + key;
            if (line_count % 10000 == 0)
                System.out.println("debug " + "matchedPattern " + matchedPattern);
            sb.append(matchedPattern + "@");
        }

        String type = NoPattern;
        if (sb.length() != 0) {
            //System.out.println("debug typeResult"+sb.substring(0,sb.length()-1));
            //<<处理去除模式
            String[] typeResult = sb.substring(0, sb.length() - 1).split("@");//去除末尾的‘@’
            String[] phaseList = new String[typeResult.length];
            String[] classList = new String[typeResult.length];
            String[] patternList = new String[typeResult.length];
            for (int i = 0; i < typeResult.length; i++) {
                String[] temp = typeResult[i].split("\\t");
                phaseList[i] = temp[0];
                patternList[i] = temp[1];
                classList[i] = temp[2];
            }
            HashSet<String> denyPatternSet = new HashSet<String>();
            for (String pa : patternList) {//匹配到的模式，由这些模式得到需要删除的模式的集合，若发现匹配到的串符合要删除的模式，则删除这些串
                if (!pattern_to_deny_pattern.containsKey(pa)) {
                    System.out.println("error+ " + pa + "没有出现在 pattern_to_deny_pattern");
                    continue;
                }
                for (String denyPattern : pattern_to_deny_pattern.get(pa)) {
                    denyPatternSet.add(denyPattern);
                }
            }

            HashSet<String> denyPhaseSet = new HashSet<String>();
            //需要包括且仅仅包括不然不能处理 		外资投资！！！！	投资*苹果*产品-苹果*产品
            for (int i = 0; i < phaseList.length; i++) {//所有匹配到的串
                boolean flag1 = false;//flag1为true时删除这个匹配到的串
                for (String s : denyPatternSet) {//由于这些串引入的否定list
                    if (s.contains(".*")) {
                        String[] al = s.split("\\.\\*");
                        boolean flag = true;
                        StringBuilder sBuilder = new StringBuilder();
                        for (String e : al) {
                            sBuilder.append(e + "|");
                            Matcher pa = Pattern.compile(e).matcher(phaseList[i]);
                            if (!pa.find()) {
                                flag = false;
                                break;
                            }
                        }
                        if (flag)//包含
                        {
                            String temp = phaseList[i];
                            if (temp.replaceAll(sBuilder.substring(0, sBuilder.length() - 1), "").length() == 0)//仅仅包含
                                flag1 = true;
                        }
                    } else {
                        Matcher pa = Pattern.compile(s).matcher(phaseList[i]);
                        if (pa.matches()) {
                            flag1 = true;
                        }
                    }
                }
                if (flag1) {
                    denyPhaseSet.add(phaseList[i]);
                }
            }

            HashSet<String> denyClassSet = new HashSet<>();
            for (String denyPhase : denyPhaseSet) {
                for (int i = 0; i < patternList.length; i++) {
                    if (isKeyType(patternList[i], denyPhase).length() != 0)//应该被deny的phase匹配上了某个pattern那么这个pattern对于的class应该被禁止
                    {
                        denyClassSet.add(classList[i]);
                    }
                }
            }

            Vector<String> result_split = new Vector<>();
            for (String cl : classList) {
                if (!denyClassSet.contains(cl))
                    result_split.add(cl);
            }
            //处理去除模式>>

            //<<处理有子类的情况下还出现父类的问题
            LinkedHashSet<String> resSet = new LinkedHashSet<>();
            for (String str : result_split)
                resSet.add(str);
            for (String str : result_split) {
                final int FLAG_ROOT = -2;
                for (int id = treeCount.getKeyId(str); id != FLAG_ROOT; id = treeCount.getFatherKeyId(id)) {
                    String strCmp = treeCount.getKeyById(id).trim();
                    for (String strOther : result_split) {
                        if (strOther.equals(str))
                            continue;
                        if (strOther.equals(strCmp))
                            resSet.remove(strOther);
                    }
                }
            }
            sb = new StringBuilder();
            for (String str : resSet) {
                sb.append(str + "|");
            }
            if (sb.length() > 0)
                type = sb.substring(0, sb.length() - 1);
            //处理有子类的情况下还出现父类的问题>>

            /**
             * 保存匹配时模式的信息，以便分析
             */
            StringBuilder sbAnaly = new StringBuilder();
            String[] arrayRes = resSet.toArray(new String[resSet.size()]);
            for (int i = 0; i < arrayRes.length; i++) {
                String cmp = arrayRes[i];
                int j = 0;
                for (j = 0; j < classList.length; j++) {
                    if (classList[j].equals(cmp))
                        break;
                }
                //debug
                if (!cmp.equals(classList[j]))
                    System.out.println("debug" + cmp + "\t" + classList[j]);
                if (!Analysis.model_shadow_Map.containsKey(patternList[j]))
                    Analysis.model_shadow_Map.put(patternList[j], new LinkedHashSet<String[]>());
                Analysis.model_shadow_Map.get(patternList[j]).add(new String[]{line, classList[j]});
                sbAnaly.append("(" + patternList[j] + "," + classList[j] + ")" + " ");
            }
            Analysis.model_compete_Map.put(line, sbAnaly.substring(0, sbAnaly.length() - 1));
        }
        if (type.equals(NoPattern)) {
            Analysis.model_compete_Map.put(line, "(!未匹配" + "," + NoPattern + ")");
            if (!Analysis.model_shadow_Map.containsKey("!未匹配"))
                Analysis.model_shadow_Map.put("!未匹配", new LinkedHashSet<String[]>());
            Analysis.model_shadow_Map.get("!未匹配").add(new String[]{line, type});
        }

        sentenceToClassMap.put(line, type);
        return type;
    }

    /**
     * 将sentence与一个class的模式比较，若匹配到一个模式后立即返回匹配结果
     *
     * @param regex 某个class的所有模式
     * @param line
     * @return 若均未匹配则return空串，否则返回匹配上regex中的第一个模式时 匹配到这个模式时sentence中的词组合+'\t'+匹配上的模式
     */
    private static String isKeyType(String regex, String line) {
        String[] regexs = regex.split("@");
        for (String s : regexs) {
            //System.out.println("debug s regexs"+s);
            StringBuilder sb = new StringBuilder();
            if (s.contains(".*")) {
                String[] al = s.split("\\.\\*");
                boolean flag = true;
                for (String e : al) {
                    //System.out.println("debug"+e);
                    Matcher pa = Pattern.compile(e).matcher(line);
                    if (!pa.find()) {
                        flag = false;
                        break;
                    } else
                        sb.append(line.substring(pa.start(), pa.end()));
                }
                if (flag) {
                    //System.out.println("debug"+s);
                    sb.append("\t" + s);  //匹配到某个的模式用的词组合+'\t'+匹配上的pattern
                    return sb.toString();
                }
            } else {
                Matcher pa = Pattern.compile(s).matcher(line);
                if (pa.find()) {
                    //System.out.println("debug"+s);
                    return line.substring(pa.start(), pa.end()) + "\t" + s;
                }
            }
        }
        return "";
    }


    public static void main(String[] args) throws Exception {
        loadPattern("Data/问题类别模式.txt");
        treeCount = new myTreeKount("Data/问题类别模式.txt");
        NoPattern = keyList.get(keyList.size() - 1).trim();
        processCluster("Data/prase_50w.in", "Data/prase_out.txt");

        File fileOut = new File("Data/贷款人所在行业.txt");
        fileOut.createNewFile();
        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(fileOut));
        BufferedWriter bw = new BufferedWriter(osw);
        bw.write("//贷款人所在行业\n");
        for (String str : keyList) {
            if (count2.containsKey(str)) {
                bw.write(str + "\t" + count2.get(str) + "\n");
            }
        }
        bw.close();

        fileOut = new File("Data/贷款流向行业.txt");
        fileOut.createNewFile();
        osw = new OutputStreamWriter(new FileOutputStream(fileOut));
        bw = new BufferedWriter(osw);
        bw.write("//贷款流向行业\n");
        for (String str : keyList) {
            if (count3.containsKey(str)) {
                bw.write(str + "\t" + count3.get(str) + "\n");
            }
        }
        bw.close();


        Analysis.Anylysis_model_compete();
        Analysis.Analysis_model_shadow();
        Analysis.Analysis_model_shadow_all(patternMap);
    }
}

