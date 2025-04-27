package com.jody.regex;

import java.util.*;
import java.util.regex.Matcher;

public class ASTMatcher {


    private final Ast regex;

    /**
     * 所有的组
     */
    final List<Ast> groupAsts;
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
     * 记录表达式引用的层级，match模式下使用到
     */
    int expressionLevel = 0;

    private final String str;

    /**
     * 记录 递归非贪婪匹配的最终结果
     */
    Set<FindResult> recursiveNoGreedyResults = new HashSet<>();

    /**
     * 当前用作结尾的 节点
     */
    Ast curEndAst = Util.END_AST;


    /**
     * 记录组的捕获情况
     */
    final int[] groupCatch;

    /**
     *记录 NumAst 的 circleNum
     */
    /**
     * 记录节点循环的次数
     */
    final int[] numAstCircleNum;
    /**
     *记录numASt已经处理过的最大状态
     */
    final int[] numAstMaxI;



    private boolean strIsEnd(int i, int end) {
        //当 match模式且expressionLevel > 0时，说明处于表达式匹配，且已经匹配到了结尾
        //只要 i >searchStart，就记录一个结果到result中
        boolean find = (matchMode && (i == end || expressionLevel > 0 && i >= findResultStart)) || (!matchMode && i >= findResultStart);
        if (find) {
            result = i;
        }
        return find;
    }

    private boolean treeIsEnd(Ast ast) {
        return Objects.equals(ast,curEndAst);
    }


