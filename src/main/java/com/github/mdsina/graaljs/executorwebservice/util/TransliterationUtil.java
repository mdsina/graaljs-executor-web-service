package com.github.mdsina.graaljs.executorwebservice.util;

import com.ibm.icu.text.Transliterator;

public class TransliterationUtil {

    // Standard "Russian->Latin/BGN" with emptied ьЬъЪ
    private static final String RULES = "::[ЁА-яё];\n" +
        "[аеиоуыэ-яё]{ы} > ·y;\n" +
        "[ЁАЕИОУЫЭ-Я]{[Ыы]} > ·Y;\n" +
        "[[[[:Uppercase:]-[[ЁАЕИОУЫЭ-Я][аеиоуыэ-яё]]][[:Lowercase:]-[[ЁАЕИОУЫЭ-Я][аеиоуыэ-яё]]]]-[Йй]]{Э} > ·E;\n" +
        "[[[[:Uppercase:]-[[ЁАЕИОУЫЭ-Я][аеиоуыэ-яё]]][[:Lowercase:]-[[ЁАЕИОУЫЭ-Я][аеиоуыэ-яё]]]]-[Йй]]{э} > ·e;\n" +
        "[[ЁАЕИОУЫЭ-Я][ЙЪЬ]]{Е}[:Uppercase:] > YE;\n" +
        "[[ЁАЕИОУЫЭ-Я][ЙЪЬ]]{Е} > Ye;\n" +
        "[[ЁАЕИОУЫЭ-Я][аеиоуыэ-яё][ЙЪЬйъь]]{е} > ye;\n" +
        "[[ЁАЕИОУЫЭ-Я][ЙЪЬ]]{Ё}[:Uppercase:] > YË;\n" +
        "[[ЁАЕИОУЫЭ-Я][ЙЪЬ]]{Ё} > Yë;\n" +
        "[[ЁАЕИОУЫЭ-Я][аеиоуыэ-яё][ЙЪЬйъь]]{ё} > yë;\n" +
        "::Null;\n" +
        "А > A;\n" +
        "а > a;\n" +
        "Б > B;\n" +
        "б > b;\n" +
        "В > V;\n" +
        "в > v;\n" +
        "Г > G;\n" +
        "г > g;\n" +
        "Д > D;\n" +
        "д > d;\n" +
        "[^[:L:][:M:][:N:]]{Е}[:Uppercase:] > YE;\n" +
        "[^[:L:][:M:][:N:]]{Е} > Ye;\n" +
        "Е > E;\n" +
        "[^[:L:][:M:][:N:]]{е} > ye;\n" +
        "е > e;\n" +
        "[^[:L:][:M:][:N:]]{Ё}[:Uppercase:] > YË;\n" +
        "[^[:L:][:M:][:N:]]{Ё}[:Lowercase:] > Yë;\n" +
        "Ё > Ë;\n" +
        "[^[:L:][:M:][:N:]]{ё} > yë;\n" +
        "ё > ë;\n" +
        "{Ж}[:Lowercase:] > Zh;\n" +
        "Ж > ZH;\n" +
        "ж > zh;\n" +
        "З > Z;\n" +
        "з > z;\n" +
        "И > I;\n" +
        "и > i;\n" +
        "{Й}[АУЫЭауыэ] > Y·;\n" +
        "{й}[АУЫЭауыэ] > y·;\n" +
        "Й > Y;\n" +
        "й > y;\n" +
        "К > K;\n" +
        "к > k;\n" +
        "Л > L;\n" +
        "л > l;\n" +
        "М > M;\n" +
        "м > m;\n" +
        "Н > N;\n" +
        "н > n;\n" +
        "О > O;\n" +
        "о > o;\n" +
        "П > P;\n" +
        "п > p;\n" +
        "Р > R;\n" +
        "р > r;\n" +
        "С > S;\n" +
        "с > s;\n" +
        "ТС > T·S;\n" +
        "Тс > T·s;\n" +
        "тс > t·s;\n" +
        "Т > T;\n" +
        "т > t;\n" +
        "У > U;\n" +
        "у > u;\n" +
        "Ф > F;\n" +
        "ф > f;\n" +
        "{Х}[:Lowercase:] > Kh;\n" +
        "Х > KH;\n" +
        "х > kh;\n" +
        "{Ц}[:Lowercase:] > Ts;\n" +
        "Ц > TS;\n" +
        "ц > ts;\n" +
        "{Ч}[:Lowercase:] > Ch;\n" +
        "Ч > CH;\n" +
        "ч > ch;\n" +
        "ШЧ > SH·CH;\n" +
        "Шч > Sh·ch;\n" +
        "шч > sh·ch;\n" +
        "{Ш}[:Lowercase:] > Sh;\n" +
        "Ш > SH;\n" +
        "ш > sh;\n" +
        "{Щ}[:Lowercase:] > Shch;\n" +
        "Щ > SHCH;\n" +
        "щ > shch;\n" +
        "Ъ > ;\n" +
        "ъ > ;\n" +
        "{Ы}[АУЫЭауыэ] > Y·;\n" +
        "{ы}[ауыэ] > y·;\n" +
        "Ы > Y;\n" +
        "ы > y;\n" +
        "Ь > ;\n" +
        "ь > ;\n" +
        "Э > E;\n" +
        "э > e;\n" +
        "{Ю}[:Lowercase:] > Yu;\n" +
        "Ю > YU;\n" +
        "ю > yu;\n" +
        "{Я}[:Lowercase:] > Ya;\n" +
        "Я > YA;\n" +
        "я > ya;";

    private static final Transliterator TRANSLITERATOR = Transliterator.createFromRules(
        "ruToLatin", RULES, Transliterator.FORWARD
    );

    public static String transliterate(String russianStr) {
        return TRANSLITERATOR.transliterate(russianStr);
    }
}
