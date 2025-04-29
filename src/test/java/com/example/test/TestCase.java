package com.example.test;

public class TestCase {

    public static String[] regexs = {
            /**
            M/d/y Date*
            */
            "^(?:(?:(?:0?[13578]|1[02])(\\/|-|\\.)31)\\1|(?:(?:0?[13-9]|1[0-2])(\\/|-|\\.)(?:29|30)\\2))(?:(?:1[6-9]|[2-9]\\d)?\\d{2})$|^(?:0?2(\\/|-|\\.)29\\3(?:(?:(?:1[6-9]|[2-9]\\d)?(?:0[48]|[2468][048]|[13579][26])|(?:(?:16|[2468][048]|[3579][26])00))))$|^(?:(?:0?[1-9])|(?:1[0-2]))(\\/|-|\\.)(?:0?[1-9]|1\\d|2[0-8])\\4(?:(?:1[6-9]|[2-9]\\d)?\\d{2})$",
            /**
            US currency*
            */
            "^\\$(\\d{1,3}(\\,\\d{3})*|(\\d+))(\\.\\d{2})?$",
            /**
            Roman numerials*
            */
            "(?i:(?=[MDCLXVI])((M{0,3})((C[DM])|(D?C{0,3}))?((X[LC])|(L?XX{0,2})|L)?((I[VX])|(V?(II{0,2}))|V)?))$",
            /**
            Text Extension*
            */
            "^(([a-zA-Z]:)|(\\\\{2}\\w+)\\$?)(\\\\(\\w[\\w ]*))+\\.(txt|TXT)$",
            /**
            yy/mm/dd Date*
            */
            "^(?:(?:(?:(?:(?:1[6-9]|[2-9]\\d)?(?:0[48]|[2468][048]|[13579][26])|(?:(?:16|[2468][048]|[3579][26])00)))(\\/|-|\\.)(?:0?2\\1(?:29)))|(?:(?:(?:1[6-9]|[2-9]\\d)?\\d{2})(\\/|-|\\.)(?:(?:(?:0?[13578]|1[02])\\2(?:31))|(?:(?:0?[1,3-9]|1[0-2])\\2(29|30))|(?:(?:0?[1-9])|(?:1[0-2]))\\2(?:0?[1-9]|1\\d|2[0-8]))))$",
            /**
            TestDetailsDateTime M/d/y hh:mm:ss*
            */
            "^(?=\\d)(?:(?:(?:(?:(?:0?[13578]|1[02])(\\/|-|\\.)31)\\1|(?:(?:0?[1,3-9]|1[0-2])(\\/|-|\\.)(?:29|30)\\2))(?:(?:1[6-9]|[2-9]\\d)?\\d{2})|(?:0?2(\\/|-|\\.)29\\3(?:(?:(?:1[6-9]|[2-9]\\d)?(?:0[48]|[2468][048]|[13579][26])|(?:(?:16|[2468][048]|[3579][26])00))))|(?:(?:0?[1-9])|(?:1[0-2]))(\\/|-|\\.)(?:0?[1-9]|1\\d|2[0-8])\\4(?:(?:1[6-9]|[2-9]\\d)?\\d{2}))($|\\ (?=\\d)))?(((0?[1-9]|1[012])(:[0-5]\\d){0,2}(\\ [AP]M))|([01]\\d|2[0-3])(:[0-5]\\d){1,2})?$",
            /**
             * Time
            */
            "^((0?[1-9]|1[012])(:[0-5]\\d){0,2}(\\ [AP]M))$|^([01]\\d|2[0-3])(:[0-5]\\d){0,2}$",
            /**
             * Days of the week
            */
            "^(Sun|Mon|(T(ues|hurs))|Fri)(day|\\.)?$|Wed(\\.|nesday)?$|Sat(\\.|urday)?$|T((ue?)|(hu?r?))\\.?$",
            /**
             * MMM dd, yyyy Date
            */
            "^(?:(((Jan(uary)?|Ma(r(ch)?|y)|Jul(y)?|Aug(ust)?|Oct(ober)?|Dec(ember)?)\\ 31)|((Jan(uary)?|Ma(r(ch)?|y)|Apr(il)?|Ju((ly?)|(ne?))|Aug(ust)?|Oct(ober)?|(Sept|Nov|Dec)(ember)?)\\ (0?[1-9]|([12]\\d)|30))|(Feb(ruary)?\\ (0?[1-9]|1\\d|2[0-8]|(29(?=,\\ ((1[6-9]|[2-9]\\d)(0[48]|[2468][048]|[13579][26])|((16|[2468][048]|[3579][26])00)))))))\\,\\ ((1[6-9]|[2-9]\\d)\\d{2}))",
            /**
             *Months
            */
            "^(?:J(anuary|u(ne|ly))|February|Ma(rch|y)|A(pril|ugust)|(((Sept|Nov|Dec)em)|Octo)ber)$",
            /**
             * xml tags
            */
           "<(\\w+)(\\s(\\w*=\".*?\")?)*((/>)|((/*?)>.*?</\\1>))",

            /**
             * ISBN 10
            */
            "ISBN\\x20(?=.{13}$)\\d{1,5}([- ])\\d{1,7}\\1\\d{1,6}\\1(\\d|X)$" ,
            /**
             * State Codes
            */
            "^(?-i:A[LKSZRAEP]|C[AOT]|D[EC]|F[LM]|G[AU]|HI|I[ADLN]|K[SY]|LA|M[ADEHINOPST]|N[CDEHJMVY]|O[HKR]|P[ARW]|RI|S[CD]|T[NX]|UT|V[AIT]|W[AIVY])$",
            /**
             * lastname
            */
            "^(?:(?<lastname>(St\\.\\ )?(?-i:[A-Z]\\'?\\w+?\\-?)+)(?<suffix>\\ (?i:([JS]R)|((X(X{1,2})?)?((I((I{1,2})|V|X)?)|(V(I{0,3})))?)))?,((?<prefix>Dr|Prof|M(r?|(is)?)s)\\ )?(?<firstname>(?-i:[A-Z]\\'?(\\w+?|\\.)\\ ??){1,2})?(\\ (?<mname>(?-i:[A-Z])(\\'?\\w+?|\\.))){0,2})$",
            /**
             *HTML 4.01 Elements
            */
            "(<\\/?)(?i:(?<element>a(bbr|cronym|ddress|pplet|rea)?|b(ase(font)?|do|ig|lockquote|ody|r|utton)?|c(aption|enter|ite|(o(de|l(group)?)))|d(d|el|fn|i(r|v)|l|t)|em|f(ieldset|o(nt|rm)|rame(set)?)|h([1-6]|ead|r|tml)|i(frame|mg|n(put|s)|sindex)?|kbd|l(abel|egend|i(nk)?)|m(ap|e(nu|ta))|no(frames|script)|o(bject|l|pt(group|ion))|p(aram|re)?|q|s(amp|cript|elect|mall|pan|t(r(ike|ong)|yle)|u(b|p))|t(able|body|d|extarea|foot|h|itle|r|t)|u(l)?|var))(\\s(?<attr>.+?))*>",
            /**
             *HTML Click Events
            */
            "(?i:on(blur|c(hange|lick)|dblclick|focus|keypress|(key|mouse)(down|up)|(un)?load|mouse(move|o(ut|ver))|reset|s(elect|ubmit)))",
            /**
             * Social Security Number
            */
            "^(?!000)([0-6]\\d{2}|7([0-6]\\d|7[012]))([ -]?)(?!00)\\d\\d\\3(?!0000)\\d{4}$",
            /**
             * abcd 不重复
            */
            "(?i:([A-D])(?!\\1)([A-D])(?!\\1|\\2)([A-D])(?!\\1|\\2|\\3)([A-D]))",
            /**
             * 密码
            */
            "^(?=[^\\d_].*?\\d)\\w(\\w|[!@#$%]){7,20}",
            /**
             *日期格式
            */
            "^(?=\\d)(?:(?:31(?!.(?:0?[2469]|11))|(?:30|29)(?!.0?2)|29(?=.0?2.(?:(?:(?:1[6-9]|[2-9]\\d)?(?:0[48]|[2468][048]|[13579][26])|(?:(?:16|[2468][048]|[3579][26])00)))(?:\\x20|$))|(?:2[0-8]|1\\d|0?[1-9]))([-./])(?:1[012]|0?[1-9])\\1(?:1[6-9]|[2-9]\\d)?\\d\\d(?:(?=\\x20\\d)\\x20|$))?(((0?[1-9]|1[012])(:[0-5]\\d){0,2}(\\x20[AP]M))|([01]\\d|2[0-3])(:[0-5]\\d){1,2})?$",






    };
        /**
         * 测试数据
         */
        public static String[][] tests = {
                new String[]{"01.1.02", "11-30-2001", "2/29/2000"},
                new String[]{"$1,234,567.89", "$123458", "$0.84"},
                new String[]{"xiv", "MCMXCIX", "III",},
                new String[]{"c:\\folder\\sub folder\\file.txt", "c:\\file.txt", "\\\\network\\folder\\file.txt"},
                new String[]{"2002-4-30", "04/2/29", "04/2/29"},
                new String[]{"12/25/2003", "08:03:31", "02/29/2004 12 AM"},
                new String[]{"1 AM", "23:00:00", "5:29:59 PM"},
                new String[]{"Sunday", "Mon", "Tu"},
                new String[]{"Jan 1, 2003", "February 29, 2004", "November 02, 3202"},
                new String[]{"January", "May", "October"},
                new String[]{"<body> text<br/>More Text </body>"},
                new String[]{"ISBN 1-56389-016-X", "ISBN 0 93028 923 4"},
                new String[]{"AA", "AL", "CA"},
                new String[]{"O'Brien, Miles", "McDonald,Mary Ann Alison", "Windsor-Smith,Barry"},
                new String[]{"<HTML>", "<a href=\"link.html\">Link</a>"},
                new String[]{"onclick", "onsubmit", "onmouseover"},
                new String[]{"078-05-1120", "078 05 1120", "078051120"},
                new String[]{"abcd", "dbca", "badc"},
                new String[]{"password1", "pa$$word2", "pa$$word2"},
                new String[]{"31/12/2003 11:59:59 PM", "29-2-2004", "01:45:02"}

        };


}
