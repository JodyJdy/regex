# regex
实现Java Regular Expression，并进行了一些扩展
支持的语法功能如下：
* ^
* \*
* \$
* |
* \+
* \? 支持非贪婪匹配
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
* (?<名称>) 分组命名
* \s \S
* \d \D
* \w \W
* \num  分组引用
* \r \t \f \v \n 
* (?i) (?m) (?d) (?n) (?s)
* \p{}
* \c

暴漏出的使用的类为ASTPattern,ASTMatcher，使用方式为：
## 1.编译正则表达式

```java
import com.jody.regex.ASTPattern;

ASTPattern astPattern = ASTPattern.compile("[\u4E00-\u9FA5]+");
ASTMatcher matcher = astPattern.matcher("你好，世界");
```
## 2. match 匹配
boolean result = matcher.isMatch();

## 3. find() 查找
```java
boolean re1 = matcher.find();
boolean re2 = matcher.find(i); //从下标i开始查找
boolean re3 = matcher.backwardFind();//从尾部开始查找
boolean re4 = matcher.backwardFind(end); //以下标end作为尾部，进行查找
FindResult result = matcher.getFindResult(); /获取查找结果的区间
```


## 4. replaceFirst(),repalceAll() 替换
```java
String str1 = matcher.replaceFirst("你好"); //将第一个出现的进行替换
String str2 = matcher.replaceAll("你好");  // 将所有出现的进行替换
```

## 5. group(int), group(name) 获取分组
```java
String g = matcher.group(0);
String g1 = matcher.group("group");

```

## todo
[[]&&[]]