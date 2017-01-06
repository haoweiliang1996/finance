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
    public static HashMap<String, Integer> count2 = new HashMap<String, Integer>(); //ͳ�ƴ�����������ҵ
    public static HashMap<String, Integer> count3 = new HashMap<String, Integer>();//ͳ�ƴ���������ҵ
    public static HashMap<String, String> sentenceToClassMap = new HashMap<>();
    public static HashMap<String, Vector<String>> pattern_to_deny_pattern = new HashMap<>();
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

                    //����Դ��� W1|W2*W3|W4������
                    String[] vauleArray = value.split("\\s+");
                    //System.out.println("debug"+vauleArray.length);
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < vauleArray.length; i++) {
                        //System.out.print("debug"+vauleArray[i]);

                        //������Ҫȥ����ģʽ������
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
                        //����Ҫȥ����ģʽ�����롷

                        //����ģʽ,������ W1|W2*w3������->(W1|W2)*W3
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
                            sb.append("@");                     //��ͬ��ģʽ��ļ������@
                    }
                    if (sb.length() > 0) {
                        value = sb.toString();
                        //System.out.println("debug"+value);
                    }
                    //����Դ��� W1|W2*W3|W4������

                    if (patternMap.containsKey(key))
                        value = patternMap.get(key) + "@" + value;
                    patternMap.put(key, value);
                }
            }
            br.close();
            System.out.println(patternMap);
            System.out.println("debug " + "pattern_to_deny_pattern " + pattern_to_deny_pattern.toString());
        } else {
            System.out.println("�Ҳ���ָ�����ļ�");
        }
    }

    /**
     * �ж������ļ��е�sentence����class
     *
     * @param inFile  ϣ������������ļ�
     *                ģʽ���еĸ�ʽ��ÿ��ģʽǰ�������0������Tab������ʾ���֮��Ĳ�ι�ϵ��ģʽ���BNF�������£�
     *                <ģʽ��>::=<Line>{<Line>}*
     *                <Line>::=<Tabs><Class><Tab><Pat1>{<Space><Pat2>}*
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
                            .map(str -> str.trim().replaceAll("\\s+", "��").replaceAll("����", "��").replaceAll("������", "��").trim())
                            .map(str -> type(str))
                            .filter(str -> str.length() != 0)
                            .peek(System.out::println)   //to-do ��ͳ��д��consumer
                            .collect(Collectors.toList()))
                    .
            while ((line = br.readLine()) != null) {
                line_count++;
                if (line_count % 1000 == 0)
                    System.out.println("�����ˣ�" + line_count);
                if (!line.isEmpty()) {
                    String[] line_c = line.split("\\t", -1);//������β����/t
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
                        line_cs = line_cs.replaceAll("\\s+", "��");
                        line_cs = line_cs.replaceAll("����", "��");
                        line_cs = line_cs.replaceAll("������", "��");
                        String result = type(line_cs).trim();

                        //<<ͳ����ҵ
                        /**
                         * ����������^���ƣ������������������û��
                         */
                        if (result.length() == 0) {
                            System.out.println("debug + line_cs: " + line_cs + " result:" + result);
                            System.out.println("��Ϊĳ����� ɾ���������ҵ���class");
                            result = "������";
                        }

                        //Ϊ��ͳ�ƣ���Ҫ�õ�class�����и�class
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
                        //>>ͳ����ҵ

                        resultTemp += result + "\t";//������������ҵ<tab>����������ҵ
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

            //����������ָ����ʽд���ļ�
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
            System.out.println("�Ҳ���ָ�����ļ�");
        }
    }

    /**
     * �ж�һ��sentence����class
     *
     * @param line sentence
     * @return ƥ�䵽��classs ��ʽ���磺class1|class2|class3
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
            //<<����ȥ��ģʽ
            String[] typeResult = sb.substring(0, sb.length() - 1).split("@");//ȥ��ĩβ�ġ�@��
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
                    continue;
                }
                for (String denyPattern : pattern_to_deny_pattern.get(pa)) {
                    denyPatternSet.add(denyPattern);
                }
            }

            HashSet<String> denyPhaseSet = new HashSet<String>();
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

            Vector<String> result_split = new Vector<>();
            for (String cl : classList) {
                if (!denyClassSet.contains(cl))
                    result_split.add(cl);
            }
            //����ȥ��ģʽ>>

            //<<���������������»����ָ��������
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

        sentenceToClassMap.put(line, type);
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
        processCluster("Data/prase_50w.in", "Data/prase_out.txt");

        File fileOut = new File("Data/������������ҵ.txt");
        fileOut.createNewFile();
        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(fileOut));
        BufferedWriter bw = new BufferedWriter(osw);
        bw.write("//������������ҵ\n");
        for (String str : keyList) {
            if (count2.containsKey(str)) {
                bw.write(str + "\t" + count2.get(str) + "\n");
            }
        }
        bw.close();

        fileOut = new File("Data/����������ҵ.txt");
        fileOut.createNewFile();
        osw = new OutputStreamWriter(new FileOutputStream(fileOut));
        bw = new BufferedWriter(osw);
        bw.write("//����������ҵ\n");
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

