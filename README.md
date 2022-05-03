# CacheServerProject

## 목적
- 다양한 프로그램에 사용되는 캐시의 기본 원리와 동작을 이해하고 학습하기 위함

## 요구 사항
### 기본 연산
- **put(key, value)**
  - 해당 key가 존재 : key에 대한 value를 업데이트하고 이전의 value를 리턴
  - 해당 key가 존재하지 않음 : null을 리
  - 해당 key가 존재하지만 expired 됨 : key에 대한 value를 업데이트하고 만료된 timestamp를 리턴
- **get(key)**
  - 해당 key가 존재 : key에 대한 value를 return하고 expire 타임스탬프를 갱신
  - 해당 key가 존재하지 않음 : null을 리턴
  - 해당 key가 존재하지만 expired 됨 : 해당 엔트리를 삭제하고 만료된 timestamp를 리턴
- **remove(key)**
    - 해당 key가 존재 : 해당 엔트리를 삭제하고 value를 리턴
    - 해당 key가 존재하지 않음 : null을 리턴
    - 해당 key가 존재하지만 expired 됨 : 해당 엔트리를 삭제하고 만료된 timestamp를 리턴
- **expired된 엔트리에 대한 연산 시 만료된 timestamp를 리턴하는 이유**
  - expire 된 key는 expire로 set 된 즉시 메모리에서 지워지지 않음
      - memcached의 정책을 참고
  - 클라이언트에게 null을 리턴하게 되면 클라이언트는 해당 키가 없는 것인지, expire 된 것인지 알 지 못함
      - Redis의 정책을 참고

### Expire
- 모든 데이터는 저장된 순간 타임스탬프를 가지며, 지정된 시간 이후에는 expired 상태가 됨
- expired 된 엔트리가 실제로 삭제되는 경우
    - key에 대한 접근이 이루어졌을 때
        - 이벤트 발생 시 삭제(eviction)
    - removeAllExpiredEntryTime을 설정하였을 때
        - 일정 주기로 삭제

### Server-Client 통신
- 클라이언트가 접속을 요청하면 서버가 accept하여 통신이 시작
- 클라이언트는 put, get, remove 연산을 할 수 있으며, 통신을 종료할 수 있음
  - 클라이언트가 통신을 종료하거나 서버가 stopServer()를 호출할 때 까지 연결 유지
- 서버에 여러 명의 클라이언트가 동시에 접속하고, cacheMemory에 접근할 수 있음
  - NIO 네트워크, Selector 등을 이용하여 서버를 구현
- 클라이언트는 key와 value를 직렬화하여 byte[]로 서버에 전달
  - 서버는 이를 받아서 key는 BytesKey 클래스로 래핑하고, value는 타임스탬프, 크기, 만료 여부 등 여러 정보화 함께 CacheValue라는 클래스로 관리
  - 클라이언트는 서버가 데이터를 어떻게 저장하는 지를 알지 못해도 되며, 서버로부터 byte[] 타입을 받아 역직렬화 함

### Cache 클래스
- **Cache 클래스의 ConcurrentHashMap 타입의 cacheMemory에 데이터가 저장됨**
  - 여기에 (BytesKey key, CacheValue value)의 형태로 관리
- **CacheBuilder를 통해서 여러 변수를 설정함**
  - maxSize : 캐시 최대 사이즈 
  - initSize : 캐시 초기 사이즈
  - expireMilliSecTime : expire 주기(밀리세컨드 단위) 
  - expireCheckSecTime : expire 체크 주기(밀리세컨드 단위) 
  - removeAllExpiredEntryTime : expire 된 entry 삭제 주기. 0으로 설정시 비활성화
  - expireQueueSize : expire 된 entry가 저장되는 큐의 크기
- **curCacheMemorySize**
  - 캐시 메모리의 사이즈로 put 연산 시 해당 값이 maxSize보다 커지면 eviction 발생

### eviction
- **캐시 메모리에 최대 용량에 도달했을 때 메모리에서 데이터를 삭제 처리하는 이벤트**
- ****크게 이벤트 기반과 시간 기반이 있음****
  - **Condition 1. get 연산 시 추가될 데이터의 크기가 maxSize보다 크면 발생**
    - Method 1. expiredQueue가 비어있지 않다면 큐의 맨 앞에 있는 key를 삭제
    - Method 2. expiredQueue가 비어있다면 랜덤으로 키를 샘플링하여 가장 오래된 key를 삭제
      - LRU(Least Recently Used) 알고리즘 적용
        - Redis, Elastic Cache 등 eviction policy로 널리 사용됨
        - 타임스탬프는 처음 데이터가 put 될 때 기록되고, get 연산 시 해당 시간으로 갱신됨
  - **Condition 2. removeAllExpiredEntryTime을 설정했을 시 일정 주기마다 expire 된 데이터를 모두 삭제**
    - Cache 객체를 생성할 때 CacheBuilder에서 설정한 값을 따름

### Usage
- **프로젝트 디렉토리 구조는 CacheServerProject라는 루트 프로젝트와 그 안에 있는 Server, Client라는 두 개의 프로젝트로 구성됨**
    - 서버-클라이언트 두 개의 프로젝트를 하나의 레포지토리 구조에 저장하기 위함
- **프로젝트 루트 디렉토리(이하 $HOME)에서 "./gradlew build" 명령어로 빌드**
- **$HOME/Server/build/libs와 $HOME/Client/build/libs 디렉토리에 각각 서버와 클라이언트에 대한 jar 파일이 존재**
    - java -jar 명령어를 통해 실행
    - 서버를 먼저 실행 후 클라이언트를 실행