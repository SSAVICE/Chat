CREATE TABLE IF NOT EXISTS roomEntity (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, -- @Id Long id
    room_id VARCHAR(255) NOT NULL UNIQUE, -- String roomId
    room_name VARCHAR(255) NOT NULL,      -- String roomName
    type VARCHAR(20) NOT NULL,            -- Enum RoomType (문자열로 저장)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- @CreatedDate
);

CREATE TABLE IF NOT EXISTS chat_member (
     id BIGINT AUTO_INCREMENT PRIMARY KEY, -- @Id Long id
     room_id VARCHAR(255) NOT NULL,        -- String roomId
     user_id VARCHAR(255) NOT NULL,      		  -- Long userId
     joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- @CreatedDate
     is_left BOOLEAN DEFAULT FALSE,
     last_read_msg_id BIGINT NOT NULL,      -- Long lastReadMsgId

    CONSTRAINT uq_room_user UNIQUE (room_id, user_id)
);