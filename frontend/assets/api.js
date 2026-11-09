import {labels} from './labels.js';
import {parseJSON, stringify} from './json.js';

export class ApiError extends Error {
    constructor(text, status = 0, uncertain = false) {
        super(text);
        this.status = status;
        this.uncertain = uncertain;
    }
}

export async function api(path, {method = 'GET', body, expected = 200, signal, headers = {}} = {}) {
    const controller = new AbortController();
    const abort = () => controller.abort();
    signal?.addEventListener('abort', abort, {once: true});
    const timer = setTimeout(abort, 25000);
    try {
        const response = await fetch('/api/v1' + path, {
            method,
            headers: {
                ...headers,
                Accept: 'application/json', ...(body !== undefined ? {'Content-Type': 'application/json'} : {})
            },
            body: body === undefined ? undefined : stringify(body),
            signal: controller.signal,
            credentials: 'same-origin',
            cache: 'no-store'
        });
        const raw = await response.text();
        let data;
        try {
            if (raw.trim()) data = parseJSON(raw);
        } catch {
        }
        if (response.status !== expected) {
            const generic = {
                400: 'Сервер отклонил запрос.',
                404: 'Объект или операция не найдены.',
                405: 'Операция не поддерживается сервером.',
                409: 'Операция конфликтует с текущими данными.',
                500: 'Внутренняя ошибка сервера.',
                502: 'Сервис не смог выполнить обращение к Ticket Service.',
                503: 'Сервис временно недоступен.'
            };
            const detail = data?.detail ?? data?.message;
            const details = Array.isArray(data?.errors) ? data.errors.map(x => [labels[x.field] ?? x.field, x.message].filter(Boolean).join(': ')).filter(Boolean).join('\n') : '';
            if (response.ok) throw new ApiError(`Сервер вернул HTTP ${response.status} вместо ${expected}.${method === 'GET' ? ' Данные не загружены.' : ' Запрос мог быть выполнен. Проверьте результат перед повторной отправкой.'}`, response.status, method !== 'GET');
            throw new ApiError(`${generic[response.status] ?? 'Не удалось выполнить запрос'} (HTTP ${response.status})${detail ? '\n' + String(detail).slice(0, 350) : ''}${details ? '\n' + details : ''}`, response.status);
        }
        if (expected === 204) return null;
        if (data === undefined) throw new ApiError(`Сервер вернул ${raw.trim() ? 'неверный формат' : 'пустой ответ'} вместо данных. (HTTP ${response.status})${method === 'GET' ? '' : ' Проверьте результат перед повторной отправкой.'}`, response.status, method !== 'GET');
        return data;
    } catch (error) {
        if (error instanceof ApiError) throw error;
        if (error.name === 'AbortError') throw new ApiError('Время ожидания истекло.' + (method === 'GET' ? ' Попробуйте обновить данные.' : ' Проверьте результат перед повторной отправкой.'), 0, method !== 'GET');
        throw new ApiError('Нет соединения с сервером. Проверьте подключение к интернету.' + (method === 'GET' ? '' : ' Проверьте результат перед повторной отправкой.'), 0, method !== 'GET');
    } finally {
        clearTimeout(timer);
        signal?.removeEventListener('abort', abort);
    }
}

