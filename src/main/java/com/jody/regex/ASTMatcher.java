package com.jody.regex;

import java.util.*;

public class ASTMatcher {


    private final Ast regex;

    /**
     * 需要捕获的组
     */
    final List<Ast> catchGroups;
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

    private final String str;


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

    /**
     * 记录处理分组时的 模式修正符，用于还原
     */
    int[] groupModifier;
    /**
     * 模式修正符
     */
    int modifier = 0;

    final boolean hasModifier;
    final List<Ast> allGroups;



    private boolean strIsEnd(int i, int end) {
        //当 match模式且expressionLevel > 0时，说明处于表达式匹配，且已经匹配到了结尾
        //只要 i >searchStart，就记录一个结果到result中
        boolean find = (matchMode && (i == end)) || (!matchMode && i >= findResultStart);
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
         this.catchGroups = regexToASTree.catchGroups;
         this.str = str;
         groupCatch = new int[catchGroups.size() * 2];
         Arrays.fill(groupCatch,Util.NONE);
         numAstMaxI = new int[regexToASTree.numAstCount];
         numAstCircleNum = new int[regexToASTree.numAstCount];
         Arrays.fill(numAstCircleNum,0);
         Arrays.fill(numAstMaxI,Util.NONE);
         allGroups = regexToASTree.allGroups;
         hasModifier = regexToASTree.hasModifier;
         if (hasModifier) {
             groupModifier = new int[regexToASTree.globalGroupCount];
         }
         this.modifier = pattern.modifiers;
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
        boolean strIsEnd = strIsEnd(i, end);
        //这里要先检查 treeIsEnd
        if (treeIsEnd(tree) && strIsEnd) {
            return true;
        }
        tree = groupStartCheck(tree, i, str);
        //只有 预查失败时，才会返回null
        if (tree == null) {
            return false;
        }
        //再次检查 treeIsEnd
        if (treeIsEnd(tree) && strIsEnd) {
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
                return false;//searchExpression(terminalAst, i, end);
            } else if (terminalAst.isRecursiveType()) {
                return false;//searchRecursive(terminalAst, i, end);
            } else {
                //普通字符的匹配
                count = terminalAst.match(str, i, end,modifier,matchMode);
            }
            // 匹配失败
            if (count < 0) {
                return false;
            }
            // 匹配成功，继续搜索
            return searchTree(getNextAndGroupEndCheck(terminalAst, i + count), i + count, end);
        }
        if (tree instanceof ModifierAst) {
            modifier = modifier | tree.openFlag;
            modifier = modifier & tree.closeFlag;
            return searchTree(getNextAndGroupEndCheck(tree, i), i, end);
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
     * 主要是防止空转， 对于  * + 有用
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
            boolean search = searchTree(numAst.ast, i, end);
            numAstMaxI[numAst.numAstNo] = Util.NONE;
            if (search) {
                return true;
            }
            return searchTree(getNextAndGroupEndCheck(numAst, i), i, end);
            //match模式或者 find模式的非贪心查找，此时优先处理next节点
        } else {
            boolean search = searchTree(getNextAndGroupEndCheck(numAst, i), i, end);
            if (search) {
                return true;
            }
            NumAstStatus status = new NumAstStatus(numAst);
            boolean r =  searchTree(numAst.ast, i, end);
            if (!r) {
                status.resumeStatus();
            }
            numAstMaxI[numAst.numAstNo] = Util.NONE;
            return r;
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
            boolean rel = searchTree(numAst.ast, i, end);
            if (!rel) {
               numAstCircleNum[numAstNo] = 0;
            }
            return rel;
        }
        if (shouldReturn(numAst, i)) {
            return false;
        }
        //find模式 且是贪心查找，特殊处理
        if (!matchMode && numAst.greedy) {
            boolean search = searchTree(numAst.ast, i, end);
            if (search) {
                //状态还原
                numAstMaxI[numAst.numAstNo] = Util.NONE;
                numAstCircleNum[numAstNo] = 0;
                return true;
            }
            return searchTree(getNextAndGroupEndCheck(numAst, i), i, end);
        } else {
            boolean search = searchTree(getNextAndGroupEndCheck(numAst, i), i, end);
            if (search) {
                //状态还原
                numAstMaxI[numAst.numAstNo] = Util.NONE;
                numAstCircleNum[numAstNo] = 0;
                return true;
            }
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
            boolean b = searchTree(rangeAst.ast, i, end);
            if (!b) {
                numAstCircleNum[rangeAst.numAstNo] = curCircle;
            } else{
                numAstCircleNum[rangeAst.numAstNo] = 0;
            }
            numAstMaxI[rangeAst.numAstNo] = Util.NONE;
            return b;
        }
        if (shouldReturn(rangeAst, i)) {
            return false;
        }
        //find模式 且是贪心查找，特殊处理
        // 这里不用保留状态是因为，优先处理 rangeAst.ast节点
        if (!matchMode && rangeAst.greedy) {
            if (curCircle + 1 <= rangeAst.end) {
                numAstCircleNum[numAstNo] = curCircle + 1;
                if (searchTree(rangeAst.ast, i, end)) {
                    return true;
                }
            }
            //搜索下个节点时，清除当前状态
            numAstCircleNum[numAstNo]= 0;
            return searchTree(getNextAndGroupEndCheck(rangeAst, i), i, end);
            //match模式 或者 find模式的非贪心查找
        } else {
            //需要保留当前状态,如果搜索失败后，当前状态需要在后续的搜索使用到
            NumAstStatus matcherStatus = new NumAstStatus(rangeAst);
            if (searchTree(getNextAndGroupEndCheck(rangeAst, i), i, end)) {
                return true;
            }
            matcherStatus.resumeStatus();
            numAstMaxI[numAstNo] = Util.NONE;
            if (curCircle + 1 <= rangeAst.end) {
                numAstCircleNum[numAstNo] = curCircle + 1;
                // 搜索失败，还原状态
                boolean suc =  searchTree(rangeAst.ast, i, end);
                if (!suc) {
                    numAstCircleNum[numAstNo] = curCircle;
                }
                return suc;
            }
        }
        return false;
    }

    /**
     * {num}
     */
    private boolean searchFixedAst(NumAst unfixed, int i, int end) {
        int curCircleNum =numAstCircleNum[unfixed.numAstNo];
        if (curCircleNum < unfixed.num) {
            numAstCircleNum[unfixed.numAstNo] = curCircleNum+1;
            boolean result =searchTree(unfixed.ast, i, end);
            if (!result) {
                numAstCircleNum[unfixed.numAstNo] = curCircleNum;
            } else{
                numAstCircleNum[unfixed.numAstNo] = 0;
                numAstMaxI[unfixed.numAstNo] = Util.NONE;
            }
            return result;
        }
        if (shouldReturn(unfixed, i)) {
            return false;
        }
        //还原
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
        if (ast.groupType != 0) {
            if (hasModifier) {
                //记录当前的模式修正符
                groupModifier[ast.globalGroupNum] = modifier;
                //设置新的模式修正符号
                modifier = modifier | ast.openFlag;
                modifier = modifier & ast.closeFlag;
            }
            if (ast.groupType == Group.CATCH_GROUP) {
                groupCatch[ast.catchGroupNum * 2] = i;
            } else if (ast.groupType == Group.NOT_CATCH_GROUP) {
                //do nothing
            } else if (ast.groupType == Group.NOT_CATCH_GROUP_WITH_MODIFIER) {
                // do nothing
            } else {
                //预查不消耗字符，为了复用原先的ast，需要记录ast当前状态，用于还原。表达式调用同理
                Ast result = null;
                //记录好当前状态，并做好预查的准备
                MatcherStatus matcherStatus = new MatcherStatus(ast);
                Ast next = ast.getNext();
                // 将next设置为当前的end节点
                Ast endAst = curEndAst;
                curEndAst = next;
                //预查使用匹配模式
                this.matchMode = true;
                //查询前，需要将ast的groupType设置成非预查模式，不然会不断的进入这里的代码，
                int groupType = ast.groupType;
                //不捕获
                ast.groupType = Group.NOT_CATCH_GROUP;
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
        if (ast.nextLeaveGroupNum >=0) {
            if (ast.nextLeaveGroupType == Group.CATCH_GROUP) {
               Ast leaveGroup = allGroups.get(ast.nextLeaveGroupNum);
                //捕获成功
                groupCatch[leaveGroup.catchGroupNum * 2 + 1] = i;
            }
            //还原模式修正符号
            if (hasModifier) {
                modifier = groupModifier[ast.nextLeaveGroupNum];
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
     *根据组编号返回捕获的组
     */
    public String group(int groupNum) {
        if(groupNum == catchGroups.size()){
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
        for (Ast ast : catchGroups) {
           if(groupName.equals(ast.groupName)){
               int left = groupCatch[ast.catchGroupNum *2];
               int right = groupCatch[ast.catchGroupNum * 2 + 1];
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
     * 处理表达式情况
     */
    private boolean searchExpression(TerminalAst expression, int i, int end) {
        return false;
//        int referenceGroupNum = Terminal.getReferenceGroupNum(expression.type);
//        if(referenceGroupNum >=groupAsts.size()){
//            throw new RuntimeException("groupNum dose not exist");
//        }
//        expressionLevel++;
//        //获取引用的组
//        Ast ast = groupAsts.get(referenceGroupNum);
//        final Ast beforeEnd = curEndAst;
//        curEndAst = ast.getNext();
//        Supplier<Boolean> curExecute = execute;
//        execute = ()->{
//            curEndAst = beforeEnd;
//            return searchTree(getNextAndGroupEndCheck(expression, this.result), this.result, end);
//        };
//        boolean suc = searchTree(ast, i, end);
//        execute = curExecute;
//        expressionLevel--;
//        return suc;
    }

    /**
     * 处理递归的情况
     */
    private boolean searchRecursive(TerminalAst recursive, int i, int end) {
        return false;
//        //贪婪模式，优先读取
//        if (recursive.recursiveGreedy) {
//            expressionLevel++;
//            boolean recursiveSuc = searchTree(regex, i, end) && searchTree(getNextAndGroupEndCheck(recursive, this.result),this.result,end);
//            expressionLevel--;
//            if(recursiveSuc){
//                return true;
//            }
//            return searchTree(getNextAndGroupEndCheck(recursive, i), i, end);
//        }
//        //非贪婪模式，优先处理下一个节点
//        if (searchTree(getNextAndGroupEndCheck(recursive, i), i, end)) {
//            return true;
//        }
//        expressionLevel++;
//        boolean recursiveSuc = searchTree(regex, i, end)&&searchTree(getNextAndGroupEndCheck(recursive, this.result),this.result,end);
//        expressionLevel--;
//        return recursiveSuc;
    }

    /**
     * 记录 NumAst状态
     */
    class NumAstStatus {
        final Ast searchAst;
        int[] numAstCircleNum;

        NumAstStatus(Ast searchAst) {
            this.searchAst = searchAst;
            int len = searchAst.nodeMaxNumAstNo - searchAst.nodeMinNumAstNo + 1;
            numAstCircleNum = new int[len];
            int nodeMinNumAstNo = searchAst.nodeMinNumAstNo;
            System.arraycopy(ASTMatcher.this.numAstCircleNum, nodeMinNumAstNo, numAstCircleNum, 0, len);
            Arrays.fill(ASTMatcher.this.numAstCircleNum, nodeMinNumAstNo, nodeMinNumAstNo + len, 0);
        }

        void resumeStatus() {
            // 状态还原
            int nodeMinNumAstNo = searchAst.nodeMinNumAstNo;
            System.arraycopy(numAstCircleNum, 0, ASTMatcher.this.numAstCircleNum, nodeMinNumAstNo, numAstCircleNum.length);
        }
    }

    /**
     * 记录 ASTMatcher的状态
     */
    class MatcherStatus {

        private final int searchStart;
        private final int findResult;
        private final boolean matchMood;

        int[] numAstCircleNum;
        /**
         *记录numASt已经处理过的最大状态
         */
        int[] numAstMaxI;

        private final Ast searchAst;

        MatcherStatus(Ast searchAst){
            this.searchStart = ASTMatcher.this.findResultStart;
            this.findResult = ASTMatcher.this.result;
            this.matchMood = ASTMatcher.this.matchMode;
            //默认值
            ASTMatcher.this.findResultStart = ASTMatcher.this.result = 0;
            this.searchAst =searchAst;
            //存储ast的状态，例如： maxI, circleNum
            if (searchAst.nodeMaxNumAstNo != Util.NONE) {
                int len = searchAst.nodeMaxNumAstNo - searchAst.nodeMinNumAstNo+1;
                numAstCircleNum = new int[len];
                numAstMaxI = new int[len];
                int nodeMinNumAstNo = searchAst.nodeMinNumAstNo;
                System.arraycopy(ASTMatcher.this.numAstMaxI, nodeMinNumAstNo, numAstMaxI, 0, len);
                System.arraycopy(ASTMatcher.this.numAstCircleNum, nodeMinNumAstNo, numAstCircleNum, 0, len);
                Arrays.fill(ASTMatcher.this.numAstMaxI,nodeMinNumAstNo,nodeMinNumAstNo+len,Util.NONE);
                Arrays.fill(ASTMatcher.this.numAstCircleNum, nodeMinNumAstNo, nodeMinNumAstNo + len, 0);
            }
        }

        void resumeStatus(){
            ASTMatcher.this.findResultStart = this.searchStart;
            ASTMatcher.this.result = this.findResult;
            ASTMatcher.this.matchMode = this.matchMood;
            // 状态还原
            if (searchAst.nodeMaxNumAstNo != Util.NONE) {
                int nodeMinNumAstNo = searchAst.nodeMinNumAstNo;
                System.arraycopy(numAstMaxI,0,ASTMatcher.this.numAstMaxI,nodeMinNumAstNo,numAstMaxI.length);
                System.arraycopy(numAstCircleNum,0,ASTMatcher.this.numAstCircleNum,nodeMinNumAstNo,numAstMaxI.length);
            }
        }

    }
}