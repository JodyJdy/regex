package com.jody.regex;

import java.util.List;
import java.util.Set;

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
     * 是否取反
     */
    private boolean isNegative = false;
    /**
     * [abc]
     */
    private Set<Character> chars;

    private List<CharRange> charRanges;

    /**
     *递归表达式的编号
     */
    int recursiveNo = Util.NONE;

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

    TerminalAst(boolean isNegative, Set<Character> chars, List<CharRange> ranges, int type) {
        this.isNegative = isNegative;
        this.chars = chars;
        this.charRanges = ranges;
        this.type = type;
    }

    boolean isGroupType() {
        return Terminal.isGroupType(type);
    }

    boolean isExpressionType() {
        return Terminal.isExpression(type);
    }

    /**
     * 递归调用自身的类型
     */
    boolean isRecursiveType() {
        return isExpressionType() && getReferenceGroupNum() == 0;
    }

    /**
     * 两种情况 单词字符跟着非单词字符 和  非单词字符跟着单词字符， 相邻的两个非单词字符不是单词边界
     * 相邻的两个单词字符也不是单词边界
     *
     * !!!! 单词边界也不考虑 end
     */
    private int isWordBorder(String str, int i) {
        //开始和结尾处的单词边界
        if (i == 0) {
            // "" 不是单词边界
            if (str.isEmpty()) {
               return Util.NONE;
            }
            // "a"   匹配  \\ba
            if(Terminal.isw(str.charAt(i))){
                return 0;
            }
        }
        if (i == str.length()) {
            if (Terminal.isw(str.charAt(i - 1))) {
                return 0;
            }
            return Util.NONE;
        }
        //字符串中的边界
        if (i > 0) {
            if (Terminal.isW(str.charAt(i - 1)) && Terminal.isw(str.charAt(i))
                    || Terminal.isw(str.charAt(i - 1)) && Terminal.isW(str.charAt(i))) {
                return 0;
            }
        }
        return Util.NONE;
    }

    /**
     * 返回匹配到的数量
     */
    int match(String str, int i, int end) {
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
        if (Terminal.isStart(type)) {
            if (i == 0) {
                return 0;
            }
            return Util.NONE;
        }
        //结束符号
        if (Terminal.isEnd(type)) {
            if (i >= str.length()) {
                return 0;
            }
            return -1;
        }

        //下面的都是占字符的终结符类型，一定要能取到字符，此时需要i进行判断
        if (i >= end || i >= str.length()) {
            return Util.NONE;
        }
        char chi = str.charAt(i);
        // 普通的字符比较
        if (Terminal.isSimple(type)) {
            //单个字符比较
            if (this.c != null) {
                return chi == this.c ? 1 : Util.NONE;
            }
            if (i + cs.length() > end || i + cs.length() > str.length()) {
                return Util.NONE;
            }
            //多个字符比较
            for (int x = 0; x < cs.length(); x++) {
                if (str.charAt(i + x) != cs.charAt(x)) {
                    return Util.NONE;
                }
            }
            return cs.length();
        }
        //复杂类型, []
        boolean result = false;
        if (Terminal.isComposite(type)) {
            result = chars.contains(chi);
            for (CharRange charRange : charRanges) {
                result = result || charRange.match(chi);
                if (result) {
                    break;
                }
            }
        }
        //\d,\w...类型
        if (!result && type != 0) {
            result = Terminal.match(chi, type);
        }
        if (isNegative != result) {
            return 1;
        }
        return Util.NONE;
    }

    /**
     * 处理 分组引用
     */
    int matchGroup(String str, int i, ASTMatcher astMatcher) {
        List<Ast> groups = astMatcher.groupAsts;
        int referenceGroupNum = getReferenceGroupNum();
        if (referenceGroupNum > groups.size()) {
            throw new RuntimeException("groupNum dose not exist");
        }
        int left = astMatcher.groupCatch[referenceGroupNum*2];
        int right = astMatcher.groupCatch[referenceGroupNum*2 + 1];
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

    /**
     * 处理表达式引用
     */
    int matchExpression(int i, List<Ast> groups, int end, ASTMatcher astMatcher) {
        Ast ast;
        //获取引用的表达式的下标
        astMatcher.expressionLevel++;
        ast = groups.get(getReferenceGroupNum());
        //记录调用前的状态
        MatcherStatus matcherStatus = new MatcherStatus(astMatcher, ast);
        Ast next = ast.getNext();
        Ast endAst = astMatcher.curEndAst;
        astMatcher.curEndAst = next;
        // 表达式匹配 使用查询模式
        astMatcher.matchMode = false;
        //查询
        int result = Util.NONE;
        if (astMatcher.findForwardChangeStart(i, i, end, ast)) {
            result = astMatcher.result - i;
            storeRecursive(astMatcher);
        }
        astMatcher.expressionLevel--;
        //还原状态
        matcherStatus.resumeStatus();
        astMatcher.curEndAst = endAst;
        return result;
    }

    /**
     * 存储递归非匹配的结果
     * <p>
     * 记录递归匹配非贪婪匹配的结果，要求，最短，最靠近左边，
     */
    private void storeRecursive(ASTMatcher astMatcher) {
        if (!this.isRecursiveType() || !astMatcher.hasRecursiveNoGreedy) {
            return;
        }
        if (astMatcher.findResultStart == astMatcher.result) {
            return;
        }
        //添加到结果集合里面
        astMatcher.recursiveNoGreedyResults.add( new FindResult(astMatcher.findResultStart, astMatcher.result));
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * a-z存储字符范围
     */
    static class CharRange {
        char left;
        char right;

        public CharRange(char left, char right) {
            this.left = left;
            this.right = right;
        }

        boolean match(char ch) {
            return ch >= left && ch <= right;
        }
    }

    /**
     *获取应用的组的编号：
     *  组引用/表达式应用
     */
    public int getReferenceGroupNum(){
       return Terminal.getReferenceGroupNum(type);
    }


}
