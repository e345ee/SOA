function dateValid(value) {
    if (!/^[0-9]{4}-[0-9]{2}-[0-9]{2}$/.test(value) || value.startsWith('0000-')) return false;
    const date = new Date(value + 'T00:00:00Z');
    return Number.isFinite(date.getTime()) && date.toISOString().slice(0, 10) === value;
}

export function today() {
    const parts = new Intl.DateTimeFormat('en', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
    }).formatToParts(new Date());
    const part = type => parts.find(p => p.type === type).value;
    return `${part('year')}-${part('month')}-${part('day')}`;
}

function dateTimeValid(value) {
    const match = value.match(/^([0-9]{4}-[0-9]{2}-[0-9]{2})T([0-9]{2}):([0-9]{2})(?::([0-9]{2})(?:\.([0-9]{1,3}))?)?(?:Z|[+-]([0-9]{2}):([0-9]{2}))?$/);
    return !!match && dateValid(match[1]) && +match[2] < 24 && +match[3] < 60 && +(match[4] ?? 0) < 60 && +(match[6] ?? 0) < 24 && +(match[7] ?? 0) < 60 && Number.isFinite(new Date(value).getTime());
}

export function numericLimits(schema) {
    if (schema.type === 'integer') return {maxLength: schema.format === 'int32' ? 11 : 20, maxFractionDigits: 0};
    if (schema.type !== 'number') return null;
    return schema.format === 'float' ? {maxLength: 16, maxFractionDigits: 9} : {maxLength: 24, maxFractionDigits: 15};
}

function validateNumericText(value, schema) {
    const limits = numericLimits(schema);
    if (!limits) return;
    if (value.length > limits.maxLength) throw new Error('Число слишком длинное.');
    const pattern = schema.type === 'integer' ? /^-?(?:0|[1-9]\d*)$/ : /^-?(?:0|[1-9]\d*)(?:\.\d+)?$/;
    if (!pattern.test(value)) throw new Error(schema.type === 'integer' ? 'Введите целое число без ведущих нулей.' : 'Введите число в формате 0.7 или 1.7 без запятой, ведущих нулей и экспоненты.');
    const separator = value.indexOf('.');
    if (separator >= 0 && value.length - separator - 1 > limits.maxFractionDigits) throw new Error(`Допустимо не более ${limits.maxFractionDigits} знаков после точки.`);
}

const decimalFormat = new Intl.NumberFormat('en-US', {useGrouping: false, maximumSignificantDigits: 21});

function preservesDecimal(text, number, format) {
    const canonical = text.replace(/(\.\d*?)0+$/, '$1').replace(/\.$/, '').replace(/^-0$/, '0');
    if (decimalFormat.format(number) !== canonical && !(number === 0 && canonical === '0')) return false;
    if (format !== 'float' || Math.fround(number) === number) return true;
    const single = Math.fround(number);
    for (let digits = 1; digits <= 9; digits++) {
        const candidate = Number(single.toPrecision(digits));
        if (Math.fround(candidate) === single) return candidate === number;
    }
    return false;
}

export function readValue(raw, schema, required, resolve) {
    schema = resolve(schema);
    const value = raw.trim();
    if (schema.uiAllowNull && value === '__null__') return null;
    if (!value) {
        if (required) throw new Error('Заполните это поле.');
        return undefined;
    }
    let parsed = value;
    validateNumericText(raw, schema);
    if (schema.type === 'integer') {
        const integer = BigInt(value);
        const low = schema.format === 'int32' ? -2147483648n : -9223372036854775808n;
        const high = schema.format === 'int32' ? 2147483647n : 9223372036854775807n;
        if (integer < low || integer > high) throw new Error('Число выходит за допустимый диапазон.');
        parsed = integer >= -9007199254740991n && integer <= 9007199254740991n ? Number(integer) : integer;
    } else if (schema.type === 'number') {
        parsed = Number(value);
        if (!Number.isFinite(parsed)) throw new Error('Введите конечное число в допустимом диапазоне.');
        if (!preservesDecimal(value, parsed, schema.format)) throw new Error('Число потеряет точность. Уменьшите количество цифр.');
    }
    if (schema.enum && !schema.enum.includes(parsed)) throw new Error('Выберите значение из списка.');
    if (schema.minimum !== undefined && (schema.exclusiveMinimum ? parsed <= schema.minimum : parsed < schema.minimum)) throw new Error(`Значение должно быть ${schema.exclusiveMinimum ? 'больше' : 'не меньше'} ${schema.minimum}.`);
    if (schema.maximum !== undefined && (schema.exclusiveMaximum ? parsed >= schema.maximum : parsed > schema.maximum)) throw new Error(`Значение должно быть ${schema.exclusiveMaximum ? 'меньше' : 'не больше'} ${schema.maximum}.`);
    if (schema.minLength && value.length < schema.minLength) throw new Error(`Не меньше ${schema.minLength} символов.`);
    if (schema.format === 'date') {
        if (!dateValid(value)) throw new Error('Введите существующую дату с годом от 1 до 9999.');
        if (schema.uiNoFuture && value > today()) throw new Error('Дата рождения не может быть в будущем.');
    }
    if (schema.format === 'date-time') {
        if (!dateTimeValid(value)) throw new Error('Введите существующую дату и время с годом от 1 до 9999.');
        parsed = new Date(value).toISOString();
    }
    return parsed;
}

