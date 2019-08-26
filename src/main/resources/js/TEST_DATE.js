const util = require('sample-util');

callFunction = () => {

    const date = util.input('DATE');

    util.output('DATE', util.formatDate(new Date(date)));
};
