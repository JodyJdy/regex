package com.example.regex;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ASTMatcher {

    private static final Map<String, ASTMatcher> treeMap = new HashMap<>();

    private final Ast regexTree;

    /**
     * 所有的组
     */
    private final List<Ast> groupAsts;
    /**
     * find模式下，查找结果的起点
     */
    int findResultStart = 0;
    /**
     * find模式下， searchStart->result，是结果集的一种;
     * match模式下：表达式引用时，用于存储表达式的结果
     */
    int result;
    /**
     * true:match模式下，要求 字符串和正则表达式完全匹配，此时无需考虑是否 贪心匹配
     * false: find模式下，要求考虑贪心匹配，且会有多个结果集
     */
    boolean matchMode;

    /**
     * 是否有递归非贪婪匹配，\\g<0>?，需要额外处理结果
     */
    boolean hasRecursiveNoGreedy;

    /**
     * 记录表达式引用的层级，match模式下使用到
     */
    int expressionLevel = 0;

    /**
     * 记录 递归非贪婪匹配的最终结果
     */
    int recursiveStart = 0, recursiveEnd = 0;

    private boolean strIsEnd(int i, int end) {
        //当 match模式且expressionLevel > 0时，说明处于表达式匹配，且已经匹配到了结尾
        //只要 i >searchStart，就记录一个结果到result中
        boolean find = (matchMode && (i == end || expressionLevel > 0 && i > findResultStart)) || (!matchMode && i >= findResultStart);
        if (find) {
            result = i;
        }
        return find;
    }

    private boolean treeIsEnd(Ast ast) {
        return ast instanceof EndAst;
    }

    public static ASTMatcher compile(String regex) {
        Util.checkEmpty(regex);
        ASTMatcher matcher = treeMap.get(regex);
        if (matcher == null) {
            RegexToASTree regexToASTree = new RegexToASTree(regex);
            Ast ast = regexToASTree.asTree();
            ASTMatcher matcher1 = new ASTMatcher(ast, regexToASTree.groupAst);
            matcher1.hasRecursiveNoGreedy = regexToASTree.hasRecursiveNoGreedy;
            treeMap.put(regex, matcher1);
            return matcher1;
        }
        return matcher;
    }

    private ASTMatcher(Ast regexTree, List<Ast> groupAsts) {
        this.regexTree = regexTree;
        this.groupAsts = groupAsts;
    }

    /**
     * 起点范围在 [searchLeft,searchRight],终点为end
     * 从 searchRight ->  searchLeft 查找， 找到一个符合的结果停止
     *
     * @param end         字符串的尾部位置
     */
    private boolean findBackWard(String str, int searchLeft, int searchRight, int end, Ast ast) {
        int search = searchRight;
        while (searchLeft <= search) {
            ast.clearNumAstStatus();
            if (searchTree(ast, search, end, str)) {
                // record
                findResultStart = search;
                return true;
            }
            search--;
        }
        return false;
    }

    /**
     * 起点范围在 [searchLeft,searchRight],终点为end
     * 从 searchLeft ->  searchRight 查找， 找到一个符合的结果停止
     *
     * @param searchLeft  左边的查找范围
     * @param searchRight 右边的查找范围
     * @param end         字符串的尾部位置
     */
    boolean findForward(String str, int searchLeft, int searchRight, int end, Ast ast) {
        int search = searchLeft;
        while (search <= searchRight) {
            ast.clearNumAstStatus();
            if (searchTree(ast, search, end, str)) {
                findResultStart = search;
                return true;
            }
            search++;
        }
        return false;

    }


    /**
     * @param start 起始查找下标
     */
    public boolean find(String str, int start) {
        return findForward(str, start, str.length(), str.length(), regexTree);
    }

    /**
     *从尾部开始查找
     */

    public boolean backwardFind(String str) {
        return backwardFind(str, 0);
    }

    /**
     * @param backEnd  反向查找的结尾位置
     */
    public boolean backwardFind(String str, int backEnd) {
        return findBackWard(str, backEnd, str.length(),str.length(), regexTree);
    }

    /**
     *默认从头开始查找
     */
    public boolean find(String str) {
        if (matchMode) {
            return isMatch(str);
        }
        return findForward(str, 0, str.length(), str.length(), regexTree);
    }

    public boolean isMatch(String str) {
        boolean recursiveNoGreedy = hasRecursiveNoGreedy;
        //match模式下 \\g<0>? 的非贪婪匹配不生效
        if(recursiveNoGreedy){
            hasRecursiveNoGreedy = false;
        }
        boolean result =doMatch(str, 0, str.length(), regexTree);
        if(recursiveNoGreedy){
            hasRecursiveNoGreedy = true;
        }
        return result;
    }

    private boolean doMatch(String str, int searchStart, int end, Ast ast) {
        matchMode = true;
        ast.clearNumAstStatus();
        return searchTree(ast, searchStart, end, str);
    }

    /**
     * end 为一个 不能取到字符的下标
     */
    private boolean searchTree(Ast tree, int i, int end, String str) {
        if (tree == null) {
            return false;
        }
        tree = groupStartCheck(tree, i, str);
        //只有 预查失败时，才会返回null
        if (tree == null) {
            return false;
        }
        //这里要先检查 treeIsEnd
        if (treeIsEnd(tree) && strIsEnd(i, end)) {
            return true;
        }
        if (tree instanceof TerminalAst) {
            TerminalAst terminalAst = (TerminalAst) tree;
            int count;
            //处理反向引用
            if (terminalAst.isGroupType()) {
                count = terminalAst.matchGroup(str, i, groupAsts);
                //处理表达式引用和递归引用
            } else if (terminalAst.isExpressionType()) {
                count = terminalAst.matchExpression(str, i, groupAsts, end, this);
            } else {
                //普通字符的匹配
                count = terminalAst.match(str, i, end);
            }
            // 匹配失败
            if (count < 0) {
                return false;
            }
            // 匹配成功，继续搜索
            return searchTree(getNextAndGroupEndCheck(terminalAst, i + count), i + count, end, str);
        }
        if (tree instanceof CatAst) {
            CatAst cat = (CatAst) tree;
            return searchTree(cat.ast.get(0), i, end, str);
        }
        if (tree instanceof OrAst) {
            return searchOrAst((OrAst) tree, i, end, str);
        }
        if (tree instanceof NumAst) {
            NumAst numAst = (NumAst) tree;
            switch (numAst.type) {
                case NumAst.AT_LEAST_0:
                    return searchAtLeastZero(numAst, i, end, str);
                case NumAst.AT_LEAST_1:
                    return searchAtLeastOne(numAst, i, end, str);
                case NumAst.MOST_1:
                    return searchMostOne(numAst, i, end, str);
                case NumAst.UN_FIX:
                    return searchFixedAst(numAst, i, end, str);
                case NumAst.RANGE:
                    return searchRangeAst(numAst, i, end, str);
            }
        }
        return false;
    }

    private boolean searchOrAst(OrAst orAst, int i, int end, String str) {
        for (Ast ast : orAst.asts) {
            if (searchTree(ast, i, end, str)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 遇到之前的状态，不应该再处理
     */
    private boolean shouldReturn(NumAst numAst, int i) {
        if (numAst.maxI >= i) {
            return true;
        }
        numAst.maxI = i;
        return false;
    }

    /**
     * *
     */
    private boolean searchAtLeastZero(NumAst numAst, int i, int end, String str) {
        if (shouldReturn(numAst, i)) {
            return false;
        }
        //find模式 且是贪心查找，特殊处理; 此时优先执行循环
        if (!matchMode && numAst.greedy) {
            numAst.circleNum++;
            if (searchTree(numAst.ast, i, end, str)) {
                return true;
            }
            return searchTree(getNextAndGroupEndCheck(numAst, i), i, end, str);
            //match模式或者 find模式的非贪心查找，此时优先处理next节点
        } else {
            if (searchTree(getNextAndGroupEndCheck(numAst, i), i, end, str)) {
                return true;
            }
            numAst.circleNum++;
            return searchTree(numAst.ast, i, end, str);
        }
    }

    /**
     * ?
     */
    private boolean searchMostOne(NumAst numAst, int i, int end, String str) {
        //贪婪模式，优先读取
        if (numAst.greedy) {
            return searchTree(numAst.ast, i, end, str) || searchTree(getNextAndGroupEndCheck(numAst, i), i, end, str);
        }
        //非贪婪模式，优先处理下一个节点
        return searchTree(getNextAndGroupEndCheck(numAst, i), i, end, str) || searchTree(numAst.ast, i, end, str);
    }

    /**
     * +
     */
    private boolean searchAtLeastOne(NumAst numAst, int i, int end, String str) {
        int curCircleNum = numAst.circleNum;
        if (curCircleNum < 1) {
            numAst.circleNum++;
            return searchTree(numAst.ast, i, end, str);
        }
        if (shouldReturn(numAst, i)) {
            return false;
        }
        //find模式 且是贪心查找，特殊处理
        if (!matchMode && numAst.greedy) {
            numAst.circleNum = curCircleNum + 1;
            if (searchTree(numAst.ast, i, end, str)) {
                return true;
            }
            numAst.circleNum = 0;
            return searchTree(getNextAndGroupEndCheck(numAst, i), i, end, str);
        } else {
            numAst.circleNum = 0;
            if (searchTree(getNextAndGroupEndCheck(numAst, i), i, end, str)) {
                return true;
            }
            numAst.circleNum = curCircleNum + 1;
            return searchTree(numAst.ast, i, end, str);
        }
    }

    /**
     * {a,b}
     */
    private boolean searchRangeAst(NumAst rangeAst, int i, int end, String str) {
        int curCircle = rangeAst.circleNum;
        if (curCircle < rangeAst.start) {
            rangeAst.circleNum++;
            return searchTree(rangeAst.ast, i, end, str);
        }
        if (shouldReturn(rangeAst, i)) {
            return false;
        }
        //find模式 且是贪心查找，特殊处理
        if (!matchMode && rangeAst.greedy) {
            if (curCircle + 1 <= rangeAst.end) {
                rangeAst.circleNum = curCircle + 1;
                if (searchTree(rangeAst.ast, i, end, str)) {
                    return true;
                }
            }
            rangeAst.circleNum = 0;
            return searchTree(getNextAndGroupEndCheck(rangeAst, i), i, end, str);
            //match模式 或者 find模式的非贪心查找
        } else {
            rangeAst.circleNum = 0;
            if (searchTree(getNextAndGroupEndCheck(rangeAst, i), i, end, str)) {
                return true;
            }
            if (curCircle + 1 <= rangeAst.end) {
                rangeAst.circleNum = curCircle + 1;
                return searchTree(rangeAst.ast, i, end, str);
            }
        }
        return false;
    }

    /**
     * {num}
     */
    private boolean searchFixedAst(NumAst unfixed, int i, int end, String str) {
        if (unfixed.circleNum != unfixed.num) {
            unfixed.circleNum++;
            return searchTree(unfixed.ast, i, end, str);
        }
        //还原
        unfixed.circleNum = 0;
        return searchTree(getNextAndGroupEndCheck(unfixed, i), i, end, str);
    }

    /**
     * 组捕获开始 的检查
     */
    private Ast groupStartCheck(Ast ast, int i, String str) {
        if (ast == null) {
            return null;
        }
        if (ast instanceof NumAst) {
            if (((NumAst) ast).circleNum != 0) {
                return ast;
            }
        }
        if (ast.groupNum != 0) {
            if (ast.groupType == Group.CATCH_GROUP) {
                ast.groupStart = i;
            } else if (ast.groupType == Group.NOT_CATCH_GROUP) {
                //do nothing
            } else {
                //预查不消耗字符，为了复用原先的ast，需要记录ast当前状态，用于还原。表达式调用同理
                Ast result = null;
                //记录好当前状态，并做好预查的准备
                MatcherStatus matcherStatus = new MatcherStatus(this, ast);
                Ast next = ast.getNext();
                Util.resetNext(ast, Util.END_AST);
                //查询前，需要将ast的groupType设置成非预查模式，不然会不断的进入这里的代码，
                int groupType = ast.groupType;
                if (ast.groupType == Group.FORWARD_POSTIVE_SEARCH) {
                    ast.groupType = Group.NOT_CATCH_GROUP;
                    if (findForward(str, i, i, str.length(), ast)) {
                        result = next;
                    }
                } else if (ast.groupType == Group.FORWARD_NEGATIVE_SEARCH) {
                    ast.groupType = Group.NOT_CATCH_GROUP;
                    //和上面相反
                    if (!findForward(str, i, i, str.length(), ast)) {
                        result = next;
                    }
                } else if (ast.groupType == Group.BACKWARD_POSTIVE_SEARCH) {
                    ast.groupType = Group.NOT_CATCH_GROUP;
                    if (findBackWard(str, 0, i - 1, i, ast)) {
                        result = next;
                    }
                } else if (ast.groupType == Group.BACKWARD_NEGATIVE_SEARCH) {
                    ast.groupType = Group.NOT_CATCH_GROUP;
                    if (!findBackWard(str, 0, i - 1, i, ast)) {
                        result = next;
                    }
                }
                //状态还原
                ast.groupType = groupType;
                Util.resetNext(ast, next);
                matcherStatus.resumeStatus();
                //如果 ast.getNext()也是一个预查节点，应该再次处理
                return groupStartCheck(result, i, str);
            }
        }
        return ast;
    }

    /**
     * 组捕获结束时的检查
     */
    private Ast getNextAndGroupEndCheck(Ast ast, int i) {
        if (ast.nextLeaveGroup) {
            Ast leaveGroup = groupAsts.get(ast.leaveGroupNum);
            //捕获成功
            if (leaveGroup.groupType == Group.CATCH_GROUP) {
                leaveGroup.groupEnd = i;
            }
        }
        return ast.getNext();
    }

    public int getResultStart() {
        if (matchMode) {
            throw new RuntimeException("match mode !");
        }
        if (hasRecursiveNoGreedy) {
            return recursiveStart;
        }
        return findResultStart;
    }

    public int getResultEnd() {
        if (matchMode) {
            throw new RuntimeException("match mode !");
        }
        if (hasRecursiveNoGreedy) {
            return recursiveEnd;
        }
        return result;
    }
}