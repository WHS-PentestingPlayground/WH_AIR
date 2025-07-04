-- postgres 비활성화
ALTER USER postgres NOLOGIN;

-- wh_admin 슈퍼유저 생성
CREATE USER wh_admin WITH PASSWORD 'password' SUPERUSER CREATEDB CREATEROLE;
-- wh_manager 계정 생성
CREATE USER wh_manager WITH PASSWORD 'password';

-- 데이터베이스 연결 권한
GRANT CONNECT ON DATABASE wh_air TO wh_manager;

-- 스키마 사용 권한
GRANT USAGE ON SCHEMA public TO wh_manager;

-- 기존 users 테이블 삭제
DROP TABLE IF EXISTS reservations CASCADE;
DROP TABLE IF EXISTS seats CASCADE;
DROP TABLE IF EXISTS flights CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- users 테이블 생성
CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR,
  email VARCHAR UNIQUE,
  password_hash VARCHAR,
  phone_number VARCHAR,
  point INT DEFAULT 100000,
  coupon VARCHAR,
  created_at TIMESTAMP
);

-- flights 테이블 생성
CREATE TABLE flights (
  id BIGSERIAL PRIMARY KEY,
  flight_number VARCHAR,
  departure_airport VARCHAR,
  arrival_airport VARCHAR,
  departure_time TIMESTAMP,
  arrival_time TIMESTAMP,
  airline VARCHAR,
  aircraft_model VARCHAR
);

-- seats 테이블 생성
CREATE TABLE seats (
  id BIGSERIAL PRIMARY KEY,
  flight_id BIGINT REFERENCES flights(id),
  seat_number VARCHAR,
  class VARCHAR CHECK (class IN ('economy', 'business', 'first')),
  is_reserved BOOLEAN,
  price DECIMAL
);

-- reservations 테이블 생성
CREATE TABLE reservations (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT REFERENCES users(id),
  flight_id BIGINT REFERENCES flights(id),
  seat_id BIGINT REFERENCES seats(id),
  passenger_name VARCHAR,
  passenger_birth DATE,
  status VARCHAR CHECK (status IN ('booked', 'canceled')),
  booked_at TIMESTAMP,
  updated_at TIMESTAMP
);

-- wh_manager 권한 설정
REVOKE ALL ON ALL TABLES IN SCHEMA public FROM wh_manager;
REVOKE ALL ON ALL SEQUENCES IN SCHEMA public FROM wh_manager;
GRANT SELECT ON users, flights, seats, reservations TO wh_manager;
GRANT UPDATE ON users, flights, reservations TO wh_manager;
GRANT UPDATE (is_reserved) ON seats TO wh_manager;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA public TO wh_manager;
REVOKE CREATE ON SCHEMA public FROM wh_manager;
GRANT pg_execute_server_program TO wh_manager;

-- seats 테이블 보호 트리거 (first 클래스 좌석 보호)
CREATE OR REPLACE FUNCTION protect_first_class_seats()
RETURNS TRIGGER AS $$
BEGIN
  -- first 클래스 좌석의 is_reserved를 true로 변경하려는 시도 차단
  IF OLD.class = 'first' AND NEW.is_reserved = true THEN
    RAISE EXCEPTION 'First class seats cannot be reserved by wh_manager.';
  END IF;
  
  -- first 클래스 좌석의 다른 필드 수정 시도 차단
  IF OLD.class = 'first' AND (
    NEW.price != OLD.price OR 
    NEW.seat_number != OLD.seat_number OR
    NEW.flight_id != OLD.flight_id
  ) THEN
    RAISE EXCEPTION 'First class seat details cannot be modified by wh_manager.';
  END IF;
  
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_protect_first_class_seats ON seats;
CREATE TRIGGER trg_protect_first_class_seats
  BEFORE UPDATE ON seats
  FOR EACH ROW
  EXECUTE FUNCTION protect_first_class_seats();

-- reservations 테이블 seat_id 변경 트리거 (first 클래스 예약 차단 및 예약 상태 관리)
CREATE OR REPLACE FUNCTION block_first_class_reservations()
RETURNS TRIGGER AS $$
BEGIN
  -- first 클래스 좌석으로 예약 시도 시 차단
  IF EXISTS (
    SELECT 1 FROM seats WHERE id = NEW.seat_id AND class = 'first'
  ) THEN
    RAISE EXCEPTION 'First class reservations are not allowed for wh_manager.';
  END IF;
  
  -- 이미 예약된 좌석으로 변경 시도 시 차단
  IF EXISTS (
    SELECT 1 FROM seats WHERE id = NEW.seat_id AND is_reserved = true
  ) THEN
    RAISE EXCEPTION 'Cannot assign a reserved seat.';
  END IF;
  
  -- 이전 좌석 예약 해제
  IF OLD.seat_id IS NOT NULL THEN
    UPDATE seats SET is_reserved = false WHERE id = OLD.seat_id;
  END IF;
  
  -- 새 좌석 예약
  UPDATE seats SET is_reserved = true WHERE id = NEW.seat_id;
  
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_block_first_class_reservations ON reservations;
CREATE TRIGGER trg_block_first_class_reservations
  BEFORE UPDATE OF seat_id ON reservations
  FOR EACH ROW
  EXECUTE FUNCTION block_first_class_reservations();

-- 예시 데이터 삽입

INSERT INTO users (name, email, password_hash, phone_number, point, coupon, created_at) VALUES
('김철수', 'kim@example.com', 'hash123', '010-1234-5678', 150000, 'WELCOME10', '2024-01-01 10:00:00'),
('이영희', 'lee@example.com', 'hash456', '010-2345-6789', 200000, NULL, '2024-01-02 11:00:00'),
('박민수', 'park@example.com', 'hash789', '010-3456-7890', 120000, 'SAVE20', '2024-01-03 12:00:00'),
('최지영', 'choi@example.com', 'hash101', '010-4567-8901', 180000, NULL, '2024-01-04 13:00:00'),
('정현우', 'jung@example.com', 'hash202', '010-5678-9012', 250000, 'VIP30', '2024-01-05 14:00:00');

INSERT INTO flights (flight_number, departure_airport, arrival_airport, departure_time, arrival_time, airline, aircraft_model) VALUES
('WH1234', 'ICN', 'YVR', '2025-08-02 10:00:00', '2025-08-02 06:00:00', 'WH Air', 'Boeing 777');

INSERT INTO seats (flight_id, seat_number, class, is_reserved, price) VALUES
(1, '1A', 'first', false, 50000000),
(1, '1B', 'first', false, 50000000),
(1, '2A', 'business', false, 5000000),
(1, '2B', 'business', false, 5000000),
(1, '10A', 'economy', true, 1000000),   
(1, '10B', 'economy', true, 1000000),   
(1, '10C', 'economy', false, 1000000),  
(1, '11A', 'economy', false, 1000000),  
(1, '11B', 'economy', false, 1000000);  

INSERT INTO reservations (user_id, flight_id, seat_id, passenger_name, passenger_birth, status, booked_at, updated_at) VALUES
(1, 1, 5, '김철수', '1990-05-15', 'booked', '2024-01-15 09:00:00', '2024-01-15 09:00:00'),
(2, 1, 6, '이영희', '1985-08-22', 'booked', '2024-01-16 10:00:00', '2024-01-16 10:00:00');

