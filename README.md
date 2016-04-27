# test-http-server


## Тестовое задание

В исходниках простенький http-web сервис.
У которого на данный момент будет один uri: /trackings/
У него есть три метода: post, get, delete
Добавить событие, получить зареганное событие и удалить соответственно.

Примеры запросов:
```
$ curl -i -X POST --data 'user_id=1234&event=registration&timestamp=1438244091263&value=testuser2' http://127.0.0.1:9030/trackings
  HTTP/1.1 201 Created
  Content-Type: text/html; charset=UTF-8
  Content-Length: 18
  Server: Jetty(9.1.3.v20140225)

  Operation success
```
```
curl -i 'http://127.0.0.1:9030/trackings?event=registration&user_id=1234'
HTTP/1.1 200 OK
Content-Type: text/html; charset=UTF-8
Content-Length: 10
Server: Jetty(9.1.3.v20140225)

testuser2
```

Из логики есть дополнительно вот такой функционал:
- если приходит событие `event=registration`, то уходит get запрос на указанный в конфиге endpoint
- если приходит событие `event=levelup` и значение value = 10, то то уходит get запрос на указанный в конфиге endpoint


Необходимо:
Рассматривая указанный web сервер как черный ящик, необходимо отдельно написать и покрыть тестами логику `/trackings` (добавление, удаление, получение событий)

Дополнительно отдельными тестами убедиться, что сервер отсылает события на указанный в конфигах endpoint c помощью open-source'ного mock-server'а mountebank (http://www.mbtest.org/)


### запуск локально test-http-server'а
```
mvn clean install
mvn exec:java -Dexec.mainClass="ru.inn.httpserver.server.Launcher"
```

