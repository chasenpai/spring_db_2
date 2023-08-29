drop table if exists item CASCADE;
create table item
(
    id        bigint generated by default as identity,
    item_name varchar(10),
    price     integer,
    quantity  integer,
    primary key (id)
);

--데이터베이스 기본 키는 3가지 조건을 모두 만족해야 함
    --null 은 허용하지 않는다, 유일해야 한다, 변해선 안됨

--테이블의 기본 키를 선택하는 전략
    --자연키
        --비즈니스에 의미가 있는 키
        --주민등록번호, 이메일, 전화번호 등

    --대리키
        --비즈니스와 관련 없는 임의로 만들어진 키, 대체 키로도 불린다
        --시퀸스, auto_increment, identity, 키생성 테이블 등

    --자연키 보단 대리키를 권장
        --이메일 전화번호는 바뀔 수 있다
        --심지어 주민번호도 바뀔 수 있다

    --비즈니스 환경은 언젠가 변한다
        --예)정보관리 정책 변경으로 인해해 주민번호를 데이터베이스에 저장할 수 없게 바뀌었다
