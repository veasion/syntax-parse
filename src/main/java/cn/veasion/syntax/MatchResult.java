package cn.veasion.syntax;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MatchResult
 *
 * @author luozhuowei
 * @date 2022/9/3
 */
public class MatchResult {

    private String words;
    private Syntax template;
    private Integer lastIdx;
    private AtomicInteger counter;
    private Map<String, String> varMap;

    public String getWords() {
        return words;
    }

    public void setWords(String words) {
        if (words != null && words.length() > 0) {
            words = words.trim();
            int start = 0;
            for (int i = 0; i < words.length(); i++) {
                if (!SyntaxParser.isSplitChar(words.charAt(i))) {
                    break;
                }
                start = i + 1;
            }
            if (start > 0) {
                words = words.substring(start).trim();
            }
        }
        this.words = words;
    }

    public Syntax getTemplate() {
        return template;
    }

    public void setTemplate(Syntax template) {
        this.template = template;
    }

    public Integer getLastIdx() {
        return lastIdx;
    }

    public void setLastIdx(Integer lastIdx) {
        this.lastIdx = lastIdx;
    }

    public AtomicInteger getCounter() {
        return counter;
    }

    public void setCounter(AtomicInteger counter) {
        this.counter = counter;
    }

    public Map<String, String> getVarMap() {
        return varMap;
    }

    public void setVarMap(Map<String, String> varMap) {
        this.varMap = varMap;
    }
}
