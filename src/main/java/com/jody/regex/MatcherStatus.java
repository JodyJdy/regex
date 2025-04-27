package com.jody.regex;

import java.util.Arrays;

/**
 * 记录当ASTMatcher的状态
 */
class MatcherStatus {


    private final ASTMatcher astMatcher;
    private final int searchStart;
    private final int findResult;
    private final boolean matchMood;

    int[] numAstCircleNum;
    /**
     *记录numASt已经处理过的最大状态
     */
    int[] numAstMaxI;

    private final Ast searchAst;

    MatcherStatus(ASTMatcher astMatcher,Ast searchAst){
        this.searchStart = astMatcher.findResultStart;
        this.findResult = astMatcher.result;
        this.matchMood = astMatcher.matchMode;
        //默认值
        astMatcher.findResultStart = astMatcher.result = 0;
        this.astMatcher = astMatcher;
        this.searchAst =searchAst;
        //存储ast的状态，例如： maxI, circleNum
        if (searchAst.nodeMaxNumAstNo != Util.NONE) {
            int len = searchAst.nodeMaxNumAstNo - searchAst.nodeMinNumAstNo+1;
            numAstCircleNum = new int[len];
            numAstMaxI = new int[len];
            int nodeMinNumAstNo = searchAst.nodeMinNumAstNo;
            System.arraycopy(astMatcher.numAstMaxI, nodeMinNumAstNo, numAstMaxI, 0, len);
            System.arraycopy(astMatcher.numAstCircleNum, nodeMinNumAstNo, numAstCircleNum, 0, len);
            Arrays.fill(astMatcher.numAstMaxI,nodeMinNumAstNo,nodeMinNumAstNo+len,Util.NONE);
            Arrays.fill(astMatcher.numAstCircleNum, nodeMinNumAstNo, nodeMinNumAstNo + len, 0);
        }
    }

    void resumeStatus(){
        astMatcher.findResultStart = this.searchStart;
        astMatcher.result = this.findResult;
        astMatcher.matchMode = this.matchMood;
        // 状态还原
        if (searchAst.nodeMaxNumAstNo != Util.NONE) {
            int nodeMinNumAstNo = searchAst.nodeMinNumAstNo;
            System.arraycopy(numAstMaxI,0,astMatcher.numAstMaxI,nodeMinNumAstNo,numAstMaxI.length);
            System.arraycopy(numAstCircleNum,0,astMatcher.numAstCircleNum,nodeMinNumAstNo,numAstMaxI.length);
        }
    }

}
