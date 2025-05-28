# API Server

---

## Environment

| 항목        | 내용                                         |
|-------------|--------------------------------------------|
| Java        | **17**<br>로컬에 Java 17 이상 설치되어 있으면  17로 빌드됨 |
| Kotlin      | 1.9.25                                     |
| Gradle      | 8.5 (Wrapper 사용 권장: `./gradlew`)           |
| Spring Boot | 3.4.5                                      |
| IDE         | IntelliJ IDEA (Ultimate 또는 Education 권장)   |



## 실행환경별 환경설정 분리


### application.yml
```yaml
spring:
  profiles:
    active: dev  # dev, prod
```
### 환경설정 유의사항
  
  - **application-dev.yml / application-prod.yml** 은 환경에 맞게 분리하여 사용할 것
  - 실수로 운영 설정(prod)을 로컬에서 사용하는 등, 혼용하지 않도록 주의할 것
  - 환경별 민감한 정보(DB 계정 등)는 외부 Git 저장소에 노출되지 않도록 관리할 것
    

### CICD
main 브랜치에 commit 이 발생할 때 deploy 됨.
delpoy 는 AMI 를 생성하는 packer 사용 (/packer)
user-data 는 ec2에 올라갔을 대 할 작업 (ENV 주입이 이때 발생함. ) - secret key 에는 다 설정했음.
.hlc 는 pakcer setting. 어떤 ec2 이미지 생성할 것인지 
