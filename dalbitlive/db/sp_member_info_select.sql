CREATE DEFINER=`inforex`@`%` PROCEDURE `****`.`sp_member_info_select`(
	IN `_data` LONGTEXT,
	OUT `ret` INT,
	OUT `ext` LONGTEXT
)
BEGIN
	DECLARE m_mem_no BIGINT;

	DECLARE m_logIdx BIGINT;
	DECLARE v_txt_error_msg TEXT;

DECLARE EXIT handler FOR SQLEXCEPTION
BEGIN
    GET DIAGNOSTICS CONDITION 1 v_txt_error_msg = MESSAGE_TEXT;
    SET ret = -9;
    SET ext = JSON_OBJECT('spName', 'sp_member_info_select', 'desc', v_txt_error_msg);

    IF @@server_id = 1 THEN
        INSERT INTO tb_log_data( sp_name, `data`, `json_type`, upd_date ) VALUES ( 'sp_member_info_select', _data, v_txt_error_msg,  CURRENT_TIMESTAMP(6));
    END IF;
END;

	IF @@server_id = 1 THEN
		INSERT INTO tb_log_data( sp_name, `data`, upd_date ) VALUES ( 'sp_member_info_select', _data, CURRENT_TIMESTAMP(6));
		SET m_logIdx = @@IDENTITY;
    END IF;

	SET m_mem_no = REPLACE(JSON_EXTRACT( _data, '$.mem_no' ), '\"','');

    SELECT
        a.mem_no AS memNo
         , a.mem_sex AS gender
         , a.mem_nick AS nickNm
         , CONCAT(a.mem_birth_year, LPAD(a.mem_birth_month, 2, '0') , LPAD(a.mem_birth_day, 2, '0')) AS birth
         , (YEAR(CURDATE()) - a.mem_birth_year) + 1 AS age
         , a.mem_userid AS memId
         , b.specialdj_badge AS badgeSpecial
         , b.image_profile AS profImgPath
         , b.msg_profile AS profMsg
         , c.ruby AS dalCnt
         , c.gold AS byeolCnt
         , IFNULL(e.recant_yn, 0) = 1 AS isCert
         , IFNULL(f.mailbox_onoff, 1) = 1 AS isMailboxOn
         , (SELECT COUNT(*) > 0
            tb_admin_test_account test
                INNER JOIN ****.tb_admin_menu_auth auth ON test.emp_no = auth.emp_no
                INNER JOIN ****.tb_admin_menu menu ON auth.menu_idx = menu.idx
            WHERE mem_no = a.mem_no
              AND menu.is_use = 1
              AND menu.mobile_yn = 'Y') AS isAdmin
    FROM tb_member_basic a INNER JOIN tb_member_profile b ON a.mem_no = b.mem_no
                           INNER JOIN tb_member_wallet c ON a.mem_no = c.mem_no
                           LEFT OUTER JOIN tb_member_certification e ON a.mem_no = e.mem_no
                           LEFT OUTER JOIN tb_member_setting f ON a.mem_no = f.mem_no
    WHERE a.mem_no = m_mem_no
    LIMIT 1;

    CALL sp_member_profile_album_list(CONCAT('{"mem_no":', m_mem_no,'}'), @ret,  @ext);

    SET ret = 0;
	SET ext = JSON_OBJECT('spName', 'sp_member_info_select', 'desc', 'token information success');

	IF @@server_id = 1 THEN
        UPDATE tb_log_data SET `json_type` = CONCAT('ret = ',ret, '  ', ext), query_time = (CURRENT_TIMESTAMP(6) - upd_date) WHERE idx = m_logIdx;
    END IF;
END