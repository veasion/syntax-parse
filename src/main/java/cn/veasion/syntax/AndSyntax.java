package cn.veasion.syntax;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AndSyntax 并联语法 你好[xxx]
 *
 * @author luozhuowei
 * @date 2022/9/3
 */
public class AndSyntax extends Syntax {

    public List<Syntax> syntaxList;

    public AndSyntax(List<Syntax> syntaxList) {
        for (int i = 0; i < syntaxList.size() - 1; i++) {
            syntaxList.get(i).setNext(syntaxList.get(i + 1));
        }
        this.syntaxList = syntaxList;
    }

    @Override
    protected int match(String dialog, int start, Map<String, Object> entityMap, Map<String, String> varMap, AtomicInteger counter, boolean tryNext) {
        int idx = -1;
        for (Syntax syntax : syntaxList) {
            if (start == dialog.length() && syntax.canIgnore()) {
                continue;
            }
            int match = syntax.match(dialog, start, entityMap, varMap, counter, tryNext);
            if (match == -1) {
                if (syntax.canIgnore()) {
                    continue;
                } else {
                    return -1;
                }
            }
            idx = start = match;
            if (!tryNext && counter != null) {
                counter.incrementAndGet();
            }
        }
        return idx;
    }

    @Override
    protected boolean canIgnore() {
        for (Syntax syntax : syntaxList) {
            if (!syntax.canIgnore()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<Syntax> children() {
        return syntaxList;
    }

    @Override
    public String toString() {
        return "AndSyntax";
    }
}
