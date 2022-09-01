package com.example.test;

import com.example.regex.ASTMatcher;

import java.util.regex.Pattern;

public class ASTRunner {
    public static void main(String[] args) {
        test();
        compare("\\babc\\b","abc",1000000);

    }

    /**
     * 测试，主要是高级特性
     */
    public static void test() {
        //匹配中文
        ASTMatcher utfMatcher = ASTMatcher.compile("[\u4E00-\u9FA5]");
        System.out.println(utfMatcher.isMatch("你"));
        //日期 yyyy-mm-dd
        ASTMatcher date = ASTMatcher.compile("(?:(?!0000)[0-9]{4}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1[0-9]|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[0-9]{2}(?:0[48]|[2468][048]|[13579][26])|(?:0[48]|[2468][048]|[13579][26])00)-02-29)");
        System.out.println(date.isMatch("2022-06-30"));
        //ipv4地址
        ASTMatcher ip = ASTMatcher.compile("((?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)");
        System.out.println(ip.isMatch("01.01.01.01"));
        //ipv6地址
        ASTMatcher ipv6 = ASTMatcher.compile("(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))");
        System.out.println(ipv6.isMatch("2345:0425:2CA1:0000:0000:0567:5673:23b5"));
        //匹配邮件
        ASTMatcher codeMatcher = ASTMatcher.compile("[\\w!#$%&'*+/=?^_`{|}~-]+(\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@([\\w]([\\w-]*[\\w])?\\.)+[\\w]([\\w-]*[\\w])?");
        System.out.println(codeMatcher.isMatch("abc@qq.com"));
        //单词边界
        ASTMatcher wordBound = ASTMatcher.compile("\\b[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}\\b");
        System.out.println(wordBound.find("asdf test@runoob.com sdf sdf"));
        //正向预查,预查是做不到 Match的
        ASTMatcher matcher1 = ASTMatcher.compile("ab(?=c)");
        System.out.println(matcher1.find("abc"));
        //正向否定预查
        ASTMatcher matcher2 = ASTMatcher.compile("ab(?!c)");
        System.out.println(matcher2.find("aba"));
        //反向预查
        ASTMatcher matcher3 = ASTMatcher.compile("(?<=c)ab");
        System.out.println(matcher3.find("cab"));
        //反向否定预查
        ASTMatcher matcher4 = ASTMatcher.compile("(?<!c)ab");
        System.out.println(matcher4.find("aab"));
        //分组捕获
        ASTMatcher matcher5 = ASTMatcher.compile("(abc)cd\\1\\1");
        System.out.println(matcher5.find("abccdabcabcalsdkjfisod"));
        //表达式引用，除了递归引用支持贪婪非贪婪,match函数不能很好的支持
        ASTMatcher matcher6 = ASTMatcher.compile("(a|b|c)xx\\g<1>");
        System.out.println(matcher6.find("axxb"));
        // 利用递归正则匹配 json， match函数可能不能很好的支持递归引用
        //但是find可以，只要找到一组符号的结果就行
        ASTMatcher astMatcher = ASTMatcher.compile("\\{\\}|\\{('\\w+':('\\w+'|\\d+|\\g<0>),)+'\\w+':('\\w+'|\\d+|\\g<0>)}|\\{'\\w+':('\\w+'|\\d+|\\g<0>)\\}");
        System.out.println(astMatcher.find("{'key1':'value1','key2':123,'key3':{},'key4':{'key5':'value5'}}"));
        System.out.println(astMatcher.getResultStart());
        System.out.println(astMatcher.getResultEnd());
    }

    /**
     * 和Java自带的Regex进行性能对比
     */
    public static void compare(String regex, String str, int n) {
        long start = System.currentTimeMillis();
        Pattern pattern = Pattern.compile(regex);
        for (int i = 0; i < n; i++) {
            boolean x = pattern.matcher(str).matches();
        }
        long end = System.currentTimeMillis();
        System.out.println("原生Regex:" + (end - start));
        ASTMatcher matcher = ASTMatcher.compile(regex);
        long start2 = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            matcher.isMatch(str);
        }
        long end2 = System.currentTimeMillis();
        System.out.println("自己Regex:" + (end2 - start2));
    }
}
