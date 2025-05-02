package com.jody.regex;


import java.util.Locale;

public class CharPredicates {


    /**
     *  the dot metacharacter when dotall is not enabled but UNIX_LINES is enabled.
     */
    static CharPredicate UNIXDOT() {
        return ch ->  ch != '\n';
    }

    static CharPredicate ALL() {
        return ch -> true;
    }

    /**
     * for the dot metacharacter when dotall is not enabled.
     */
    static CharPredicate DOT() {
        return ch ->
                (ch != '\n' && ch != '\r'
                        && (ch|1) != '\u2029'
                        && ch != '\u0085');
    }

    static CharPredicate VertWS() {
        return cp -> (cp >= 0x0A && cp <= 0x0D) ||
                cp == 0x85 || cp == 0x2028 || cp == 0x2029;
    }

    /**
     * matches a Perl horizontal whitespace
     */
    static CharPredicate HorizWS() {
        return cp ->
                cp == 0x09 || cp == 0x20 || cp == 0xa0 || cp == 0x1680 ||
                        cp == 0x180e || cp >= 0x2000 && cp <= 0x200a ||  cp == 0x202f ||
                        cp == 0x205f || cp == 0x3000;
    }

    static CharPredicate ASCII_DIGIT() {
        return ch -> ch < 128 && ASCII.isDigit(ch);
    }
    static CharPredicate ASCII_WORD() {
        return ch -> ch < 128 && ASCII.isWord(ch);
    }
    static CharPredicate ASCII_SPACE() {
        return ch -> ch < 128 && ASCII.isSpace(ch);
    }


    static CharPredicate ALPHABETIC() {
        return Character::isAlphabetic;
    }

    // \p{gc=Decimal_Number}
    static CharPredicate DIGIT() {
        return Character::isDigit;
    }

    static CharPredicate LETTER() {
        return Character::isLetter;
    }

    static CharPredicate IDEOGRAPHIC() {
        return Character::isIdeographic;
    }

    static CharPredicate LOWERCASE() {
        return Character::isLowerCase;
    }

    static CharPredicate UPPERCASE() {
        return Character::isUpperCase;
    }

    static CharPredicate TITLECASE() {
        return Character::isTitleCase;
    }

    // \p{Whitespace}
    static CharPredicate WHITE_SPACE() {
        return ch ->
                ((((1 << Character.SPACE_SEPARATOR) |
                        (1 << Character.LINE_SEPARATOR) |
                        (1 << Character.PARAGRAPH_SEPARATOR)) >> Character.getType(ch)) & 1)
                        != 0 || (ch >= 0x9 && ch <= 0xd) || (ch == 0x85);
    }

    // \p{gc=Control}
    static CharPredicate CONTROL() {
        return ch -> Character.getType(ch) == Character.CONTROL;
    }

    // \p{gc=Punctuation}
    static CharPredicate PUNCTUATION() {
        return ch ->
                ((((1 << Character.CONNECTOR_PUNCTUATION) |
                        (1 << Character.DASH_PUNCTUATION) |
                        (1 << Character.START_PUNCTUATION) |
                        (1 << Character.END_PUNCTUATION) |
                        (1 << Character.OTHER_PUNCTUATION) |
                        (1 << Character.INITIAL_QUOTE_PUNCTUATION) |
                        (1 << Character.FINAL_QUOTE_PUNCTUATION)) >> Character.getType(ch)) & 1)
                        != 0;
    }

    // \p{gc=Decimal_Number}
    // \p{Hex_Digit}    -> PropList.txt: Hex_Digit
    static CharPredicate HEX_DIGIT() {
        return ch -> DIGIT().isCh(ch) || ((ch >= 0x0030 && ch <= 0x0039) ||
                (ch >= 0x0041 && ch <= 0x0046) ||
                (ch >= 0x0061 && ch <= 0x0066) ||
                (ch >= 0xFF10 && ch <= 0xFF19) ||
                (ch >= 0xFF21 && ch <= 0xFF26) ||
                (ch >= 0xFF41 && ch <= 0xFF46));
    }

