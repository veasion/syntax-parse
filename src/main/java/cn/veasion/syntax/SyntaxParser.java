package cn.veasion.syntax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * SyntaxParser
 *
 * @author luozhuowei
 * @date 2022/9/3
 */
public class SyntaxParser {

    /**
     * 匹配对话
     *
     * @param dialogContext 上下文
     * @return 匹配结果
     */
    public static List<MatchResult> matchDialog(DialogContext dialogContext) {
        boolean exact = dialogContext.isExact();
        List<MatchResult> result = new ArrayList<>();
        String dialog = dialogContext.getDialog();
        List<Syntax> templates = dialogContext.getTemplates();
        char[] chars = dialog.toCharArray();
        Map<Syntax, MatchResult> progress = new HashMap<>();
        Map<String, String> tempVarMap = new HashMap<>();
        for (int i = 0; i < chars.length; i++) {
            for (Syntax template : templates) {
                tempVarMap.clear();
                MatchResult cacheResult = progress.get(template);
                if (cacheResult != null && (cacheResult.getLastIdx() == chars.length || i < cacheResult.getLastIdx() && !exact)) {
                    continue;
                } else if (cacheResult != null && i >= cacheResult.getLastIdx()) {
                    cacheResult = null;
                    progress.remove(template);
                }
                AtomicInteger counter = new AtomicInteger();
                int idx = template.match(dialog, i, dialogContext.getEntityMap(), tempVarMap, counter);
                if (idx <= i) {
                    continue;
                }
                if (cacheResult != null && exact) {
                    if (counter.get() > cacheResult.getCounter().get()) {
                        cacheResult.setWords(dialog.substring(i, idx));
                        cacheResult.setCounter(counter);
                        if (tempVarMap.size() > 0) {
                            cacheResult.setVarMap(new HashMap<>(tempVarMap));
                        }
                    }
                } else {
                    MatchResult matchResult = new MatchResult();
                    matchResult.setTemplate(template);
                    matchResult.setLastIdx(idx);
                    matchResult.setWords(dialog.substring(i, idx));
                    matchResult.setCounter(counter);
                    if (tempVarMap.size() > 0) {
                        matchResult.setVarMap(new HashMap<>(tempVarMap));
                    }
                    result.add(matchResult);
                    progress.put(template, matchResult);
                }
            }
        }
        return result;
    }

    /**
     * 模板解析为语法树
     */
    public static Syntax parseTemplate(String template) {
        List<SplitGroupUtils.Group> groupList = SplitGroupUtils.group(template, "[", "]", true, true);
        Syntax syntax = parseGroup(groupList);
        syntax.setTemplate(template);
        return syntax;
    }

    private static Syntax parseGroup(List<SplitGroupUtils.Group> groupList) {
        List<Syntax> list = new ArrayList<>();
        for (SplitGroupUtils.Group group : groupList) {
            if (group.getValue().trim().length() == 0) {
                continue;
            }
            if (group.getType() == 0) {
                list.add(new StringSyntax(group.getValue().trim()));
            } else {
                try {
                    list.add(buildSyntax(group));
                } catch (NumberFormatException e) {
                    throw new RuntimeException("语法错误：" + group.getValue() + " => 数字不合规");
                } catch (SyntaxException e) {
                    throw e;
                } catch (Exception e) {
                    throw new SyntaxException("未知语法：" + group.getValue());
                }
            }
        }
        if (list.isEmpty()) {
            throw new SyntaxException("未知语法");
        }
        for (int i = 0; i < list.size() - 1; i++) {
            list.get(i).setNext(list.get(i + 1));
        }
        return list.size() == 1 ? list.get(0) : new AndSyntax(list);
    }

