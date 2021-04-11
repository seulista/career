CREATE DEFINER=`inforex`@`%` PROCEDURE `****`.`sp_member_login`(
	IN `_data` LONGTEXT,
	OUT `ret` INT,
	OUT `ext` LONGTEXT
)
BEGIN
	DECLARE m_mem_slct VARCHAR(10);
	DECLARE m_id VARCHAR(50);
	DECLARE m_pw VARCHAR(50);
	DECLARE m_os VARCHAR(10);
	DECLARE m_adId VARCHAR(50);
	DECLARE m_deviceUuid VARCHAR(100);
	DECLARE m_deviceToken VARCHAR(256);
	DECLARE m_appVersion VARCHAR(50);
	DECLARE m_buildVersion VARCHAR(50);
	DECLARE m_browser VARCHAR(50);
	DECLARE m_location VARCHAR(50);
	DECLARE m_ip VARCHAR(50);
	DECLARE m_room_no VARCHAR(20);

	DECLARE m_idx BIGINT;
	DECLARE m_mem_no BIGINT;
	DECLARE m_memState TINYINT;
	DECLARE m_memSex VARCHAR(1);
	DECLARE m_blockDay TINYINT;
	DECLARE m_blockEndDate DATETIME;
	DECLARE m_rowCount INT(11) DEFAULT 0;
	DECLARE m_aMem_no BIGINT;
	DECLARE m_auth TINYINT;
	DECLARE m_aAuth TINYINT;
	DECLARE m_aRoom_no BIGINT;
	DECLARE m_state TINYINT;
	DECLARE m_memPwd VARCHAR(256);

	DECLARE m_connectedRoomCnt INT DEFAULT 0;
	DECLARE m_count INT DEFAULT 0;
	DECLARE m_deviceUuidCon VARCHAR(100);
	DECLARE m_roomList TEXT;

	DECLARE m_opCode SMALLINT;
	DECLARE m_opMsg LONGTEXT;

	DECLARE m_now DATETIME DEFAULT NOW();
	DECLARE m_blockCnt INT;

	DECLARE m_logIdx BIGINT;
	DECLARE v_txt_error_msg TEXT;

DECLARE EXIT handler FOR SQLEXCEPTION
BEGIN
    GET DIAGNOSTICS CONDITION 1 v_txt_error_msg = MESSAGE_TEXT;
    SET ret = -9;
        SET ext = JSON_OBJECT('spName', 'sp_member_login', 'desc', v_txt_error_msg);
    ROLLBACK;

    INSERT INTO tb_log_data_err( sp_name, `data`, `json_type`, upd_date ) VALUES ( 'sp_member_login', _data, v_txt_error_msg,  CURRENT_TIMESTAMP(6));
    UPDATE tb_log_data SET `json_type` = CONCAT('ret = ',ret, '  ', ext), query_time = (CURRENT_TIMESTAMP(6) - upd_date) WHERE idx = m_logIdx;
