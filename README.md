# CacheServerProject

## 목적
- **캐시의 기본 원리와 동작을 구현해보며 학습**
  - put, get, remove 기본 연산
  - eviction, expire 등 캐시와 관련된 정책

- **다중 클라이언트를 관리하기 위한 NIO 네트워크 프로그래밍 및 Java 학습**
  - Selector, SocketChannel 
  - Serialize-Deserialize
  - Thread, Collection Framework

## 구현
### 기본 연산
- **put(key, value)**
  - 해당 key가 존재 : key에 대한 value를 업데이트하고 이전의 value 리턴
  - 해당 key가 존재하지 않음 : null을 리턴
  - 해당 key가 존재하지만 expired 됨 : key에 대한 value를 업데이트하고 null 리턴
- **get(key)**
  - 해당 key가 존재 : key에 대한 value를 return하고 LRU에 대한 타임스탬프를 갱신(expire 타임스탬프는 그대로)
  - 해당 key가 존재하지 않음 : null 리턴
  - 해당 key가 존재하지만 expired 됨 : 해당 엔트리를 삭제하고 null 리턴
- **remove(key)**
    - 해당 key가 존재 : 해당 엔트리를 삭제하고 value 리턴
    - 해당 key가 존재하지 않음 : null 리턴
    - 해당 key가 존재하지만 expired 됨 : 해당 엔트리를 삭제하고 null 리턴

### 타임스탬프와 expire
- 모든 데이터는 저장된 순간 타임스탬프를 가지며, 두 가지 타임스탬프가 존재함
  1. timeStamp : LRU(Eviction)에 사용되는 타임스탬프 
     - 처음 put 연산 시 CacheValue 필드에 생성 됨
     - 이미 존재하는 key에 대해 put, get 연산 시 갱신 됨
  2. expireTimeStamp : expire에 사용되는 타임스탬프 
     - 처음 put 연산 시 CacheValue 필드에 생성 후에 불변함(final)
     - (현재 타임스탬프 - expireTimeStamp >= expireTime)인 경우에 expired로 판단하고 데이터 삭제
     - 이러한 판단과 삭제 동작을 수행하는 별도의 스레드를 통해 일정 주기(expireCheckTime)마다 체크함
       - 만약 expireTime이 지났으나 아직 체크되지 않은 엔트리의 경우 연산 시에 처리됨

### eviction
- **캐시 메모리가 최대 용량에 도달했을 때 메모리에서 데이터를 삭제 처리하는 이벤트**
- **Event-Based : put 연산 시 추가될 데이터의 크기가 maxSize보다 크면 발생**
  - expire가 time-based의 삭제 방식이라면, eviction은 event-based로 동작
  - cacheMemory에서 정해진 수 만큼의 키를 랜덤으로 샘플링하여 그 중 timeStamp가 가장 오래된 key를 삭제
    - LRU(Least Recently Used) 알고리즘 적용
      - Redis, ElastiCache 등 캐시의 eviction policy로 널리 사용됨
    - 이때의 timeStamp는 expireTimeStamp와는 다른 타임스탬프임
    - timeStamp는 처음 데이터가 put 될 때 기록되고 put, get 연산 시 해당 시간으로 갱신됨
      - cf. expireTimeStamp는 expire에서 사용되며, 불변함

### Server-Client 통신
- 클라이언트가 접속을 요청하면 서버가 accept하여 통신이 시작
- 클라이언트는 put, get, remove 연산을 할 수 있으며, 통신을 종료할 수 있음
  - 클라이언트가 통신을 종료하거나 서버가 stopServer()를 호출할 때 까지 연결 유지
- 서버에 여러 명의 클라이언트가 동시에 접속하고, cacheMemory에 접근할 수 있음
  - NIO 네트워크, Selector 등을 이용하여 서버를 구현
- 클라이언트는 key와 value를 직렬화하여 byte[]로 서버에 전달
  - 서버는 이를 받아서 key는 BytesKey 클래스로 래핑하고, value는 타임스탬프, 크기 등 여러 정보화 함께 CacheValue라는 클래스로 관리
  - byte[]로 직렬화 되기 때문에 객체도 주고 받을 수 있음

### Cache 클래스
- **Cache 클래스의 ConcurrentHashMap 타입의 cacheMemory에 데이터가 저장됨**
  - 여기에 (BytesKey key, CacheValue value)의 형태로 관리
- **CacheBuilder를 통해서 여러 변수를 설정함**
  - maxSize : 캐시 최대 사이즈 
  - initSize : 캐시 초기 사이즈
  - expireTime : expire 주기(밀리세컨드 단위) 
  - expireCheckTime : expire 체크 주기(밀리세컨드 단위) 
- **curCacheMemorySize**
  - 캐시 메모리의 사이즈로 put 연산 시 maxSize보다 커지면 eviction 발생

### Usage
- **프로젝트 디렉토리 구조는 CacheServerProject라는 루트 프로젝트와 그 안에 있는 Server, Client라는 두 개의 프로젝트로 구성됨**
    - 서버-클라이언트 두 개의 프로젝트를 하나의 레포지토리 구조에 저장하기 위함
- **JDK11 필요**
- **프로젝트 루트 디렉토리(이하 $HOME)에서 "./gradlew build" 명령어로 빌드**
- **$HOME/Server/build/libs와 $HOME/Client/build/libs 디렉토리에 각각 서버와 클라이언트에 대한 jar 파일이 존재**
    - java -jar 명령어를 통해 실행
    - 서버를 먼저 실행 후 클라이언트를 실행
    - 클라이언트의 요청 형식은 [Operation] [인자 1] [인자 2] 형태임
      - ex) put key1 value1
      - ex) get key1
