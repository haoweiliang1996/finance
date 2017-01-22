/**
 * Created by haowei on 16-11-23.
 */
package EXP;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import main.Main;
import org.jetbrains.annotations.Contract;
import tree.Tree;
import util.FileWriter;

public class EXP {
    private static ConcurrentHashMap<String, String> patternMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, List<String>> pattern_to_deny_pattern = new ConcurrentHashMap<>();
    private static Vector<String> keyList = new Vector<>();
    private static String NoPattern = ""; //�������ģʽ�����һ��ΪNoPattern������
    private static myTreeKount treeCount;
    private static int line_count = 0;

    //ǰ�����������ҵ�class�����и�class �Ա�ͳ��
    private static class myTreeKount {
        private static class flag {
            private int ROOT = -2; //pre[i]Ϊ-2ʱ���ڵ�������
            private int UN_PRASE = -1;
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
        private myTreeKount(String strFile) throws IOException {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(
                    strFile), "GBK"));
            List<String> lines = br.lines().collect(Collectors.toList());
            br.close();
            lines.stream()
                    .filter(line -> line.lastIndexOf("\t") == -1)
                    .forEach(line -> System.out.println("check in myTreecount\t" + line));
            lines.stream()
                    .map(line -> line.lastIndexOf('\t') == -1 ? line : line.substring(0, line.lastIndexOf('\t')))
                    .peek(keyList::add)
                    .map(String::trim)
                    .forEach(key -> key_to_keyid.put(key, keyList.size() - 1));
           /* File file = new File(strFile);
            if (file.isFile() && file.exists()) {

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
            */
            pre = new int[keyList.size()];
            for (int i = 0; i < pre.length; i++)
                pre[i] = FLAG.UN_PRASE;//���ڵ�
            IntStream.range(0, pre.length).filter(i -> pre[i] == FLAG.UN_PRASE).forEach(i -> buildTree(i, FLAG.ROOT));
        }

        @Contract(pure = true)
        private int getFatherKeyId(int keyid) {
            return pre[keyid];
        }

        private int getKeyId(String key) {
            //System.out.println("debug getKeyId:"+key);
            if (!key_to_keyid.containsKey(key))
                return FLAG.ROOT;
            return key_to_keyid.get(key);
        }

        private String getKeyById(int id) {
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
                .peek(x -> pattern_to_deny_pattern.put(x, new ArrayList<>()))
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
    private static void loadPattern(String strFile)
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
    private static void processCluster(String inFile, String outFile)
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
                patternMap.entrySet().stream().map(x -> new String[]{x.getKey(), isKeyType(x.getValue(), line)})
                        .filter(x -> x[1].length() != 0)
                        .map(x -> x[1] + '\t' + x[0])
                        .reduce((a, b) -> a + '@' + b).orElse("")
        );
        //System.out.println(sb.toString());
        String type = NoPattern;
        if (sb.length() != 0) {
            //System.out.println("debug typeResult"+sb.substring(0,sb.length()-1));
            //<<����ȥ��ģʽ
            String[] typeResult = sb.toString().split("@");//ȥ��ĩβ�ġ�@��
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
                } else
                    pattern_to_deny_pattern.get(pa).forEach(denyPatternSet::add);
            }

            BiPredicate<String, String> allPhasesContained = (pattern, shortSentence) -> Stream.of(pattern.split("\\.\\*"))
                    .allMatch(phase -> Pattern.compile(phase).matcher(shortSentence).find());
            BiPredicate<String, String> onlyContainThesePhases = (pattern, shortSentence) -> shortSentence
                    .replaceAll(pattern.replaceAll("\\.\\*", "|"), "").length() == 0;
            HashSet<String> denyPhaseSet = new HashSet<>();
            //��Ҫ�����ҽ���������Ȼ���ܴ��� 		����Ͷ�ʣ�������	Ͷ��*ƻ��*��Ʒ-ƻ��*��Ʒ
            Predicate<String> shouldDenied = phase -> denyPatternSet.stream()
                    .anyMatch(pattern -> allPhasesContained.and(onlyContainThesePhases).test(pattern, phase));
            Stream.of(phaseList).filter(shouldDenied).forEach(denyPhaseSet::add);
            HashSet<String> denyClassSet = new HashSet<>();
            //Ӧ�ñ�deny��phaseƥ������ĳ��pattern��ô���pattern���ڵ�classӦ�ñ���ֹ
            //��Ϊ����phase���Ѿ��ҵ��ˣ����Ӧ��patternҲ�Ͷ�������patternList��
            // �����Ѱ��Ӧ�ñ�deny��phase��ƥ���ϵ�patternʱֻ�����pattenList���ɣ�����������ƥ���������-ģʽ��
            denyPhaseSet.forEach(denyPhase -> IntStream.range(0, patternList.length)
                    .filter(i -> isKeyType(patternList[i], denyPhase).length() != 0).forEach(i -> denyClassSet.add(classList[i])));
            List<String> result_split = new ArrayList<>(Arrays.asList(classList));
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
            type = resSet.stream().reduce((a, b) -> a + "|" + b).orElse("todo");
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
     * findFirst��ʽ��ƥ������п�����Щdenypattern û�ܿ��ǵ�
     *
     * @param regexs ĳ��class������ģʽ
     * @param line
     * @return ����δƥ����return�մ������򷵻�ƥ����regex�еĵ�һ��ģʽʱ ƥ�䵽���ģʽʱsentence�еĴ����+'\t'+ƥ���ϵ�ģʽ
     */
    private static String isKeyType(String regexs, String line) {
        BiPredicate<String, String> allPhasesContained = (pattern, shortSentence) -> Stream.of(pattern.split("\\.\\*"))
                .allMatch(phase -> Pattern.compile(phase).matcher(shortSentence).find());
        String matchedPattern = Arrays.stream(regexs.split("@"))
                .filter(regex -> allPhasesContained.test(regex, line)).findFirst().orElse("");
        if (matchedPattern.equals(""))
            return matchedPattern;
        String matchedPhase = Stream.of(matchedPattern.split("\\.\\*"))
                .map(phase -> Pattern.compile(phase).matcher(line))
                .peek(Matcher::find)
                .map(matcher -> line.substring(matcher.start(), matcher.end()))
                .reduce(String::concat).orElse("bug report");
        return matchedPhase + "\t" + matchedPattern;
    }


    public static void main(String[] args) throws Exception {
        loadPattern("Data/�������ģʽ.txt");
        treeCount = new myTreeKount("Data/�������ģʽ.txt");
        NoPattern = keyList.get(keyList.size() - 1).trim();
        processCluster("Data/sentence.txt", "Data/parse_out.txt");

        Analysis.Anylysis_model_compete();
        Analysis.Analysis_model_shadow();
        Analysis.Analysis_model_shadow_all(patternMap);
    }
}