     ASTMatcher(ASTPattern pattern,String str) {
         RegexToASTree regexToASTree = pattern.regexToASTree;
         this.regex = pattern.ast;
         this.groupAsts = pattern.groups;
         this.str = str;
         groupCatch = new int[groupAsts.size() * 2];
         Arrays.fill(groupCatch,Util.NONE);
         numAstMaxI = new int[regexToASTree.numAstCount];
         numAstCircleNum = new int[regexToASTree.numAstCount];
         Arrays.fill(numAstCircleNum,0);
         Arrays.fill(numAstMaxI,Util.NONE);
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
            clearNumAstStatus(ast);
            if (searchTree(ast, search, end)) {
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
            clearNumAstStatus(ast);
            if (searchTree(ast, search, end)) {
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
            clearNumAstStatus(ast);
            if (searchTree(ast, searchLeft, search)) {
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
        boolean result =doMatch(0, str.length(), regex);
        this.matchMode = tempMatchMode;
        return result;
    }

    private boolean doMatch(int searchStart, int end, Ast ast) {
        clearNumAstStatus(ast);
        return searchTree(ast, searchStart, end);
    }

    /**
     * end 为一个 不能取到字符的下标
     */
    private boolean searchTree(Ast tree, int i, int end) {
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
                count = terminalAst.matchGroup(str,i, this);
                //处理表达式引用
            } else if (terminalAst.isExpressionType() && !terminalAst.isRecursiveType()) {
                count = terminalAst.matchExpression(i, groupAsts, end, this);
            } else if (terminalAst.isRecursiveType()) {
                return searchRecursive(terminalAst, i, end);
            } else {
                //普通字符的匹配
                count = terminalAst.match(str, i, end);
            }
            // 匹配失败
            if (count < 0) {
                return false;
            }
            // 匹配成功，继续搜索
            return searchTree(getNextAndGroupEndCheck(terminalAst, i + count), i + count, end);
        }
        if (tree instanceof CatAst) {
            CatAst cat = (CatAst) tree;
            return searchTree(cat.asts.get(0), i, end);
        }
        if (tree instanceof OrAst) {
            return searchOrAst((OrAst) tree, i, end);
        }
        if (tree instanceof NumAst) {
            NumAst numAst = (NumAst) tree;
            switch (numAst.type) {
                case NumAst.AT_LEAST_0:
                    return searchAtLeastZero(numAst, i, end);
                case NumAst.AT_LEAST_1:
                    return searchAtLeastOne(numAst, i, end);
                case NumAst.MOST_1:
                    return searchMostOne(numAst, i, end);
                case NumAst.UN_FIX:
                    return searchFixedAst(numAst, i, end);
                case NumAst.RANGE:
                    return searchRangeAst(numAst, i, end);
            }
        }
        return false;
    }

    private boolean searchOrAst(OrAst orAst, int i, int end) {
        for (Ast ast : orAst.asts) {
            if (searchTree(ast, i, end)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 遇到之前的状态，不应该再处理
     */
    private boolean shouldReturn(NumAst numAst, int i) {
        if (numAstMaxI[numAst.numAstNo] >= i) {
            return true;
        }
        numAstMaxI[numAst.numAstNo] = i;
        return false;
    }

    /**
     * *
     */
    private boolean searchAtLeastZero(NumAst numAst, int i, int end) {
        if (shouldReturn(numAst, i)) {
            return false;
        }
        //find模式 且是贪心查找，特殊处理; 此时优先执行循环
        if (!matchMode && numAst.greedy) {
            numAstCircleNum[numAst.numAstNo]++;
            if (searchTree(numAst.ast, i, end)) {
                return true;
            }
            return searchTree(getNextAndGroupEndCheck(numAst, i), i, end);
            //match模式或者 find模式的非贪心查找，此时优先处理next节点
        } else {
            if (searchTree(getNextAndGroupEndCheck(numAst, i), i, end)) {
                return true;
            }
            numAstCircleNum[numAst.numAstNo]++;
            return searchTree(numAst.ast, i, end);
        }
    }

    /**
     * ?
     */
    private boolean searchMostOne(NumAst numAst, int i, int end) {
        //贪婪模式，优先读取
        if (numAst.greedy) {
            return searchTree(numAst.ast, i, end) || searchTree(getNextAndGroupEndCheck(numAst, i), i, end);
        }
        //非贪婪模式，优先处理下一个节点
        return searchTree(getNextAndGroupEndCheck(numAst, i), i, end) || searchTree(numAst.ast, i, end);
    }

    /**
     * +
     */
    private boolean searchAtLeastOne(NumAst numAst, int i, int end) {
        int numAstNo = numAst.numAstNo;
        int curCircleNum = numAstCircleNum[numAstNo];
        if (curCircleNum < 1) {
            numAstCircleNum[numAstNo]++;
            return searchTree(numAst.ast, i, end);
        }
        if (shouldReturn(numAst, i)) {
            return false;
        }
        //find模式 且是贪心查找，特殊处理
        if (!matchMode && numAst.greedy) {
            numAstCircleNum[numAstNo]= curCircleNum + 1;
            if (searchTree(numAst.ast, i, end)) {
                return true;
            }
            numAstCircleNum[numAstNo]  = 0;
            return searchTree(getNextAndGroupEndCheck(numAst, i), i, end);
        } else {
            numAstCircleNum[numAstNo] = 0;
            if (searchTree(getNextAndGroupEndCheck(numAst, i), i, end)) {
                return true;
            }
            numAstCircleNum[numAstNo]= curCircleNum + 1;
            return searchTree(numAst.ast, i, end);
        }
    }

    /**
     * {a,b}
     */
    private boolean searchRangeAst(NumAst rangeAst, int i, int end) {
        int numAstNo = rangeAst.numAstNo;
        int curCircle = numAstCircleNum[numAstNo];
        if (curCircle < rangeAst.start) {
            numAstCircleNum[numAstNo]++;
            return searchTree(rangeAst.ast, i, end);
        }
        if (shouldReturn(rangeAst, i)) {
            return false;
        }
        //find模式 且是贪心查找，特殊处理
        if (!matchMode && rangeAst.greedy) {
            if (curCircle + 1 <= rangeAst.end) {
                numAstCircleNum[numAstNo] = curCircle + 1;
                if (searchTree(rangeAst.ast, i, end)) {
                    return true;
                }
            }
            numAstCircleNum[numAstNo]= 0;
            return searchTree(getNextAndGroupEndCheck(rangeAst, i), i, end);
            //match模式 或者 find模式的非贪心查找
        } else {
            numAstCircleNum[numAstNo] = 0;
            if (searchTree(getNextAndGroupEndCheck(rangeAst, i), i, end)) {
                return true;
            }
            if (curCircle + 1 <= rangeAst.end) {
                numAstCircleNum[numAstNo] = curCircle + 1;
                return searchTree(rangeAst.ast, i, end);
            }
        }
        return false;
    }

    /**
     * {num}
     */
    private boolean searchFixedAst(NumAst unfixed, int i, int end) {
        if (numAstCircleNum[unfixed.numAstNo] != unfixed.num) {
            numAstCircleNum[unfixed.numAstNo]++;
            return searchTree(unfixed.ast, i, end);
        }
        //还原
        numAstCircleNum[unfixed.numAstNo] = 0;
        return searchTree(getNextAndGroupEndCheck(unfixed, i), i, end);
    }

    /**
     * 组捕获开始 的检查
     */
    private Ast groupStartCheck(Ast ast, int i, String str) {
        if (ast == null) {
            return null;
        }
        if (ast instanceof NumAst) {
            if (numAstCircleNum[((NumAst) ast).numAstNo] != 0) {
                return ast;
            }
        }
        if (ast.groupNum != 0) {
            if (ast.groupType == Group.CATCH_GROUP) {
                groupCatch[ast.groupNum * 2] = i;
            } else if (ast.groupType == Group.NOT_CATCH_GROUP) {
                //do nothing
            } else {
                //预查不消耗字符，为了复用原先的ast，需要记录ast当前状态，用于还原。表达式调用同理
                Ast result = null;
                //记录好当前状态，并做好预查的准备
                MatcherStatus matcherStatus = new MatcherStatus(this, ast);
                Ast next = ast.getNext();
                // 将next设置为当前的end节点
                Ast endAst = curEndAst;
                curEndAst =  next;
                //预查使用匹配模式
                this.matchMode = true;
                //查询前，需要将ast的groupType设置成非预查模式，不然会不断的进入这里的代码，
                int groupType = ast.groupType;
                ast.groupType = Group.CATCH_GROUP;
                if (groupType == Group.FORWARD_POSTIVE_SEARCH) {
                    if (findForwardChangeEnd(i, i, str.length(), ast)) {
                        result = next;
                    }
                } else if (groupType == Group.FORWARD_NEGATIVE_SEARCH) {
                    //和上面相反
                    if (!findForwardChangeEnd(i, i, str.length(), ast)) {
                        result = next;
                    }
                } else if (groupType == Group.BACKWARD_POSTIVE_SEARCH) {
                    if (findBackWard(0, i, i, ast)) {
                        result = next;
                    }
                } else if (groupType == Group.BACKWARD_NEGATIVE_SEARCH) {
                    if (!findBackWard(0, i, i, ast)) {
                        result = next;
                    }
                }
                //状态还原
                ast.groupType = groupType;
                curEndAst = endAst;
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
                groupCatch[leaveGroup.groupNum * 2 + 1] = i;
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

    /**
     *根据组编号返回捕获的组
     */
    public String group(int groupNum) {
        // \\1 捕获第一个组， 访问时，则使用 group(0)
        groupNum++;
        if(groupNum == groupAsts.size()){
            return null;
        }
        int left = groupCatch[groupNum * 2];
        int right = groupCatch[groupNum * 2 + 1];
        if(left == Util.NONE || right == Util.NONE){
            return null;
        }
        return str.substring(left,right);
    }

    /**
     *根据名称返回捕获的组
     */
    public String group(String groupName) {
        if (groupName == null || groupName.isEmpty()) {
            return null;
        }
        for (Ast ast : groupAsts) {
           if(groupName.equals(ast.groupName)){
               int left = groupCatch[ast.groupNum*2];
               int right = groupCatch[ast.groupNum * 2 + 1];
               if(left == Util.NONE || right == Util.NONE){
                   return null;
               }
               return str.substring(left,right);
           }
        }
        return null;
    }

    /**
     *
    清除 NumAst的 circleNum,maxI信息
     */
    private void clearNumAstStatus(Ast ast) {
        if (ast.nodeMaxNumAstNo != Util.NONE) {
            for (int i = ast.nodeMinNumAstNo; i <= ast.nodeMaxNumAstNo; i++) {
               numAstCircleNum[i] = 0;
               numAstMaxI[i] = Util.NONE;
            }
        }
    }

    /**
     * 处理递归的情况
     */
    private boolean searchRecursive(TerminalAst recursive, int i, int end) {
        int start = this.findResultStart;
        //贪婪模式，优先读取
        if (recursive.recursiveGreedy) {
            expressionLevel++;
            boolean recursiveSuc = searchTree(regex, i, end) && searchTree(getNextAndGroupEndCheck(recursive, this.result),this.result,end);
            expressionLevel--;
            if(recursiveSuc){
                return true;
            }
            return searchTree(getNextAndGroupEndCheck(recursive, i), i, end);
        }
        //非贪婪模式，优先处理下一个节点
        if (searchTree(getNextAndGroupEndCheck(recursive, i), i, end)) {
            return true;
        }
        expressionLevel++;
        boolean recursiveSuc = searchTree(regex, i, end)&&searchTree(getNextAndGroupEndCheck(recursive, this.result),this.result,end);
        expressionLevel--;
        return recursiveSuc;
    }
}