from flask import Flask, render_template, request, redirect, url_for, flash, jsonify, session
from flask import abort
import psycopg2
import psycopg2.extras
from datetime import datetime
import os
from dotenv import load_dotenv
from functools import wraps

load_dotenv()

app = Flask(__name__)
app.secret_key = 'your-secret-key-here'

# 로그인 데코레이터
def login_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if 'admin_logged_in' not in session:
            return redirect(url_for('login'))
        return f(*args, **kwargs)
    return decorated_function

# 데이터베이스 연결 설정
DB_CONFIG = {
    'host': os.getenv('DB_HOST', 'db-server'),
    'port': os.getenv('DB_PORT', '5432'),
    'database': os.getenv('DB_NAME', 'wh_air'),
    'user': os.getenv('DB_USER', 'wh_admin'),
    'password': os.getenv('DB_PASSWORD', '!ADpasswd@@')
}

def get_db_connection():
    """데이터베이스 연결을 반환합니다."""
    return psycopg2.connect(**DB_CONFIG)

# DB 서버 IP에서 직접 접근 시 curl, wget, httpie 등 CLI 툴 차단
@app.before_request
def block_db_server_curl():
    if request.remote_addr == "172.30.0.3":
        user_agent = request.headers.get("User-Agent", "").lower()
        blocked_agents = ["curl", "wget", "httpie", "python-requests"]
        if any(agent in user_agent for agent in blocked_agents):
            abort(403, description="admin page is only accessible via a web browser.")
            
@app.route('/login', methods=['GET', 'POST'])
def login():
    """관리자 로그인"""
    if request.method == 'POST':
        username = request.form.get('username')
        password = request.form.get('password')
        
        # 하드코딩된 관리자 계정 (admin/admin)
        if username == 'admin' and password == 'admin':
            session['admin_logged_in'] = True
            session['admin_username'] = username
            flash('관리자로 로그인되었습니다.', 'success')
            return redirect(url_for('index'))
        else:
            flash('아이디 또는 비밀번호가 올바르지 않습니다.', 'error')
    
    return render_template('login.html')

@app.route('/logout')
def logout():
    """관리자 로그아웃"""
    session.pop('admin_logged_in', None)
    session.pop('admin_username', None)
    flash('로그아웃되었습니다.', 'info')
    return redirect(url_for('login'))

@app.route('/')
def root():
    """루트 경로를 로그인 페이지로 리다이렉트"""
    return redirect(url_for('login'))

@app.route('/dashboard')
@login_required
def index():
    """관리자 메인 페이지"""
    return render_template('index.html')

@app.route('/reservations')
@login_required
def reservations():
    """예약 관리 페이지"""
    conn = get_db_connection()
    cur = conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor)
    
    try:
        # 모든 예약 정보 조회 (사용자, 항공편, 좌석 정보 포함)
        cur.execute("""
            SELECT 
                r.id,
                r.passenger_name,
                r.passenger_birth,
                r.booked_at,
                r.updated_at,
                u.name as user_name,
                u.email as user_email,
                f.flight_number,
                f.departure_airport,
                f.arrival_airport,
                f.departure_time,
                s.seat_number,
                s.class as seat_class,
                s.seat_price,
                s.fuel_price
            FROM reservations r
            JOIN users u ON r.user_id = u.id
            JOIN flights f ON r.flight_id = f.id
            JOIN seats s ON r.seat_id = s.id
            ORDER BY r.booked_at DESC
        """)
        reservations = cur.fetchall()
        
        return render_template('reservations.html', reservations=reservations)
    
    except Exception as e:
        flash(f'데이터 조회 중 오류가 발생했습니다: {str(e)}', 'error')
        return render_template('reservations.html', reservations=[])
    
    finally:
        cur.close()
        conn.close()

@app.route('/seats')
@login_required
def seats():
    """좌석 관리 페이지"""
    conn = get_db_connection()
    cur = conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor)
    
    try:
        # 항공편 목록 조회
        cur.execute("SELECT id, flight_number, departure_airport, arrival_airport, departure_time FROM flights ORDER BY departure_time")
        flights = cur.fetchall()
        
        # 선택된 항공편의 좌석 정보 조회
        flight_id = request.args.get('flight_id', flights[0]['id'] if flights else None)
        seats = []
        
        if flight_id:
            cur.execute("""
                SELECT 
                    s.id,
                    s.seat_number,
                    s.class,
                    s.is_reserved,
                    s.seat_price,
                    s.fuel_price,
                    r.passenger_name,
                    r.id as reservation_id
                FROM seats s
                LEFT JOIN reservations r ON s.id = r.seat_id
                WHERE s.flight_id = %s
                ORDER BY 
                    CASE s.class 
                        WHEN 'first' THEN 1 
                        WHEN 'business' THEN 2 
                        WHEN 'economy' THEN 3 
                    END,
                    s.seat_number
            """, (flight_id,))
            seats = cur.fetchall()
        
        return render_template('seats.html', flights=flights, seats=seats, selected_flight_id=flight_id)
    
    except Exception as e:
        flash(f'데이터 조회 중 오류가 발생했습니다: {str(e)}', 'error')
        return render_template('seats.html', flights=[], seats=[], selected_flight_id=None)
    
    finally:
        cur.close()
        conn.close()

