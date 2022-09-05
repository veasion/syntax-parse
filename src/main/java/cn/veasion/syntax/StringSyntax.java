package cn.veasion.syntax;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * StringSyntax 字符串 xxx
 *
 * @author luozhuowei
 * @date 2022/9/3
 */
public class StringSyntax extends Syntax {

    public String str;

    public StringSyntax(String str) {
        this.str = str;
    }

    @Override
    protected int match(String dialog, int start, Map<String, Object> entityMap, Map<String, String> varMap, AtomicInteger counter, boolean tryNext) {
        if (startsWith(dialog, start, str)) {
            return start + str.length();
        }
        return -1;
    }

    @Override
    public String toString() {
        return "StringSyntax => {" + str + '}';
    }
}
