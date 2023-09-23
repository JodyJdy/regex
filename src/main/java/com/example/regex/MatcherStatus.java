package com.example.regex;

import java.util.HashMap;
import java.util.Map;

/**
 * 记录当ASTMatcher的状态
 */
class MatcherStatus {

    /**
     * 存储ast牵扯到的所有状态
     */
    private final Map<Ast,AstStatus> astStatusMap = new HashMap<>();

    private final ASTMatcher astMatcher;
    private final int searchStart;
    private final int findResult;
    private final boolean matchMood;

    private final Ast searchAst;

    MatcherStatus(ASTMatcher astMatcher,Ast searchAst){
        this.searchStart = astMatcher.findResultStart;
        this.findResult = astMatcher.result;
        this.matchMood = astMatcher.matchMode;
        //默认值
        astMatcher.findResultStart = astMatcher.result = 0;
        this.astMatcher = astMatcher;
        this.searchAst =searchAst;
        //存储ast的状态
        searchAst.storeStatus(astStatusMap);
    }

    void resumeStatus(){
        astMatcher.findResultStart = this.searchStart;
        astMatcher.result = this.findResult;
        astMatcher.matchMode = this.matchMood;
        this.searchAst.loadStatus(astStatusMap);
    }



    /**
     * 用于存储 Ast牵扯到的所有状态
     */
    static class AstStatus{
        int groupStart;
        int groupEnd;
        int circleNum;
        int maxI;

        AstStatus(Ast ast){
            this.groupStart = ast.groupStart;
            this.groupEnd = ast.groupEnd;
            if(ast instanceof NumAst){
                NumAst numAst = (NumAst)ast;
                this.circleNum = numAst.circleNum;
                this.maxI = numAst.maxI;
                numAst.circleNum = 0;
                numAst.maxI = -1;
            }
        }
    }
}
