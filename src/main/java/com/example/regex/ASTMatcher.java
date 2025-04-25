package com.example.regex;

import java.util.*;

public class ASTMatcher {


    private final Ast regex;

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

    private final String str;

    /**
     * 记录 递归非贪婪匹配的最终结果
     */
    Set<FindResult> recursiveNoGreedyResults = new HashSet<>();

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


     ASTMatcher(ASTPattern pattern,String str) {
         RegexToASTree regexToASTree = pattern.regexToASTree;
         this.regex = regexToASTree.astTree();
        this.groupAsts = regexToASTree.groupAsts;
        this.hasRecursiveNoGreedy = regexToASTree.hasRecursiveNoGreedy;
        this.str = str;
    }

    /**
     * 起点范围在 [searchLeft,searchRight],终点为end
     * 从 searchRight ->  searchLeft 查找， 找到一个符合的结果停止
     *
     * @param end         字符串的尾部位置
     */
    private boolean findBackWard(int searchLeft, int searchRight, int end, Ast ast) {
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
     * 调整起点 找到一个符合的结果停止
     *
     * @param searchLeft  左边的查找范围
     * @param searchRight 右边的查找范围
     * @param end         字符串的尾部位置
     */
    boolean findForwardChangeStart(int searchLeft, int searchRight, int end, Ast ast) {
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
     * 终点范围在 [searchRight,end],起点为searchLeft
     *  调整终点 找到一个符合的结果停止
     *
     * @param searchLeft  左边的查找范围
     * @param searchRight 右边的查找范围
     * @param end         字符串的尾部位置
     */
    boolean findForwardChangeEnd(int searchLeft, int searchRight, int end, Ast ast) {
        int search = searchRight;
        while (search <= end) {
            ast.clearNumAstStatus();
            if (searchTree(ast, searchLeft, search, str)) {
                findResultStart = searchLeft;
                return true;
            }
            search++;
        }
        return false;
    }


    /**
     * @param start 起始查找下标
     */
    public boolean find(int start) {
        return findForwardChangeStart(start, str.length(), str.length(), regex);
    }

    /**
     *从尾部开始查找
     */

    public boolean backwardFind() {
        return backwardFind(0);
    }

    /**
     *将str中符合条件的内容替换成 replacement
     */
    public String replaceFirst(String replacement) {
        reset();
        if (!find()) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        int start =findResultStart;
        int end = result;
        if (start != 0) {
            sb.append(str, 0, start);
        }
        sb.append(replacement);
        if (end < str.length()) {
            sb.append(str, end, str.length());
        }
        return sb.toString();
    }

    /**
     *将str中符合条件的全部替换成replacement
     */
    public String replaceAll(String replacement) {
        reset();
        StringBuilder sb = new StringBuilder();

        if (find()) {
            int start;
            int end;
            int lastAppendPosition = 0;
            do {
                start = findResultStart;
                end = result;
                if (start > lastAppendPosition) {
                    sb.append(str, lastAppendPosition, start);
                }
                sb.append(replacement);
                lastAppendPosition = end;
                int nextSearch = end;
                if (nextSearch == start) {
                    nextSearch++;
                }
                if (!find(nextSearch)) {
                    break;
                }
            } while (true);
            if (end < str.length()) {
                sb.append(str, end, str.length());
            }
            return sb.toString();
        }
        return str;
    }
    /**
     * @param backEnd  反向查找的结尾位置
     */
    public boolean backwardFind(int backEnd) {
        return findBackWard(backEnd, str.length(),str.length(), regex);
    }

    /**
     *默认从头开始查找
     */
    public boolean find() {
        if (matchMode) {
            return isMatch();
        }
        return findForwardChangeStart(0, str.length(), str.length(), regex);
    }

    public boolean isMatch() {
        boolean tempMatchMode = this.matchMode;
        this.matchMode = true;
        boolean recursiveNoGreedy = hasRecursiveNoGreedy;
        //match模式下 \\g<0>? 的非贪婪匹配不生效
        if(recursiveNoGreedy){
            hasRecursiveNoGreedy = false;
        }
        boolean result =doMatch(0, str.length(), regex);
        if(recursiveNoGreedy){
            hasRecursiveNoGreedy = true;
        }
        this.matchMode = tempMatchMode;
        return result;
    }

    private boolean doMatch(int searchStart, int end, Ast ast) {
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
        if (treeIsEnd(tree) && (strIsEnd(i, end))) {
            return true;
        }
        if (tree instanceof TerminalAst) {
            TerminalAst terminalAst = (TerminalAst) tree;
            int count;
            //处理反向引用
            if (terminalAst.isGroupType()) {
                count = terminalAst.matchGroup(str,i, groupAsts);
                //处理表达式引用和递归引用
            } else if (terminalAst.isExpressionType()) {
                count = terminalAst.matchExpression(i, groupAsts, end, this);
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
                //预查使用匹配模式
                boolean tempMatchMode = this.matchMode;
                this.matchMode = true;
                //查询前，需要将ast的groupType设置成非预查模式，不然会不断的进入这里的代码，
                int groupType = ast.groupType;
                if (ast.groupType == Group.FORWARD_POSTIVE_SEARCH) {
                    ast.groupType = Group.NOT_CATCH_GROUP;
                    if (findForwardChangeEnd(i, i, str.length(), ast)) {
                        result = next;
                    }
                } else if (ast.groupType == Group.FORWARD_NEGATIVE_SEARCH) {
                    ast.groupType = Group.NOT_CATCH_GROUP;
                    //和上面相反
                    if (!findForwardChangeEnd(i, i, str.length(), ast)) {
                        result = next;
                    }
                } else if (ast.groupType == Group.BACKWARD_POSTIVE_SEARCH) {
                    ast.groupType = Group.NOT_CATCH_GROUP;
                    if (findBackWard(0, i, i, ast)) {
                        result = next;
                    }
                } else if (ast.groupType == Group.BACKWARD_NEGATIVE_SEARCH) {
                    ast.groupType = Group.NOT_CATCH_GROUP;
                    if (!findBackWard(0, i, i, ast)) {
                        result = next;
                    }
                }
                this.matchMode = tempMatchMode;
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


    public void reset(){
       this.findResultStart = 0;
       this.result = 0;
    }

    /**
     *返回一个查询结果
     */
    public FindResult getFindResult() {
        return new FindResult(findResultStart,result);
    }

    /**
     *递归非贪婪匹配模式下返回所有结果
     */
    public List<FindResult> getRecursiveNoGreedyFindResult() {
        recursiveNoGreedyResults.add(new FindResult(findResultStart,result));
        List<FindResult> findResults = new ArrayList<>(recursiveNoGreedyResults);
        Collections.sort(findResults);
        return findResults;
    }
}