END;

    INSERT INTO tb_log_data( sp_name, `data`, upd_date ) VALUES ( 'sp_member_login', _data, CURRENT_TIMESTAMP(6));
    SET m_logIdx = @@IDENTITY;

	SET m_mem_slct = REPLACE(JSON_EXTRACT( _data, '$.memSlct' ), '\"','');
	SET m_id = REPLACE(JSON_EXTRACT( _data, '$.id' ), '\"','');
	SET m_pw = REPLACE(JSON_EXTRACT( _data, '$.pw' ), '\"','');
	SET m_os = REPLACE(JSON_EXTRACT( _data, '$.os' ), '\"','');
	SET m_adId = REPLACE(JSON_EXTRACT( _data, '$.adid' ), '\"','');
	SET m_deviceUuid = REPLACE(JSON_EXTRACT( _data, '$.deviceUuid' ), '\"','');
	SET m_deviceToken = REPLACE(JSON_EXTRACT( _data, '$.deviceToken' ), '\"','');
	SET m_appVersion = REPLACE(JSON_EXTRACT( _data, '$.appVersion' ), '\"','');
	SET m_buildVersion = REPLACE(JSON_EXTRACT( _data, '$.buildVersion' ), '\"','');
	SET m_browser = REPLACE(JSON_EXTRACT( _data, '$.browser' ), '\"','');
	SET m_location = REPLACE(JSON_EXTRACT( _data, '$.location' ), '\"','');
	SET m_ip = REPLACE(JSON_EXTRACT( _data, '$.ip' ), '\"','');
	SET m_room_no = REPLACE(JSON_EXTRACT( _data, '$.room_no' ), '\"','');

	SET m_room_no = if( length(ifnull(m_room_no, '')) = 0, 0,  m_room_no);

    SELECT COUNT(*) INTO m_blockCnt
    FROM tb_login_block
    WHERE block_text IS NOT NULL AND block_text != ''
    	AND block_end_date > m_now
        AND (
            ( block_type = 1 AND block_text = m_deviceUuid )
            OR ( block_type = 2 AND block_text = m_ip )
        );

    IF IFNULL(m_blockCnt, 0) > 0 THEN
		SET ret = -7;
		SET ext = JSON_OBJECT('spName', 'sp_member_login', 'desc', 'block ip or device id');
    ELSE
		START TRANSACTION;

		IF m_mem_slct = 'a' THEN
            SELECT mem_no INTO m_mem_no FROM tb_member_session_a WHERE device_uuid = m_deviceUuid ORDER BY last_upd_date DESC LIMIT 1;
            IF( FOUND_ROWS() > 0 ) THEN
                DELETE FROM tb_member_session_a WHERE device_uuid = m_deviceUuid;
                INSERT INTO tb_member_session_a( mem_no, os_type, device_uuid, device_token, app_version, build_version, browser, adid, location, ip, last_upd_date ) VALUES
                ( m_mem_no, m_os, m_deviceUuid, m_deviceToken, m_appVersion, m_buildVersion, m_browser, m_adid, m_location, m_ip, NOW());
            ELSE
				SET m_mem_no = CAST( fn_generate_nid(8) AS UNSIGNED );

				SET @idOk = 1;
				WHILE( @idOk ) DO
                    SELECT idx INTO m_idx FROM tb_member_basic_a WHERE mem_no = m_mem_no;
                    IF( FOUND_ROWS() = 0 ) THEN
						SET @idOk = 0;
                    ELSE
						SET m_mem_no = CAST( fn_generate_nid(8) AS UNSIGNED );
                    END IF;
                END WHILE;

                INSERT INTO tb_member_basic_a( mem_no, mem_slct, mem_join_date, last_upd_date ) VALUES
                ( m_mem_no, m_mem_slct, NOW(), NOW() );

                INSERT INTO tb_member_session_a( mem_no, os_type, device_uuid, device_token, app_version, build_version, browser, adid, location, ip, last_upd_date ) VALUES
                ( m_mem_no, m_os, m_deviceUuid, m_deviceToken, m_appVersion, m_buildVersion, m_browser, m_adid, m_location, m_ip, NOW());

                INSERT INTO tb_member_session_a_history( mem_no, slct, os_type, device_uuid, device_token, app_version, build_version, browser, adid,  location, ip, last_upd_date ) VALUES
                ( m_mem_no, 'login', m_os, m_deviceUuid, m_deviceToken, m_appVersion, m_buildVersion, m_browser, m_adId, m_location, m_ip, NOW());
            END if;

			SET ret = 0;
			SET ext = JSON_OBJECT('spName', 'sp_member_login', 'desc', 'login success', 'mem_no', m_mem_no);
        ELSE
            SELECT mem_no, mem_state, block_day, block_end_date, mem_sex, mem_passwd INTO m_mem_no, m_memState, m_blockDay, m_blockEndDate, m_memSex, m_memPwd FROM tb_member_basic WHERE mem_slct = m_mem_slct AND mem_id = m_id;
            IF( FOUND_ROWS() > 0 ) THEN
				IF( m_memState = 3 AND m_blockEndDate < NOW() ) THEN
					SET m_memState = 1;
                    UPDATE  tb_member_basic SET mem_state = m_memState, block_day = 0, block_end_date = NULL WHERE mem_no = m_mem_no;
                END IF;

                IF ( m_mem_slct = 'p' AND m_memPwd <> sha2(m_pw, 256) ) THEN
					SET ret = -1;
					SET ext = JSON_OBJECT('spName', 'sp_member_login', 'desc', 'password mismatch');
				ELSEIF( m_memState > 2 AND m_memState <> 6 ) THEN #휴면계정 비밀번호 체크 및 로그인 처리 필요하여 제외
					IF( m_memState = 3 ) THEN
                        SELECT
                            op_code, op_msg INTO m_opCode, m_opMsg
                        FROM tb_member_report
                        WHERE reported_mem_no = m_mem_no
                          AND op_code > 2
                        ORDER BY op_date DESC
                        LIMIT 1;

                        SET ret = -3;
						SET ext = JSON_OBJECT('mem_no', m_mem_no, 'block_day', m_blockDay, 'expected_end_date', m_blockEndDate, 'opCode', m_opCode, 'opMsg', m_opMsg);
					ELSEIF( m_memState = 5 ) THEN
                        SELECT
                            op_code, op_msg INTO m_opCode, m_opMsg
                        FROM tb_member_report
                        WHERE reported_mem_no = m_mem_no
                          AND op_code > 2
                        ORDER BY op_date DESC
                        LIMIT 1;

                        SET ret = -5;
						SET ext = JSON_OBJECT('mem_no', m_mem_no, 'spName', 'sp_member_login', 'desc', 'forever block', 'opCode', m_opCode, 'opMsg', m_opMsg);
                    ELSE
						SET ret = -4;
						SET ext = JSON_OBJECT('spName', 'sp_member_login', 'desc', 'withdrawal');
                    END IF;
                ELSE
					IF m_room_no > 0 THEN
                        SELECT GROUP_CONCAT(b.room_no) INTO m_roomList FROM tb_broadcast_room_live_list AS a INNER JOIN tb_broadcast_room_member AS b ON a.room_no = b.room_no
                        WHERE b.mem_no = m_mem_no AND b.state = 0 AND  b.auth < 3;
                    END IF;

					IF( LENGTH(IFNULL(m_roomList, '')) > 0 ) THEN
						SET ret = -6;
						SET ext = JSON_OBJECT('spName', 'sp_member_login', 'desc', 'connected Room', 'mem_no', m_mem_no, 'room_no', m_roomList);
                    ELSE
						IF( m_os IN (1,2) ) THEN
                            DELETE FROM tb_member_stmp_token WHERE mem_no = m_mem_no;
                            INSERT INTO tb_member_stmp_token (mem_no, os_type, device_uuid, device_token, app_version, build_version, browser, adid, location, ip, last_upd_date) VALUES
                            ( m_mem_no, IF( m_os = 1, 'a', 'b'), m_deviceUuid, m_deviceToken, m_appVersion, m_buildVersion, m_browser, m_adId, m_location, m_ip, NOW())
                                ON DUPLICATE KEY UPDATE mem_no = m_mem_no, os_type = IF( m_os = 1, 'a', 'b'), device_uuid = m_deviceUuid, app_version = m_appVersion, build_version = m_buildVersion, browser = m_browser
                                                        , adid = m_adId,  location = m_location, ip = m_ip, last_upd_date = NOW() ;

                            INSERT INTO tb_member_stmp( mem_no, os_type, device_uuid, device_token, app_version, build_version, browser, adid,  location, ip, last_upd_date ) VALUES
                            ( m_mem_no, IF( m_os = 1, 'a', 'b'), m_deviceUuid, m_deviceToken, m_appVersion, m_buildVersion, m_browser, m_adId, m_location, m_ip, NOW())
                                ON DUPLICATE KEY UPDATE os_type = IF( m_os = 1, 'a', 'b'), device_uuid = m_deviceUuid, device_token = m_deviceToken, app_version = m_appVersion, build_version = m_buildVersion, browser = m_browser
                                                        ,adid = m_adId,  location = m_location, ip = m_ip, last_upd_date = NOW()  ;

                            UPDATE tb_member_basic SET mem_adid = IFNULL(m_adId, mem_adid) WHERE mem_no = m_mem_no;
                        END IF;

						INSERT INTO tb_member_session( mem_no, os_type, device_uuid, device_token, app_version, build_version, browser, adid,  location, ip, last_upd_date ) VALUES
													 ( m_mem_no, m_os, m_deviceUuid, m_deviceToken, m_appVersion, m_buildVersion, m_browser, m_adId, m_location, m_ip, NOW())
                            ON DUPLICATE KEY UPDATE device_uuid = m_deviceUuid, device_token = m_deviceToken, app_version = m_appVersion, build_version = m_buildVersion, browser = m_browser
                                                    , adid = m_adId,  location = m_location, ip = m_ip, last_upd_date = NOW();

                        INSERT INTO tb_member_session_history( mem_no, slct, os_type, device_uuid, device_token, app_version, build_version, browser, adid, location, ip, last_upd_date ) VALUES
                        ( m_mem_no, 'login', m_os, m_deviceUuid, m_deviceToken, m_appVersion, m_buildVersion, m_browser, m_adid, m_location, m_ip, NOW());

                        INSERT INTO tb_member_connect_state (mem_no,tot_login_cnt,last_login_ip,last_device_uuid,last_os_type,last_login_date) VALUES
                        ( m_mem_no, 1, m_ip, m_deviceUuid, m_os, NOW() )
                            ON DUPLICATE KEY UPDATE tot_login_cnt = tot_login_cnt + 1, last_login_ip = m_ip, last_device_uuid = m_deviceUuid, last_os_type = m_os, last_login_date = NOW();

						SET ret = 0;
                        SELECT mem_no INTO m_aMem_no FROM tb_member_session_a WHERE device_uuid = m_deviceUuid ;
                        IF( FOUND_ROWS() > 0 ) THEN
							IF( m_room_no > 0 ) THEN
                                SELECT state INTO @state FROM tb_broadcast_room WHERE room_no = m_room_no;
                                IF( FOUND_ROWS() > 0 AND  @state <> 4 ) THEN
                                    SELECT
                                        idx, room_no, guest_streamid, guest_publish_tokenid, guest_play_tokenid, bj_streamid, bj_publish_tokenid, bj_play_tokenid, subject_type
                                    INTO m_idx, m_aRoom_no, @guest_streamid, @guest_publish_tokenid, @guest_play_tokenid, @bj_streamid, @bj_publish_tokenid, @bj_play_tokenid, @subject_type
                                    FROM tb_broadcast_room_member WHERE room_no = m_room_no AND mem_no = m_aMem_no;
                                    IF( FOUND_ROWS() > 0 ) THEN
                                        SET @auth := 0;
                                        SELECT auth INTO @auth FROM tb_broadcast_room_member WHERE room_no = m_room_no AND mem_no = m_mem_no;
                                        IF( @auth = 3 ) THEN
                                            SET ret = -6;
                                        ELSE
                                            INSERT INTO tb_broadcast_room_member( room_no, mem_no, auth, control, guest_streamid, guest_publish_tokenid, guest_play_tokenid, bj_streamid, bj_publish_tokenid, bj_play_tokenid, os_type, app_version, device_uuid, subject_type, mem_sex, join_date, last_upd_date ) VALUES
                                            ( m_room_no, m_mem_no, 0, '0000000000', @guest_streamid, @guest_publish_tokenid, @guest_play_tokenid, @bj_streamid, @bj_publish_tokenid, @bj_play_tokenid, m_os, m_appVersion, m_deviceUuid, @subject_type, m_memSex, NOW(), NOW() )
                                                ON DUPLICATE KEY UPDATE state = 0, guest_streamid = @guest_streamid, guest_publish_tokenid = @guest_publish_tokenid, guest_play_tokenid = @guest_play_tokenid, bj_streamid = @bj_streamid, bj_publish_tokenid = @bj_publish_tokenid, bj_play_tokenid = @bj_play_tokenid, os_type = m_os, app_version = m_appVersion, device_uuid = m_deviceUuid, join_date = NOW();
                                            DELETE FROM tb_broadcast_room_member WHERE room_no = m_room_no AND mem_no = m_aMem_no;
                                        END IF;
                                    END IF;
                                END IF;
                            ELSE
                                SELECT
                                    idx, room_no, guest_streamid, guest_publish_tokenid, guest_play_tokenid, bj_streamid, bj_publish_tokenid, bj_play_tokenid, subject_type
                                INTO m_idx, m_aRoom_no, @guest_streamid, @guest_publish_tokenid, @guest_play_tokenid, @bj_streamid, @bj_publish_tokenid, @bj_play_tokenid, @subject_type
                                FROM tb_broadcast_room_member WHERE mem_no = m_aMem_no AND state = 0 AND device_uuid = m_deviceUuid ORDER BY idx DESC LIMIT 1;
                                IF( FOUND_ROWS() > 0 ) THEN
                                    SELECT state INTO @state FROM tb_broadcast_room WHERE room_no = m_aRoom_no;
                                        IF( FOUND_ROWS() > 0 AND  @state <> 4 ) THEN
                                            SET @auth := 0;
                                            SELECT auth INTO @auth FROM tb_broadcast_room_member WHERE room_no = m_aRoom_no AND mem_no = m_mem_no;
                                            IF( @auth = 3 ) THEN
                                                SET ret = -6;
                                            ELSE
                                                INSERT INTO tb_broadcast_room_member( room_no, mem_no, auth, control, guest_streamid, guest_publish_tokenid, guest_play_tokenid, bj_streamid, bj_publish_tokenid, bj_play_tokenid, os_type, app_version, device_uuid, subject_type, mem_sex, join_date, last_upd_date ) VALUES
                                                ( m_aRoom_no, m_mem_no, 0, '0000000000', @guest_streamid, @guest_publish_tokenid, @guest_play_tokenid, @bj_streamid, @bj_publish_tokenid, @bj_play_tokenid, m_os, m_appVersion, m_deviceUuid, @subject_type, m_memSex, NOW(), NOW() )
                                                    ON DUPLICATE KEY UPDATE state = 0, guest_streamid = @guest_streamid, guest_publish_tokenid = @guest_publish_tokenid, guest_play_tokenid = @guest_play_tokenid, bj_streamid = @bj_streamid, bj_publish_tokenid = @bj_publish_tokenid, bj_play_tokenid = @bj_play_tokenid, os_type = m_os, app_version = m_appVersion, device_uuid = m_deviceUuid, join_date = NOW();

                                                DELETE FROM tb_broadcast_room_member WHERE room_no = m_aRoom_no AND mem_no = m_aMem_no;
                                            END IF;
                                        END IF;
                                END IF;
                            END IF;

							IF (ret = 0) THEN
                                DELETE FROM tb_member_basic_a WHERE mem_no = m_aMem_no;
                                DELETE FROM tb_member_session_a WHERE mem_no = m_aMem_no;
                            END IF;
                        END IF;

						IF (ret = 0) THEN
                            SELECT COUNT(*) INTO m_connectedRoomCnt FROM tb_broadcast_room_member WHERE mem_no = m_mem_no AND state = 0;

                            SELECT
                                a.room_no, a.auth, a.device_uuid, b.state
                            FROM (
                                     SELECT room_no, auth, device_uuid FROM tb_broadcast_room_member WHERE mem_no = m_mem_no AND state = 0
                                 ) a INNER JOIN tb_broadcast_room b ON a.room_no = b.room_no;

                            CALL sp_member_info_select(CONCAT('{"mem_no":', m_mem_no,'}'), @ret,  @ext);

                            IF( m_memState = 6 ) THEN
								SET ret = -8;
								SET ext = JSON_OBJECT('mem_no', m_mem_no, 'connectedCnt', m_connectedRoomCnt, 'spName', 'sp_member_login', 'desc', 'sleep member');
                            ELSE
								SET ext = JSON_OBJECT('mem_no', m_mem_no, 'connectedCnt', m_connectedRoomCnt, 'spName', 'sp_member_login', 'desc', 'login success');
                            END IF;
                        ELSE
							SET ext = JSON_OBJECT('spName', 'sp_member_login', 'desc', 'connected Room', 'mem_no', m_mem_no);
                        END IF;
                    END IF;
                END IF;
            ELSE
				SET ret = 1;
				SET ext = JSON_OBJECT('spName', 'sp_member_login', 'desc', 'not found member');
            END IF;
        END IF;

        COMMIT;
    END IF;

    UPDATE tb_log_data SET `json_type` = CONCAT('ret = ',ret, '  ', ext), query_time = (CURRENT_TIMESTAMP(6) - upd_date) WHERE idx = m_logIdx;
END