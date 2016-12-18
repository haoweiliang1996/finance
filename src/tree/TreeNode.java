package tree;

public class TreeNode {
	//�սڵ㣬��parrent��ָ���Լ�
	public static final TreeNode EMPTY = new TreeNode("");
	static {
		EMPTY.setParrent(EMPTY);
	}
	
	private TreeNode parrent = EMPTY;
	private String name;
	private boolean isDeleted = false;
    private boolean isDisplay = true;
	
	/**
	 * �����ڵ�
	 * @param name �ڵ�����
	 * @param key ����ֵ
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
	 * ���ýڵ㲻��displayʱ���
	 */
	public void setNotDisplay() {
		this.isDisplay = false;
	}
	
	/**
	 * �жϽڵ��Ƿ���Ҫ��display�����
	 * @return
	 */
	public boolean needDisplay() {
		return this.isDisplay;
	}
	
	/**
	 * ������ֵ�ж�����node�Ƿ���ͬ
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
	 * ����һ���ڵ��״̬Ϊɾ��
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