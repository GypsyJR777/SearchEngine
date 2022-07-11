# SearchEngine
____
:grey_exclamation: _Java v1.8.0_322_

:page_facing_up: _Stack_:
Spring Framework,
JDBC,
Hibernate,
JSOUP,
SQL,
Morphology Library Lucene.
____
application.yaml
```yaml
conf:
  sitesUrl:
    - http://www.playback.ru
    - https://www.skillbox.ru
  sitesName:
    - Плейбек.ру
    - Skillbox
```
____
Statistics method returns info about indexed sites to the dashboard.
![image](https://user-images.githubusercontent.com/42184326/178167379-967a8cd2-544f-4bb9-bd5b-0b070b3a090d.png)
____
Management section used to start/stop indexing or to index/reindex specific webpage or all webpages, but page have to be related to sites given in application.yml.
![image](https://user-images.githubusercontent.com/42184326/178167559-6789add3-1902-4f28-8a38-b81126cf5683.png)
____
The search results looks likes this:
![image](https://user-images.githubusercontent.com/42184326/178167659-392fe2af-29c4-4bbe-af1e-885cfdfb7729.png)
