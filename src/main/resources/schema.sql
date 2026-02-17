CREATE TABLE IF NOT EXISTS room (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, -- @Id Long id
    room_id VARCHAR(255) NOT NULL,        -- String roomId
    room_name VARCHAR(255) NOT NULL,      -- String roomName
    type VARCHAR(20) NOT NULL,            -- Enum MessageType (문자열로 저장)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- @CreatedDate
);

CREATE TABLE IF NOT EXISTS chat_member (
     id BIGINT AUTO_INCREMENT PRIMARY KEY, -- @Id Long id
     room_id VARCHAR(255) NOT NULL,        -- String roomId
     user_id BIGINT NOT NULL,      		  -- Long userId
     joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- @CreatedDate
     last_read_msg_id BIGINT NOT NULL      -- Long lastReadMsgId
);