package com.jody.regex;

import java.util.HashMap;
import java.util.Map;

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
            for (int i = 0; i < len; i++) {
                numAstMaxI[i] = astMatcher.numAstMaxI[nodeMinNumAstNo + i];
                numAstCircleNum[i] = astMatcher.numAstCircleNum[nodeMinNumAstNo + i];
                astMatcher.numAstMaxI[nodeMinNumAstNo + i] = Util.NONE;
                astMatcher.numAstCircleNum[nodeMinNumAstNo + i] = 0;
            }
        }
    }

    void resumeStatus(){
        astMatcher.findResultStart = this.searchStart;
        astMatcher.result = this.findResult;
        astMatcher.matchMode = this.matchMood;
        // 状态还原
        if (searchAst.nodeMaxNumAstNo != Util.NONE) {
            int nodeMinNumAstNo = searchAst.nodeMinNumAstNo;
            for (int i = 0; i < numAstMaxI.length; i++) {
                astMatcher.numAstMaxI[nodeMinNumAstNo + i] = numAstMaxI[i];
                astMatcher.numAstCircleNum[nodeMinNumAstNo + i] = numAstCircleNum[i];
            }
        }
    }

}
