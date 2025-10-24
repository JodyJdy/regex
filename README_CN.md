# regex
Implemented Java Regular Expression and made some extensions
The supported grammar functions are as follows:
* ^
* \*
* \$
* |
* \+
* \? Support non-greedy matching
* []  
* {} Support {n} {n,} {n,m}
* ()  Group Catch
* .
* \b
* \B
* (?=)
* (?<=)
* (?!)
* (?<!)
* (?<Name>) Group naming
* \s \S
* \d \D
* \w \W
* \num  Group reference
* \r \t \f \v \n 
* (?i) (?m) (?d) (?n) (?s)
* \p{}
* \c
* \Q \E \A \z \Z \R 

The exposed used classes are ASTPattern and ASTMatcher. The usage method is as follows:
## 1.Compiler

```java
import com.jody.regex.ASTPattern;

ASTPattern astPattern = ASTPattern.compile("[\u4E00-\u9FA5]+");
ASTMatcher matcher = astPattern.matcher("hello world");
```
## 2. match 
boolean result = matcher.isMatch();

## 3. find() 
```java
boolean re1 = matcher.find();
boolean re2 = matcher.find(i); //Start the search from the i.
boolean re3 = matcher.backwardFind();//Search from the tail end
boolean re4 = matcher.backwardFind(end); //Search with "end" as the ending character.
FindResult result = matcher.getFindResult(); //Obtain the range of the search results
```


## 4. replaceFirst(),repalceAll() 
```java
String str1 = matcher.replaceFirst("你好"); 
String str2 = matcher.replaceAll("你好"); 
```

## 5. group(int), group(name) 
```java
String g = matcher.group(0);
String g1 = matcher.group("group");

```

## todo
More tests