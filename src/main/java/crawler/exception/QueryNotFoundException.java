package crawler.exception;

public class QueryNotFoundException extends RuntimeException {
    public QueryNotFoundException(String msg) {
        super(msg);
    }
}
