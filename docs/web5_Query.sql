-- web5 SQL
USE busan14;

-- 1. 회원 정보 테이블
CREATE TABLE member (
                        member_id       varchar(30)     PRIMARY KEY,		-- 아이디
                        member_password varchar(100)    NOT NULL,			-- 비밀번호(암호화)
                        member_name     varchar(30)     NOT NULL,			-- 이름
                        email           varchar(50),						-- 이메일
                        phone           varchar(30),						-- 전화번호
                        address         varchar(200),						-- 주소
                        enabled tinyint(1) DEFAULT 1 					    -- 계정 상태(1: 사용가능, 0:사용불가능)
                            CHECK (enabled IN (0, 1)),
                        rolename varchar(30) DEFAULT 'ROLE_USER' 		    -- 권한 구분
                            CHECK (rolename IN ('ROLE_USER', 'ROLE_ADMIN'))
);

-- 자동 로그인 용(Remember-me)
CREATE TABLE persistent_logins (
                                   series       VARCHAR(64)     PRIMARY KEY,
                                   username     VARCHAR(64)     NOT NULL,
                                   token        VARCHAR(64)     NOT NULL,
                                   last_used    TIMESTAMP       NOT NULL
);
SELECT * from persistent_logins;

-- 2. 게시판 테이블
CREATE TABLE board (
                       board_num        integer         AUTO_INCREMENT PRIMARY KEY, 	-- 게시글 번호
                       member_id        varchar(30),								    -- 작성자 id (외래키)
                       title            varchar(1000)   NOT NULL,						-- 글 제목
                       contents         text            NOT NULL,						-- 글 내용
                       view_count       integer         DEFAULT 0,						-- 조회수
                       like_count       integer         DEFAULT 0,						-- 추천수
                       original_name    varchar(300),							        -- 첨부파일 원래 이름
                       file_name        varchar(100),								    -- 첨부파일 변경된 이름
                       create_date timestamp DEFAULT CURRENT_TIMESTAMP,	    -- 작성일
                       update_date timestamp DEFAULT CURRENT_TIMESTAMP 	    -- 수정일
                           ON UPDATE CURRENT_TIMESTAMP, 	                -- 수정시 자동 갱신 가능

    -- 외래키 제약조건
                       CONSTRAINT fk_board_member FOREIGN KEY (member_id)
                           REFERENCES member (member_id) ON DELETE SET NULL
);

-- 3. 댓글 테이블
CREATE TABLE reply (
                       reply_num        integer         AUTO_INCREMENT PRIMARY KEY, 	-- 댓글 번호
                       member_id        varchar(30), 								    -- 작성자 id (외래키)
                       contents         varchar(2000)   NOT NULL,					    -- 댓글 내용
                       create_date timestamp DEFAULT CURRENT_TIMESTAMP,	    -- 작성 시간
                       board_num integer,									-- 게시글 번호 (외래키)

    -- 외래키 제약조건
                       CONSTRAINT fk_reply_member FOREIGN KEY (member_id)
                           REFERENCES member (member_id) ON DELETE SET NULL,
                       CONSTRAINT fk_reply_board FOREIGN KEY (board_num)
                           REFERENCES board (board_num) ON DELETE CASCADE
);

-- 4. 게시글 추천 이력 테이블
CREATE TABLE board_like (
                            like_num        integer         AUTO_INCREMENT PRIMARY KEY,     -- 추천 이력 번호
                            board_num       integer         NOT NULL,                       -- 추천된 게시글 번호 (외래키)
                            member_id       varchar(30)     NOT NULL,                       -- 추천한 회원 id (외래키)
                            create_date timestamp DEFAULT CURRENT_TIMESTAMP,     -- 추천 일시

    -- 외래키 제약조건
                            CONSTRAINT fk_like_board FOREIGN KEY (board_num)
                                REFERENCES board (board_num) ON DELETE CASCADE,
                            CONSTRAINT fk_like_member FOREIGN KEY (member_id)
                                REFERENCES member (member_id) ON DELETE CASCADE,

    -- 핵심: 한 사용자가 한 게시글에 중복으로 추천하는 것을 DB 레벨에서 차단
                            CONSTRAINT uk_board_member UNIQUE (board_num, member_id)
);


-- -------------------------------------------------------------------------------------------------
-- 조회
SELECT * FROM member;
SELECT * FROM board;
SELECT * FROM reply;
COMMIT;

-- 더미 데이터 저장 (페이징 처리용)
INSERT INTO board (
                    member_id, title, contents, view_count
                  , like_count, original_name, file_name
)
SELECT
    'aaa',                  -- 작성자 ID
    concat('샘플 제목 ', seq),
    concat('샘플 내용입니다. 게시글 번호 ', seq),
    floor(rand() * 100),    -- 조회수: 0~99 랜덤
    floor(rand() * 50),      -- 추천수: 0~49 랜덤
    concat('sample', seq, '.txt'),
    concat('file_', seq, '.dat')
FROM (
         select 1 as seq
         union all select 2  union all select 3  union all select 4  union all select 5
         union all select 6  union all select 7  union all select 8  union all select 9
         union all select 10 union all select 11 union all select 12 union all select 13
         union all select 14 union all select 15 union all select 16 union all select 17
         union all select 18 union all select 19 union all select 20 union all select 21
         union all select 22 union all select 23 union all select 24 union all select 25
         union all select 26 union all select 27 union all select 28 union all select 29
         union all select 30 union all select 31 union all select 32 union all select 33
         union all select 34 union all select 35 union all select 36 union all select 37
         union all select 38 union all select 39 union all select 40 union all select 41
         union all select 42 union all select 43 union all select 44 union all select 45
         union all select 46 union all select 47 union all select 48 union all select 49
         union all select 50
     ) AS seqs;