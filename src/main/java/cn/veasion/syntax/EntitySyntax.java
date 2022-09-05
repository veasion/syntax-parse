package cn.veasion.syntax;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

/**
 * EntitySyntax 实体 @xxx
 *
 * @author luozhuowei
 * @date 2022/9/3
 */
public class EntitySyntax extends Syntax {

    private static final Map<String, Object> VAR_MAP = new HashMap<>();

    static {
        // 系统地址
        String sys_location = "[" +
                "[[" +
                "[海南|浙江|云南|山西|安徽|辽宁|河南|陕西|福建|吉林|湖北|甘肃|江西|湖南|青海|山东|广东|四川|台湾|江苏|贵州|河北|黑龙江][省?]|" +
                "[北京|上海|深圳|重庆|天津][市?]|" +
                "[新疆|西藏|广西|宁夏|澳门|内蒙古|香港]" +
                "]?]" +
                "[[[广州|杭州|南京|苏州|武汉|成都|天津|重庆|无锡|宁波|济南|长沙|青岛|郑州|合肥|福州|西安|佛山|南通|大连|常州|沈阳|厦门|东莞|昆明|绍兴|烟台|嘉兴|鄂尔多斯|南昌|长春|太厦|泉州|珠海|唐山|温州|扬州|潍坊|泰州|台州|石家庄|徐州|湖州|贵阳|镇江|盐城|东营|金华|洛阳|榆林|乌鲁木齐|哈尔滨|芜湖|威海|兰州|宜昌][市?]]?]" +
                "[[[W:1-5^{[在|去|到|是|住|往|，|,|。|！|？|?|.|!|；]}][市|县|区|自治州]]?]" +
                "[[[W:1-5^{[在|去|到|是|住|往|，|,|。|！|？|?|.|!|；]}][县|镇|村]]?]" +
                "[[[W:1-6^{[在|去|到|是|住|往|，|,|。|！|？|?|.|!|；]}][镇|村||街|街道|路]]?]" +
                "[[[W:1-6^{[在|去|到|是|住|往|，|,|。|！|？|?|.|!|；]}][路|栋|弄|号楼|园区|小区|馆|店|公园|地铁站|火车站|机场|科技园|产业园|商场|房间|塔|寺|湖|海|大厦]]?]" +
                "[[[W:1-5>{[1|2|3|4|5|6|7|8|9|0|零|一|二|三|四|五|六|七|八|九]}][室|房间]]?]" +
                "]";
        bind("sys_location", SyntaxParser.parseTemplate(sys_location));

        // 时间
        String sys_time = "[" +
                "[[[1|2|一|二|公元|公元前][W:0-3>{[零|一|二|三|四|五|六|七|八|九|十|百|千|0|1|2|3|4|5|6|7|8|9]}]年]?]" +
                "[[阳历|阴历]?]" +
                "[[[0?][一|二|三|四|五|六|七|八|九|十|十一|十二|正|1|2|3|4|5|6|7|8|9|10|11|12]月[份?]]?]" +
                "[[[[十|二十|三十|0|1|2|3]?][一|二|三|四|五|六|七|八|九|0|1|2|3|4|5|6|7|8|9][日|号]]?]" +
                "[初[一|二|三|四|五|六|七|八|九|0|1|2|3|4|5|6|7|8|9]?]" +
                "[[今天|明天|昨天|后天|前天|去年|前年|明年]?]" +
                "[[[星期|周|上周|下周][一|二|三|四|五|六|日|天|1|2|3|4|5|6]]?]" +
                "[[下午|上午|早上|早晨|凌晨|晚上|半夜]?]" +
                "[[[[十|二十|[N:1]]?][一|二|三|四|五|六|七|八|九|1|2|3|4|5|6|7|8|9]点]?]" +
                "[[[[十|二十|三十|四十|五十|[N:1]]?][一|二|三|四|五|六|七|八|九|1|2|3|4|5|6|7|8|9]分]?]" +
                "[[[[十|二十|三十|四十|五十|[N:1]]?][一|二|三|四|五|六|七|八|九|1|2|3|4|5|6|7|8|9]秒]?]" +
                "]";
        bind("sys_time", SyntaxParser.parseTemplate(sys_time));

        // 姓名
        String sys_name = "[李|王|张|刘|陈|杨|赵|黄|周|吴|徐|孙|胡|朱|高|林|何|郭|马|罗|梁|宋|郑|谢|韩|唐|冯|于|董|萧|程|曹|袁|邓|许|傅|沈|曾|彭|吕|苏|卢|蒋|蔡|贾|丁|魏|薛|叶|阎|余|潘|杜|戴|夏|钟|汪|田|任|姜|范|方|石|姚|谭|廖|邹|熊|金|陆|郝|孔|白|崔|康|毛|邱|秦|江|史|顾|侯|邵|孟|龙|万|段|漕|钱|汤|尹|黎|易|常|武|乔|贺|赖|龚|文|庞|樊|兰|殷|施|陶|洪|翟|安|颜|倪|严|牛|温|芦|季|俞|章|鲁|葛|伍|韦|申|尤|毕|聂|丛|焦|向|柳|邢|路|岳|齐|沿|梅|莫|庄|辛|管|祝|左|涂|谷|祁|时|舒|耿|牟|卜|路|詹|关|苗|凌|费|纪|靳|盛|童|欧|甄|项|曲|成|游|阳|裴|席|卫|查|屈|鲍|位|覃|霍|翁|隋|植|甘|景|薄|单|包|司|柏|宁|柯|阮|桂|闵|欧阳|解|强|柴|华|车|冉|房|边|辜|吉|饶|刁|瞿|戚|丘|古|米|池|滕|晋|苑|邬|臧|畅|宫|来|嵺|苟|全|褚|廉|简|娄|盖|符|奚|木|穆|党|燕|郎|邸|冀|谈|姬|屠|连|郜|晏|栾|郁|商|蒙|计|喻|揭|窦|迟|宇|敖|糜|鄢|冷|卓|花|仇|艾|蓝|都|巩|稽|井|练|仲|乐|虞|卞|封|竺|冼|原|官|衣|楚|佟|栗|匡|宗|应|台|巫|鞠|僧|桑|荆|谌|银|扬|明|沙|薄|伏|岑|习|胥|保|和|蔺|司马|上官|欧阳|诸葛|东方|皇甫|尉迟|公羊|淳于|公孙|轩辕|令狐|宇文|长孙|慕容|司徒][W:1-2^{[，|,|。|！|？|?|.|!|；|0|1|2|3|4|5|6|7|8|9]}]";
        bind("sys_name", SyntaxParser.parseTemplate(sys_name));

        // 量词
        String sys_num = "[W:1-4>{[一|两|二|三|四|五|六|七|八|九|十|百|千|万|0|1|2|3|4|5|6|7|8|9]}][个|张|条|只|把|对|根|双|次|片|遍|棵]";
        bind("sys_num", SyntaxParser.parseTemplate(sys_num));
    }

