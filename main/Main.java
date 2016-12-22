package main;
import java.io.IOException;
import java.util.*;
import tree.Tree;
import tree.TreeNode;
import util.FileReader;

public class Main {
	public static final String charset = "UTF-8";
	
	public static void startTest() {
		Tree tree = new Tree("Data/test.txt", charset);
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
     *
     * @return 载入好了配置文件的tree
     * @throws IOException
     */
    public static Tree load_tree()
            throws IOException{
        Tree tree = new Tree("Data/行业分类-用于分类封装.txt", charset);
        FileReader fr = new FileReader("Data/分类封装config.txt", charset);
        String line = fr.readLine();
        while (line != null) {
            String[] parts = line.split("\t", -1);
            switch (parts[0]) {
                //merge node
                case "M":
                    List<String> lst = Arrays.asList(Arrays.copyOfRange(parts, 1, parts.length));
                    tree.merge(lst, false);
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
        //tree.display();
        fr.close();
        return tree;
    }

	//传入class,返回包含其父节点的class列表

    /**
     * note:abandon this method
     * @param
     * @return
     * @throws IOException
     */
	public static void start()
	throws IOException{
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

	
	public static void main(String[] args)
    throws IOException{
		start();
		//startTest();
	}
}
