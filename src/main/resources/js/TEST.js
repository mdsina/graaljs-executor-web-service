const util = require('sample-util');

callFunction = () => {

    const ruStr = util.input('RU_STR');

    util.output('EN_STR', util.transliterate(ruStr));
};
