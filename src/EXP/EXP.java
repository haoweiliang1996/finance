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
import tree.Tree;
import util.FileWriter;

public class EXP {
    private static ConcurrentHashMap<String, String> patternMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, List<String>> pattern_to_deny_pattern = new ConcurrentHashMap<>();
    private static Vector<String> keyList = new Vector<>();
    private static String NoPattern = ""; //问题类别模式的最后一行为NoPattern的名字
    private static myTreeKount treeCount;
    private static int line_count = 0;

    //前向树，用来找到class的所有父class 以便统计
    private static class myTreeKount {
        private static class flag {
            private int ROOT = -2; //pre[i]为-2时，节点是树根
            private int UN_PRASE = -1;
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
        private myTreeKount(String strFile) throws IOException {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(
                    strFile), "GBK"));
            List<String> lines = br.lines().collect(Collectors.toList());
            br.close();
            lines.stream()
                    .filter(line -> line.lastIndexOf("\t") == -1)
                    .forEach(line -> System.out.println("check in myTreecount\t" + line));
            lines.stream()
                    .map(line -> line.lastIndexOf('\t') == -1 ? line.trim() : line.substring(0, line.lastIndexOf('\t')).trim())
                    .peek(keyList::add)
                    .forEach(key -> key_to_keyid.put(key, keyList.size()));
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
                System.out.println("info " + "这行的class无效");
            */
            pre = new int[keyList.size()];
            for (int i = 0; i < pre.length; i++)
                pre[i] = FLAG.UN_PRASE;//根节点
            IntStream.range(0, pre.length).filter(i -> pre[i] != FLAG.ROOT).forEach(i -> buildTree(i, FLAG.ROOT));
        }

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
                System.out.println("KeyList下标越界：" + id + " " + keyList.size());
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
     * 载入问题类别模式,建立class(去掉了前面的tab)与模式的对应关系，获得模式与-模式的对应关系
     *
     * @param strFile 问题类别模式所在的文件
     * @throws IOException 打不开文件
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
     * 判断输入文件中的sentence所属class
     *
     * @param inFile  希望处理的输入文件
     *                模式库中的格式：每个模式前面可能有0个或多个Tab键，表示类别之间的层次关系。模式库的BNF定义如下：
     *                <模式库>::=<Line>{<Line>}*
     *                <Line>::=<Tabs><Class><Tabs><Pat1>{<Space><Pat2>}*
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
        System.out.println("分类处理结束");
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
     * 判断一个sentence所属class
     *
     * @param line sentence
     * @return 匹配到的classs 格式形如：class1|class2|class3
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
            //<<处理去除模式
            String[] typeResult = sb.toString().split("@");//去除末尾的‘@’
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
                } else
                    pattern_to_deny_pattern.get(pa).forEach(denyPatternSet::add);
            }

            BiPredicate<String, String> allPhasesContained = (pattern, shortSentence) -> Stream.of(pattern.split("\\.\\*"))
                    .allMatch(phase -> Pattern.compile(phase).matcher(shortSentence).find());
            BiPredicate<String, String> onlyContainThesePhases = (pattern, shortSentence) -> shortSentence
                    .replaceAll(pattern.replaceAll("\\.\\*", "|"), "").length() == 0;
            HashSet<String> denyPhaseSet = new HashSet<>();
            //需要包括且仅仅包括不然不能处理 		外资投资！！！！	投资*苹果*产品-苹果*产品
            Predicate<String> shouldDenied = phase -> denyPatternSet.stream()
                    .anyMatch(pattern -> allPhasesContained.and(onlyContainThesePhases).test(pattern, phase));
            Stream.of(phaseList).filter(shouldDenied).forEach(denyPhaseSet::add);
            HashSet<String> denyClassSet = new HashSet<>();
            //应该被deny的phase匹配上了某个pattern那么这个pattern对于的class应该被禁止
            //因为所有phase都已经找到了，其对应的pattern也就都放在了patternList中
            // ，因此寻找应该被deny的phase所匹配上的pattern时只需遍历pattenList即可，而不用重新匹配整个类别-模式库
            denyPhaseSet.forEach(denyPhase -> IntStream.range(0, patternList.length)
                    .filter(i -> isKeyType(patternList[i], denyPhase).length() != 0).forEach(i -> denyClassSet.add(classList[i])));
            List<String> result_split = Arrays.asList(classList);
            //result_split.removeIf(denyClassSet::contains);
            //处理去除模式>>

            //<<处理有子类的情况下还出现父类的问题
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

        return type;
    }

    /**
     * 将sentence与一个class的模式比较，若匹配到一个模式后立即返回匹配结果
     * findFirst形式的匹配类别，有可能有些denypattern 没能考虑到
     *
     * @param regexs 某个class的所有模式
     * @param line
     * @return 若均未匹配则return空串，否则返回匹配上regex中的第一个模式时 匹配到这个模式时sentence中的词组合+'\t'+匹配上的模式
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
        loadPattern("Data/问题类别模式.txt");
        treeCount = new myTreeKount("Data/问题类别模式.txt");
        NoPattern = keyList.get(keyList.size() - 1).trim();
        processCluster("Data/sentence.txt", "Data/prase_out.txt");

        Analysis.Anylysis_model_compete();
        Analysis.Analysis_model_shadow();
        Analysis.Analysis_model_shadow_all(patternMap);
    }
}

