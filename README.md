# 프로젝트명
> 점심한끼 (LunchOnMeal)

![](./README.gif)

# 프로젝트 내용
혼자 점심을 먹는 직장인들이 점점 많아지고 있는 현대 사회에서 주위에 근무하고 있는 직장인들이 같이 점심 한끼 같이 할 수 있는 문화가 있으면 좋을 것 같다는 생각으로 기획하였습니다. 
자신의 근무지 근처의 지역 맛집을 등록 할 수 있고 맛집을 등록한 사용자끼리 간단한 채팅과 사진을 주고 받을 수 있습니다. 

(신입 개발자일때 포트폴리오로 만들어본 안드로이드 데모로 현재는 실행되지 않습니다.)
<br/>
<br/>


## 개발 환경 및 사용기술
* Language : Java, C++, PHP

* OS : Android, Ubuntu 16.04.2 LTS

* WebServer : Apache

* Database : MySQL, SQLite

* Library : OpenCV, Glide, GoolgeMap API, NaverMap API,Tmap API, Okhttp3, FCM

* Protocol : HTTP, TCP/IP, SMTP


<br/>

## 기능 설명

1. 회원가입 + 로그인기능
- 정규식으로 회원가입에 대한 문자열을 검사
- SMTP(Simple Mail Transfer Protocol)를 이용하여 메일 인증이 되어야 회원가입 가능  
<br/>
 
2. 프로필 사진 업로드
- OpenCV를 이용한 얼굴 인식 기능 및 프로필 사진 업로드  
<br/>

3. 맛집 등록 기능
- 네이버 지도 API를 이용한 지점 검색  
<br/>

4. 맛집 분류 기능
- ListView에서 Filterable 인터페이스를 구현하여 카테고리별, 거리별로 분류하여 표시
- GoogleMap에서 현재 위치의 좌표를 이용하여 카테고리별 거리별로 분류하여 표시
<br/>


5. 맛집에 대한 길찾기 기능

- DaumMap을 이용한 대중교통 길찾기
- Tmap Api와 GoogleMaps을 이용한 도보 길찾기 기능
<br/>

6. 공유 기능
- 맛집에 대한 정보를 카카오톡으로 공유 하는 기능
<br/>


7. 채팅기능
- TCP Socket을 이용한 다중채팅 기능
<br/>

