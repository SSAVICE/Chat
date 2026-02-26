CREATE TABLE IF NOT EXISTS room (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, -- @Id Long id
    room_id VARCHAR(255) NOT NULL UNIQUE, -- String roomId
    room_name VARCHAR(255) NOT NULL,      -- String roomName
    type VARCHAR(20) NOT NULL,            -- Enum RoomType (문자열로 저장)
    created_at TIMESTAMP NOT NULL,        -- createdAt
    last_service_id BIGINT,               -- Long lastServiceId
    last_msg_id BIGINT,                   -- Long lastMsgId
    last_msg_at TIMESTAMP,                -- lastMsgAt
    last_msg VARCHAR(255)                 -- lastMsg

    );

CREATE TABLE IF NOT EXISTS chat_member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, -- @Id Long id
    room_id VARCHAR(255) NOT NULL,        -- String roomId
    subject BIGINT NOT NULL,              -- Long subject
    joined_at TIMESTAMP NOT NULL,         -- joinedAt
    is_left BOOLEAN DEFAULT FALSE,        -- Boolean isLeft Default: false
    last_read_msg_id BIGINT NOT NULL,     -- Long readMsgIds

    CONSTRAINT uq_room_user UNIQUE (room_id, subject)
    );

CREATE TABLE IF NOT EXISTS chat_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,   -- @Id Long id
    message_id BIGINT NOT NULL,
    room_type VARCHAR(20) NOT NULL,         -- Enum RoomType (문자열로 저장)
    message_type VARCHAR(20) NOT NULL,      -- Enum MessageType (문자열로 저장)
    room_id VARCHAR(255) NOT NULL,          -- String roomId
    receiver BIGINT,                        -- Long receiver
    sender BIGINT NOT NULL,      		    -- Long sender
    message VARCHAR(255),      		        -- String sender
    created_at TIMESTAMP NOT NULL           -- createdAt
    );

CREATE TABLE IF NOT EXISTS account (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,           -- @Id Long id
    provider_id VARCHAR(255) NOT NULL,              -- providerId
    role VARCHAR(20) NOT NULL                       -- role
);

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,            -- @Id Long id
    is_deleted BIT(1) NOT NULL,                      -- isDeleted
    image_resource_id BIGINT UNIQUE,                 -- imageResourceId
    name VARCHAR(255) NOT NULL                       -- name
);

CREATE TABLE IF NOT EXISTS company (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,           -- @Id Long id
    is_deleted BIT(1) NOT NULL,                     -- isDeleted
    image_resource_id BIGINT UNIQUE,                -- imageResourceId
    company_name VARCHAR(255) NOT NULL             -- companyName
);