    private static Syntax buildSyntax(SplitGroupUtils.Group group) {
        String context = group.getContext().trim();
        String value = group.getValue();
        if (value.startsWith("[W:")) {
            context = context.substring(2);
            WildcardSyntax syntax = new WildcardSyntax();
            boolean isLike = false, isIn = false;
            int idx = context.indexOf("{", 2);
            String[] number;
            if (idx != -1) {
                switch (context.charAt(idx - 1)) {
                    case '~':
                        isLike = true;
                        break;
                    case '>':
                        isIn = true;
                        break;
                    case '^':
                        break;
                    default:
                        throw new SyntaxException("通配符语法错误：" + value + " ==> " + context.substring(idx - 1, 2));
                }
                if (!context.endsWith("}")) {
                    throw new SyntaxException("通配符" + (isLike ? "包含" : (isIn ? "限定" : "否定")) + "语法错误：" + value + " => 缺少 }");
                }
                number = context.substring(0, idx - 1).split("-");
                String sub = context.substring(idx + 1, context.length() - 1);
                if (sub.length() > 0) {
                    if (isLike) {
                        syntax.likeMatch = parseTemplate(sub);
                    } else if (isIn) {
                        syntax.inMatch = parseTemplate(sub);
                    } else {
                        syntax.notMatch = parseTemplate(sub);
                    }
                }
            } else {
                number = context.split("-");
            }
            if (number.length == 1) {
                syntax.min = Integer.parseInt(number[0]);
                syntax.max = syntax.min;
            } else if (number.length == 2) {
                syntax.min = Integer.parseInt(number[0]);
                syntax.max = Integer.parseInt(number[1]);
                if (syntax.min > syntax.max) {
                    throw new SyntaxException("通配符语法格式错误；" + value + " ==> 最小匹配个数不能大于" + syntax.max);
                }
            } else {
                throw new SyntaxException("通配符语法格式错误；" + value);
            }
            return syntax;
        } else if (value.startsWith("[N:")) {
            context = context.substring(2);
            NumberSyntax syntax = new NumberSyntax();
            String[] number = context.split("-");
            if (number.length == 1) {
                syntax.min = Integer.parseInt(number[0]);
                syntax.max = syntax.min;
            } else if (number.length == 2) {
                syntax.min = Integer.parseInt(number[0]);
                syntax.max = Integer.parseInt(number[1]);
                if (syntax.min > syntax.max) {
                    throw new SyntaxException("任意数字语法格式错误；" + value + " ==> 最小匹配个数不能大于" + syntax.max);
                }
            } else {
                throw new SyntaxException("任意数字语法格式错误；" + value);
            }
            return syntax;
        } else if (value.startsWith("[@")) {
            if (group.getChildren() != null && group.getChildren().size() > 0) {
                throw new SyntaxException("实体语法格式错误；" + value);
            }
            context = context.substring(1).trim();
            if (context.length() == 0) {
                throw new SyntaxException("模糊匹配语法格式错误；" + value);
            }
            return new EntitySyntax(context);
        } else if (value.startsWith("[~")) {
            if (group.getChildren() != null && group.getChildren().size() > 0) {
                throw new SyntaxException("模糊匹配语法格式错误；" + value);
            }
            context = context.substring(1).trim();
            if (context.length() == 0) {
                throw new SyntaxException("模糊匹配语法格式错误；" + value);
            }
            return new LikeSyntax(context);
        } else if (value.startsWith("[#")) {
            context = context.substring(1).trim();
            int idx = context.indexOf(":");
            if (idx == -1) {
                throw new SyntaxException("变量语法格式错误：" + value + " => 缺少:");
            }
            String var = context.substring(0, idx);
            String sub = context.substring(idx + 1).trim();
            if (sub.length() == 0) {
                throw new SyntaxException("变量语法格式错误：" + value + " => 缺少主体");
            }
            return new VarSyntax(var, parseTemplate(sub));
        } else if (value.endsWith("?]")) {
            context = context.substring(0, context.length() - 1).trim();
            if (context.length() == 0) {
                throw new SyntaxException("非必选语法格式错误；" + value);
            }
            return new NotEssentialSyntax(parseTemplate(context));
        } else {
            if (group.getChildren() != null && group.getChildren().size() > 0) {
                // 多个、或者
                return parseSyntaxChildren(group);
            } else {
                // 或者
                String[] split = context.split("\\|");
                List<Syntax> children = Arrays.stream(split).map(s -> new StringSyntax(s.trim())).filter(s -> s.str.length() > 0).collect(Collectors.toList());
                if (children.size() == 0) {
                    throw new SyntaxException("可选语法格式错误；" + value);
                }
                return new OrSyntax(children);
            }
        }
    }

