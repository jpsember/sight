package sight.gen;

public enum DrillStatus {

  NONE, ACTIVE, DONE, RETRY, DONE_SESSION;

  public static final DrillStatus DEFAULT_INSTANCE = NONE;

}