    static CharPredicate ASSIGNED() {
        return ch -> Character.getType(ch) != Character.UNASSIGNED;
    }

    // PropList.txt:Noncharacter_Code_Point
    static CharPredicate NONCHARACTER_CODE_POINT() {
        return ch -> (ch & 0xfffe) == 0xfffe || (ch >= 0xfdd0 && ch <= 0xfdef);
    }

    // \p{alpha}
    // \p{digit}
    static CharPredicate ALNUM() {
        return ch -> ALPHABETIC().isCh(ch) || DIGIT().isCh(ch);
    }

    // \p{Whitespace} --
    // [\N{LF} \N{VT} \N{FF} \N{CR} \N{NEL}  -> 0xa, 0xb, 0xc, 0xd, 0x85
    //  \p{gc=Line_Separator}
    //  \p{gc=Paragraph_Separator}]
    static CharPredicate BLANK() {
        return ch ->
                Character.getType(ch) == Character.SPACE_SEPARATOR ||
                        ch == 0x9; // \N{HT}
    }

    // [^
    //  \p{space}
    //  \p{gc=Control}
    //  \p{gc=Surrogate}
    //  \p{gc=Unassigned}]
    static CharPredicate GRAPH() {
        return ch ->
                ((((1 << Character.SPACE_SEPARATOR) |
                        (1 << Character.LINE_SEPARATOR) |
                        (1 << Character.PARAGRAPH_SEPARATOR) |
                        (1 << Character.CONTROL) |
                        (1 << Character.SURROGATE) |
                        (1 << Character.UNASSIGNED)) >> Character.getType(ch)) & 1)
                        == 0;
    }

    // \p{graph}
    // \p{blank}
    // -- \p{cntrl}
    static CharPredicate PRINT() {
        return ch -> (GRAPH().isCh(ch) || BLANK().isCh(ch)) && (!CONTROL().isCh(ch));
    }

    //  200C..200D    PropList.txt:Join_Control
    static CharPredicate JOIN_CONTROL() {
        return ch -> ch == 0x200C || ch == 0x200D;
    }

    //  \p{alpha}
    //  \p{gc=Mark}
    //  \p{digit}
    //  \p{gc=Connector_Punctuation}
    //  \p{Join_Control}    200C..200D
    static CharPredicate WORD() {
        return ch -> ALPHABETIC().isCh(ch) || (((((1 << Character.NON_SPACING_MARK) |
                (1 << Character.ENCLOSING_MARK) |
                (1 << Character.COMBINING_SPACING_MARK) |
                (1 << Character.DECIMAL_DIGIT_NUMBER) |
                (1 << Character.CONNECTOR_PUNCTUATION))
                >> Character.getType(ch)) & 1) != 0) || JOIN_CONTROL().isCh(ch);
    }


    public static class EmptyCharPredicate implements CharPredicate{

        @Override
        public boolean isCh(char ch) {
            throw new RuntimeException("空表达式");
        }
        @Override
        public CharPredicate and(CharPredicate p) {
            return p;
        }

        @Override
        public CharPredicate union(CharPredicate p) {
            return p;
        }

        @Override
        public CharPredicate union(CharPredicate p1, CharPredicate p2) {
            return p1.union(p2);
        }

        @Override
        public CharPredicate negate() {
            throw new RuntimeException("空表达式不支持该操作");
        }
    }

    public interface CharPredicate {
        boolean isCh(char ch);

        default CharPredicate and(CharPredicate p) {
            return CharPredicates.and(this, p);
        }

        default CharPredicate union(CharPredicate p) {
            return CharPredicates.union(this, p);
        }

        default CharPredicate union(CharPredicate p1,
                                    CharPredicate p2) {
            return CharPredicates.union(this, p1, p2);
        }

