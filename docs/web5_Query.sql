-- web5 SQL
USE busan14;

-- 1. 회원 정보 테이블
CREATE TABLE member (
    member_id varchar(30) PRIMARY KEY,				-- 아이디
    member_password varchar(100) NOT NULL,			-- 비밀번호(암호화)
    member_name varchar(30) NOT NULL,				-- 이름
    email varchar(50),								-- 이메일
    phone varchar(30),								-- 전화번호
    address varchar(200),							-- 주소
    enabled tinyint(1) DEFAULT 1 					-- 계정 상태(1: 사용가능, 0:사용불가능)
        CHECK (enabled IN (0, 1)),
    rolename varchar(30) DEFAULT 'ROLE_USER' 		-- 권한 구분
        CHECK (rolename IN ('ROLE_USER', 'ROLE_ADMIN'))
);
