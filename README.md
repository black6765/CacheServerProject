# CacheServerProject

### 목적
- 다양한 프로그램에 사용되는 캐시의 기본 원리와 동작을 이해하고 학습하기 위함

### 요구 사항
1. 기본 연산
    - put(key, value)
      - 해당 key가 존재 : key에 대한 value를 업데이트하고 이전의 value를 리턴
      - 해당 key가 존재하지 않음 : null을 리
      - 해당 key가 존재하지만 expired 됨 : key에 대한 value를 업데이트하고 "Expired key"를 리턴
    - get(key)
      - 해당 key가 존재 : key에 대한 value를 return하고 expire 타임스탬프를 갱신
      - 해당 key가 존재하지 않음 : null을 리턴
      - 해당 key가 존재하지만 expired 됨 : 해당 엔트리를 삭제하고 "Expired key"를 리턴
    - remove(key)
        - 해당 key가 존재 : 해당 엔트리를 삭제하고 value를 리턴
        - 해당 key가 존재하지 않음 : null을 리턴
        - 해당 key가 존재하지만 expired 됨 : 해당 엔트리를 삭제하고 "Expired key"를 리턴
    - expired된 엔트리에 대한 연산 시 "Expired Key"를 리턴하는 이유
      - 
2. Server-Client 통신
    - 클라이언트가 접속을 요청하면 서버가 accept하여 통신이 시작
    - 클라이언트는 put, get, remove 연산을 할 수 있으며, 통신을 종료할 수 있음
      - 클라이언트가 통신을 종료하거나 서버가 stopServer()를 호출할 때 까지 연결 유지
    - 

3. 다중 클라이언트 지원
    - 서버에 다수의 클라이언트가 동시에 접속할 수 있어야 함
    - 따라서 동시성 문제 해결이 필요

4. eviction
    - 캐시 메모리에 일정 용량에 도달했을 때 메모리에서 데이터를 삭제 처리
    

### 추가 사항
- TTL 설정
    - eviction이 메모리 용량이 일정 임계치에 도달 했을 때 실행하는 것이라면, TTL은 시간적 측면
    - 데이터가 put() 되고 일정 시간이 지날 시 메모리에서 삭제
- putAll(), getAll(), removeAll() 연산
    - batch 서비스

### 구현
- Cache 메모리로는 ConcurrentHashMap을 사용
- 통신 방식은 SocketChannel을 이용한 NIO 네트워크 방식
    - 서버는 싱글 스레드로 Selector를 사용하여 이벤트를 감지 후 적절한 응답을 취함
- eviction은 LRU, FIFO 등 다양한 알고리즘을 적용할 수 있음
    - 본 프로젝트에서는 기본적으로 LRU 알고리즘을 적용함
    - 큐처럼 사용하는 LinkedList를 이용하여, eviction 시 삭제 대상을 관리함

### **설명 & Usage**
- **localhost 내부에서의 통신, 외부 네트워크와의 통신에서 정상 작동 확인**
- 통신 기본 값은 하나의 localhost에서 Server와 Client를 함께 실행하도록 되어 있음
    - `socketChannel.connect(new InetSocketAddress("hostname", 포트))에서 변경 가능`
- **2022/4/27 기준 피드백을 받기 전 v.0.1.0**
- **프로젝트 디렉토리 구조는 CacheServerProject라는 루트 프로젝트와 그 안에 있는 Server, Client라는 두 개의 프로젝트로 구성됨**
    - 서버-클라이언트 두 개의 프로젝트를 하나의 레포지토리 구조에 저장하기 위함
- **프로젝트 루트 디렉토리(이하 $HOME)에서 "./gradlew build" 명령어로 빌드**
- **$HOME/Server/build/libs와 $HOME/Client/build/libs 디렉토리에 각각 서버와 클라이언트에 대한 jar 파일이 존재**
    - java -jar 명령어를 통해 실행
    - **서버 먼저 실행시켜야 하며, 클라이언트는 한 번의 실행 당 1번의 연산을 할 수 있음**
        - 클라이언트는 put, get, remove 연산을 선택할 수 있고, 각 연산에 따른 argument(s)를 서버에게 보냄
        - 서버는 요청을 받아서 처리하고, 이에 대한 return을 클라이언트에게 보냄
            - 이에 대한 로그를 서버와 클라이언트에 각각 출력
        - 클라이언트의 request에 대한 리턴 값을 클라이언트가 받은 후 연결 종료
        - test에서는 loop를 통해 다중 클라이언트, 대량의 request에 대한 연산에서 이상 없음 확인
    - **서버는 한번 실행 시 종료하지 않는 한 cacheMemory에 데이터 저장**
        - CacheImpl에 정의된 메모리 최대 사이즈에 도달하면 다음 put 연산 시에 eviction이 실행됨
            - 전체 메모리의 25%를 LRU 알고리즘으로 삭제 처리
