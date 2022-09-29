package cn.veasion.syntax;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * NotEssentialSyntax 非必选 [xxx?] [[你好|您好]?]
 *
 * @author luozhuowei
 * @date 2022/9/3
 */
public class NotEssentialSyntax extends Syntax {

    public Syntax syntax;

    public NotEssentialSyntax(Syntax syntax) {
        this.syntax = syntax;
    }

    @Override
    protected int match(String dialog, int start, Map<String, Object> entityMap, Map<String, String> varMap, AtomicInteger counter, boolean tryNext) {
        AtomicInteger _counter = tryNext ? null : new AtomicInteger();
        int idx = syntax.match(dialog, start, entityMap, varMap, _counter, tryNext);
        if (idx != -1) {
            // 尽量匹配下一个 next
            if (next != null) {
                int match = next.match(dialog, idx, entityMap, varMap, null, true);
                if (match == -1 && !next.canIgnore()) {
                    match = next.match(dialog, start, entityMap, varMap, null, true);
                    if (match != -1 || next.canIgnore()) {
                        return -1;
                    }
                }
            }
            if (!tryNext && counter != null) {
                counter.addAndGet(_counter.get());
            }
        }
        return idx;
    }

    @Override
    protected boolean canIgnore() {
        return true;
    }

    @Override
    public List<Syntax> children() {
        return Collections.singletonList(syntax);
    }

    @Override
    public String toString() {
        return "NotEssentialSyntax";
    }

}
