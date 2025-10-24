# regex
Implemented Java Regular Expression and made some extensions
The supported grammar functions are as follows

**support regex grammar**

reference: [Pattern](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html)

**not support java regex grammar**

* \G The end of the previous match
 
**extensions:**
* \g  support simple recursive regex




## Usage
**The exposed used classes are ASTPattern and ASTMatcher. The usage method is as follows:**
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
## 6. recursive
```java
ASTPattern pattern = ASTPattern.compile("x|a\\g*b");
ASTMatcher matcher = pattern.matcher("axxxxxxxxxxxxxxb");
System.out.println(matcher.isMatch());
```

## todo
More tests