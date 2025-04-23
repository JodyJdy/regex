# regex
实现Java Regular Expression，并进行了一些扩展
支持的语法功能如下：
* ^
* *
* $
* |
* +
* ? 支持非贪婪匹配
* []  
* {} 包含 {n} {n,} {n,m}
* ()  分组
* .
* \b
* \B
* (?=)
* (?<=)
* (?!)
* (?<!)
* \s \S
* \d \D
* \w \W
* \num  分组引用
* \g<num> 表达式引用   0表示表达式本身，所以\g<0>是递归匹配， \g<0>? 是非贪婪递归匹配

暴漏出的使用的类为ASTMatcher，使用方式为：
## 1.编译正则表达式
ASTMatcher matcher = ASTMatcher.compile("[\u4E00-\u9FA5]");

## 2. match 匹配
boolean result = matcher.isMatch("你好，世界");

## 3. find() 查找
boolean re1 = matcher.find("hello");
boolean re2 = matcher.find("hello",i); //从下标i开始查找
boolean re3 = matcher.backwardFind("hello");//从尾部开始查找
boolean re4 = matcher.backwardFind("hello",end); //以下标end作为尾部，进行查找
FindResult result = matcher.getFindResult(); /获取查找结果的区间
特殊情况： 对于 \g<0>? 递归非贪婪匹配，会返回多个查找区间，使用
List<FindResult> resultList = matcher.getRecursiveNoGreedyFindResult();

## 4. replaceFirst(),repalceAll() 替换

String str1 = matcher.replaceFirst("你好"); //将第一个出现的进行替换
String str2 = matcher.replaceAll("你好");  // 将所有出现的进行替换
