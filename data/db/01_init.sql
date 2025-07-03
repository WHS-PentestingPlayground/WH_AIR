-- PostgreSQL 14.15 초기화 스크립트
-- WH_AIR 데이터베이스 테이블 생성

-- ENUM 타입 정의
CREATE TYPE seat_class_enum AS ENUM ('economy', 'business', 'first');
CREATE TYPE reservation_status_enum AS ENUM ('booked', 'canceled');

-- users 테이블 생성
CREATE TABLE users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    point INTEGER DEFAULT 100000,
    coupon VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- flights 테이블 생성
CREATE TABLE flights (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    flight_number VARCHAR(20) NOT NULL,
    departure_airport VARCHAR(100) NOT NULL,
    arrival_airport VARCHAR(100) NOT NULL,
    departure_time TIMESTAMP NOT NULL,
    arrival_time TIMESTAMP NOT NULL,
    airline VARCHAR(100) NOT NULL,
    aircraft_model VARCHAR(50)
);

-- seats 테이블 생성
CREATE TABLE seats (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    flight_id BIGINT NOT NULL,
    seat_number VARCHAR(10) NOT NULL,
    class seat_class_enum NOT NULL,
    is_reserved BOOLEAN DEFAULT FALSE,
    price NUMERIC(10, 2) NOT NULL,
    FOREIGN KEY (flight_id) REFERENCES flights(id) ON DELETE CASCADE
);

-- reservations 테이블 생성
CREATE TABLE reservations (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL,
    flight_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    passenger_name VARCHAR(100) NOT NULL,
    passenger_birth DATE NOT NULL,
    status reservation_status_enum DEFAULT 'booked',
    booked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (flight_id) REFERENCES flights(id) ON DELETE CASCADE,
    FOREIGN KEY (seat_id) REFERENCES seats(id) ON DELETE CASCADE
);

-- 인덱스 생성 (성능 최적화)
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_flights_number ON flights(flight_number);
CREATE INDEX idx_flights_departure_time ON flights(departure_time);
CREATE INDEX idx_seats_flight_id ON seats(flight_id);
CREATE INDEX idx_seats_class ON seats(class);
CREATE INDEX idx_reservations_user_id ON reservations(user_id);
CREATE INDEX idx_reservations_flight_id ON reservations(flight_id);
CREATE INDEX idx_reservations_seat_id ON reservations(seat_id);
CREATE INDEX idx_reservations_status ON reservations(status);

-- 트리거 함수 생성 (updated_at 자동 업데이트)
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- reservations 테이블에 updated_at 자동 업데이트 트리거 적용
CREATE TRIGGER update_reservations_updated_at 
    BEFORE UPDATE ON reservations 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- 제약조건 추가
ALTER TABLE seats ADD CONSTRAINT unique_seat_per_flight 
    UNIQUE (flight_id, seat_number);

ALTER TABLE reservations ADD CONSTRAINT unique_seat_reservation 
    UNIQUE (seat_id);

-- 코멘트 추가
COMMENT ON TABLE users IS '사용자 정보 테이블';
COMMENT ON TABLE flights IS '항공편 정보 테이블';
COMMENT ON TABLE seats IS '좌석 정보 테이블';
COMMENT ON TABLE reservations IS '예약 정보 테이블';

COMMENT ON COLUMN users.point IS '사용자 포인트 (기본값: 100,000)';
COMMENT ON COLUMN seats.class IS '좌석 등급: economy, business, first';
COMMENT ON COLUMN reservations.status IS '예약 상태: booked, canceled';
