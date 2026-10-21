const $ = (selector, root = document) => root.querySelector(selector);
const $$ = (selector, root = document) => [...root.querySelectorAll(selector)];
const escape = value => String(value ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const get = (object, path) => path.split('.').reduce((value, key) => value?.[key], object);
const set = (object, path, value) => { const keys = path.split('.'); const last = keys.pop(); let target = object; for (const key of keys) target = target[key] ??= {}; target[last] = value; };
const app = $('#app');
const modal = $('#modal');
let contract;
let listController;
let generation = 0;
let entity = /^\/persons\/?$/.test(location.pathname) ? 'persons' : 'tickets';
const bookingPage = /^\/booking\/?$/.test(location.pathname);
const states = Object.fromEntries(['tickets','persons'].map(key => [key, {filter:{},sort:[{field:'id',direction:'ASC'}],page:0,size:20,data:null,error:null}]));
const labels = {
 page:'Страница',size:'Размер страницы',filter:'Фильтр',sort:'Сортировка',id:'ID',name:'Название',coordinates:'Координаты','coordinates.x':'Координата X','coordinates.y':'Координата Y',creationDate:'Дата создания',price:'Цена',type:'Тип',person:'Человек',personId:'ID человека',eventId:'ID события',birthday:'Дата рождения',weight:'Вес',location:'Местоположение','location.x':'Координата X местоположения','location.y':'Координата Y местоположения','location.name':'Название местоположения','person.id':'Человек: ID','person.birthday':'Человек: дата рождения','person.weight':'Человек: вес','person.location.x':'Координата X местоположения человека','person.location.y':'Координата Y местоположения человека','person.location.name':'Название местоположения человека',ticketId:'ID билета',discount:'Скидка, %'
};
const ticketColumns = [
 ['id',['id']],['name',['nameContains']],['coordinates.x',['coordinatesXFrom','coordinatesXTo']],['coordinates.y',['coordinatesYFrom','coordinatesYTo']],['creationDate',['creationDateFrom','creationDateTo']],['price',['priceFrom','priceTo']],['type',['type']],['person.birthday',['personBirthdayFrom','personBirthdayTo']],['person.weight',['personWeightFrom','personWeightTo']],['person.location.x',['personLocationX']],['person.location.y',['personLocationY']],['person.location.name',['personLocationNameContains']],['eventId',['eventId']]
];
const personColumns = [['id',['id']],['birthday',['birthdayFrom','birthdayTo']],['weight',['weightFrom','weightTo']],['location.x',['locationX']],['location.y',['locationY']],['location.name',['locationNameContains']]];
const columns = () => entity === 'tickets' ? ticketColumns : personColumns;
const schemas = () => contract.ticket.components.schemas;
const resolve = (schema, source = contract.ticket) => schema?.$ref ? resolve(schema.$ref.slice(2).split('/').reduce((node,key) => node[key], source), source) : schema;
const filterSchema = () => schemas()[entity === 'tickets' ? 'TicketSearchFilter' : 'PersonSearchFilter'];
const objectSchema = () => schemas()[entity === 'tickets' ? 'Ticket' : 'Person'];
const noun = () => entity === 'tickets' ? 'билет' : 'человека';

// Preserve int64 identifiers instead of rounding them to JavaScript's 53-bit integers.
function parseJSON(source) {
 const marker = '__soa_integer_' + crypto.randomUUID() + ':';
 let result = '';
 for (let i = 0; i < source.length;) {
  if (source[i] === '"') { let end = i + 1; for (; end < source.length; end++) { if (source[end] === '\\') { end++; continue; } if (source[end] === '"') { end++; break; } } result += source.slice(i,end); i=end; }
  else if (source[i] === '-' || /\d/.test(source[i])) { const token = source.slice(i).match(/^-?(?:0|[1-9]\d*)(?:\.\d+)?(?:[eE][+-]?\d+)?/); if (!token) throw new Error('Некорректный JSON'); const value=token[0]; const large = !/[.eE]/.test(value) && (BigInt(value)>9007199254740991n || BigInt(value)<-9007199254740991n); result += large ? JSON.stringify(marker + value) : value; i += value.length; }
  else result += source[i++];
 }
 return JSON.parse(result, (key,value) => typeof value==='string' && value.startsWith(marker) ? BigInt(value.slice(marker.length)) : value);
}
function stringify(value) {
 if (typeof value === 'bigint') return value.toString();
 if (Array.isArray(value)) return '[' + value.map(item => stringify(item) ?? 'null').join(',') + ']';
 if (value && typeof value === 'object') return '{' + Object.entries(value).filter(([,v])=>v!==undefined).map(([key,v])=>JSON.stringify(key)+':'+stringify(v)).join(',') + '}';
 return JSON.stringify(value);
}
function dateValid(value) {
 if (!/^[0-9]{4}-[0-9]{2}-[0-9]{2}$/.test(value) || value.startsWith('0000-')) return false;
 const date = new Date(value+'T00:00:00Z');
 return Number.isFinite(date.getTime()) && date.toISOString().slice(0,10) === value;
}
function today() {
 const parts = new Intl.DateTimeFormat('en', {timeZone:'Europe/Moscow',year:'numeric',month:'2-digit',day:'2-digit'}).formatToParts(new Date());
 const part = type => parts.find(p=>p.type===type).value;
 return `${part('year')}-${part('month')}-${part('day')}`;
}
function dateTimeValid(value) {
 const match=value.match(/^([0-9]{4}-[0-9]{2}-[0-9]{2})T([0-9]{2}):([0-9]{2})(?::([0-9]{2})(?:\.([0-9]{1,3}))?)?(?:Z|[+-]([0-9]{2}):([0-9]{2}))?$/);
 return !!match && dateValid(match[1]) && +match[2]<24 && +match[3]<60 && +(match[4]??0)<60 && +(match[6]??0)<24 && +(match[7]??0)<60 && Number.isFinite(new Date(value).getTime());
}
function readValue(raw, schema, required) {
 schema = resolve(schema);
 const value = raw.trim();
 if (!value) { if (required) throw new Error('Заполните это поле.'); return undefined; }
 let parsed = value;
 if (schema.type === 'integer') {
  if (!/^-?\d+$/.test(value)) throw new Error('Введите целое число.');
  const integer=BigInt(value); const low=schema.format==='int32'?-2147483648n:-9223372036854775808n; const high=schema.format==='int32'?2147483647n:9223372036854775807n;
  if(integer<low || integer>high) throw new Error('Число выходит за допустимый диапазон.');
  parsed=integer>=-9007199254740991n && integer<=9007199254740991n ? Number(integer) : integer;
 } else if (schema.type === 'number') {
  if (!/^-?(?:\d+(?:\.\d*)?|\.\d+)(?:[eE][+-]?\d+)?$/.test(value)) throw new Error('Введите число.');
  parsed=Number(value); if (!Number.isFinite(parsed)) throw new Error('Введите конечное число в допустимом диапазоне.');
  const nonzero=/[1-9]/.test(value.split(/[eE]/)[0]);
  if ((parsed===0 && nonzero) || (schema.format==='float' && (!Number.isFinite(Math.fround(parsed)) || (parsed!==0 && Math.fround(parsed)===0)))) throw new Error('Число выходит за диапазон типа '+(schema.format==='float'?'float.':'double.'));
 }
 if (schema.enum && !schema.enum.includes(parsed)) throw new Error('Выберите значение из списка.');
 if (schema.minimum !== undefined && (schema.exclusiveMinimum ? parsed <= schema.minimum : parsed < schema.minimum)) throw new Error(`Значение должно быть ${schema.exclusiveMinimum?'больше':'не меньше'} ${schema.minimum}.`);
 if (schema.maximum !== undefined && (schema.exclusiveMaximum ? parsed >= schema.maximum : parsed > schema.maximum)) throw new Error(`Значение должно быть ${schema.exclusiveMaximum?'меньше':'не больше'} ${schema.maximum}.`);
 if (schema.minLength && value.length < schema.minLength) throw new Error(`Не меньше ${schema.minLength} символов.`);
 if (schema.format === 'date') {
  if(!dateValid(value)) throw new Error('Введите существующую дату с годом от 1 до 9999.');
  if(schema.uiNoFuture && value>today()) throw new Error('Дата рождения не может быть в будущем.');
 }
 if (schema.format === 'date-time') { if(!dateTimeValid(value)) throw new Error('Введите существующую дату и время с годом от 1 до 9999.'); parsed=new Date(value).toISOString(); }
 return parsed;
}
function inputType(schema) { schema=resolve(schema); return schema.format==='date'?'date':schema.format==='date-time'?'datetime-local':'text'; }
function fields(schema, prefix='') {
 schema=resolve(schema); const result=[];
 for (const [key,original] of Object.entries(schema.properties??{})) {
  const item={...resolve(original),...(key==='birthday'?{uiNoFuture:true}:{})}; const path=prefix?prefix+'.'+key:key;
  if (item.readOnly) continue;
  if (item.type === 'object') result.push(...fields(item,path));
  else result.push({path,schema:item,required:(schema.required??[]).includes(key),label:labels[path]??labels[key]??path});
 }
 return result;
}
function inputHTML(def, value='', placeholder='') {
 const schema=resolve(def.schema); const attributes=`name="${escape(def.path)}" aria-label="${escape(def.label)}" ${def.required?'required':''}`;
 if (schema.enum) return `<select ${attributes}><option value="">${def.required?'Выберите…':'Любой / не задан'}</option>${schema.enum.filter(x=>x!==null).map(x=>`<option value="${escape(x)}" ${String(x)===String(value)?'selected':''}>${escape(x)}</option>`).join('')}</select>`;
 if (schema.format==='date-time' && value) { const date=new Date(value); if(Number.isFinite(date.getTime())) value=new Date(date.getTime()-date.getTimezoneOffset()*60000).toISOString().slice(0,16); }
 return `<input ${attributes} type="${inputType(schema)}" ${schema.format==='date'?`min="0001-01-01" max="${schema.uiNoFuture?today():'9999-12-31'}"`:schema.format==='date-time'?'min="0001-01-01T00:00" max="9999-12-31T23:59"':''} ${['integer','number'].includes(schema.type)?`inputmode="${schema.type==='integer'?'numeric':'decimal'}"`:''} value="${escape(value)}" placeholder="${escape(placeholder)}" autocomplete="off">`;
}
function fieldHTML(def, value='') { return `<label>${escape(def.label)}${def.required?' *':''}${inputHTML(def,value)}<span class="field-error" aria-live="polite"></span></label>`; }
function bindForm(form, definitions) {
 form.noValidate=true; form._fields=definitions;
 for (const def of definitions) { const input=form.elements.namedItem(def.path); if(!input) continue; input.addEventListener('input',()=>{input.removeAttribute('aria-invalid'); const hint=input.closest('label')?.querySelector('.field-error'); if(hint)hint.textContent='';}); }
}
function collect(form) {
 const body={}; let first;
 for (const def of form._fields??[]) {
  const input=form.elements.namedItem(def.path); if(!input || input.disabled)continue;
  try { const value=readValue(input.value,def.schema,def.required); if(value!==undefined)set(body,def.path,value); input.removeAttribute('aria-invalid'); const hint=input.closest('label')?.querySelector('.field-error'); if(hint)hint.textContent=''; }
  catch(error) { input.setAttribute('aria-invalid','true'); const hint=input.closest('label')?.querySelector('.field-error'); if(hint)hint.textContent=error.message; first??=input; }
 }
 if(first){first.focus();throw new Error('Проверьте отмеченные поля.');}
 for(const [key,value]of Object.entries(body)) {
  if(key.endsWith('From') && body[key.slice(0,-4)+'To']!==undefined && value>body[key.slice(0,-4)+'To']) throw new Error('Начало диапазона не должно быть больше конца.');
 }
 return body;
}
function message(target, text='', type='error') { target.textContent=text; target.className='message '+type; target.setAttribute('role',type==='error'?'alert':'status'); }
class ApiError extends Error { constructor(text,status=0,uncertain=false){super(text);this.status=status;this.uncertain=uncertain;} }
async function api(path,{method='GET',body,expected=200,signal}={}) {
 const controller=new AbortController(); const abort=()=>controller.abort(); signal?.addEventListener('abort',abort,{once:true}); const timer=setTimeout(abort,25000);
 try {
  const response=await fetch('/api/v1'+path,{method,headers:{Accept:'application/json',...(body!==undefined?{'Content-Type':'application/json'}:{})},body:body===undefined?undefined:stringify(body),signal:controller.signal,credentials:'same-origin',cache:'no-store'});
  const raw=await response.text(); let data; try{if(raw.trim())data=parseJSON(raw);}catch{}
  if(response.status!==expected) {
   const generic={400:'Сервер отклонил запрос.',404:'Объект или операция не найдены.',405:'Операция не поддерживается сервером.',409:'Операция конфликтует с текущими данными.',500:'Внутренняя ошибка сервера.',502:'Сервис не смог выполнить обращение к Ticket Service.',503:'Сервис временно недоступен.'};
   const detail=data?.detail??data?.message;
   const details=Array.isArray(data?.errors)?data.errors.map(x=>[labels[x.field]??x.field,x.message].filter(Boolean).join(': ')).filter(Boolean).join('\n'):'';
   if(response.ok)throw new ApiError(`Сервер вернул HTTP ${response.status} вместо ${expected}.${method==='GET'?' Данные не загружены.':' Запрос мог быть выполнен. Проверьте результат перед повторной отправкой.'}`,response.status,method!=='GET');
   throw new ApiError(`${generic[response.status]??'Не удалось выполнить запрос'} (HTTP ${response.status})${detail?'\n'+String(detail).slice(0,350):''}${details?'\n'+details:''}`,response.status);
  }
  if(expected===204)return null;
  if(data===undefined)throw new ApiError(`Сервер вернул ${raw.trim()?'неверный формат':'пустой ответ'} вместо данных. (HTTP ${response.status})${method==='GET'?'':' Проверьте результат перед повторной отправкой.'}`,response.status,method!=='GET');
  return data;
 } catch(error) {
  if(error instanceof ApiError)throw error;
  if(error.name==='AbortError')throw new ApiError('Время ожидания истекло.'+(method==='GET'?' Попробуйте обновить данные.':' Проверьте результат перед повторной отправкой.'),0,method!=='GET');
  throw new ApiError('Нет соединения с сервером. Проверьте подключение к интернету.'+(method==='GET'?'':' Проверьте результат перед повторной отправкой.'),0,method!=='GET');
 } finally {clearTimeout(timer);signal?.removeEventListener('abort',abort);}
}
function format(value,key='') {
 if(value===undefined || value===null)return '—';
 if(/birthday$/.test(key) && /^\d{4}-\d{2}-\d{2}$/.test(String(value)))return String(value).split('-').reverse().join('.');
 if(key==='creationDate'){const date=new Date(value);if(Number.isFinite(date.getTime()))return new Intl.DateTimeFormat('ru-RU',{dateStyle:'short',timeStyle:'short',timeZone:'Europe/Moscow'}).format(date);}
 if(typeof value==='object')return stringify(value);
 return String(value);
}
function headerHTML(cols,sorting=[]) { return `<tr>${cols.map(([key])=>{const index=sorting.findIndex(x=>x.field===key);const active=sorting[index];return `<th class="sortable" aria-sort="${active?(active.direction==='ASC'?'ascending':'descending'):'none'}"><button type="button" data-sort="${escape(key)}" title="Сортировать: ${escape(labels[key])}. Shift — добавить поле">${escape(labels[key])} ${active?(active.direction==='ASC'?'↑':'↓')+(sorting.length>1?index+1:''):''}</button></th>`;}).join('')}<th>Действия</th></tr>`; }
function filterDefs() {return columns().flatMap(([field,keys])=>keys.map(key=>({path:key,schema:resolve(filterSchema().properties[key]),required:false,label:labels[field]+(key.endsWith('From')?' · от':key.endsWith('To')?' · до':keys[0].includes('Contains')?' · содержит':'')})));}
function renderList() {
 const title=entity==='tickets'?'Билеты':'Люди';document.title=`SOA · ${title}`;
 const state=states[entity];
 app.innerHTML=`<div class="page-head"><h1>${title}</h1><button type="button" class="primary" id="create">${entity==='tickets'?'Создать билет':'Создать человека'}</button></div>
 <form class="search" id="quick-search"><label>Поиск по полю<select name="quick-field">${columns().map(([key])=>`<option value="${escape(key)}" ${key===(entity==='tickets'?'name':'id')?'selected':''}>${escape(labels[key])}</option>`).join('')}</select></label><label>Значение<span id="quick-input"></span></label><button class="primary" type="submit">Найти</button></form>
 <div class="list-tools"><details><summary>Фильтры${Object.keys(state.filter).length?' ('+Object.keys(state.filter).length+')':''}</summary><form id="filters"><div class="filter-grid">${filterDefs().map(def=>fieldHTML(def,state.filter[def.path]??'')).join('')}</div><div class="actions"><button type="submit" class="primary">Применить</button><button type="button" id="reset">Сбросить</button></div></form></details>
 <details><summary>Ещё</summary><div class="actions"><button type="button" id="reload">Обновить</button></div>${entity==='tickets'?'<div class="actions"><button type="button" id="sum">Сумма цен</button><button type="button" id="average">Средняя цена</button><button type="button" id="count-person">Количество по человеку</button></div><div id="statistics-result" class="message" aria-live="polite"></div>':''}</details></div>
 <div id="list-message" class="message" aria-live="polite"></div><p class="hint">Сортировка — нажмите заголовок. Несколько полей — Shift + нажатие.</p>
 <div class="table-wrap" tabindex="0" aria-label="Таблица: ${title}"><table id="data-table"><thead>${headerHTML(columns(),state.sort)}</thead><tbody id="rows"><tr><td class="empty" colspan="${columns().length+1}">Загрузка…</td></tr></tbody></table></div>
 <div class="pager"><span id="summary" aria-live="polite">Данные не загружены</span><div class="controls"><button id="previous" type="button" aria-label="Предыдущая страница">←</button><span id="page-label">Страница ${state.page+1}</span><button id="next" type="button" aria-label="Следующая страница">→</button><label>На странице <select id="size">${[10,20,50,100].map(size=>`<option ${state.size===size?'selected':''}>${size}</option>`).join('')}</select></label></div></div>`;
 bindForm($('#filters'),filterDefs());
 $('#create').onclick=()=>objectForm('create');$('#reload').onclick=loadList;
 $('#reset').onclick=()=>{state.filter={};state.sort=[{field:'id',direction:'ASC'}];state.page=0;renderList();loadList();};
 $('#filters').onsubmit=event=>{event.preventDefault();try{state.filter=collect(event.currentTarget);state.page=0;loadList();}catch(error){message($('#list-message'),error.message);}};
 $('#size').onchange=event=>{state.size=Number(event.target.value);state.page=0;loadList();};
 $('#previous').onclick=()=>{if(state.page>0){state.page--;loadList();}};
 $('#next').onclick=()=>{state.page++;loadList();};
 attachSorting();renderQuickInput();$('#quick-search').elements['quick-field'].onchange=renderQuickInput;
 $('#quick-search').onsubmit=event=>{event.preventDefault();try{const field=event.currentTarget.elements['quick-field'].value;const col=columns().find(x=>x[0]===field);const schema=resolve(filterSchema().properties[col[1][0]]);const value=readValue(event.currentTarget.elements['quick-value'].value,schema,true);const filters=collect($('#filters'));for(const key of col[1])filters[key]=value;state.filter=filters;state.page=0;renderList();loadList();}catch(error){message($('#list-message'),error.message);}};
 if(entity==='tickets'){$('#sum').onclick=()=>statistic('/tickets/price/sum','Сумма цен');$('#average').onclick=()=>statistic('/tickets/price/average','Средняя цена');$('#count-person').onclick=countPersonForm;}
}
function renderQuickInput(){const field=$('#quick-search').elements['quick-field'].value;const col=columns().find(x=>x[0]===field);const schema=filterSchema().properties[col[1][0]];$('#quick-input').innerHTML=inputHTML({path:'quick-value',schema,required:true,label:'Значение для поиска'});}
function attachSorting(){ $$('[data-sort]',app).forEach(button=>button.onclick=event=>{const state=states[entity];const key=button.dataset.sort;const old=state.sort.find(x=>x.field===key);const next={field:key,direction:old?.direction==='ASC'?'DESC':'ASC'};state.sort=event.shiftKey?[...state.sort.filter(x=>x.field!==key),next]:[next];state.page=0;const header=$('#data-table thead');header.innerHTML=headerHTML(columns(),state.sort);attachSorting();loadList();}); }
async function loadList(){
 listController?.abort();listController=new AbortController();const version=++generation;const current=entity;const state=states[current];
 message($('#list-message'),'');app.setAttribute('aria-busy','true');$('#rows').innerHTML=`<tr><td class="empty" colspan="${columns().length+1}">Загрузка…</td></tr>`;$('#next').disabled=true;$('#previous').disabled=true;
 try{
  let data;
  if(Object.keys(state.filter).length || state.sort.length) data=await api('/'+current+'/search',{method:'POST',body:{filter:state.filter,page:state.page,size:state.size,sort:state.sort},signal:listController.signal});
  else {const params=new URLSearchParams({page:String(state.page),size:String(state.size),sort:state.sort.map(x=>(x.direction==='DESC'?'-':'')+x.field).join(',')});data=await api('/'+current+'?'+params,{signal:listController.signal});}
  if(version!==generation||current!==entity)return;
  if(!data||!Array.isArray(data.content))throw new ApiError('Сервер не вернул коллекцию объектов. Данные не загружены.');
  if(data.page!==undefined && data.page!==state.page)throw new ApiError('Сервер вернул другую страницу. Данные не показаны, чтобы не перепутать результаты.');
  if(Number.isSafeInteger(data.totalPages)&&data.totalPages>=0&&state.page>Math.max(0,data.totalPages-1)){state.page=Math.max(0,data.totalPages-1);return loadList();}
  state.data=data;state.error=null;renderRows();
 }catch(error){if(version!==generation||current!==entity)return;state.data=null;state.error=error;const pending=error.status===202;message($('#list-message'),pending?'':error.message);$('#rows').innerHTML=`<tr><td class="empty" colspan="${columns().length+1}">${pending?'Кажется, ничего не найдено.':'Нет загруженных данных.<br>Исправьте ошибку или нажмите «Обновить».'}</td></tr>`;}
 finally{if(version===generation&&current===entity){app.setAttribute('aria-busy','false');renderPager();}}
}
function renderRows(){const data=states[entity].data;$('#rows').innerHTML=data.content.length?data.content.map((row,index)=>`<tr>${columns().map(([key])=>`<td title="${escape(get(row,key))}">${escape(format(get(row,key),key))}</td>`).join('')}<td><div class="row-actions"><button type="button" data-open="${index}">Открыть</button><button type="button" data-edit="${index}">Обновить</button><button type="button" data-delete="${index}">Удалить</button></div></td></tr>`).join(''):`<tr><td class="empty" colspan="${columns().length+1}">Ничего не найдено. Измените фильтры или создайте объект.</td></tr>`;
 $$('[data-open]').forEach(button=>button.onclick=()=>readObject(data.content[button.dataset.open].id));$$('[data-edit]').forEach(button=>button.onclick=()=>objectForm('edit',data.content[button.dataset.edit]));$$('[data-delete]').forEach(button=>button.onclick=()=>deleteForm(data.content[button.dataset.delete]));}
function renderPager(){const state=states[entity];const data=state.data;$('#summary').textContent=data?(data.totalElements!==undefined?`Всего: ${data.totalElements}. На странице: ${data.content.length}.`:`На странице: ${data.content.length}.`):'Данные не загружены';$('#page-label').textContent='Страница '+(state.page+1)+(data?.totalPages?' из '+data.totalPages:'');$('#previous').disabled=state.page===0;$('#next').disabled=!data||!(data.hasNext??(data.totalPages!==undefined?state.page+1<data.totalPages:data.content.length===state.size));}
function showModal(title,content){modal.innerHTML=`<div class="dialog-head"><h2 id="modal-title">${escape(title)}</h2><button type="button" class="close" aria-label="Закрыть">×</button></div>${content}`;$('.close',modal).onclick=()=>modal.close();if(!modal.open)modal.showModal();$('input:not(:disabled),select:not(:disabled)',modal)?.focus();}
function cancelButton(){return '<button type="button" data-cancel>Отмена</button>';}
function attachCancel(){ $$('[data-cancel]',modal).forEach(button=>button.onclick=()=>modal.close()); }
function personPickerHTML(def) {
 return `<label>${escape(def.label)} *<select name="${escape(def.path)}" aria-label="${escape(def.label)}" required disabled><option value="">Загрузка людей…</option></select><span class="field-error" aria-live="polite"></span></label><div class="message" data-picker-message aria-live="polite"></div><button type="button" data-picker-more hidden>Загрузить ещё</button><button type="button" data-picker-retry hidden>Повторить загрузку</button>`;
}
function personPicker(form, def, initialPerson) {
 const select=form.elements.namedItem(def.path), box=select.closest('#existing-person');
 const info=$('[data-picker-message]',box), more=$('[data-picker-more]',box), retry=$('[data-picker-retry]',box);
 const records=new Map(); let nextPage=0, loading=false, failed=false;
 const active=()=>$('#person-mode',form)?.value==='id';
 const update=()=>{select.disabled=!active()||loading||records.size===0;};
 async function load(reset=false) {
  if(loading)return;
  if(reset){records.clear();nextPage=0;select.innerHTML='<option value="">Выберите человека…</option>';}
  loading=true;failed=false;update();retry.hidden=true;more.hidden=true;message(info,'Загрузка людей…','success');
  try {
   const data=await api('/persons?'+new URLSearchParams({page:String(nextPage),size:'100'}));
   if(!form.isConnected)return;
   if(!data||!Array.isArray(data.content))throw new Error('Сервер не вернул список людей.');
   if(data.page!==undefined&&data.page!==nextPage)throw new Error('Сервер вернул другую страницу людей.');
   for(const person of data.content) {
    if(!person||!['number','bigint'].includes(typeof person.id)||(typeof person.id==='number'&&!Number.isSafeInteger(person.id)))throw new Error('В списке людей получен некорректный ID.');
    readValue(String(person.id),def.schema,true);
   }
   if(nextPage===0 && initialPerson?.id!==undefined && !data.content.some(p=>String(p.id)===String(initialPerson.id)))data.content.push(await api('/persons/'+encodeURIComponent(initialPerson.id)));
   if(!form.isConnected)return;
   if(nextPage===0)select.innerHTML='<option value="">Выберите человека…</option>';
   let added=0;
   for(const person of data.content){const id=String(person.id);if(records.has(id))continue;records.set(id,person);const option=document.createElement('option');option.value=id;option.textContent=`№${id} · ${format(person.birthday,'birthday')} · ${person.location?.name??'Местоположение не задано'}`;select.append(option);added++;}
   const hasNext=data.hasNext??(data.totalPages!==undefined?nextPage+1<data.totalPages:data.content.length===100);
   if(hasNext&&!added)throw new Error('Сервер повторяет страницу людей. Загрузка остановлена.');
   if(nextPage===0 && initialPerson?.id!==undefined)select.value=String(initialPerson.id);
   nextPage++;more.hidden=!hasNext;
   message(info,records.size?'':'Людей пока нет. Создайте человека на странице «Люди» или выберите создание нового вместе с билетом.',records.size?'success':'error');
  }catch(error){if(!form.isConnected)return;failed=true;message(info,'Не удалось загрузить людей. '+error.message);retry.hidden=false;}
  finally{loading=false;if(form.isConnected)update();}
 }
 more.onclick=()=>load();retry.onclick=()=>load(nextPage===0);
 return {update,load:()=>load(true), selected(){
  const person=records.get(select.value);if(!person)throw new Error('Выберите человека из списка.');const body={};
  for(const field of fields(schemas().Person)){const raw=get(person,field.path);const value=readValue(raw===null||raw===undefined?'':String(raw),field.schema,field.required);if(value!==undefined)set(body,field.path,value);}
  return body;
 },validate(){if(!active())return;if(loading)throw new Error('Дождитесь загрузки списка людей.');if(!records.has(select.value))throw new Error(failed?'Список людей не загружен. Повторите загрузку.':'Выберите человека из списка.');}};
}
function recordId(record) {
 const id=record?.id;
 if(!['number','bigint'].includes(typeof id)||(typeof id==='number'&&!Number.isSafeInteger(id)))throw new Error('У объекта некорректный ID. Обновите список.');
 return readValue(String(id),{type:'integer',format:'int64',minimum:1},true);
}
function objectForm(mode,record={}){
 const creating=mode==='create';let targetId;if(!creating){try{targetId=recordId(record);}catch(error){message($('#list-message'),error.message);return;}}const title=(creating?'Создать ':'Обновить ')+noun();
 const schema=schemas()[entity==='tickets'?(creating?'TicketCreate':'Ticket'):(creating?'PersonCreate':'Person')];
 let defs=fields(schema);let html='',personIdDef;
 if(entity==='tickets'){
  const common=defs.filter(x=>x.path!=='personId'&&!x.path.startsWith('person.'));const person=fields(schemas().PersonCreate,'person');
  personIdDef={path:'personId',schema:{type:'integer',format:'int64',minimum:1},required:true,label:'Человек из списка'};defs=[...common,personIdDef,...person];
  html=common.map(def=>fieldHTML(def,get(record,def.path))).join('')+'<label class="wide">Человек<select id="person-mode"><option value="id">Выбрать из списка</option><option value="new">Создать нового вместе с билетом</option></select></label><div class="wide" id="existing-person">'+personPickerHTML(personIdDef)+'</div><div class="wide form-grid" id="new-person" hidden>'+person.map(def=>fieldHTML(def)).join('')+'</div>';
 }else html=defs.map(def=>fieldHTML(def,get(record,def.path))).join('');
 showModal(title,`<form id="object-form"><div class="form-grid">${html}</div><div id="form-message" class="message" aria-live="polite"></div><div class="dialog-actions">${cancelButton()}<button type="submit" class="primary">${creating?'Создать':'Сохранить'}</button></div></form>`);
 const form=$('#object-form');bindForm(form,defs);attachCancel();if(!creating)for(const def of defs){if(def.path==='id'||def.path.endsWith('.id'))form.elements.namedItem(def.path).readOnly=true;}
 let picker;
 if(personIdDef){picker=personPicker(form,personIdDef,creating?null:record.person);const update=()=>{const existing=$('#person-mode').value==='id';$('#existing-person').hidden=!existing;$('#new-person').hidden=existing;$$('input,select',$('#new-person')).forEach(input=>input.disabled=existing);picker.update();};$('#person-mode').onchange=update;update();picker.load();}
 form.onsubmit=async event=>{event.preventDefault();if($('button[type=submit]',form).disabled)return;const target=$('#form-message');let payload;try{payload=collect(form);picker?.validate();if(picker&&((payload.personId!==undefined)===(payload.person!==undefined)))throw new Error('Выберите существующего человека или создайте нового.');if(picker&&!creating&&payload.personId!==undefined){payload.person=picker.selected();delete payload.personId;}}catch(error){message(target,error.message);return;}const button=$('button[type=submit]',form);button.disabled=true;message(target,'Сохранение…','success');try{await api('/'+entity+(creating?'':'/'+encodeURIComponent(targetId)),{method:creating?'POST':'PUT',body:payload,expected:creating?201:200});modal.close();states[entity].page=0;await loadList();message($('#list-message'),creating?'Объект создан.':'Изменения сохранены.','success');}catch(error){message(target,error.message);if(!error.uncertain)button.disabled=false;}};
}
async function readObject(id,target){try{if(id===undefined)throw new Error('Сервер не передал ID объекта.');const object=await api('/'+entity+'/'+encodeURIComponent(id));if(!object||typeof object!=='object'||Array.isArray(object))throw new Error('Сервер вернул неверный формат объекта.');const details=fields(objectSchema());if(entity==='tickets'){details.unshift({path:'id',label:'ID'}, {path:'creationDate',label:labels.creationDate});}showModal((entity==='tickets'?'Билет':'Человек')+' №'+String(id),`<dl class="object-details">${details.map(def=>`<dt>${escape(def.label)}</dt><dd>${escape(format(get(object,def.path),def.path))}</dd>`).join('')}</dl><div class="dialog-actions">${cancelButton()}<button type="button" id="view-delete">Удалить</button><button type="button" id="view-edit" class="primary">Обновить</button></div>`);attachCancel();$('#view-edit').onclick=()=>objectForm('edit',object);$('#view-delete').onclick=()=>deleteForm(object);}catch(error){message(target??$('#list-message'),error.message);}}
function deleteForm(record){let targetId;try{targetId=recordId(record);}catch(error){message($('#list-message'),error.message);return;}showModal('Удаление',`<p>Удалить ${noun()} №${escape(record.id)}?</p><div id="delete-message" class="message"></div><div class="dialog-actions">${cancelButton()}<button type="button" class="primary" id="confirm-delete">Удалить</button></div>`);attachCancel();$('#confirm-delete').onclick=async event=>{event.currentTarget.disabled=true;try{await api('/'+entity+'/'+encodeURIComponent(targetId),{method:'DELETE',expected:204});modal.close();await loadList();message($('#list-message'),'Объект удалён.','success');}catch(error){message($('#delete-message'),error.message);if(!error.uncertain)$('#confirm-delete').disabled=false;}};}
async function statistic(path,label){const target=$('#statistics-result');message(target,'Загрузка…','success');try{const value=await api(path);if(typeof value!=='number'||!Number.isFinite(value))throw new Error('Сервер не вернул числовой результат.');message(target,label+': '+String(value),'success');}catch(error){message(target,error.message);}}
function countPersonForm(){const defs=fields(schemas().Person);showModal('Количество билетов по человеку',`<form id="count-form"><div class="form-grid">${defs.map(def=>fieldHTML(def)).join('')}</div><div class="message" id="count-message"></div><div class="dialog-actions">${cancelButton()}<button type="submit" class="primary">Посчитать</button></div></form>`);const form=$('#count-form');bindForm(form,defs);attachCancel();form.onsubmit=async event=>{event.preventDefault();try{const payload=collect(form);const result=await api('/tickets/count-by-person',{method:'POST',body:payload});if(!['number','bigint'].includes(typeof result)||!/^\d+$/.test(String(result)))throw new Error('Сервер не вернул количество билетов.');message($('#count-message'),'Количество билетов: '+String(result),'success');}catch(error){message($('#count-message'),error.message);}};}
function renderBooking(){document.title='SOA · Booking';app.innerHTML=`<h1>Booking Service</h1><div class="booking-grid"><section class="panel"><h2>Продажа со скидкой</h2><p>Создать билет на основе существующего для указанного человека.</p><form id="discount-form"><div class="form-grid" id="discount-fields"></div><button type="submit" class="primary">Создать билет со скидкой</button><div class="message" id="discount-message" aria-live="polite"></div></form></section><section class="panel"><h2>Отмена события</h2><p>Удалить все билеты указанного события.</p><form id="cancel-form"><div id="cancel-fields"></div><label class="check"><input type="checkbox" name="confirmation">Подтверждаю удаление всех билетов этого события</label><button type="submit" class="primary">Отменить событие</button><div class="message" id="cancel-message" aria-live="polite"></div></form></section></div><section id="booking-result" class="result" hidden><h2>Созданный билет</h2><div class="table-wrap"><table><thead><tr>${ticketColumns.map(([key])=>`<th>${escape(labels[key])}</th>`).join('')}</tr></thead><tbody id="booking-row"></tbody></table></div></section>`;
 const paths=contract.booking.paths;const discountPath='/sell/discount/{ticket-id}/{person-id}/{discount}';const cancelPath='/event/{event-id}/cancel';
 const makeDefs=path=>paths[path].post.parameters.map(p=>({path:p.name,label:({'ticket-id':'ID исходного билета','person-id':'ID человека','event-id':'ID события',discount:'Скидка, %'})[p.name],required:p.required,schema:resolve(p.schema,contract.booking)}));
 const discount=makeDefs(discountPath);const cancel=makeDefs(cancelPath);$('#discount-fields').innerHTML=discount.map(def=>fieldHTML(def)).join('');$('#cancel-fields').innerHTML=cancel.map(def=>fieldHTML(def)).join('');bindForm($('#discount-form'),discount);bindForm($('#cancel-form'),cancel);
 for(const [id,path,defs,expected]of [['discount',discountPath,discount,201],['cancel',cancelPath,cancel,204]]){
  const form=$('#'+id+'-form');form.onsubmit=async event=>{event.preventDefault();const target=$('#'+id+'-message');let payload;try{payload=collect(form);if(id==='cancel'&&!form.elements.confirmation.checked)throw new Error('Подтвердите удаление билетов события.');}catch(error){message(target,error.message);return;}const button=$('button[type=submit]',form);button.disabled=true;message(target,'Выполнение…','success');if(id==='discount')$('#booking-result').hidden=true;
   try{let url=path;for(const def of defs)url=url.replace('{'+def.path+'}',encodeURIComponent(String(payload[def.path])));const result=await api('/reservation'+url,{method:'POST',expected});if(id==='discount'){if(!result||typeof result!=='object'||Array.isArray(result))throw new ApiError('Сервер не вернул созданный билет. Проверьте результат перед повторной отправкой.',expected,true);$('#booking-row').innerHTML='<tr>'+ticketColumns.map(([key])=>`<td>${escape(format(get(result,key),key))}</td>`).join('')+'</tr>';$('#booking-result').hidden=false;}message(target,id==='discount'?'Билет создан.':'Событие отменено. Билеты удалены.','success');}
   catch(error){message(target,error.message);if(error.uncertain)return;}button.disabled=false;
  };
 }
}
try{
 const response=await fetch('/assets/contracts.json',{cache:'no-cache'});if(!response.ok)throw new Error('Не удалось загрузить описание API.');contract=await response.json();
 $('#nav-'+(bookingPage?'booking':entity)).setAttribute('aria-current','page');
 if(bookingPage){renderBooking();app.setAttribute('aria-busy','false');}else{renderList();loadList();}
}catch(error){app.setAttribute('aria-busy','false');app.innerHTML=`<div class="message" role="alert">${escape(error.message)} Обновите страницу.</div>`;}