@app.route('/api/available_seats/<int:flight_id>')
@login_required
def get_available_seats(flight_id):
    """특정 항공편의 사용 가능한 좌석 목록을 반환합니다."""
    conn = get_db_connection()
    cur = conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor)
    
    try:
        cur.execute("""
            SELECT id, seat_number, class, seat_price, fuel_price
            FROM seats 
            WHERE flight_id = %s AND is_reserved = false
            ORDER BY 
                CASE class 
                    WHEN 'first' THEN 1 
                    WHEN 'business' THEN 2 
                    WHEN 'economy' THEN 3 
                END,
                seat_number
        """, (flight_id,))
        seats = cur.fetchall()
        
        return jsonify([dict(seat) for seat in seats])
    
    except Exception as e:
        return jsonify({'error': str(e)}), 500
    
    finally:
        cur.close()
        conn.close()

@app.route('/api/change_seat', methods=['POST'])
@login_required
def change_seat():
    """예약된 좌석을 변경합니다."""
    data = request.get_json()
    reservation_id = data.get('reservation_id')
    new_seat_id = data.get('new_seat_id')
    
    if not reservation_id or not new_seat_id:
        return jsonify({'error': '필수 파라미터가 누락되었습니다.'}), 400
    
    conn = get_db_connection()
    cur = conn.cursor()
    
    try:
        # 트랜잭션 시작
        conn.autocommit = False
        
        # 새 좌석이 사용 가능한지 확인
        cur.execute("SELECT is_reserved, class FROM seats WHERE id = %s", (new_seat_id,))
        seat_info = cur.fetchone()
        
        if not seat_info:
            conn.rollback()
            return jsonify({'error': '좌석을 찾을 수 없습니다.'}), 404
        
        if seat_info[0]:  # is_reserved가 True
            conn.rollback()
            return jsonify({'error': '이미 예약된 좌석입니다.'}), 400
        
        # wh_admin은 first 클래스 좌석도 변경 가능
        # (트리거에서 wh_manager만 제한하므로 wh_admin은 모든 좌석 변경 가능)
        
        # 예약 정보 업데이트
        cur.execute("""
            UPDATE reservations 
            SET seat_id = %s, updated_at = NOW() 
            WHERE id = %s
        """, (new_seat_id, reservation_id))
        
        if cur.rowcount == 0:
            conn.rollback()
            return jsonify({'error': '예약을 찾을 수 없습니다.'}), 404
        
        # 트랜잭션 커밋
        conn.commit()
        
        return jsonify({'message': '좌석이 성공적으로 변경되었습니다.'})
    
    except Exception as e:
        conn.rollback()
        return jsonify({'error': f'좌석 변경 중 오류가 발생했습니다: {str(e)}'}), 500
    
    finally:
        cur.close()
        conn.close()

@app.route('/api/cancel_reservation', methods=['POST'])
@login_required
def cancel_reservation():
    """예약을 취소(삭제)합니다."""
    data = request.get_json()
    reservation_id = data.get('reservation_id')
    
    if not reservation_id:
        return jsonify({'error': '예약 ID가 필요합니다.'}), 400
    
    conn = get_db_connection()
    cur = conn.cursor()
    
    try:
        # reservations에서 삭제만 하면 트리거가 알아서 seats도 처리
        cur.execute("DELETE FROM reservations WHERE id = %s", (reservation_id,))
        if cur.rowcount == 0:
            conn.rollback()
            return jsonify({'error': '예약을 찾을 수 없습니다.'}), 404
        conn.commit()
        return jsonify({'message': '예약이 성공적으로 취소(삭제)되었습니다.'})
    except Exception as e:
        conn.rollback()
        return jsonify({'error': f'예약 취소 중 오류: {str(e)}'}), 500
    finally:
        cur.close()
        conn.close()

@app.route('/whoami')
@login_required
def whoami():
    conn = get_db_connection()
    cur = conn.cursor()
    cur.execute('SELECT current_user;')
    user = cur.fetchone()[0]
    cur.close()
    conn.close()
    return f'현재 DB 접속 계정: {user}'

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8080, debug=True) 