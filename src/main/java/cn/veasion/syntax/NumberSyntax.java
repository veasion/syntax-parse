package cn.veasion.syntax;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * NumberSyntax 任意数字 [N:a-b]
 *
 * @author luozhuowei
 * @date 2022/9/3
 */
public class NumberSyntax extends Syntax {

    public int min;
    public int max;

    @Override
    protected int match(String dialog, int start, Map<String, Object> entityMap, Map<String, String> varMap, AtomicInteger counter, boolean tryNext) {
        int endIdx = Math.min(start + max, dialog.length());
        int idx = -1;
        int maxNextIdx = -1;
        for (int i = start; i < endIdx; i++) {
            if (Character.isDigit(dialog.charAt(i))) {
                if (i >= start + min - 1) {
                    idx = i + 1;
                    // 尽量匹配下一个 next
                    if (next != null) {
                        int match = next.match(dialog, idx, entityMap, varMap, counter, true);
                        if (match != -1 || next.canIgnore()) {
                            maxNextIdx = idx;
                            if (tryNext || !MAX_MATCH) {
                                break;
                            }
                        } else if (tryNext) {
                            return -1;
                        }
                    }
                }
            } else {
                break;
            }
            if (i >= start + max - 1) {
                break;
            }
        }
        return maxNextIdx != -1 ? maxNextIdx : idx;
    }

    @Override
    protected boolean canIgnore() {
        return min == 0;
    }

    @Override
    protected int priority() {
        return super.priority() + 1;
    }

    @Override
    public String toString() {
        return "NumberSyntax => {min=" + min + ", max=" + max + '}';
    }
}
