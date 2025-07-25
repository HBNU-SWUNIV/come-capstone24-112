# 한밭대학교 컴퓨터공학과 거기112조팀

## 팀 구성
- **20201754 조수민** – 졸업 요건 데이터 정리, 스케줄 관리와 UI/UX, 백엔드 보조
- **20181614 문태신** – 앱의 전반적인 UI/UX를 수정하고 최적화
- **20201868 박지원** – 백엔드 및 데이터베이스 관리를 전담(메인 기능 구현)

## <u>Teamate</u> Project Background
- ### 필요성
  - 대부분의 대학생들이 본인의 **졸업 요건 충족 여부를 직접 계산**하고 있음
  - 학과에 따라 졸업 기준이 다양하고 복잡해 실수 가능성이 존재 
  - 커뮤니티 플랫폼 존재하지만, **졸업 요건 확인 기능이 연계된 서비스는 부재**
  - 학과별 맞춤 정보 공유 및 일정 관리가 가능한 자체 플랫폼의 필요성 대두
- ### 기존 해결책의 문제점
  - 종이 성적표, 엑셀 등 비효율적인 도구 사용
  - 전공 맞춤형 기능 부재 (예: 영역별 학점 계산, 필수 과목 자동 체크 등)

## 사용 기술 
![Android Studio](https://img.shields.io/badge/android%20studio-346ac1?style=for-the-badge&logo=android%20studio&logoColor=white) ![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white) ![AWS RDS](https://img.shields.io/badge/AWS%20RDS-%23FF9900.svg?style=for-the-badge&logo=amazon-aws&logoColor=white) ![AWS EC2](https://img.shields.io/badge/AWS%20EC2-%23FF9900.svg?style=for-the-badge&logo=amazon-aws&logoColor=white) ![Google Cloud Vision API](https://img.shields.io/badge/Google%20Cloud%20Vision%20API-%234285F4.svg?style=for-the-badge&logo=google-cloud&logoColor=white)

## System Design
<img width="947" height="392" alt="image" src="https://github.com/user-attachments/assets/23782033-3173-4825-95e2-09ecef377e43" />

  - ### System Requirements
    - **로그인 및 회원가입**
      - 이메일/비밀번호 기반 사용자 등록 및 로그인

    - **커뮤니티 기능**
      - 자유게시판, Q&A 게시판, 댓글 기능
      - 공지사항 열람 (홈페이지로 접속)
  
    - **졸업 요건 계산기**
      - 성적표 이미지 업로드
      - `Google Cloud Vision API` 활용 OCR 처리
      - 과목명 자동 추출 및 전공/교양 분류
      - 학점 계산 및 **졸업 충족 여부 시각화**
  
    - **관리 기능**
      - 게시글 관리
      - 내 정보 관리(닉네임, 비밀번호, 프로필사진 등)
     
## Conclusion
  - ### 이미지를 통해 졸업 요건 확인 가능
  - ### 졸업 계획 수립의 효율성 향상 

## Project Outcome
<img width="882" height="390" alt="1" src="https://github.com/user-attachments/assets/c85200fb-a14a-48fe-b5e8-e31d392cd715" />
<img width="886" height="394" alt="2" src="https://github.com/user-attachments/assets/a3a693aa-1ccb-4364-8204-dc795311b27c" />

