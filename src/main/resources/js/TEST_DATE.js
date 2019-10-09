const util = require('sample-util');

((globalScope) => {
    const DateUtils = Java.type('com.github.mdsina.graaljs.executorwebservice.util.DateUtils');
    const MILLI_OF_SECOND = Java.type('java.time.temporal.ChronoField').MILLI_OF_SECOND;
    globalScope.Date = class extends Date {
        constructor(...options) {
            if (
                options.length === 1
                && options[0]
                && (
                    (typeof options[0] === 'string' || options[0].constructor === String)
                    || (typeof options[0] === 'number' || options[0].constructor === Number)
                )
            ) {
                const date = DateUtils.toDate(options[0]);
                super(
                    date.getYear(),
                    date.getMonthValue() - 1,
                    date.getDayOfMonth(),
                    date.getHour(),
                    date.getMinute(),
                    date.getSecond(),
                    date.get(MILLI_OF_SECOND)
                );
            } else {
                super(...options);
            }
        }
    };
})(global || this);

callFunction = () => {
    util.output('DATE', util.formatDate(new Date(util.input('DATE'))));
    util.output('DATE1', new Date(util.input('DATE1')));
    util.output('DATE2', new Date(util.input('DATE2')));
};
