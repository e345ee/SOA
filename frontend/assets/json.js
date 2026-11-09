// Preserve int64 identifiers instead of rounding them to JavaScript's 53-bit integers.
export function parseJSON(source) {
    const marker = '__soa_integer_' + crypto.randomUUID() + ':';
    let result = '';
    for (let i = 0; i < source.length;) {
        if (source[i] === '"') {
            let end = i + 1;
            for (; end < source.length; end++) {
                if (source[end] === '\\') {
                    end++;
                    continue;
                }
                if (source[end] === '"') {
                    end++;
                    break;
                }
            }
            result += source.slice(i, end);
            i = end;
        } else if (source[i] === '-' || /\d/.test(source[i])) {
            const token = source.slice(i).match(/^-?(?:0|[1-9]\d*)(?:\.\d+)?(?:[eE][+-]?\d+)?/);
            if (!token) throw new Error('Некорректный JSON');
            const value = token[0];
            const large = !/[.eE]/.test(value) && (BigInt(value) > 9007199254740991n || BigInt(value) < -9007199254740991n);
            result += large ? JSON.stringify(marker + value) : value;
            i += value.length;
        } else result += source[i++];
    }
    return JSON.parse(result, (key, value) => typeof value === 'string' && value.startsWith(marker) ? BigInt(value.slice(marker.length)) : value);
}

export function stringify(value) {
    if (typeof value === 'bigint') return value.toString();
    if (Array.isArray(value)) return '[' + value.map(item => stringify(item) ?? 'null').join(',') + ']';
    if (value && typeof value === 'object') return '{' + Object.entries(value).filter(([, v]) => v !== undefined).map(([key, v]) => JSON.stringify(key) + ':' + stringify(v)).join(',') + '}';
    return JSON.stringify(value);
}

