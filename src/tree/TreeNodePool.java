package tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreeNodePool {
	/**
	 * �������ڵ�����ֵ->�ڵ�
	 */
	Map<String, TreeNode> treeNodeMap = new HashMap<String, TreeNode>();
	
	/**
	 * ��ȡ�ڵ㣬���û������ֵ�ڵ��򱨴�
	 * @param hashName �ڵ������ֵ
	 * @return
	 * @throws TreeNodeNotFoundException 
	 */
	private TreeNode _getNode(String hashName) throws TreeNodeNotFoundException {
		if (!treeNodeMap.containsKey(hashName)) {
			String message = "[TreeNodePool::_getNode(hashName)]: Can't find node! Name is: " + hashName;
			throw new TreeNodeNotFoundException(message);
		}
		return treeNodeMap.get(hashName);
	}
	
	/**
	 * ��ӽڵ㣬�������ֵ�Ѿ������򱨴�</br>
	 * ���ʱ��ֱ��ʹ��node�������´����ڵ㣬�������Ա�֤node�����й�ϵ�����Ա�������node���ӽڵ���Ϣ
	 * @param node ����ӵĽڵ�
	 * @throws TreeNodeDuplicatedException 
	 */
	private void _addNode(TreeNode node) throws TreeNodeDuplicatedException {
		String hashName = node.getName();
		if (treeNodeMap.containsKey(hashName)) {
			String message = "[TreeNodePool::_addNode(hashName)]: Duplicate node! Name is: " + hashName;
			throw new TreeNodeDuplicatedException(message);
		}
		treeNodeMap.put(hashName, node);
	}
	
	/**
	 * ɾ���ڵ㣬����ڵ㲻�����򱨴�
	 * @param hashName �ڵ������ֵ
	 * @return
	 * @throws TreeNodeNotFoundException 
	 */
	private TreeNode _deleteNode(String hashName) throws TreeNodeNotFoundException {
		if (!treeNodeMap.containsKey(hashName)) {
			String message = "[TreeNodePool::_deleteNode(hashName)]: Can't find node! Name is: " + hashName;
			throw new TreeNodeNotFoundException(message);
		}
		TreeNode node = _getNode(hashName);
		treeNodeMap.remove(hashName);
		return node;
	}
	
	/**
	 * �������е����нڵ�
	 * @return
	 */
	public List<TreeNode> getAllNodes() {
		Collection<TreeNode> values = treeNodeMap.values();
		return new ArrayList<TreeNode>(values);
	}
	
	/**
	 * �������ڵ�
	 * @param newName �����ݣ�����Ϊ�ڵ������ֵ
	 * @param oldName �ڵ��ԭ���ݣ�Ҳ�ǽڵ��ԭ����ֵ
	 * @throws TreeNodeNotFoundException 
	 * @throws TreeNodeDuplicatedException 
	 */
	public void renameNode(String newName, String oldName) throws TreeNodeNotFoundException, TreeNodeDuplicatedException {
		TreeNode node = _deleteNode(oldName);
		node.setName(newName);
//		node.setKey(newName);
		_addNode(node);
	}
	
	/**
	 * ���ؽڵ㣬����ڵ㲻���ڣ��ܳ��쳣
	 * @param hashName �ڵ�����ֵ
	 * @return
	 * @throws TreeNodeNotFoundException
	 */
	public TreeNode getNode(String hashName) throws TreeNodeNotFoundException {
		return _getNode(hashName);
	}
	
	/**
	 * ���ҽڵ㣬��������ڣ��򴴽���Ӧ�ڵ㡣�������ڵ�ʱ���ڵ�����ݺ�����ֵ������ΪhashName
	 * @param hashName ���ҽڵ�ʱ���ڵ������ֵ
	 * @return
	 * @throws TreeNodeNotFoundException 
	 * @throws TreeNodeDuplicatedException 
	 */
	public TreeNode getNodeOrAdd(String hashName) throws TreeNodeNotFoundException, TreeNodeDuplicatedException {
		if (treeNodeMap.containsKey(hashName))
			return _getNode(hashName);
		else {
			TreeNode node = TreeNode.CreatNode(hashName);
			_addNode(node);
			return node;
		}
	}
	
//	/**
//	 * ���ҽڵ㣬��������ڣ��򴴽���Ӧ�ڵ㡣�������ڵ�ʱ���ڵ������ΪnodeName������ֵΪhashName
//	 * @param hashName ���ҽڵ�ʱ���ڵ������ֵ
//	 * @param nodeName �����ڵ�ʱ���ڵ������
//	 * @return
//	 * @throws TreeNodeNotFoundException 
//	 * @throws TreeNodeDuplicatedException 
//	 */
//	public TreeNode getNodeOrAdd(String hashName, String nodeName) throws TreeNodeNotFoundException, TreeNodeDuplicatedException {
//		if (treeNodeMap.containsKey(hashName))
//			return _getNode(hashName);
//		else {
//			TreeNode node = TreeNode.CreatNode(nodeName, hashName);
//			_addNode(node);
//			return node;
//		}
//	}
}

