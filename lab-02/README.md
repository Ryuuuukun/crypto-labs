### Использование программы

Параметры программы задаются в файле `results/meta.json`:

```jsonc
{
    "method": "plain|encrypted|encrypted random", // required
    "source": "path",                             // required

    // Если не указаны, то генерируются случайно
    "message": "string|null",
    "password": "string|null",

    // параметры генерации сообщения, в случае messageAuto=true 
    // поле message игнорируется
    "messageAuto": "true|false",
    "messageSeed": "number",    // default: random
    "messageLength": "number",  // default: 50

    // параметры генерации пароля, в случае passwordAuto=true 
    // поле password игнорируется
    "passwordAuto": "true|false",
    "passwordSeed": "number",     // default: random
    "passwordLength": "number"    // default: 50
}
```
