package main;

import java.io.IOException;
import java.util.*;

import tree.Tree;
import tree.TreeNode;
import util.FileReader;
import util.FileWriter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static final String charset = "UTF-8";

    public static void startTest() {
        Tree tree = new Tree("Data/行业分类-用于分类封装.txt", "utf-8");
//		Tree tree = new Tree("C:\\Users\\wuyuming\\Desktop\\行业分类-用于分类封装.txt");
        Scanner cin = new Scanner(System.in);
        tree.display();
        while (cin.hasNext()) {
            String line = cin.nextLine();
            String[] parts = line.split("\t", -1);
            switch (parts[0]) {
                //merge node
                case "M":
                    List<String> lst = Arrays.asList(Arrays.copyOfRange(parts, 1, parts.length));
                    tree.merge(lst, true);
                    break;
                //delete node
                case "D":
                    tree.delete(parts[1]);
                    break;
                //insert node
                case "I":
                    tree.add(parts[1], parts[2]);
                    break;
                //replace node
                case "R":
                    tree.rename(parts[1], parts[2]);
                    break;
                //get all parents
                case "G":
                    System.out.println(tree.getAllParentsAndItself(parts[1]));
                    break;
                default:
                    System.out.println("Invalid Input!");
                    System.out.println("\tM for merge node");
                    System.out.println("\tD for delete node");
                    System.out.println("\tI for insert node");
                    System.out.println("\tR for replace node");
                    System.out.println("\tG for get all parents node");
                    continue;
            }
            tree.display();
        }
        cin.close();
    }

    /**
     * @return 载入好了配置文件的tree
     * @throws IOException
     */
    public static Tree load_tree()
            throws IOException {
        Tree tree = new Tree("Data/行业分类-用于分类封装.txt", charset);
        FileReader fr = new FileReader("Data/分类封装config.txt", charset);
        String line = fr.readLine();
        while (line != null) {
            String[] parts = line.split("\t", -1);
            switch (parts[0]) {
                //merge node
                case "M":
                    List<String> lst = Arrays.asList(Arrays.copyOfRange(parts, 1, parts.length));
                    tree.merge(lst, true);
                    break;
                //delete node
                case "D":
                    tree.delete(parts[1]);
                    break;
                //insert node
                case "I":
                    tree.add(parts[1], parts[2]);
                    break;
                //replace node
                case "R":
                    tree.rename(parts[1], parts[2]);
                    tree.linkNameToNameInTheTree.apply(parts[2],parts[1]);
                    break;
                //get all parents
                case "G":
                    System.out.println(tree.getAllParentsAndItself(parts[1]));
                    break;
                default:
                    break;
            }
            line = fr.readLine();
        }
        //tree.display();
        fr.close();
        return tree;
    }

    public static Tree load_init_tree()
        throws IOException{
        return new Tree("Data/行业分类-用于分类封装.txt", charset);
    }

    //传入class,返回包含其父节点的class列表

    /**
     * note:abandon this method
     *
     * @param
     * @return
     * @throws IOException
     */
    public static void start()
            throws IOException {
        Tree tree = new Tree("Data/问题类别模式.txt.bak", charset);
        FileReader fr = new FileReader("Data/分类封装config.txt", charset);
        String line = fr.readLine();
        while (line != null) {
            String[] parts = line.split("\t", -1);
            switch (parts[0]) {
                //merge node
                case "M":
                    List<String> lst = Arrays.asList(Arrays.copyOfRange(parts, 1, parts.length));
                    tree.merge(lst, true);
                    break;
                //delete node
                case "D":
                    tree.delete(parts[1]);
                    break;
                //insert node
                case "I":
                    tree.add(parts[1], parts[2]);
                    break;
                //replace node
                case "R":
                    tree.rename(parts[1], parts[2]);
                    break;
                //get all parents
                case "G":
                    System.out.println(tree.getAllParentsAndItself(parts[1]));
                    break;
                default:
                    break;
            }
            line = fr.readLine();
        }
        tree.display();
        fr.close();
    }

    public static void startGet() throws IOException {
        Tree tree = load_tree();
        tree.display();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("Data/分类.txt"), "utf-8"));
        List<String> lines = br.lines().collect(Collectors.toList());
        FileWriter fw = new FileWriter("Data/分类_getPa.txt", "utf-8", false, true);
        lines.stream().filter(x -> !x.equals("信息不全"))
                .forEach(x -> fw.write(x + "\t" + Stream.of(x.split("\\|")).
                        map(str ->  tree.getAllParentsAndItself(str).toString()).reduce((a,b) -> a+"|"+b).orElse("bug")+"\n"
                    )
                );
    }

    public static void main(String[] args)
            throws IOException {
        startGet();
        //Main();
        //start();
        //startTest();
        //Tree tree = load_tree();//new Tree("Data/行业分类-用于分类封装.txt", "utf-8");
        //tree.display();
        //tree.display();
        //System.out.println(tree.getAllParentsAndItself("金属制品修理"));
        //System.out.println(tree.getAllParentsAndItself("金属制品、机械和设备修理业"));


    }
}
