const { chromium } = require('playwright');
const assert = require('node:assert/strict');
const path = require('node:path');

const files = path.resolve(__dirname, '../frontend');
const person = {id: 2, birthday:'2000-01-01', location:{x:0,y:0,name:'Тест'}};
const ticket = {id:1,name:'Тест',person,price:33.333332,eventId:1,coordinates:{x:1e-20,y:0},creationDate:'2026-01-01T23:30:00Z'};

async function check(browser) {
 const page = await browser.newPage();
 const calls = [];
 let loseResponse = true;
 await page.route('http://localhost/**', async route => {
  const req = route.request(), url = new URL(req.url());
  if (!url.pathname.startsWith('/api/v1/')) {
   const asset = url.pathname.startsWith('/assets/') ? url.pathname.slice(1) : 'index.html';
   return route.fulfill({path:path.join(files,asset)});
  }
  calls.push({path:url.pathname,method:req.method(),key:req.headers()['idempotency-key'],data:req.postData()});
  if (url.pathname.includes('/sell/discount')) {
   if (loseResponse) { loseResponse=false; return route.abort(); }
   return route.fulfill({status:201,json:ticket});
  }
  if (url.pathname === '/api/v1/tickets' || req.method()==='PUT') {
   return route.fulfill({status:req.method()==='PUT'?200:201,json:ticket});
  }
  return route.fulfill({json:{content:[url.pathname.includes('/persons')?person:ticket],page:0,totalElements:1}});
 });
 await page.goto('http://localhost/tickets/');
 const stamp = page.locator('#rows time');
 await stamp.waitFor();
 const cdp = await page.context().newCDPSession(page);
 for (const [timezoneId,expected] of [['UTC','01.01.2026, 23:30'],['Europe/Moscow','02.01.2026, 02:30'],['America/Los_Angeles','01.01.2026, 15:30']]) {
  await cdp.send('Emulation.setTimezoneOverride',{timezoneId});
  await page.evaluate(()=>window.dispatchEvent(new Event('focus')));
  assert.equal(await stamp.textContent(),expected);
  assert.equal(await stamp.getAttribute('datetime'),ticket.creationDate);
  assert.ok((await page.locator('#rows').textContent()).includes('01.01.2000'));
 }
 await page.locator('[data-edit="0"]').click();
 await page.locator('#object-form [name="personId"]').selectOption('2');
 const unchanged=page.waitForRequest(r=>r.method()==='PUT');
 await page.locator('#object-form').evaluate(f=>f.requestSubmit());
 const original=(await unchanged).postDataJSON();
 assert.equal(original.price,ticket.price);
 assert.equal(original.coordinates.x,ticket.coordinates.x);
 await page.locator('#object-form').waitFor({state:'hidden'});
 await stamp.waitFor();
 const invalid = ['01,7','01.7','-01.7','1,7','0.'+'9'.repeat(80),'1.1234567890123456','9007199254740993','1e307','1e-333','-495','Infinity','NaN','1.','０.７'];
 async function rejected(form,input,values) {
  const before = calls.length;
  for (const value of values) {
   await input.fill(value);
   await form.evaluate(f=>f.requestSubmit());
  }
  assert.equal(calls.length,before);
 }
 for (const mode of ['create','edit']) {
  await page.locator(mode==='create'?'#create':'[data-edit="0"]').click();
  const form=page.locator('#object-form'), field=name=>form.locator('[name="'+name+'"]');
  await field('personId').selectOption('2');
  await field('name').fill('Тест');
  await field('price').fill('20');
  await field('eventId').fill('1');
  await rejected(form,field('coordinates.x'),invalid);
  await field('coordinates.x').fill('1.7');
  await rejected(form,field('price'),['16777217','1.99999999','-1']);
  await field('price').fill('1.7');
  await rejected(form,field('coordinates.y'),['01','1.7','2147483648','-263']);
  await field('coordinates.y').fill('0');
  const sent=page.waitForRequest(r=>r.method()===(mode==='create'?'POST':'PUT') && /\/tickets(?:\/1)?$/.test(r.url()));
  await form.evaluate(f=>f.requestSubmit());
  assert.equal((await sent).postDataJSON().coordinates.x,1.7);
  await form.waitFor({state:'hidden'});
  await stamp.waitFor();
 }
 await page.locator('details').first().locator('summary').click();
 const filters=page.locator('#filters'), x=filters.locator('[name="coordinatesXFrom"]');
 await rejected(filters,x,invalid);
 await x.fill('1.7');
 let sent=page.waitForRequest(r=>r.url().endsWith('/tickets/search'));
 await filters.evaluate(f=>f.requestSubmit());
 assert.equal((await sent).postDataJSON().filter.coordinatesXFrom,1.7);
 await stamp.waitFor();
 const quick=page.locator('#quick-search');
 await quick.locator('select').selectOption('coordinates.x');
 await rejected(quick,quick.locator('[name="quick-value"]'),invalid);
 await quick.locator('[name="quick-value"]').fill('2.7');
 sent=page.waitForRequest(r=>r.url().endsWith('/tickets/search'));
 await quick.evaluate(f=>f.requestSubmit());
 assert.equal((await sent).postDataJSON().filter.coordinatesXTo,2.7);
 await page.goto('http://localhost/booking/');
 const form=page.locator('#discount-form');
 async function fillSale() {
  for(const [name,value] of [['ticket-id','1'],['person-id','2'],['discount','10']]) await form.locator('[name="'+name+'"]').fill(value);
 }
 await fillSale();
 await rejected(form,form.locator('[name="discount"]'),['01.7','101','-1','1.99999999']);
 await fillSale();
 await form.locator('button').click();
 await page.getByText(/Результат продажи пока неизвестен/).waitFor();
 await page.reload();
 await fillSale();
 await form.locator('button').click();
 await page.locator('#booking-row time').waitFor();
 await form.locator('button').click();
 await page.waitForFunction(()=>!document.querySelector('#discount-form button').disabled);
 const sales=calls.filter(x=>x.path.includes('/sell/discount'));
 assert.equal(sales.length,3);
 assert.ok(sales[0].key);
 assert.equal(sales[0].key,sales[1].key);
 assert.notEqual(sales[1].key,sales[2].key);
 await page.close();
}

(async()=>{
 const browser=await chromium.launch();
 try {await check(browser); console.log('Проверены часовые пояса, формы, отправленные запросы и повтор продажи.');}
 finally {await browser.close();}
})().catch(error=>{console.error(error);process.exitCode=1;});
