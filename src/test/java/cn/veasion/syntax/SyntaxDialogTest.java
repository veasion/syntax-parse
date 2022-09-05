package cn.veasion.syntax;

import org.junit.jupiter.api.Test;

/**
 * SyntaxDialogTest
 *
 * @author luozhuowei
 * @date 2022/9/3
 */
public class SyntaxDialogTest extends BaseTest {

    @Test
    public void test() {
        String dialog = "hi，兄弟。你好，我要买两张从上海前往北京的机票，非常感谢！我要订一张从北京到上海的高铁票啊";
        String template = "[W:0-5]" +
                "[[我要|帮我][订|买]]" +
                "[@sys_num]" +
                "从" +
                "[#出发地:[@sys_location]]" +
                "[出发?]" +
                "[到|去|到达|前往]" +
                "[#目的地:[@sys_location]]" +
                "的" +
                "[票|车票|火车票|高铁票|机票|飞机票]" +
                "[W:0-5]";
        matchDialog(dialog, template);
    }

    @Test
    public void test1() {
        String dialog = "你今年18";
        // 模板一：测试优先级，N语法要优先于W语法
        String template1 = "[W:3-5][#age:[N:1-2]][岁?]";
        // 模板二：测试动态分配，需要尽可能匹配模板
        String template2 = "[#1:[W:1-2]][#2:[W:2-3]][#3:[W:1-3]][#4:[W:1-3]]";
        matchDialog(dialog, template1, template2);
    }

    @Test
    public void test2() {
        String dialog = "你好，我今年18岁，哈哈，不是吧，你18岁，是的，就是18。你现在不在家中吧18";
        // 测试模板匹配结果不能包含 “不是吧”、“不在家中吧”
        String template = "[W:2~{[你|哈|的]}]" +
                "[W:1-6^{" +
                "不" +
                "[好|是|[@sys_name]|[W:1-3~{家[[里|中][？?]]}]]" +
                "吧" +
                "}]" +
                "[#age:[N:1-2]]" +
                "[岁?]";
        matchDialog(dialog, template);
    }

    @Test
    public void test3() {
        String template = "[你好|[我|他|她]好|[W:1]好|[~好]]";
        String dialog = "她好。都好，大家好吗";
        matchDialog(dialog, template);
    }

    @Test
    public void test4() {
        // 量词
        String template = "[@sys_num]";
        String dialog = "二十个。三百张，四十一条，5片，六个核桃，12棵树";
        matchDialog(dialog, template);
    }

    @Test
    public void test5() {
        // 姓名
        String template = "[@sys_name]";
        String dialog = "我叫张三，你是李四，他是王老六，你个老六，请叫我王大锤，诸葛亮666，我玩孙悟空";
        matchDialog(dialog, template);
    }

    @Test
    public void test6() {
        // 时间
        String template = "[@sys_time]";
        String dialog = "公元21年，我今天下午去喝酒，大概要晚上九点才回来，他们是在阴历八月初六举行婚礼，那天正好周六，阳历9月21号，现在北京时间2022年09月3号16点54分1秒，二零二二年九月三号";
        matchDialog(dialog, template);
    }

    @Test
    public void test7() {
        // 地标
        String template = "[@sys_location]";
        String dialog = "我要到上海闵行区茸锦科技园A栋1001室，请问该如何走？" +
                "我们去杭州玩吧？不，我想去深圳或者广州，这有啥好玩的，还不如一起去新疆呢，或者西藏。" +
                "对了，上海有啥玩的，东方明珠塔，中心大厦，世纪公园啥的，都没啥玩的，本地人从来不去。" +
                "你家住哪？我家在浦东新区xxx路101号楼202房间，不对我搬家了，是在302房间。" +
                "中国城市排行榜，北上广深位列前四，广东江苏山东浙江这几个省gdp增速最高";
        matchDialog(dialog, template);
    }

    @Test
    public void test8() {
        String template = "我[想|要|[W:0-1]][从|去|到|前往][#目的地:[@sys_location]][W:0-2][请问|敢问]该[怎么|如何]走[？?]";
        String dialog = "你好，我要到上海闵行区茸锦科技园A栋1001室，请问该如何走？" +
                "左边右转一公里就能到。" +
                "兄弟，我想前往茸锦科技园A栋，敢问该怎么走" +
                "不知道";
        matchDialog(dialog, template);
    }

}