        default CharPredicate negate() {
            return CharPredicates.negate(this);
        }

    }
        private static CharPredicate category(final int typeMask) {
            return ch -> (typeMask & (1 << Character.getType(ch))) != 0;
        }

        // unicode categories, aliases, properties, java methods ...
        static CharPredicate forProperty(String name, boolean caseIns) {
            CharPredicate result = null;
            switch (name) {
                case "Cn":
                    result = category(1 << Character.UNASSIGNED);
                    break;
                case "Lu":
                    result = category(caseIns ? (1 << Character.LOWERCASE_LETTER) |
                            (1 << Character.UPPERCASE_LETTER) |
                            (1 << Character.TITLECASE_LETTER)
                            : (1 << Character.UPPERCASE_LETTER));
                    break;
                case "Ll":
                    result = category(caseIns ? (1 << Character.LOWERCASE_LETTER) |
                            (1 << Character.UPPERCASE_LETTER) |
                            (1 << Character.TITLECASE_LETTER)
                            : (1 << Character.LOWERCASE_LETTER));
                    break;
                case "Lt":
                    result = category(caseIns ? (1 << Character.LOWERCASE_LETTER) |
                            (1 << Character.UPPERCASE_LETTER) |
                            (1 << Character.TITLECASE_LETTER)
                            : (1 << Character.TITLECASE_LETTER));
                    break;
                case "Lm":
                    result = category(1 << Character.MODIFIER_LETTER);
                    break;
                case "Lo":
                    result = category(1 << Character.OTHER_LETTER);
                    break;
                case "Mn":
                    result = category(1 << Character.NON_SPACING_MARK);
                    break;
                case "Me":
                    result = category(1 << Character.ENCLOSING_MARK);
                    break;
                case "Mc":
                    result = category(1 << Character.COMBINING_SPACING_MARK);
                    break;
                case "Nd":
                    result = category(1 << Character.DECIMAL_DIGIT_NUMBER);
                    break;
                case "Nl":
                    result = category(1 << Character.LETTER_NUMBER);
                    break;
                case "No":
                    result = category(1 << Character.OTHER_NUMBER);
                    break;
                case "Zs":
                    result = category(1 << Character.SPACE_SEPARATOR);
                    break;
                case "Zl":
                    result = category(1 << Character.LINE_SEPARATOR);
                    break;
                case "Zp":
                    result = category(1 << Character.PARAGRAPH_SEPARATOR);
                    break;
                case "Cc":
                    result = category(1 << Character.CONTROL);
                    break;
                case "Cf":
                    result = category(1 << Character.FORMAT);
                    break;
                case "Co":
                    result = category(1 << Character.PRIVATE_USE);
                    break;
                case "Cs":
                    result = category(1 << Character.SURROGATE);
                    break;
                case "Pd":
                    result = category(1 << Character.DASH_PUNCTUATION);
                    break;
                case "Ps":
                    result = category(1 << Character.START_PUNCTUATION);
                    break;
                case "Pe":
                    result = category(1 << Character.END_PUNCTUATION);
                    break;
                case "Pc":
                    result = category(1 << Character.CONNECTOR_PUNCTUATION);
                    break;
                case "Po":
                    result = category(1 << Character.OTHER_PUNCTUATION);
                    break;
                case "Sm":
                    result = category(1 << Character.MATH_SYMBOL);
                    break;
                case "Sc":
                    result = category(1 << Character.CURRENCY_SYMBOL);
                    break;
                case "Sk":
                    result = category(1 << Character.MODIFIER_SYMBOL);
                    break;
                case "So":
                    result = category(1 << Character.OTHER_SYMBOL);
                    break;
                case "Pi":
                    result = category(1 << Character.INITIAL_QUOTE_PUNCTUATION);
                    break;
                case "Pf":
                    result = category(1 << Character.FINAL_QUOTE_PUNCTUATION);
                    break;
                case "L":
                    result = category(((1 << Character.UPPERCASE_LETTER) |
                            (1 << Character.LOWERCASE_LETTER) |
                            (1 << Character.TITLECASE_LETTER) |
                            (1 << Character.MODIFIER_LETTER) |
                            (1 << Character.OTHER_LETTER)));
                    break;
                case "M":
                    result = category(((1 << Character.NON_SPACING_MARK) |
                            (1 << Character.ENCLOSING_MARK) |
                            (1 << Character.COMBINING_SPACING_MARK)));
                    break;
                case "N":
                    result = category(((1 << Character.DECIMAL_DIGIT_NUMBER) |
                            (1 << Character.LETTER_NUMBER) |
                            (1 << Character.OTHER_NUMBER)));
                    break;
                case "Z":
                    result = category(((1 << Character.SPACE_SEPARATOR) |
                            (1 << Character.LINE_SEPARATOR) |
                            (1 << Character.PARAGRAPH_SEPARATOR)));
                    break;
                case "C":
                    result = category(((1 << Character.CONTROL) |
                            (1 << Character.FORMAT) |
                            (1 << Character.PRIVATE_USE) |
                            (1 << Character.SURROGATE) |
                            (1 << Character.UNASSIGNED)));
                    break; // Other
                case "P":
                    result = category(((1 << Character.DASH_PUNCTUATION) |
                            (1 << Character.START_PUNCTUATION) |
                            (1 << Character.END_PUNCTUATION) |
                            (1 << Character.CONNECTOR_PUNCTUATION) |
                            (1 << Character.OTHER_PUNCTUATION) |
                            (1 << Character.INITIAL_QUOTE_PUNCTUATION) |
                            (1 << Character.FINAL_QUOTE_PUNCTUATION)));
                    break;
                case "S":
                    result = category(((1 << Character.MATH_SYMBOL) |
                            (1 << Character.CURRENCY_SYMBOL) |
                            (1 << Character.MODIFIER_SYMBOL) |
                            (1 << Character.OTHER_SYMBOL)));
                    break;
                case "LC":
                    result = category(((1 << Character.UPPERCASE_LETTER) |
                            (1 << Character.LOWERCASE_LETTER) |
                            (1 << Character.TITLECASE_LETTER)));
                    break;
                case "LD":
                    result = category(((1 << Character.UPPERCASE_LETTER) |
                            (1 << Character.LOWERCASE_LETTER) |
                            (1 << Character.TITLECASE_LETTER) |
                            (1 << Character.MODIFIER_LETTER) |
                            (1 << Character.OTHER_LETTER) |
                            (1 << Character.DECIMAL_DIGIT_NUMBER)));
                    break;
                case "L1":
                    result = range(0x00, 0xFF);
                    break; // Latin-1
                case "all":
                    result = (ch) -> true;
                    break;
                // Posix regular expression character classes, defined in
                // http://www.unix.org/onlinepubs/009695399/basedefs/xbd_chap09.html
                case "ASCII":
                    result = range(0x00, 0x7F);
                    break;    // ASCII
                case "Alnum":
                    result = ctype(ASCII.ALNUM);
                    break;   // Alphanumeric characters
                case "Alpha":
                    result = ctype(ASCII.ALPHA);
                    break;   // Alphabetic characters
                case "Blank":
                    result = ctype(ASCII.BLANK);
                    break;   // Space and tab characters
                case "Cntrl":
                    result = ctype(ASCII.CNTRL);
                    break;   // Control characters
                case "Digit":
                    result = range('0', '9');      // Numeric characters
                    break;
                case "Graph":
                    result = ctype(ASCII.GRAPH);
                    break;   // printable and visible
                case "Lower":
                    result = caseIns ? ctype(ASCII.ALPHA)
                            : range('a', 'z');
                    break; // Lower-case alphabetic
                case "Print":
                    result = range(0x20, 0x7E);
                    break;    // Printable characters
                case "Punct":
                    result = ctype(ASCII.PUNCT);
                    break;   // Punctuation characters
                case "Space":
                    result = ctype(ASCII.SPACE);
                    break;   // Space characters
                case "Upper":
                    result = caseIns ? ctype(ASCII.ALPHA)
                            : range('A', 'Z');
                    break; // Upper-case alphabetic
                case "XDigit":
                    result = ctype(ASCII.XDIGIT);
                    break; // hexadecimal digits

                // Java character properties, defined by methods in Character.java
                case "javaLowerCase":
                    result = caseIns ? c -> Character.isLowerCase(c) ||
                            Character.isUpperCase(c) ||
                            Character.isTitleCase(c)
                            : Character::isLowerCase;
                    break;
                case "javaUpperCase":
                    result = caseIns ? c -> Character.isUpperCase(c) ||
                            Character.isLowerCase(c) ||
                            Character.isTitleCase(c)
                            : Character::isUpperCase;
                    break;
                case "javaAlphabetic":
                    result = Character::isAlphabetic;
                    break;
                case "javaIdeographic":
                    result = Character::isIdeographic;
                    break;
                case "javaTitleCase":
                    result = caseIns ? c -> Character.isTitleCase(c) ||
                            Character.isLowerCase(c) ||
                            Character.isUpperCase(c)
                            : Character::isTitleCase;
                    break;
                case "javaDigit":
                    result = Character::isDigit;
                    break;
                case "javaDefined":
                    result = Character::isDefined;
                    break;
                case "javaLetter":
                    result = Character::isLetter;
                    break;
                case "javaLetterOrDigit":
                    result = Character::isLetterOrDigit;
                    break;
                case "javaJavaIdentifierStart":
                    result = Character::isJavaIdentifierStart;
                    break;
                case "javaJavaIdentifierPart":
                    result = Character::isJavaIdentifierPart;
                    break;
                case "javaUnicodeIdentifierStart":
                    result = Character::isUnicodeIdentifierStart;
                    break;
                case "javaUnicodeIdentifierPart":
                    result = Character::isUnicodeIdentifierPart;
                    break;
                case "javaIdentifierIgnorable":
                    result = Character::isIdentifierIgnorable;
                    break;
                case "javaSpaceChar":
                    result = Character::isSpaceChar;
                    break;
                case "javaWhitespace":
                    result = Character::isWhitespace;
                    break;
                case "javaISOControl":
                    result = Character::isISOControl;
                    break;
                case "javaMirrored":
                    result = Character::isMirrored;
                    break;
                default:
            }
            ;
            return result;
        }

