package tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import util.FileReader;
import util.ListSolver;

public class Tree {
	//����������ڵ㣬���ļ������е������ĸ�
	public static final TreeNode ROOT = TreeNode.EMPTY;
	TreeNodePool pool = new TreeNodePool();
	
	public Tree(String filePath, String charset) {
		fromFile(filePath, charset);
	}
	
	/**
	 * ���ļ�������
	 * @param filePath
	 * @param charset
	 */
	private void fromFile(String filePath, String charset) {
		//�ж��ļ��и��������ڵ������Ƿ��ظ�
		Set<String> nameSet = new HashSet<String>();
		
		FileReader fr = new FileReader(filePath, charset);
		List<String> parrents = new ArrayList<String>();
		ListSolver.set(parrents, 0, "");
		String line = fr.readLine();
		while (line != null) {
			int depth = 0;
			while (depth < line.length() && line.charAt(depth) == '\t') {
				depth++;
			}
			String name = line.trim();
			if (nameSet.contains(name)) {
				System.err.println("Duplicate name! Name is: " + name);
				System.exit(1);
			}
			nameSet.add(name);
			if (name.length() == 0)
				continue;
			add(name, parrents.get(depth));
			ListSolver.set(parrents, depth + 1, name);
			line = fr.readLine();
		}
		fr.close();
	}
	
	/**
	 * �����rootΪ�ڵ����
	 * @param prefix ���ʱ������ı���ǰ׺������Ϊ��λ����ÿ�ж��д�ǰ׺��
	 * @param root
	 */
	private void _display(String prefix, TreeNode root) {
		if (root.isDeleted()) {
			return;
		}
		String nextPrefix = null;
		if (root.needDisplay()) {
			nextPrefix = prefix + "\t";
			System.out.println(prefix + root.getName());
		}
		else {
			nextPrefix = prefix;
		}
		List<TreeNode> nodes = pool.getAllNodes();
		for (TreeNode node: nodes) {
			if (node.getParrent().equals(root)) {
				_display(nextPrefix, node);
			}
		}
	}
	
	/**
	 * �ж�һ���ڵ��Ƿ�ɾ��
	 * @param node
	 * @return
	 */
	public boolean isDeleted(TreeNode node) {
		while (node != null && !node.equals(ROOT)) {
			if (node.isDeleted())
				return true;
			node = node.getParrent();
		}
		return false;
	}
	
	/**
	 * ���������
	 */
	public void display() {
		List<TreeNode> nodes = pool.getAllNodes();
		for (TreeNode node: nodes) {
			if (node.getParrent().equals(ROOT) && !node.equals(ROOT)) {
				_display("", node);
			}
		}
	}
	
	/**
	 * ��ȡĳ���ڵ�����и��ף��������Լ�������ڵ��Ѿ���ɾ������ֱ�Ӹ��ڵ�Ϊ������..��
	 * @param name
	 * @return ���飺[���ڵ㣬һ�����ڵ㣬...��ֱ�Ӹ��ڵ�]
	 */
	public List<TreeNode> getAllParents(String name) {
		List<TreeNode> parrents = new ArrayList<TreeNode>();
		TreeNode node = null;
		try {
			node = pool.getNode(name);
		} catch (TreeNodeNotFoundException e) {
			e.printStackTrace();
		}
		node = node.getParrent();
		while (node != null && !node.equals(ROOT)) {
			if (node.needDisplay())
				parrents.add(node);
			node = node.getParrent();
		}
		Collections.reverse(parrents);
		int len = 0;
		while (len < parrents.size() && !parrents.get(len).isDeleted())
			len++;
		return parrents.subList(0, len);
	}

	
	/**
	 * ��ȡĳ���ڵ�����и��ף������Լ�������ڵ��Ѿ���ɾ������ֱ�Ӹ��ڵ�Ϊ��������
	 * @param name
	 * @return ���飺[���ڵ㣬һ�����ڵ㣬...��ֱ�Ӹ��ڵ㣬�Լ�]
	 */
	public List<TreeNode> getAllParentsAndItself(String name) {
		TreeNode node = null;
		try {
			node = pool.getNode(name);
		} catch (TreeNodeNotFoundException e) {
			e.printStackTrace();
		}
		List<TreeNode> ret = getAllParents(name);
		if (!isDeleted(node))
			ret.add(node);
		return ret;
	}
	
	/**
	 * A newName rootName
	 * @param name
	 */
	public void add(String name, String rootName) {
		TreeNode parrent = null;
		TreeNode child = null;
		try {
			parrent = pool.getNodeOrAdd(rootName);
			child = pool.getNodeOrAdd(name);
		} catch (TreeNodeNotFoundException e) {
			e.printStackTrace();
		} catch (TreeNodeDuplicatedException e) {
			e.printStackTrace();
		}
		child.setParrent(parrent);
	}
	
	/**
	 * R newName oldName
	 * @param newName
	 * @param oldName
	 */
	public void rename(String newName, String oldName) {
		try {
			pool.renameNode(newName, oldName);
		} catch (TreeNodeNotFoundException e) {
			e.printStackTrace();
		} catch (TreeNodeDuplicatedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * D name
	 * @param name
	 */
	public void delete(String name) {
		TreeNode node = null;
		try {
			node = pool.getNode(name);
		} catch (TreeNodeNotFoundException e) {
			e.printStackTrace();
		}
		String parrentName = node.getParrentName();
		TreeNode newRootNode = null;
		try {
			newRootNode = pool.getNodeOrAdd("����" + parrentName);
		} catch (TreeNodeNotFoundException e) {
			e.printStackTrace();
		} catch (TreeNodeDuplicatedException e) {
			e.printStackTrace();
		}
		newRootNode.setParrent(node.getParrent());
//		newRootNode.setNotDisplay();
		node.setParrent(newRootNode);
		node.setDelete();
	}
	
	/**
	 * M new_name name_0 name_1 ...
	 * @param names
	 * @param notDisplayMergingNode���������Ϊtrue����name_0 name_1 ...��displayʱ����ʾ�� ������displayʱ��ʾ
	 */
	public void merge(List<String> names, boolean notDisplayMergingNode) {
		if (names.size() < 2){
			System.err.println("[Tree::merge(names)]: The length of Merge list is less than 2! List is: " + names);
			System.exit(1);
		}
		String newName = names.get(0);
		String parrentNames = null;
		TreeNode node = null, parrentNode = null;
		TreeNode mergedNode = null;
		try {
			mergedNode = pool.getNodeOrAdd(newName);
		} catch (TreeNodeNotFoundException e1) {
			e1.printStackTrace();
		} catch (TreeNodeDuplicatedException e1) {
			e1.printStackTrace();
		}
		for (int i = 1; i < names.size(); i++) {
			String name = names.get(i);
			try {
				node = pool.getNode(name);
				if (notDisplayMergingNode) {
					node.setNotDisplay();
				}
			} catch (TreeNodeNotFoundException e) {
				e.printStackTrace();
			}
			if (parrentNames == null) {
				parrentNames = node.getParrentName();
				parrentNode = node.getParrent();
			}
			else if (!node.getParrent().equals(parrentNode)){
				System.err.println("[Tree::merge(names)]: The parrent of nodes in merge list is not equal! List is: " + names);
				System.exit(1);
			}
			node.setParrent(mergedNode);
		}
		mergedNode.setParrent(parrentNode);
	}
}
