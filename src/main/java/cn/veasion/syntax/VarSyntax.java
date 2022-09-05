package cn.veasion.syntax;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * VarSyntax 变量 [#变量名:你好[xxx]] [#变量名:[W:a-b]]
 *
 * @author luozhuowei
 * @date 2022/9/3
 */
public class VarSyntax extends Syntax {

    public String var;
    public Syntax syntax;

    public VarSyntax(String var, Syntax syntax) {
        this.var = var;
        this.syntax = syntax;
    }

    @Override
    protected int match(String dialog, int start, Map<String, Object> entityMap, Map<String, String> varMap, AtomicInteger counter, boolean tryNext) {
        int idx = syntax.match(dialog, start, entityMap, varMap, counter, tryNext);
        if (idx != -1 && !tryNext) {
            varMap.put(var, dialog.substring(start, idx));
        }
        return idx;
    }

    @Override
    protected boolean canIgnore() {
        return syntax.canIgnore();
    }

    @Override
    protected int priority() {
        return syntax.priority();
    }

    @Override
    public List<Syntax> children() {
        return syntax == null ? null : Collections.singletonList(syntax);
    }

    @Override
    public String toString() {
        return "VarSyntax => {" + var + '}';
    }
}
