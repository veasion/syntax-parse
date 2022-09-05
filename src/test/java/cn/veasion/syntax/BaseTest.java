package cn.veasion.syntax;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * BaseTest
 *
 * @author luozhuowei
 * @date 2022/9/3
 */
public abstract class BaseTest {

    protected void matchDialog(String dialog, String... templates) {
        DialogContext dialogContext = new DialogContext();
        dialogContext.setDialog(dialog);
        dialogContext.setExact(true);
        dialogContext.setTemplates(Arrays.stream(templates).map(SyntaxParser::parseTemplate).collect(Collectors.toList()));
        // 输出语法树
        printSyntaxTree(dialogContext.getTemplates(), 0);
        // 匹配对话
        List<MatchResult> matchResults = SyntaxParser.matchDialog(dialogContext);
        System.out.println();
        printResult(matchResults);
    }

    protected void printResult(List<MatchResult> matchResults) {
        if (matchResults.isEmpty()) {
            System.out.println("未匹配");
            return;
        }
        // 根据模板分组
        Map<Syntax, List<MatchResult>> map = matchResults.stream().collect(Collectors.groupingBy(MatchResult::getTemplate));
        for (Map.Entry<Syntax, List<MatchResult>> entry : map.entrySet()) {
            System.out.println("---------------------------");
            Syntax template = entry.getKey();
            System.out.println("模板：" + template.getTemplate());
            for (MatchResult matchResult : entry.getValue()) {
                System.out.println("匹配：" + matchResult.getWords());
                if (matchResult.getVarMap() != null && !matchResult.getVarMap().isEmpty()) {
                    System.out.println("变量：" + matchResult.getVarMap());
                }
                // System.out.println("计数：" + matchResult.getCounter().get());
            }
        }
    }

    protected void printSyntaxTree(String template) {
        printSyntaxTree(Collections.singletonList(SyntaxParser.parseTemplate(template)), 0);
    }

    protected void printSyntaxTree(List<Syntax> list, int tab) {
        String start = "";
        for (int i = 0; i < tab; i++) {
            start += "----";
        }
        for (Syntax syntax : list) {
            System.out.println(start + syntax.toString());
            List<Syntax> children = syntax.children();
            if (children != null) {
                printSyntaxTree(children, tab + 1);
            }
        }
    }

}
