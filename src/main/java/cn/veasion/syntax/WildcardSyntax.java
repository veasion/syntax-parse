package cn.veasion.syntax;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * WildcardSyntax 通配符 [W:a-b] 通配符否定语法 [W:a-b^{xxx[xxx|xxx]}] 通配符包含语法 [W:a-b~{xxx}] 通配符限定语法 [W:a-b>{[1|2|3|4]}]
 *
 * @author luozhuowei
 * @date 2022/9/3
 */
public class WildcardSyntax extends Syntax {

    public int min;
    public int max;
    public Syntax likeMatch;
    public Syntax notMatch;
    public Syntax inMatch;

    @Override
    protected int match(String dialog, int start, Map<String, Object> entityMap, Map<String, String> varMap, AtomicInteger counter, boolean tryNext) {
        int idx = -1;
        int maxNextIdx = -1;
        boolean like = false;
        int endIdx = Math.min(start + max, dialog.length());
        for (int i = start; i < endIdx; i++) {
            if (likeMatch != null) {
                if (like || likeMatch.match(dialog.substring(i, endIdx), 0, entityMap, varMap, counter, tryNext) != -1) {
                    like = true;
                } else {
                    continue;
                }
            }
            if (notMatch != null && notMatch.match(dialog.substring(i, endIdx), 0, entityMap, varMap, counter, tryNext) != -1) {
                break;
            }
            if (inMatch != null) {
                int match = inMatch.match(dialog, i, entityMap, varMap, counter, tryNext);
                if (match == -1) {
                    break;
                }
                i = match - 1;
            }
            if (i >= start + min - 1) {
                idx = i + 1;
                // 尽量匹配下一个 next
                if (next != null) {
                    int match = next.match(dialog, idx, entityMap, varMap, counter, true);
                    if (match != -1 || next.canIgnore()) {
                        maxNextIdx = idx;
                        if (tryNext || !MAX_MATCH || next.priority() > priority()) {
                            break;
                        }
                    } else if (tryNext) {
                        return -1;
                    }
                }
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
    public List<Syntax> children() {
        if (likeMatch != null) {
            return Collections.singletonList(likeMatch);
        }
        if (notMatch != null) {
            return Collections.singletonList(notMatch);
        }
        if (inMatch != null) {
            return Collections.singletonList(inMatch);
        }
        return null;
    }

    @Override
    public String toString() {
        if (likeMatch != null) {
            return "WildcardSyntax-LikeMatch => {min=" + min + ", max=" + max + '}';
        } else if (notMatch != null) {
            return "WildcardSyntax-NotMatch => {min=" + min + ", max=" + max + '}';
        } else if (inMatch != null) {
            return "WildcardSyntax-InMatch => {min=" + min + ", max=" + max + '}';
        } else {
            return "WildcardSyntax => {min=" + min + ", max=" + max + '}';
        }
    }
}
