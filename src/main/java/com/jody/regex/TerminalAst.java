package com.jody.regex;

import java.util.List;

class TerminalAst extends Ast implements Cloneable {
    /**
     * 记录TerminalAst的类型
     */
    final int type;
    Character c;
    /**
     * 多个普通字符连接在一起，用于减少ast节点的数量
     */
    String cs;
    /**
     */
    CharPredicates.CharPredicate predicate;

    /**
     * 当前的模式修正符
     */
    int modifier = 0;


    TerminalAst(int type) {
        this.type = type;
    }

    TerminalAst(String cs, int type) {
        this.cs = cs;
        this.type = type;
    }

    TerminalAst(Character c, int type) {
        this.c = c;
        this.type = type;
    }


    boolean isGroupType() {
        return Terminal.isGroupType(type);
    }


    /**
     * 两种情况 单词字符跟着非单词字符 和  非单词字符跟着单词字符， 相邻的两个非单词字符不是单词边界
     * 相邻的两个单词字符也不是单词边界
     *
     * !!!! 单词边界也不考虑 end
     */
    private int isWordBorder(String str, int i) {
        CharPredicates.CharPredicate w   = Modifier.openUnicodeCharacterClass(modifier) ?
                CharPredicates.WORD() :CharPredicates.ASCII_WORD();
        CharPredicates.CharPredicate W   = w.negate();
        //开始和结尾处的单词边界
        if (i == 0) {
            // "" 不是单词边界
            if (str.isEmpty()) {
               return Util.NONE;
            }
            // "a"   匹配  \\ba
            if(w.isCh(str.charAt(i))){
                return 0;
            }
        }
        if (i == str.length()) {
            if (w.isCh(str.charAt(i - 1))) {
                return 0;
            }
            return Util.NONE;
        }
        //字符串中的边界
        if (i > 0) {
            if (W.isCh(str.charAt(i - 1)) && w.isCh(str.charAt(i))
                    || w.isCh(str.charAt(i - 1)) && W.isCh(str.charAt(i))) {
                return 0;
            }
        }
        return Util.NONE;
    }

    /**
     * 返回匹配到的数量
     */
    int match(String str, int i, int end,boolean matchMode) {
        //边界符号，匹配成功返回0，因为边界符号不占空间
        if (Terminal.isB(type) || Terminal.isb(type)) {
            int result = isWordBorder(str, i);
            if (Terminal.isb(type)) {
                return result;
            }
            //和单词边界相反， 由于存在复合类型 [\b\B], 这里的 Terminal.isB() 判断是必须的
            if (Terminal.isB(type)) {
                return result == 0 ? Util.NONE : 0;
            }
        }
        // 目前仅以str字符串开始和结尾来判断 ^ $
        // 开始符号
        if (Terminal.isStartOfLine(type)) {
            if (i == 0) {
                return 0;
            }
            //查找模式下，开启了多行， \r \n 匹配 ^
            if (modifier != 0 && !matchMode && i > 0 && Modifier.openMultiline(modifier)) {
                if(str.charAt(i-1)=='\n'||(!Modifier.openUnixLine(modifier)&& str.charAt(i-1)=='\r')){
                    return 0;
                }
            }
            return Util.NONE;
        }
        //结束符号
        if (Terminal.isEndOfLine(type)) {
            if (i >= str.length()) {
                return 0;
            }
            //查找模式下，开启了多行， \r \n 匹配 ^
            if (modifier != 0 && !matchMode && Modifier.openMultiline(modifier)) {
                if(str.charAt(i)=='\n'||(!Modifier.openUnixLine(modifier)&& str.charAt(i)=='\r')){
                    return 0;
                }
            }
            return -1;
        }
        // \A
        if (Terminal.isStartOfInput(type)) {
            if (i == 0) {
                return 0;
            }
        }
        // \z
        if (Terminal.isEndOfInput(type)) {
            if (i >= str.length()) {
               return 0;
            }
        }
        // \Z
        if (Terminal.isEndOfInputWithTerminator(type)) {
            if (i >= str.length()) {
                return 0;
            }
            if (i == str.length() - 1 && (str.charAt(i) == '\n' || str.charAt(i) == '\r')) {
               return 1;
            }
        }

        // \R
        if (Terminal.isUniCodeLineBreak(type)) {
            if (i < end) {
                char ch = str.charAt(i);
                if (ch == 0x0A || ch == 0x0B || ch == 0x0C ||
                        ch == 0x85 || ch == 0x2028 || ch == 0x2029){
                    return 1;
                }
                if (ch == 0x0D) {
                    //到达结尾
                    if (i + 1 >= end || i + 1 >= str.length()) {
                        return 1;
                    } else{
                        ch = str.charAt(i + 1);
                        if (ch == 0x0A) {
                            return 2;
                        }
                    }
                    return Util.NONE;
                }
            }

        }
        //下面的都是占字符的终结符类型，一定要能取到字符，此时需要i进行判断
        if (i >= end || i >= str.length()) {
            return Util.NONE;
        }
        char chi = str.charAt(i);


        //大小写不敏感
        boolean caseInsensitive = Modifier.openCaseInsensitive(modifier);
        // 普通的字符比较
        if (Terminal.isSimple(type)) {
            //单个字符比较
            if (this.c != null) {
                if(chi == this.c || caseInsensitive && Character.toLowerCase(chi) == Character.toLowerCase(this.c)) {
                    return 1;
                }
                return Util.NONE;
            }
            if (i + cs.length() > end || i + cs.length() > str.length()) {
                return Util.NONE;
            }
            //多个字符比较
            for (int x = 0; x < cs.length(); x++) {
                if (str.charAt(i + x) == cs.charAt(x)) {
                    //什么也不做
                } else if (caseInsensitive && Character.toLowerCase(str.charAt(i + x)) == Character.toLowerCase(cs.charAt(x))) {
                    //什么也不做
                } else {
                    //匹配失败
                    return Util.NONE;
                }
            }
            return cs.length();
        }
        if (predicate != null && predicate.isCh(chi)) {
           return 1;
        }
        return Util.NONE;
    }

    /**
     * 处理 分组引用
     */
    int matchGroup(String str, int i, ASTMatcher astMatcher) {
        List<Ast> groups = astMatcher.catchGroups;
        int referenceGroupNum = getReferenceGroupNum() - 1;
        if (referenceGroupNum >= groups.size()) {
            throw new RuntimeException("不存在的组编号");
        }
        //引用的referenceGroupNum和实际的使用不一定是一个
        // 例如 ((a)) 只有一个Ast节点，在groups里面占据了两个位置，使用下标最小的那个也就是1
        Ast ast = groups.get(referenceGroupNum);
        int groupNum = ast.catchGroupNum;
        int left = astMatcher.groupCatch[groupNum*2];
        int right = astMatcher.groupCatch[groupNum*2 + 1];
        //未成功捕获
        if(left == Util.NONE || right == Util.NONE){
           return  Util.NONE;
        }
        String catchStr = str.substring(left, right);
        if (i + catchStr.length() <= str.length()) {
            for (int x = 0; x < catchStr.length(); x++) {
                if (str.charAt(i + x) != catchStr.charAt(x)) {
                    return Util.NONE;
                }
            }
            return catchStr.length();
        }
        return Util.NONE;

    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    /**
     *获取应用的组的编号：
     *  组引用/表达式应用
     */
    public int getReferenceGroupNum(){
       return Terminal.getReferenceGroupNum(type);
    }


}
