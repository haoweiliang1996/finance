package tree;

public class TreeNodeException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2574716856589553973L;

	String message;

	public TreeNodeException(String message) {
		this.message = message;
	}
	
	@Override
	public String getMessage() {
		return message;
	}
}

class TreeNodeNotFoundException extends TreeNodeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8975290091434029842L;

	public TreeNodeNotFoundException(String message) {
		super(message);
	}
	
}

class TreeNodeDuplicatedException extends TreeNodeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6221922070103394900L;

	public TreeNodeDuplicatedException(String message) {
		super(message);
	}
	
}
