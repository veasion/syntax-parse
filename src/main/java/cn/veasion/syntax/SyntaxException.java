package cn.veasion.syntax;

/**
 * SyntaxException
 *
 * @author luozhuowei
 * @date 2022/9/3
 */
public class SyntaxException extends RuntimeException {

    public SyntaxException(String message) {
        super(message);
    }

    public SyntaxException(String message, Throwable cause) {
        super(message, cause);
    }
}