    public String var;

    public EntitySyntax(String var) {
        this.var = var;
    }

    public static void bind(String var, List<String> list) {
        VAR_MAP.put(var, list);
    }

    public static void bind(String var, BiFunction<String, Integer, Integer> function) {
        VAR_MAP.put(var, function);
    }

    public static void bind(String var, Syntax syntax) {
        VAR_MAP.put(var, syntax);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected int match(String dialog, int start, Map<String, Object> entityMap, Map<String, String> varMap, AtomicInteger counter, boolean tryNext) {
        Object entity = entityMap != null ? entityMap.get(var) : null;
        if (entity == null) {
            entity = VAR_MAP.get(var);
        }
        if (entity == null) {
            return -1;
        }
        if (entity instanceof Syntax) {
            return ((Syntax) entity).match(dialog, start, entityMap, varMap, counter);
        } else if (entity instanceof List) {
            List<String> list = (List<String>) entity;
            String sub = dialog.substring(start);
            for (String s : list) {
                if (sub.startsWith(s)) {
                    return start + s.length();
                }
            }
        } else if (entity instanceof BiFunction) {
            Integer idx = ((BiFunction<String, Integer, Integer>) entity).apply(dialog, start);
            if (idx == null || idx == -1) {
                return -1;
            } else {
                return idx;
            }
        } else {
            String s = entity.toString();
            if (startsWith(dialog, start, s)) {
                return start + s.length();
            }
        }
        return -1;
    }

    @Override
    public String toString() {
        return "EntitySyntax => {" + var + '}';
    }
}