    private static Syntax parseSyntaxChildren(SplitGroupUtils.Group group) {
        List<Syntax> list = new ArrayList<>();
        List<Syntax> tempAnds = new ArrayList<>();
        int orNum = 0;
        for (SplitGroupUtils.Group child : group.getChildren()) {
            String v = child.getValue().trim();
            if (v.length() == 0) {
                continue;
            }
            if (child.getType() == 1) {
                tempAnds.add(buildSyntax(child));
            } else if ("|".equals(v)) {
                if (tempAnds.size() > 0) {
                    if (tempAnds.size() == 1) {
                        list.add(tempAnds.get(0));
                    } else {
                        list.add(new AndSyntax(tempAnds));
                    }
                    tempAnds = new ArrayList<>();
                } else if (orNum != 0) {
                    throw new SyntaxException("可选语法格式错误；" + group.getValue() + " ==> " + v);
                }
                orNum = 1;
            } else if (v.contains("|")) {
                if (v.startsWith("|")) {
                    if (tempAnds.size() > 0) {
                        if (tempAnds.size() == 1) {
                            list.add(tempAnds.get(0));
                        } else {
                            list.add(new AndSyntax(tempAnds));
                        }
                        tempAnds = new ArrayList<>();
                    } else if (orNum != 0) {
                        throw new SyntaxException("可选语法格式错误；" + group.getValue() + " ==> " + v);
                    }
                    v = v.substring(1);
                }
                boolean hasEnd = false;
                if (v.endsWith("|")) {
                    hasEnd = true;
                    v = v.substring(0, v.length() - 1);
                }
                List<String> vs = Arrays.stream(v.split("\\|")).map(String::trim).collect(Collectors.toList());
                for (int i = 0; i < vs.size(); i++) {
                    String s = vs.get(i);
                    if (s.length() == 0) {
                        throw new SyntaxException("可选语法格式错误；" + group.getValue() + " ==> " + v);
                    }
                    if (!hasEnd && i == vs.size() - 1) {
                        tempAnds.add(new StringSyntax(s));
                    } else if ((hasEnd || orNum > 0) && i == 0) {
                        tempAnds.add(new StringSyntax(s));
                        if (tempAnds.size() == 1) {
                            list.add(tempAnds.get(0));
                        } else {
                            list.add(new AndSyntax(tempAnds));
                        }
                        tempAnds = new ArrayList<>();
                    } else {
                        list.add(new StringSyntax(s));
                    }
                }
                if (hasEnd) {
                    orNum = 1;
                }
            } else {
                orNum = 0;
                tempAnds.add(new StringSyntax(v));
            }
        }
        if (tempAnds.size() > 0) {
            if (tempAnds.size() == 1) {
                list.add(tempAnds.get(0));
            } else {
                list.add(new AndSyntax(tempAnds));
            }
        }
        if (list.isEmpty()) {
            throw new SyntaxException("可选语法格式错误；" + group.getValue());
        }
        return new OrSyntax(list);
    }

    public static int getEndIdx(char[] chars, int start, int endIdx) {
        for (int i = start + 1; i < endIdx; i++) {
            if (isSplitChar(chars[i])) {
                endIdx = i;
                break;
            }
        }
        return endIdx;
    }

    public static boolean isSplitChar(char c) {
        switch (c) {
            case ',':
            case '，':
            case '。':
            case '.':
            case '！':
            case '!':
            case '?':
            case '？':
            case ';':
            case '；':
            case '~':
            case '\n':
                return true;
        }
        return false;
    }

}
