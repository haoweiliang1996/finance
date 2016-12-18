package tree;

public class TreeNode {
	//空节点，其parrent会指向自己
	public static final TreeNode EMPTY = new TreeNode("");
	static {
		EMPTY.setParrent(EMPTY);
	}
	
	private TreeNode parrent = EMPTY;
	private String name;
	private boolean isDeleted = false;
    private boolean isDisplay = true;
	
	/**
	 * 创建节点
	 * @param name 节点内容
	 * @param key 索引值
	 * @return
	 */
	public static TreeNode CreatNode(String name) {
		if (name == null || name.length() == 0)
			return EMPTY;
		else
			return new TreeNode(name);
	}
	
	private TreeNode(String name) {
		setName(name);
	}
	
	/**
	 * 设置节点不在display时输出
	 */
	public void setNotDisplay() {
		this.isDisplay = false;
	}
	
	/**
	 * 判断节点是否需要在display中输出
	 * @return
	 */
	public boolean needDisplay() {
		return this.isDisplay;
	}
	
	/**
	 * 用索引值判断两个node是否相同
	 */
	@Override
	public boolean equals(Object other) {
        if (other == null) return false;
        if (getClass() != other.getClass()) return false;
        TreeNode rhs = (TreeNode)other;
        return name.equals(rhs.name);
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public String getParrentName() {
		return getParrent().getName();
	}
	
	public TreeNode getParrent() {
		return parrent;
	}
	
	/**
	 * 设置一个节点的状态为删除
	 */
	public void setDelete() {
		this.isDeleted = true;
	}
	
	public boolean isDeleted() {
		return this.isDeleted;
	}
	
	public void setParrent(TreeNode parrent) {
		this.parrent = parrent;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
}