<p align="center">
    <img width="200px;" src="https://raw.githubusercontent.com/woowacourse/atdd-subway-admin-frontend/master/images/main_logo.png"/>
</p>
<p align="center">
  <img alt="npm" src="https://img.shields.io/badge/npm-%3E%3D%205.5.0-blue">
  <img alt="node" src="https://img.shields.io/badge/node-%3E%3D%209.3.0-blue">
  <a href="https://edu.nextstep.camp/c/R89PYi5H" alt="nextstep atdd">
    <img alt="Website" src="https://img.shields.io/website?url=https%3A%2F%2Fedu.nextstep.camp%2Fc%2FR89PYi5H">
  </a>
  <img alt="GitHub" src="https://img.shields.io/github/license/next-step/atdd-subway-service">
</p>

<br>

# 인프라공방 샘플 서비스 - 지하철 노선도

<br>

## 🚀 Getting Started

### Install
#### npm 설치
```
cd frontend
npm install
```
> `frontend` 디렉토리에서 수행해야 합니다.

### Usage
#### webpack server 구동
```
npm run dev
```
#### application 구동
```
./gradlew clean build
```
<br>

## 미션

* 미션 진행 후에 아래 질문의 답을 README.md 파일에 작성하여 PR을 보내주세요.

### 0단계 - pem 키 생성하기

1. 서버에 접속을 위한 pem키를 [구글드라이브](https://drive.google.com/drive/folders/1dZiCUwNeH1LMglp8dyTqqsL1b2yBnzd1?usp=sharing)에 업로드해주세요

2. 업로드한 pem키는 무엇인가요.
- KEY-ch0213

### 1단계 - 망 구성하기
1. 구성한 망의 서브넷 대역을 알려주세요
- 대역 : 
  - ch0213-public-a : 192.168.7.0/26
  - ch0213-public-c : 192.168.7.64/26
  - ch0213-internal-a : 192.168.7.128/27
  - ch0213-bastion-c : 192.168.7.160/27

2. 배포한 서비스의 공인 IP(혹은 URL)를 알려주세요

- URL : http://chkim-infra-workshop.kro.kr/



---

### 2단계 - 배포하기
1. TLS가 적용된 URL을 알려주세요

- URL : https://chkim-infra-workshop.kro.kr/

2. 설정 파일 나누기
- application.yml
- application-local.yml
- application-test.yml
- application-prod.yml (submodule)
```
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://192.168.7.151:3306/subway
    username: root
    password: masterpw

  jpa:
    hibernate:
      ddl-auto: validate

  flyway:
    baselineOnMigrate: true
    enabled: true

account:
  name: produser
  password: prod
```

- application-auth.yml (submodule)
```
security:
  jwt:
    token:
      secret-key: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIiLCJuYW1lIjoiSm9obiBEb2UiLCJpYXQiOjE1MTYyMzkwMjJ9.ih1aovtQShabQ7l0cINw4k1fagApg3qLWiB8Kt59Lno
      expire-length: 3600000
```

3. flyway 적용
- 기존 테이블에 대한 V1__init.sql 작성
- prod 환경에서만 적용하기 위해 application.yml의 flyway 기본 설정을 false로 변경

4. submodule 적용
- 운영 DB에 대한 정보가 있는 application-prod.yml
- jwt token secret-key가 있는 application-auth.yml

5. 정적테스트(SonarLint)

<img width="311" alt="스크린샷 2022-06-26 오후 10 21 34" src="https://user-images.githubusercontent.com/49121847/175816175-275ee162-f889-412f-b7b6-b4060263d883.png">

6. 로컬테스트(MultiRun)

<img width="251" alt="스크린샷 2022-06-26 오후 10 24 06" src="https://user-images.githubusercontent.com/49121847/175816276-921521bf-ac6b-42fb-b90d-bfdd59b99241.png">

---

### 3단계 - 배포 스크립트 작성하기

1. 작성한 배포 스크립트를 공유해주세요.
```shell
#!/bin/bash

## 변수 설정
txtrst='\033[1;37m' # White
txtred='\033[1;31m' # Red
txtylw='\033[1;33m' # Yellow
txtpur='\033[1;35m' # Purple
txtgrn='\033[1;32m' # Green
txtgra='\033[1;30m' # Gray

EXECUTION_PATH=$(pwd)
SHELL_SCRIPT_PATH=$(dirname $0)
BRANCH=$1
PROFILE=$2

## 변경사항 확인
function check_df() {
  echo -e "${txtylw}=======================================${txtrst}"
  echo -e "${txtgrn} << check_df >>${txtrst}"
  echo -e "${txtylw}=======================================${txtrst}"

  git fetch
  master=$(git rev-parse $BRANCH)
  remote=$(git rev-parse origin $BRANCH)
  remote=($(echo $remote | tr " ", "\n"))

  if [[ $master == ${remote[1]} ]]; then
    echo -e "[$(date)] Nothing to do!!! 😫"
    exit 0
  fi
}

## 저장소 pull
function pull() {
  echo -e "${txtylw}=======================================${txtrst}"
  echo -e "${txtgrn} << Pull Request 🏃♂️ >>${txtrst}"
  echo -e "${txtylw}=======================================${txtrst}"

  git pull origin $BRANCH
  git submodule foreach git pull https://$BRANCH:$SUBMODULE_TOKEN@github.com/$BRANCH/infra-workshop-config.git main
}

## gradle build
function build () {
  echo -e "${txtylw}=======================================${txtrst}"
  echo -e "${txtgrn} << gradle build >>${txtrst}"
  echo -e "${txtylw}=======================================${txtrst}"

  ./gradlew clean build
  JAR_FILE=$(basename -- build/libs/*.jar)
}

## 프로세스 pid를 찾고 종료
function kill_process () {
  echo -e "${txtylw}=======================================${txtrst}"
  echo -e "${txtgrn} << find and kill process >>${txtrst}"
  echo -e "${txtylw}=======================================${txtrst}"

  PID=$(pgrep -f $JAR_FILE)
  if [ -z "$PID" ]
  then
    echo -e ">> 실행중인 프로세스가 없습니다."
  else
    sudo kill -2 $PID
    echo -e ">> 실행중인 프로세스를 종료했습니다."
  fi
}

## 어플리케이션 실행
function run () {
  echo -e "${txtylw}=======================================${txtrst}"
  echo -e "${txtgrn} << 어플리케이션을 실행합니다. >>${txtrst}"
  echo -e "${txtylw}=======================================${txtrst}"

  nohup java -jar -Dspring.profiles.active=$PROFILE build/libs/$JAR_FILE 1> application.log 2>&1  &
}

## 배포 스크립트 실행
if [[ $# -ne 2 ]]
then
    echo -e "${txtylw}=======================================${txtrst}"
    echo -e "${txtgrn}  << 스크립트 🧐 >>${txtrst}"
    echo -e ""
    echo -e "${txtgrn} $0 브랜치이름 ${txtred}{ prod | dev }"
    echo -e "${txtylw}=======================================${txtrst}"
    exit
else
  check_df
  pull
  build
  kill_process
  run
fi
```

2. crontab
```shell
*/5 * * * * /home/ubuntu/nextstep/infra-subway-deploy/deploy.sh ch0213 prod >> /home/ubuntu/nextstep/infra-subway-deploy/deploy.log 2>&1
```
