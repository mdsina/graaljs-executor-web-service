# graaljs-executor-web-service
JS executing web service using graal-sdk

This service provide ability to execute some specific JS code with high load.

For example do POST request and see result:

`POST /api/v1/script/TEST`

body:
```
{
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
      "type": "TEXT"
    }
  ]
}
```

The source of JS code can be found at resources `js/TEST.js`