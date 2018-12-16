meta = {
    "description": "Transliterate russian symbols to latin",
    "inputs": [
        {
            "name": "RU_STR",
            "type": "TEXT"
        }
    ],
    "outputs": [
        {
            "name": "EN_STR",
            "type": "TEXT"
        }
    ],
    "examples": [
        {
            "name": "ex1",
            "inputs": [
                {
                    "name": "RU_STR",
                    "type": "TEXT",
                    "value": "Съешь еще этих мягких булок да выпей чаю"
                }
            ],
            "outputs": [
                {
                    "name": "EN_STR",
                    "type": "TEXT",
                    "value": "Syesh yeshche etikh myagkikh bulok da vypey chayu"
                }
            ]
        }
    ]
};

const util = require('sample-util');

callFunction = () => {

    const ruStr = util.input('RU_STR');

    util.output('EN_STR', util.transliterate(ruStr));
};

