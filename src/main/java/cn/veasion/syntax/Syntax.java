package cn.veasion.syntax;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Syntax
 *
 * @author luozhuowei
 * @date 2022/9/3
 */
public abstract class Syntax {

    /**
     * <pre>
     * 遇到范围字符匹配时是否按最大字符匹配
     * true 是（尽可能匹配最少结果）
     * false 否（会尽可能匹配多个结果）
     *
     * 示例：
     *     模板为：[N:0-3][N:0-3]
     *     匹配语句为：123456
     *     为true时结果：[123456]
     *     为false时结果：[123, 456]
     * </pre>
     */
    protected static boolean MAX_MATCH = true;

    private String template;
    protected Syntax next;

    /**
     * 语句匹配
     *
     * @param dialog    对话
     * @param start     开始
     * @param entityMap 实体变量
     * @param varMap    变量存放
     * @param counter   匹配语法计数器
     * @return 未匹配：-1 匹配：结尾下标
     */
    public int match(String dialog, int start, Map<String, Object> entityMap, Map<String, String> varMap, AtomicInteger counter) {
        return match(dialog, start, entityMap, varMap, counter, false);
    }

    /**
     * 语句匹配（内部调用）
     *
     * @param dialog    对话
     * @param start     开始
     * @param entityMap 实体变量
     * @param varMap    变量存放
     * @param counter   匹配语法计数器
     * @param tryNext   正在尝试匹配下一个（范围匹配触发）
     * @return 未匹配：-1 匹配：结尾下标
     */
    protected abstract int match(String dialog, int start, Map<String, Object> entityMap, Map<String, String> varMap, AtomicInteger counter, boolean tryNext);

    public List<Syntax> children() {
        return null;
    }

    protected void setNext(Syntax next) {
        this.next = next;
        List<Syntax> children = children();
        if (children != null && children.size() > 0) {
            Syntax last = children.get(children.size() - 1);
            if (last.next == null) {
                last.setNext(next);
            }
        }
    }

    protected boolean canIgnore() {
        // 当没有匹配时是否可以忽略，如 [N:0-3] 因为包含0所以没有匹配时可以忽略
        return false;
    }

    protected int priority() {
        // 越大越优先，在最大范围匹配时生效
        // 示例：
        // 模板：[W:1-2][#年龄:[N:1-2]][岁?]
        // 对话：我18岁
        // 这个时候匹配结果变量的 年龄应该是 18 而不是 8，所以数字匹配要优先于通配符
        return 0;
    }

    protected static boolean startsWith(String dialog, int start, String eq) {
        if (eq.length() > dialog.length() - start) {
            return false;
        }
        for (int i = 0; i < eq.length(); i++) {
            if (eq.charAt(i) != dialog.charAt(start + i)) {
                return false;
            }
        }
        return true;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }
}