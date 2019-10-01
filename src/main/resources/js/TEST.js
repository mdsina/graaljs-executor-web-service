const util = require('sample-util');

callFunction = async (inputs) => {
    const input = inputs['I'];

    const res = await util.request(`https://postman-echo.com/get?input=${input}`, "GET");

    const Thread = Java.type('java.lang.Thread');

    console.log("RES:", JSON.stringify(res));
    console.log("THREAD:", Thread.currentThread().getName());

    return {
        RESULT: res.args.input
    };
};
