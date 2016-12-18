package tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreeNodePool {
	/**
	 * 索引：节点索引值->节点
	 */
	Map<String, TreeNode> treeNodeMap = new HashMap<String, TreeNode>();
	
	/**
	 * 获取节点，如果没有索引值节点则报错
	 * @param hashName 节点的索引值
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
	 * 添加节点，如果索引值已经存在则报错</br>
	 * 添加时，直接使用node而不是新创建节点，这样可以保证node的所有关系都得以保留，如node的子节点信息
	 * @param node 待添加的节点
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
	 * 删除节点，如果节点不存在则报错
	 * @param hashName 节点的索引值
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
	 * 返回树中的所有节点
	 * @return
	 */
	public List<TreeNode> getAllNodes() {
		Collection<TreeNode> values = treeNodeMap.values();
		return new ArrayList<TreeNode>(values);
	}
	
	/**
	 * 重命名节点
	 * @param newName 新内容，会作为节点的索引值
	 * @param oldName 节点的原内容，也是节点的原索引值
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
	 * 返回节点，如果节点不存在，跑出异常
	 * @param hashName 节点索引值
	 * @return
	 * @throws TreeNodeNotFoundException
	 */
	public TreeNode getNode(String hashName) throws TreeNodeNotFoundException {
		return _getNode(hashName);
	}
	
	/**
	 * 查找节点，如果不存在，则创建相应节点。当创建节点时，节点的内容和索引值均设置为hashName
	 * @param hashName 查找节点时，节点的索引值
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
//	 * 查找节点，如果不存在，则创建相应节点。当创建节点时，节点的内容为nodeName，索引值为hashName
//	 * @param hashName 查找节点时，节点的索引值
//	 * @param nodeName 创建节点时，节点的内容
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

