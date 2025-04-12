CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password TEXT NOT NULL,
    email VARCHAR(255) UNIQUE,
    profile_picture TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_time TIMESTAMP,
    failed_login_attempts INT DEFAULT 0 NOT NULL, 
    lockout_until TIMESTAMP                       
);


CREATE TABLE user_sessions (
    session_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE invites (
    id SERIAL PRIMARY KEY,
    watch_party_id INT NOT NULL,
    username VARCHAR(255) NOT NULL,
    status VARCHAR(20) DEFAULT 'pending', -- Can be 'pending', 'accepted', or 'declined'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (watch_party_id) REFERENCES watch_parties(id) ON DELETE CASCADE
);
