package cn.veasion.syntax;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * LikeSyntax 模糊匹配 [~xxx]
 *
 * @author luozhuowei
 * @date 2022/9/3
 */
public class LikeSyntax extends Syntax {

    private static final int MAX_LENGTH = 15;

    public String str;

    public LikeSyntax(String str) {
        this.str = str;
    }

    @Override
    protected int match(String dialog, int start, Map<String, Object> entityMap, Map<String, String> varMap, AtomicInteger counter, boolean tryNext) {
        int endIdx = Math.min(dialog.length(), start + MAX_LENGTH);
        char[] chars = dialog.toCharArray();
        endIdx = SyntaxParser.getEndIdx(chars, start, endIdx);
        int subIdx = dialog.substring(start, endIdx).indexOf(str);
        if (subIdx == -1) {
            return -1;
        }
        if (next == null && MAX_MATCH) {
            return endIdx;
        }
        int idx = subIdx + start + 1;
        if (next != null) {
            int maxNextIdx = -1;
            // 尽量匹配下一个 next
            for (int i = idx; i < endIdx; i++) {
                int match = next.match(dialog, i, entityMap, varMap, counter, true);
                if (match != -1 || next.canIgnore()) {
                    maxNextIdx = i;
                    if (tryNext || !MAX_MATCH) {
                        break;
                    }
                } else if (tryNext) {
                    return -1;
                }
            }
            return maxNextIdx != -1 ? maxNextIdx : idx;
        }
        return idx;
    }

    @Override
    public String toString() {
        return "LikeSyntax => {" + str + '}';
    }
}
