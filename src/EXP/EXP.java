/**
 * Created by haowei on 16-11-23.
 */
package EXP;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import main.Main;
import tree.Tree;
import util.FileWriter;

public class EXP {
    public static ConcurrentHashMap<String, String> patternMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, List<String>> pattern_to_deny_pattern = new ConcurrentHashMap<>();
    public static Vector<String> keyList = new Vector<>();
    public static String NoPattern = ""; //�������ģʽ�����һ��ΪNoPattern������
    public static myTreeKount treeCount;
    public static int line_count = 0;

    //ǰ�����������ҵ�class�����и�class �Ա�ͳ��
    public static class myTreeKount {
        private static class flag {
            public int ROOT = -2; //pre[i]Ϊ-2ʱ���ڵ�������
            public int UN_PRASE = -1;
        }

        private flag FLAG = new flag();
        private int pre[];
        private HashMap<String, Integer> key_to_keyid = new HashMap<>();

        /**
         * @param s
         * @return ���Ŀ�ͷtab�ĸ���
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
                System.out.println("error " + "����ʱkey����Խ�� keyId: " + keyId);
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
         * �����������ģʽ�����νṹ���������class�洢��keylist�У�classǰ���tab�������������ʱ��Ȼ���пո񣬴Ӷ���֤�������Ϊ����
         *
         * @param strFile �������ģʽ���ļ���
         * @throws IOException
         */
        public myTreeKount(String strFile) throws IOException {
            File file = new File(strFile);
            if (file.isFile() && file.exists()) {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(
                        file), "GBK"));
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
                System.out.println("info " + "���е�class��Ч");
            pre = new int[keyList.size()];
            for (int i = 0; i < pre.length; i++)
                pre[i] = FLAG.UN_PRASE;//���ڵ�
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
                System.out.println("KeyList�±�Խ�磺" + id + " " + keyList.size());
            return keyList.get(id);
        }
    }

    private static void parseSingleLinePattern(String key, String patterns) {
        Function<String, String> parseSplitBar = str -> Stream.of(str.split("\\.\\*"))
                .map(x -> x.contains("|") ? "(" + x + ")" : x)
                .reduce((a, b) -> a + ".*" + b).orElse("bug report");
        Vector<String> patternListOfTheKey = new Vector<>();
        Stream.of(patterns.split("\\s+"))
                .filter(x -> !x.contains("^"))
                .map(parseSplitBar)
                .peek(x -> pattern_to_deny_pattern.put(x,new ArrayList<>()))
                .forEach(patternListOfTheKey::add);
        BiFunction<String, String, List<String>> parseAbandonPattern = (pattern, denyPatterns) -> pattern_to_deny_pattern.put(parseSplitBar.apply(pattern)
                , Stream.of(denyPatterns.split(",")).map(parseSplitBar).collect(Collectors.toList()));
        Stream.of(patterns.split("\\s+")).filter(x -> x.contains("^"))
                .peek(x -> patternListOfTheKey.add(parseSplitBar.apply(x.substring(0, x.indexOf('^')))))
                .forEach(x -> parseAbandonPattern.apply(x.substring(0, x.indexOf('^')), x.substring(x.indexOf('^') + 1)));
        patternMap.put(key, patternListOfTheKey.stream().reduce((a, b) -> a + "@" + b).orElse("empty patterns bug!"));
    }

    /**
     * �����������ģʽ,����class(ȥ����ǰ���tab)��ģʽ�Ķ�Ӧ��ϵ�����ģʽ��-ģʽ�Ķ�Ӧ��ϵ
     *
     * @param strFile �������ģʽ���ڵ��ļ�
     * @throws IOException �򲻿��ļ�
     */
    public static void loadPattern(String strFile)
            throws IOException {
        File file = new File(strFile);
        if (file.isFile() && file.exists()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "GBK"));
            br.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && line.contains("\t"))
                    .map(line -> line.replaceAll("\\+", "").replaceAll("\\*", ".*"))
                    .forEach(line -> parseSingleLinePattern(line.substring(0, line.indexOf('\t')), line.substring(line.indexOf('\t') + 1).trim()));
        }
        System.out.println(patternMap);
        System.out.println(patternMap.size());
        System.out.println(pattern_to_deny_pattern);
    }

    /**
     * �ж������ļ��е�sentence����class
     *
     * @param inFile  ϣ������������ļ�
     *                ģʽ���еĸ�ʽ��ÿ��ģʽǰ�������0������Tab������ʾ���֮��Ĳ�ι�ϵ��ģʽ���BNF�������£�
     *                <ģʽ��>::=<Line>{<Line>}*
     *                <Line>::=<Tabs><Class><Tabs><Pat1>{<Space><Pat2>}*
     *                |<TABs><Class>
     *                <TABs>::={<Tab>}*
     *                ʾ����
     *                Class1        Pat11  Pat12  Pat13...
     *                Class11       Pat111  Pat112  ...
     *                Class12       Pat121  Pat122  ...
     *                Class2        Pat21  Pat22  ...
     *                Class21       Pat211  Pat212  ...
     *                Class22       Pat221  Pat222  ...
     *                Class23       Pat231  Pat232  ...
     * @param outFile ���ڴ���������Ϊ��
     *                ID��tab�����ġ�tab��������ҵ�̾䡶tab��������ҵ�̾䡶tab��������ҵclass��tab��������ҵclass
     * @throws IOException
     */
    public static void processCluster(String inFile, String outFile)
            throws IOException {
        FileWriter fw = new FileWriter(outFile, "GBK", false, true);
        FileWriter fwC = new FileWriter(outFile.split("\\.")[0] + "_client.txt", "GBK", false, true);
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), "GBK"));

        System.out.println("debug start process");

        List<String> lines = br.lines().collect(Collectors.toList());
        br.close();

        ConcurrentMap<String, String> resultCache = new ConcurrentHashMap<>();
        lines.parallelStream()
                .filter(str -> !str.isEmpty())
                .map(str -> str.split("\\t", -1))
                .map(strList -> Arrays.asList(strList[2], strList[3]))
                .flatMap(List::stream)
                .distinct()
                .filter(str -> str.length() != 0)
                .forEach(str -> resultCache.put(str, type(str.toUpperCase())));
        System.out.println("���ദ�����");
        System.out.println(resultCache);
        System.out.println(lines);
        Tree tree = Main.load_tree();
        Function<String, String> getParentClass = strWithPipe -> Stream.of(strWithPipe.split("\\|"))
                .map(singleStr -> tree.getAllParentsAndItself(singleStr).toString())
                .reduce((a, b) -> a + "|" + b).orElse("");
        resultCache.keySet().forEach(key -> resultCache.put(key, getParentClass.apply(resultCache.get(key))));

        Function<String, String> getResultByCache = str -> str.length() == 0 ? "" : "\t" + resultCache.get(str);
        BiFunction<String, String, String> compareTwoResult = (first, second) ->
                resultCache.get(second).equals("[" + NoPattern + "]") || second.equals("") ? first : second;
        lines.stream()
                .peek(x -> System.out.print(x + "\n"))
                .map(str -> str.split("\t", -1))
                .peek(x -> System.out.print(x[3] + "\n"))
                .peek(strList -> fw.write(String.join("\t", Arrays.asList(strList))
                        + getResultByCache.apply(strList[2]) + getResultByCache.apply(strList[3]) + "\n"))
                .forEach(strList -> fwC.write(String.join("\t", Arrays.asList(strList))
                        + getResultByCache.apply(compareTwoResult.apply(strList[2], strList[3])) + "\n"));
    }

    /**
     * �ж�һ��sentence����class
     *
     * @param line sentence
     * @return ƥ�䵽��classs ��ʽ���磺class1|class2|class3
     */
    private static String type(String line) {

        StringBuilder sb = new StringBuilder();
        sb.append(
                patternMap.entrySet().stream().map(x -> new String[]{x.getKey(),isKeyType(x.getValue(),line)})
                .filter(x -> x[1].length()!=0)
                .map(x -> x[1] +'\t' +x[0])
                .reduce((a,b) -> a+'@'+b).orElse("")
        );
        //Iterator<Entry<String, String>> iter = patternMap.entrySet().iterator();


        /*while (iter.hasNext()) {
            Entry<String, String> entry = iter.next();
            String key = entry.getKey();
            String regex = entry.getValue();

            String matchedPattern = isKeyType(regex, line);
            if (matchedPattern.length() == 0)
                continue;
            matchedPattern += "\t" + key;
            //if (line_count % 10000 == 0)
            //  System.out.println("debug " + "matchedPattern " + matchedPattern);
            sb.append(matchedPattern + "@");
        }*/

        String type = NoPattern;
        if (sb.length() != 0) {
            //System.out.println("debug typeResult"+sb.substring(0,sb.length()-1));
            //<<����ȥ��ģʽ
            String[] typeResult = sb.toString().split("@");//ȥ��ĩβ�ġ�@��
            //List<String []> temp = Arrays.stream(typeResult).map(x -> x.split("@")).collect(Collectors.toList());
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
            for (String pa : patternList) {//ƥ�䵽��ģʽ������Щģʽ�õ���Ҫɾ����ģʽ�ļ��ϣ�������ƥ�䵽�Ĵ�����Ҫɾ����ģʽ����ɾ����Щ��
                if (!pattern_to_deny_pattern.containsKey(pa)) {
                    System.out.println("error+ " + pa + "û�г����� pattern_to_deny_pattern");
                }
                else
                    pattern_to_deny_pattern.get(pa).forEach(denyPatternSet::add);
            }

            HashSet<String> denyPhaseSet = new HashSet<>();
            //��Ҫ�����ҽ���������Ȼ���ܴ��� 		����Ͷ�ʣ�������	Ͷ��*ƻ��*��Ʒ-ƻ��*��Ʒ
            for (int i = 0; i < phaseList.length; i++) {//����ƥ�䵽�Ĵ�
                boolean flag1 = false;//flag1Ϊtrueʱɾ�����ƥ�䵽�Ĵ�
                for (String s : denyPatternSet) {//������Щ������ķ�list
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
                        if (flag)//����
                        {
                            String temp = phaseList[i];
                            if (temp.replaceAll(sBuilder.substring(0, sBuilder.length() - 1), "").length() == 0)//��������
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
                    if (isKeyType(patternList[i], denyPhase).length() != 0)//Ӧ�ñ�deny��phaseƥ������ĳ��pattern��ô���pattern���ڵ�classӦ�ñ���ֹ
                    {
                        denyClassSet.add(classList[i]);
                    }
                }
            }
            //denyPhaseSet.forEach();
            List<String> result_split = Arrays.asList(classList);
            result_split.removeIf(denyClassSet::contains);

            //����ȥ��ģʽ>>

            //<<���������������»����ָ��������
            LinkedHashSet<String> resSet = new LinkedHashSet<>(result_split);
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
            //sb = new StringBuilder();
            /*for (String str : resSet) {
                sb.append(str + "|");
            }*/
            resSet.stream().reduce((a,b) -> a+ "|" +b).orElse("todo");
            /*if (sb.length() > 0)
                type = sb.substring(0, sb.length() - 1);*/
            //���������������»����ָ��������>>

            /**
             * ����ƥ��ʱģʽ����Ϣ���Ա����
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
            Analysis.model_compete_Map.put(line, "(!δƥ��" + "," + NoPattern + ")");
            if (!Analysis.model_shadow_Map.containsKey("!δƥ��"))
                Analysis.model_shadow_Map.put("!δƥ��", new LinkedHashSet<String[]>());
            Analysis.model_shadow_Map.get("!δƥ��").add(new String[]{line, type});
        }

        return type;
    }

    /**
     * ��sentence��һ��class��ģʽ�Ƚϣ���ƥ�䵽һ��ģʽ����������ƥ����
     *
     * @param regex ĳ��class������ģʽ
     * @param line
     * @return ����δƥ����return�մ������򷵻�ƥ����regex�еĵ�һ��ģʽʱ ƥ�䵽���ģʽʱsentence�еĴ����+'\t'+ƥ���ϵ�ģʽ
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
                    sb.append("\t" + s);  //ƥ�䵽ĳ����ģʽ�õĴ����+'\t'+ƥ���ϵ�pattern
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
        loadPattern("Data/�������ģʽ.txt");
        treeCount = new myTreeKount("Data/�������ģʽ.txt");
        NoPattern = keyList.get(keyList.size() - 1).trim();
        processCluster("Data/sentence.txt", "Data/prase_out.txt");

        Analysis.Anylysis_model_compete();
        Analysis.Analysis_model_shadow();
        Analysis.Analysis_model_shadow_all(patternMap);
    }
}