        private static CharPredicate getPosixPredicate(String name, boolean caseIns) {
            CharPredicate result;
            switch (name) {
                case "ALPHA":
                    result = ALPHABETIC();
                    break;
                case "LOWER":
                    result = caseIns
                            ? union(LOWERCASE() , UPPERCASE(),  TITLECASE())
                            : LOWERCASE();
                    break;
                case "UPPER":
                    result = caseIns
                            ? union(UPPERCASE() , LOWERCASE() , TITLECASE())
                            : UPPERCASE();
                    break;
                case "SPACE":
                    result = WHITE_SPACE();
                    break;
                case "PUNCT":
                    result = PUNCTUATION();
                    break;
                case "XDIGIT":
                    result = HEX_DIGIT();
                    break;
                case "ALNUM":
                    result = ALNUM();
                    break;
                case "CNTRL":
                    result = CONTROL();
                    break;
                case "DIGIT":
                    result = DIGIT();
                    break;
                case "BLANK":
                    result = BLANK();
                    break;
                case "GRAPH":
                    result = GRAPH();
                    break;
                case "PRINT":
                    result = PRINT();
                    break;
                default:
                    result = null;
                    break;
            }
            ;
            return result;
        }

        private static CharPredicate getUnicodePredicate(String name, boolean caseIns) {
            CharPredicate result = null;
            switch (name) {
                case "ALPHABETIC":
                    result = ALPHABETIC();
                    break;
                case "ASSIGNED":
                    result = ASSIGNED();
                    break;
                case "CONTROL":
                    result = CONTROL();
                    break;
                case "HEXDIGIT":
                case "HEX_DIGIT":
                    result = HEX_DIGIT();
                    break;
                case "IDEOGRAPHIC":
                    result = IDEOGRAPHIC();
                    break;
                case "JOINCONTROL":
                case "JOIN_CONTROL":
                    result = JOIN_CONTROL();
                    break;
                case "LETTER":
                    result = LETTER();
                    break;
                case "LOWERCASE":
                    result = caseIns
                            ? union(LOWERCASE(), UPPERCASE() , TITLECASE())
                            : LOWERCASE();
                    break;
                case "NONCHARACTERCODEPOINT":
                case "NONCHARACTER_CODE_POINT":
                    result = NONCHARACTER_CODE_POINT();
                    break;
                case "TITLECASE":
                    result = caseIns
                            ? union(TITLECASE() , LOWERCASE() , UPPERCASE())
                            : TITLECASE();
                    break;
                case "PUNCTUATION":
                    result = PUNCTUATION();
                    break;
                case "UPPERCASE":
                    result = caseIns
                            ? union(UPPERCASE() , LOWERCASE(),  TITLECASE())
                            : UPPERCASE();
                    break;
                case "WHITESPACE":
                case "WHITE_SPACE":
                    result = WHITE_SPACE();
                    break;
                case "WORD":
                    result = WORD();
                    break;
            }
            ;
            return result;
        }

