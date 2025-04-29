package com.jody.regex;

import java.util.Arrays;

class NumAstStatus {
    final Ast searchAst;
    final ASTMatcher matcher;
    int[] numAstCircleNum;

    NumAstStatus(ASTMatcher astMatcher, Ast searchAst) {
        this.searchAst = searchAst;
        this.matcher = astMatcher;
        int len = searchAst.nodeMaxNumAstNo - searchAst.nodeMinNumAstNo + 1;
        numAstCircleNum = new int[len];
        int nodeMinNumAstNo = searchAst.nodeMinNumAstNo;
        System.arraycopy(astMatcher.numAstCircleNum, nodeMinNumAstNo, numAstCircleNum, 0, len);
        Arrays.fill(astMatcher.numAstCircleNum, nodeMinNumAstNo, nodeMinNumAstNo + len, 0);
    }

    void resumeStatus() {
        // 状态还原
        int nodeMinNumAstNo = searchAst.nodeMinNumAstNo;
        System.arraycopy(numAstCircleNum, 0, matcher.numAstCircleNum, nodeMinNumAstNo, numAstCircleNum.length);
    }
}
