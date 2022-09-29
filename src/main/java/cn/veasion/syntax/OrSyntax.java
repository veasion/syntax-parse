package cn.veasion.syntax;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * OrSyntax 可选 [xxx|xxx|[@xxx]|[W:a-b]|[N:a-b]|[~xxx]]
 *
 * @author luozhuowei
 * @date 2022/9/3
 */
public class OrSyntax extends Syntax {

    private static final int OR_STRING_MAX = 8;

    public List<Syntax> syntaxList;

    public OrSyntax(List<Syntax> syntaxList) {
        // StringSyntax 连续超过一定数量会压缩成 OrStringSyntax 加快匹配速度
        if (syntaxList.size() >= OR_STRING_MAX) {
            List<String> stringList = new ArrayList<>();
            for (int i = 0; i < syntaxList.size(); i++) {
                Syntax syntax = syntaxList.get(i);
                if (syntax instanceof StringSyntax) {
                    stringList.add(((StringSyntax) syntax).str);
                } else {
                    if (stringList.size() >= OR_STRING_MAX) {
                        int removeIdx = i - stringList.size();
                        for (int j = 0; j < stringList.size(); j++) {
                            syntaxList.remove(removeIdx);
                        }
                        syntaxList.add(removeIdx, new OrStringSyntax(stringList));
                        i = removeIdx;
                    }
                    stringList.clear();
                }
            }
            if (stringList.size() >= OR_STRING_MAX) {
                int removeIdx = syntaxList.size() - stringList.size();
                for (int j = 0; j < stringList.size(); j++) {
                    syntaxList.remove(removeIdx);
                }
                syntaxList.add(removeIdx, new OrStringSyntax(stringList));
            }
        }
        this.syntaxList = syntaxList;
    }

    @Override
    protected int match(String dialog, int start, Map<String, Object> entityMap, Map<String, String> varMap, AtomicInteger counter, boolean tryNext) {
        int idx = -1;
        for (Syntax syntax : syntaxList) {
            AtomicInteger _counter = tryNext ? null : new AtomicInteger();
            idx = syntax.match(dialog, start, entityMap, varMap, _counter, tryNext);
            if (idx != -1) {
                if (!tryNext && counter != null) {
                    counter.addAndGet(_counter.get());
                }
                break;
            }
        }
        return idx;
    }

    @Override
    public List<Syntax> children() {
        return syntaxList;
    }

    @Override
    protected boolean canIgnore() {
        for (Syntax syntax : syntaxList) {
            if (syntax.canIgnore()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "OrSyntax";
    }

    static class OrStringSyntax extends Syntax {

        private Map<Character, List<String>> treeMap;

        public OrStringSyntax(List<String> list) {
            treeMap = new HashMap<>();
            for (String s : list) {
                treeMap.compute(s.charAt(0), (k, v) -> {
                    if (v == null) {
                        v = new ArrayList<>();
                    }
                    v.add(s);
                    return v;
                });
            }
        }

        @Override
        protected int match(String dialog, int start, Map<String, Object> entityMap, Map<String, String> varMap, AtomicInteger counter, boolean tryNext) {
            if (start >= dialog.length()) {
                return -1;
            }
            List<String> list = treeMap.get(dialog.charAt(start));
            if (list != null) {
                for (String s : list) {
                    if (startsWith(dialog, start, s)) {
                        return start + s.length();
                    }
                }
            }
            return -1;
        }

        @Override
        public String toString() {
            return "OrStringSyntax => {" + treeMap.values().stream().flatMap(Collection::stream).collect(Collectors.joining("|")) + '}';
        }
    }

}
