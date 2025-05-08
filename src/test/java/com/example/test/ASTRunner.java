package com.example.test;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.jody.regex.ASTMatcher;
import com.jody.regex.ASTPattern;

import java.io.*;
import java.net.URISyntaxException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ASTRunner {
    public static void main(String[] args) throws IOException, URISyntaxException {
        executeTestCase();
        test();
    }

    public static void test(){
        //        //高级特性测试
        testFeature();
        //测试replace方法
        testReplace();
        //测试分组
        testGroup();
        String emailRex = "[\\w!#$%&'*+/=?^_`{|}~-]+(\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@([\\w]([\\w-]*[\\w])?\\.)+[\\w]([\\w-]*[\\w])?";
        String email = "abc@qq.com";
        performanceCompare(emailRex,email,1000000);
        performanceCompare("\\babc\\b","abc",1000000);
    }

    /**
     * 测试，主要是高级特性
     */
    public static void testFeature() {
        System.out.println("--------------------test-------------------");
        //匹配中文
        ASTPattern utfPattern = ASTPattern.compile("[\u4E00-\u9FA5]");
        ASTMatcher utfMatcher = utfPattern.matcher("你");
        System.out.println(utfMatcher.isMatch());
        //日期 yyyy-mm-dd
        ASTPattern date = ASTPattern.compile("(?:(?!0000)[0-9]{4}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1[0-9]|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[0-9]{2}(?:0[48]|[2468][048]|[13579][26])|(?:0[48]|[2468][048]|[13579][26])00)-02-29)");
        System.out.println(date.matcher("2022-06-30").isMatch());
        //ipv4地址
        ASTPattern ip = ASTPattern.compile("((?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)");
        System.out.println(ip.matcher("01.01.01.01").isMatch());
        //ipv6地址
        ASTPattern ipv6 = ASTPattern.compile("(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))");
        System.out.println(ipv6.matcher("2345:0425:2CA1:0000:0000:0567:5673:23b5").isMatch());
        //匹配邮件
        ASTPattern codeMatcher = ASTPattern.compile("[\\w!#$%&'*+/=?^_`{|}~-]+(\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@([\\w]([\\w-]*[\\w])?\\.)+[\\w]([\\w-]*[\\w])?");
        System.out.println(codeMatcher.matcher("abc@qq.com").isMatch());
        //单词边界
        ASTPattern wordBound = ASTPattern.compile("\\b[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}\\b");
        System.out.println(wordBound.matcher("asdf test@runoob.com sdf sdf").find());
        //正向预查,预查是做不到 Match的
        ASTPattern matcher1 = ASTPattern.compile("ab(?=c)");
        System.out.println(matcher1.matcher("abc").find());
        //正向否定预查
        ASTPattern matcher2 = ASTPattern.compile("ab(?!c)");
        System.out.println(matcher2.matcher("aba").find());
        //反向预查
        ASTPattern matcher3 = ASTPattern.compile("(?<=c)ab");
        System.out.println(matcher3.matcher("cab").find());
        //反向否定预查
        ASTPattern matcher4 = ASTPattern.compile("(?<!c)ab");
        System.out.println(matcher4.matcher("aab").find());
        //分组捕获
        ASTPattern matcher5 = ASTPattern.compile("(abc)cd\\1\\1");
        System.out.println(matcher5.matcher("abccdabcabcalsdkjfisod").find());

    }
    /**
     *测试 replace
     */
    public static void testReplace(){
        System.out.println("--------------------testReplace-------------------");
        Pattern p = Pattern.compile("(?:(?!0000)[0-9]{4}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1[0-9]|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[0-9]{2}(?:0[48]|[2468][048]|[13579][26])|(?:0[48]|[2468][048]|[13579][26])00)-02-29)");
        Matcher matcher = p.matcher("  2022-06-30 --  2022-06-30  bbb -- ");
        ASTPattern astMatcher = ASTPattern.compile("(?:(?!0000)[0-9]{4}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1[0-9]|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[0-9]{2}(?:0[48]|[2468][048]|[13579][26])|(?:0[48]|[2468][048]|[13579][26])00)-02-29)");
        String str1 = matcher.replaceAll("bb");
        String str2 = astMatcher.matcher("  2022-06-30 --  2022-06-30  bbb -- ").replaceAll( "bb");
        System.out.println(str1.equals(str2));
    }
    /**
     * 测试 分组
     */
    public static void testGroup(){
        System.out.println("--------------------testGroup-------------------");
        ASTPattern pattern = ASTPattern.compile("(a)(b)(c)(?<hello>d)");
        ASTMatcher matcher = pattern.matcher("abcdef");
        matcher.find();
        System.out.println(matcher.group(0));
        System.out.println(matcher.group(1));
        System.out.println(matcher.group("hello"));
    }

    /**
     * 和Java自带的Regex进行性能对比
     */
    public static void performanceCompare(String regex, String str, int n) {
        System.out.println("--------------------testPerformance-------------------");
        long start = System.currentTimeMillis();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher2 = pattern.matcher(str);
        for (int i = 0; i < n; i++) {
            boolean x = matcher2.matches();
        }
        long end = System.currentTimeMillis();
        System.out.println("原生Regex:" + (end - start));
        ASTPattern matcher = ASTPattern.compile(regex);
        long start2 = System.currentTimeMillis();
        ASTMatcher matcher1 = matcher.matcher(str);
        for (int i = 0; i < n; i++) {
            matcher1.isMatch();
        }
        long end2 = System.currentTimeMillis();
        System.out.println("自己Regex:" + (end2 - start2));
    }
    public static void executeTestCase() throws IOException {
        System.out.println("-------------------- 执行测试用例 ---------------");
        FileInputStream fileInputStream = new FileInputStream("regex.json");
        JSONArray objects = JSON.parseArray(fileInputStream.readAllBytes());
        System.out.println(objects.size());
        for(int i=0; i<objects.size(); i++){
            JSONObject object = objects.getJSONObject(i);
            String regex = object.getString("regex");
            JSONArray array = object.getJSONArray("case");
            ASTPattern astPattern = ASTPattern.compile(regex);
            Pattern pattern = Pattern.compile(regex);
            for(int j=0; j<array.size(); j++){
                String text = array.getString(j);
                ASTMatcher astMatcher = astPattern.matcher(text);
                Matcher matcher = pattern.matcher(text);
                if (astMatcher.find() != matcher.find()) {
                    System.out.println("find测试错误: regex:\n"+ regex+"\n case:\n"+text);
                    break;
                }
                if (astMatcher.isMatch() != matcher.matches()) {
                    System.out.println("match测试错误: regex:\n"+ regex+"\n case:\n"+text);
                    break;
                }
            }
        }
        System.out.println("-------------------- 执行测试用例结束 ---------------");
    }
}
