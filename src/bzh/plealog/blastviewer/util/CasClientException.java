package bzh.plealog.blastviewer.util;

public class CasClientException extends RuntimeException {
  private TYPE _type;
  
  private static final long serialVersionUID = 1931425558567213759L;

  public static enum TYPE {BAD_CREDENTIALS, UNKNOWN};
  
  public CasClientException(String message) {
    this(message, TYPE.UNKNOWN);
  }

  public CasClientException(String message, TYPE type) {
    super(message);
    setType(type);
  }

  public TYPE getExceptionType() {
    return _type;
  }
  public void setType(TYPE type) {
    _type=type;
  }
}