        public static CharPredicate forUnicodeProperty(String propName, boolean caseIns) {
            propName = propName.toUpperCase(Locale.ROOT);
            CharPredicate p = getUnicodePredicate(propName, caseIns);
            if (p != null)
                return p;
            return getPosixPredicate(propName, caseIns);
        }

        public static CharPredicate forPOSIXName(String propName, boolean caseIns) {
            return getPosixPredicate(propName.toUpperCase(Locale.ENGLISH), caseIns);
        }

        /////////////////////////////////////////////////////////////////////////////

        /**
         * Returns a predicate matching all characters belong to a named
         * UnicodeScript.
         */
        static CharPredicate forUnicodeScript(String name) {
            final Character.UnicodeScript script;
            try {
                script = Character.UnicodeScript.forName(name);
                return ch -> script == Character.UnicodeScript.of(ch);
            } catch (IllegalArgumentException iae) {
            }
            return null;
        }

        /**
         * Returns a predicate matching all characters in a UnicodeBlock.
         */
        static CharPredicate forUnicodeBlock(String name) {
            final Character.UnicodeBlock block;
            try {
                block = Character.UnicodeBlock.forName(name);
                return ch -> block == Character.UnicodeBlock.of(ch);
            } catch (IllegalArgumentException iae) {
            }
            return null;
        }

        static CharPredicate ctype(final int ctype) {
            return ch -> ch < 128 && ASCII.isType(ch, ctype);
        }

        static CharPredicate range(final int lower, final int upper) {
            return ch -> lower <= ch && ch <= upper;
        }

        static CharPredicate union(CharPredicate left, CharPredicate right) {
            return ch -> (left.isCh(ch) || right.isCh(ch));
        }

    static CharPredicate union(CharPredicate left,CharPredicate middle, CharPredicate right) {
        return ch -> (left.isCh(ch)|| middle.isCh(ch) || right.isCh(ch));
    }

        static CharPredicate and(CharPredicate left, CharPredicate right) {
            return ch -> (left.isCh(ch) && right.isCh(ch));
        }

        static CharPredicate negate(CharPredicate predicate) {
            return ch -> !predicate.isCh(ch);
        }
}
