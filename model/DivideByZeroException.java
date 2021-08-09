package proj02.model;

public class DivideByZeroException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6890386190522602344L;

	public DivideByZeroException() {
		// TODO Auto-generated constructor stub
	}

	public DivideByZeroException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public DivideByZeroException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public DivideByZeroException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public DivideByZeroException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
