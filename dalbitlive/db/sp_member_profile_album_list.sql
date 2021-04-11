CREATE DEFINER=`inforex`@`%` PROCEDURE `****`.`sp_member_profile_album_list`(
	IN `_data` LONGTEXT,
	OUT `ret` int,
	OUT `ext` longtext
)
BEGIN
    DECLARE m_idx          BIGINT UNSIGNED;
    DECLARE m_mem_no       BIGINT UNSIGNED;
    DECLARE m_totalCnt     INT;
    DECLARE m_profileImage VARCHAR(256);

    DECLARE v_txt_error_msg text;

DECLARE exit handler for SQLEXCEPTION
BEGIN
    GET DIAGNOSTICS CONDITION 1 v_txt_error_msg = MESSAGE_TEXT;

    ROLLBACK;

    SET ret = -9;
    SET ext = JSON_OBJECT('spName', 'sp_member_profile_album_list', 'desc', v_txt_error_msg);
    IF( @@server_id = 1 ) THEN
        INSERT INTO tb_log_data( sp_name, `data`, upd_date ) VALUES ( 'sp_member_profile_album_list_err', v_txt_error_msg, current_timestamp(6));
    END IF;
END;
    IF( @@server_id = 1 ) THEN
        INSERT INTO tb_log_data( sp_name, `data`, upd_date ) VALUES ( 'sp_member_profile_album_list', _data, current_timestamp(6));
    END IF;

    SET m_mem_no = CAST(REPLACE(JSON_EXTRACT( _data, '$.mem_no' ), '\"','') AS UNSIGNED);

    SELECT idx INTO m_idx FROM tb_member_basic WHERE mem_no = m_mem_no;
    IF( found_rows() = 0 ) THEN
        SET ret = -1;
        SET ext = JSON_OBJECT('spName', 'sp_member_profile_album_list', 'desc', 'mem_no not member', 'mem_no', m_mem_no);
    ELSE
        SELECT count(*) INTO m_totalCnt
        FROM tb_member_profile_album
        WHERE mem_no = m_mem_no;

        IF( m_totalCnt = 0 ) THEN
            SELECT image_profile INTO m_profileImage FROM tb_member_profile WHERE mem_no = m_mem_no;

            IF( m_profileImage <> '' ) THEN
                START TRANSACTION;

                INSERT INTO tb_member_profile_album ( mem_no, image_profile, leader_yn )
                VALUES ( m_mem_no, m_profileImage, 1 );

                COMMIT;
                SET m_totalCnt = 1;
            END IF;
        END IF;

        SELECT a.idx,
               a.mem_no,
               a.image_profile AS profileImage,
               a.leader_yn,
               b.mem_sex AS memSex
        FROM tb_member_profile_album a
                 INNER JOIN tb_member_basic b ON a.mem_no = b.mem_no
        WHERE a.mem_no = m_mem_no
        ORDER BY FIELD(a.leader_yn, 1) DESC, a.idx DESC;

    SET ret = m_totalCnt;
        SET ext = JSON_OBJECT('spName', 'sp_member_profile_album_list', 'desc', 'select success');
    END IF;
END