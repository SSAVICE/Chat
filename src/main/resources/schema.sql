CREATE TABLE IF NOT EXISTS room (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, -- @Id Long id
    room_id VARCHAR(255) NOT NULL UNIQUE, -- String roomId
    room_name VARCHAR(255) NOT NULL,      -- String roomName
    type VARCHAR(20) NOT NULL,            -- Enum RoomType (문자열로 저장)
    created_at TIMESTAMP NOT NULL,        -- createdAt
    last_service_id BIGINT                -- Long lastServiceId
);

CREATE TABLE IF NOT EXISTS chat_member (
     id BIGINT AUTO_INCREMENT PRIMARY KEY, -- @Id Long id
     room_id VARCHAR(255) NOT NULL,        -- String roomId
     subject VARCHAR(255) NOT NULL,        -- String subject
     joined_at TIMESTAMP NOT NULL,         -- joinedAt
     is_left BOOLEAN DEFAULT FALSE,        -- Boolean isLeft Default: false
     last_read_msg_id BIGINT NOT NULL,     -- Long lastReadMsgId

    CONSTRAINT uq_room_user UNIQUE (room_id, subject)
);

CREATE TABLE IF NOT EXISTS chat_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,   -- @Id Long id
    room_type VARCHAR(20) NOT NULL,         -- Enum RoomType (문자열로 저장)
    message_type VARCHAR(20) NOT NULL,      -- Enum MessageType (문자열로 저장)
    room_id VARCHAR(255) NOT NULL,          -- String roomId
    receiver VARCHAR(255),                  -- String receiver
    sender VARCHAR(255) NOT NULL,      		-- String sender
    message VARCHAR(255),      		        -- String sender
    created_at TIMESTAMP NOT NULL            -- createdAt
);