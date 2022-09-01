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

    private ASTMatcher astMatcher;

    private boolean matchStart;
    private boolean matchEnd ;
    private int searchStart;
    private int findResult;
    private boolean matchMood;

    private Ast searchAst;

    MatcherStatus(ASTMatcher astMatcher,Ast searchAst){
        this.matchStart = astMatcher.matchStart;
        this.matchEnd = astMatcher.matchEnd;
        this.searchStart = astMatcher.searchStart;
        this.findResult = astMatcher.result;
        this.matchMood = astMatcher.matchMode;
        //默认值
        astMatcher.searchStart = astMatcher.result = 0;
        this.astMatcher = astMatcher;
        this.searchAst =searchAst;
        //存储ast的状态
        searchAst.storeStatus(astStatusMap);
    }

    void resumeStatus(){
        astMatcher.matchStart = this.matchStart;
        astMatcher.matchEnd = this.matchEnd;
        astMatcher.searchStart = this.searchStart;
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
