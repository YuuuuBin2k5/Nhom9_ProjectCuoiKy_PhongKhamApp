package com.example.phongkham_app.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "phongkham_app.db";
    private static final int DATABASE_VERSION = 6;

    // Table Names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_CUSTOMERS = "customers";
    public static final String TABLE_DOCTORS = "doctors";
    public static final String TABLE_ADMINS = "admins";
    public static final String TABLE_POINT_TRANSACTIONS = "point_transactions";
    
    public static final String TABLE_SERVICE_CATEGORIES = "service_categories";
    public static final String TABLE_SERVICES = "services";
    public static final String TABLE_SERVICE_IMAGES = "service_images";
    public static final String TABLE_CLINIC_ROOMS = "clinic_rooms";
    public static final String TABLE_SHIFT_TEMPLATES = "shift_templates";
    public static final String TABLE_DOCTOR_SCHEDULES = "doctor_schedules";
    
    public static final String TABLE_APPOINTMENTS = "appointments";
    public static final String TABLE_CHECK_IN_QUEUE = "check_in_queue";
    public static final String TABLE_MEDICAL_RECORDS = "medical_records";
    public static final String TABLE_MEDICAL_RECORD_DETAILS = "medical_record_details";
    
    public static final String TABLE_NOTIFICATIONS = "notifications";
    public static final String TABLE_REVIEWS = "reviews";
    public static final String TABLE_INVOICES = "invoices";
    public static final String TABLE_CHAT_ROOMS = "chat_rooms";
    public static final String TABLE_CHAT_MESSAGES = "chat_messages";
    public static final String TABLE_MEDICINES = "medicines";
    public static final String TABLE_PRESCRIPTIONS = "prescriptions";

    // Common Column Names
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CREATED_AT = "created_at";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating tables...");
        // Group 1: Users, Roles & Loyalty
        db.execSQL("CREATE TABLE " + TABLE_USERS + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "first_name TEXT, last_name TEXT, password_hash TEXT, email TEXT, avatar_url TEXT, role TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_CUSTOMERS + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "phone TEXT, dob TEXT, gender TEXT, address TEXT, reward_points INTEGER)");

        db.execSQL("CREATE TABLE " + TABLE_DOCTORS + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "specialty TEXT, experience_years INTEGER, bio TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_ADMINS + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT)");

        db.execSQL("CREATE TABLE " + TABLE_POINT_TRANSACTIONS + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "customer_id INTEGER, points_changed INTEGER, transaction_type TEXT, description TEXT, " +
                COLUMN_CREATED_AT + " TEXT)");

        // Group 2: Services, Rooms & Schedules
        db.execSQL("CREATE TABLE " + TABLE_SERVICE_CATEGORIES + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, description TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_SERVICES + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "category_id INTEGER, name TEXT, description TEXT, price REAL, duration_minutes INTEGER, is_active INTEGER DEFAULT 1)");

        db.execSQL("CREATE TABLE " + TABLE_SERVICE_IMAGES + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "service_id INTEGER, image_url TEXT, display_order INTEGER, is_thumbnail INTEGER)");

        db.execSQL("CREATE TABLE " + TABLE_CLINIC_ROOMS + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, description TEXT, map_image_url TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_SHIFT_TEMPLATES + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, start_time TEXT, end_time TEXT, is_active INTEGER)");

        db.execSQL("CREATE TABLE " + TABLE_DOCTOR_SCHEDULES + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "doctor_id INTEGER, clinic_room_id INTEGER, shift_template_id INTEGER, work_date TEXT)");

        // Group 3: Appointments, Queue & Medical Records
        db.execSQL("CREATE TABLE " + TABLE_APPOINTMENTS + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "customer_id INTEGER, doctor_id INTEGER, service_id INTEGER, appointment_datetime TEXT, " +
                "status TEXT, notes TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_CHECK_IN_QUEUE + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "appointment_id INTEGER, clinic_room_id INTEGER, check_in_time TEXT, status TEXT, " +
                "queue_number TEXT, estimated_wait_time INTEGER, delay_minutes INTEGER, treatment_start_time TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_MEDICAL_RECORDS + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "appointment_id INTEGER, customer_id INTEGER, doctor_id INTEGER, diagnosis TEXT, " +
                "treatment_plan TEXT, prescription TEXT, created_at TEXT, updated_at TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_MEDICAL_RECORD_DETAILS + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "medical_record_id INTEGER, service_id INTEGER, tooth_number INTEGER, quantity INTEGER, " +
                "price REAL, treatment_note TEXT)");

        // Group 4: Interactions, Finance & Chat
        db.execSQL("CREATE TABLE " + TABLE_NOTIFICATIONS + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, title TEXT, message TEXT, is_read INTEGER, " +
                COLUMN_CREATED_AT + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_REVIEWS + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "appointment_id INTEGER, customer_id INTEGER, doctor_id INTEGER, rating INTEGER, " +
                "comment TEXT, " + COLUMN_CREATED_AT + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_INVOICES + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "medical_record_id INTEGER, total_amount REAL, payment_status TEXT, payment_date TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_CHAT_ROOMS + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user1_id INTEGER, user2_id INTEGER, " + COLUMN_CREATED_AT + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_CHAT_MESSAGES + "(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "chat_room_id INTEGER, sender_id INTEGER, message_content TEXT, is_read INTEGER, " +
                COLUMN_CREATED_AT + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_MEDICINES + "(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, unit TEXT, price REAL, stock INTEGER)");

        db.execSQL("CREATE TABLE " + TABLE_PRESCRIPTIONS + "(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "medical_record_id INTEGER, medicine_id INTEGER, dosage TEXT, frequency TEXT, duration TEXT)");


        Log.d(TAG, "Tables created successfully.");
        seedData(db);
    }


    private void seedData(SQLiteDatabase db) {
        Log.d(TAG, "Seeding data...");
        // Seed some initial data for demo
        
        // 1. Admin
        ContentValues adminUser = new ContentValues();
        adminUser.put("first_name", "Admin");
        adminUser.put("last_name", "System");
        adminUser.put("email", "admin@phongkham.com");
        adminUser.put("password_hash", "admin123");
        adminUser.put("role", "ADMIN");
        long adminId = db.insert(TABLE_USERS, null, adminUser);
        
        ContentValues adminProfile = new ContentValues();
        adminProfile.put(COLUMN_ID, adminId);
        db.insert(TABLE_ADMINS, null, adminProfile);

        // 2. Doctor
        ContentValues doctorUser = new ContentValues();
        doctorUser.put("first_name", "Nguyễn");
        doctorUser.put("last_name", "Văn A");
        doctorUser.put("email", "doctor@phongkham.com");
        doctorUser.put("password_hash", "doctor123");
        doctorUser.put("role", "DOCTOR");
        long doctorId = db.insert(TABLE_USERS, null, doctorUser);
        
        ContentValues doctorProfile = new ContentValues();
        doctorProfile.put(COLUMN_ID, doctorId);
        doctorProfile.put("specialty", "Nha khoa thẩm mỹ");
        doctorProfile.put("experience_years", 10);
        db.insert(TABLE_DOCTORS, null, doctorProfile);

        // 3. Customers (Waiting Users)
        String[][] customers = {
            {"Trần", "Thị B", "customer1@gmail.com", "0912345678"},
            {"Lê", "Quang C", "customer2@gmail.com", "0922345678"},
            {"Phạm", "Duy D", "customer3@gmail.com", "0932345678"},
            {"Hoàng", "Lan E", "customer4@gmail.com", "0942345678"},
            {"Vũ", "Minh F", "customer5@gmail.com", "0952345678"},
            {"Đặng", "Thu G", "customer6@gmail.com", "0962345678"}
        };
        long[] customerIds = new long[customers.length];
        for(int i=0; i<customers.length; i++) {
            ContentValues userVal = new ContentValues();
            userVal.put("first_name", customers[i][0]);
            userVal.put("last_name", customers[i][1]);
            userVal.put("email", customers[i][2]);
            userVal.put("password_hash", "password123");
            userVal.put("role", "CUSTOMER");
            customerIds[i] = db.insert(TABLE_USERS, null, userVal);
            
            ContentValues custVal = new ContentValues();
            custVal.put(COLUMN_ID, customerIds[i]);
            custVal.put("phone", customers[i][3]);
            db.insert(TABLE_CUSTOMERS, null, custVal);
        }

        // New Category Structure
        long catDiagnosis = insertCategory(db, "Khám & Chẩn đoán", "Các dịch vụ khám và chẩn đoán hình ảnh");
        long catGeneral = insertCategory(db, "Nha khoa Tổng quát", "Các dịch vụ điều trị và chăm sóc răng miệng cơ bản");
        long catSurgery = insertCategory(db, "Tiểu phẫu", "Các thủ thuật nhổ răng và tiểu phẫu răng miệng");
        long catAesthetic = insertCategory(db, "Thẩm mỹ", "Dịch vụ làm đẹp răng và phục hình sứ");
        long catOrthodontics = insertCategory(db, "Chỉnh nha", "Các dịch vụ niềng răng thẩm mỹ");

        // Services & Images
        long s1 = insertService(db, catDiagnosis, "Khám và tư vấn", "Khám tổng quát sức khỏe răng miệng", 100000, 30);
        long s2 = insertService(db, catGeneral, "Lấy cao răng", "Vệ sinh răng miệng", 200000, 30);
        long s3 = insertService(db, catSurgery, "Nhổ răng khôn", "Tiểu phẫu răng khôn", 1500000, 60);

        // Rooms (based on layout image)
        String[][] rooms = {
            {"Phòng Chẩn đoán 1", "Tầng 1 - Khám và tư vấn ban đầu"},
            {"Phòng Chẩn đoán 2", "Tầng 1 - Khám và tư vấn ban đầu"},
            {"Phòng Nha tổng quát", "Tầng 1 - Các dịch vụ chăm sóc cơ bản"},
            {"Phòng Tiểu phẫu", "Tầng 2 - Nhổ răng và phẫu thuật"},
            {"Phòng Thẩm mỹ", "Tầng 2 - Bọc sứ, tẩy trắng"},
            {"Phòng Chỉnh nha", "Tầng 2 - Niềng răng thẩm mỹ"}
        };
        long[] roomIds = new long[rooms.length];
        for(int i=0; i<rooms.length; i++) {
            ContentValues roomVal = new ContentValues();
            roomVal.put("name", rooms[i][0]);
            roomVal.put("description", rooms[i][1]);
            roomIds[i] = db.insert(TABLE_CLINIC_ROOMS, null, roomVal);
        }

        // Add waiting patients to each room
        for(int i=0; i<roomIds.length; i++) {
            // Create a fake appointment
            ContentValues appVal = new ContentValues();
            appVal.put("customer_id", customerIds[i % customerIds.length]);
            appVal.put("doctor_id", doctorId);
            appVal.put("service_id", s1);
            appVal.put("appointment_datetime", "2026-03-15 08:00");
            appVal.put("status", "CHECKED_IN");
            long appId = db.insert(TABLE_APPOINTMENTS, null, appVal);

            // Add to queue with status 'WAITING'
            ContentValues queueVal = new ContentValues();
            queueVal.put("appointment_id", appId);
            queueVal.put("clinic_room_id", roomIds[i]);
            queueVal.put("status", "WAITING");
            queueVal.put("check_in_time", "08:15");
            queueVal.put("queue_number", "A-" + String.format("%02d", i + 1));
            queueVal.put("estimated_wait_time", (i + 1) * 15);
            db.insert(TABLE_CHECK_IN_QUEUE, null, queueVal);
        }

        db.execSQL("INSERT INTO " + TABLE_SHIFT_TEMPLATES + " (name, start_time, end_time, is_active) VALUES ('Ca sáng', '08:00', '12:00', 1)");
        db.execSQL("INSERT INTO " + TABLE_SHIFT_TEMPLATES + " (name, start_time, end_time, is_active) VALUES ('Ca chiều', '13:30', '17:30', 1)");

        // Medicines
        db.execSQL("INSERT INTO " + TABLE_MEDICINES + " (name, unit, price, stock) VALUES ('Paracetamol 500mg', 'Viên', 2000, 1000)");
        db.execSQL("INSERT INTO " + TABLE_MEDICINES + " (name, unit, price, stock) VALUES ('Amoxicillin 500mg', 'Viên', 5000, 500)");

        Log.d(TAG, "Data seeded successfully.");
    }

    private long insertCategory(SQLiteDatabase db, String name, String desc) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("description", desc);
        return db.insert(TABLE_SERVICE_CATEGORIES, null, values);
    }

    private long insertService(SQLiteDatabase db, long categoryId, String name, String desc, double price, int duration) {
        ContentValues values = new ContentValues();
        values.put("category_id", categoryId);
        values.put("name", name);
        values.put("description", desc);
        values.put("price", price);
        values.put("duration_minutes", duration);
        values.put("is_active", 1);
        return db.insert(TABLE_SERVICES, null, values);
    }

    private void insertServiceImages(SQLiteDatabase db, long serviceId, String folder, String[] imageFiles) {
        for (int i = 0; i < imageFiles.length; i++) {
            ContentValues values = new ContentValues();
            values.put("service_id", serviceId);
            values.put("image_url", "images/" + imageFiles[i]);
            values.put("display_order", i + 1);
            values.put("is_thumbnail", (i == 0) ? 1 : 0);
            db.insert(TABLE_SERVICE_IMAGES, null, values);
        }
    }

    // Auth Methods
    public long registerCustomer(String firstName, String lastName, String email, String password, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues userValues = new ContentValues();
            userValues.put("first_name", firstName);
            userValues.put("last_name", lastName);
            userValues.put("email", email);
            userValues.put("password_hash", password);
            userValues.put("role", "CUSTOMER");
            long userId = db.insert(TABLE_USERS, null, userValues);

            if (userId != -1) {
                ContentValues customerValues = new ContentValues();
                customerValues.put(COLUMN_ID, userId);
                customerValues.put("phone", phone);
                customerValues.put("reward_points", 0);
                db.insert(TABLE_CUSTOMERS, null, customerValues);
                db.setTransactionSuccessful();
            }
            return userId;
        } catch (Exception e) {
            Log.e(TAG, "Error registering customer", e);
            return -1;
        } finally {
            db.endTransaction();
        }
    }

    public Cursor login(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT u.*, " +
                "CASE " +
                "WHEN a.id IS NOT NULL THEN 'ADMIN' " +
                "WHEN d.id IS NOT NULL THEN 'DOCTOR' " +
                "WHEN c.id IS NOT NULL THEN 'CUSTOMER' " +
                "ELSE 'USER' END as role " +
                "FROM " + TABLE_USERS + " u " +
                "LEFT JOIN " + TABLE_ADMINS + " a ON u.id = a.id " +
                "LEFT JOIN " + TABLE_DOCTORS + " d ON u.id = d.id " +
                "LEFT JOIN " + TABLE_CUSTOMERS + " c ON u.id = c.id " +
                "WHERE u.email = ? AND u.password_hash = ?";
        
        return db.rawQuery(query, new String[]{email, password});
    }

    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_ID}, "email = ?", new String[]{email}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Clinic Management Methods
    public Cursor getAllServices() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_SERVICES, null, null, null, null, null, "name ASC");
    }

    public Cursor getAllDoctors() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT u.first_name || ' ' || u.last_name as name, d.* " +
                "FROM " + TABLE_USERS + " u " +
                "JOIN " + TABLE_DOCTORS + " d ON u.id = d.id";
        return db.rawQuery(query, null);
    }

    public long addAppointment(int customerId, int doctorId, int serviceId, String datetime, String notes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("customer_id", customerId);
        values.put("doctor_id", doctorId);
        values.put("service_id", serviceId);
        values.put("appointment_datetime", datetime);
        values.put("status", "SCHEDULED");
        values.put("notes", notes);
        return db.insert(TABLE_APPOINTMENTS, null, values);
    }

    public Cursor getLatestAppointment(int customerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT a.*, s.name as service_name, u.first_name || ' ' || u.last_name as doctor_name " +
                "FROM " + TABLE_APPOINTMENTS + " a " +
                "JOIN " + TABLE_SERVICES + " s ON a.service_id = s.id " +
                "JOIN " + TABLE_USERS + " u ON a.doctor_id = u.id " +
                "WHERE a.customer_id = ? " +
                "ORDER BY a.appointment_datetime DESC LIMIT 1";
        return db.rawQuery(query, new String[]{String.valueOf(customerId)});
    }

    public boolean updateAppointmentStatus(int appointmentId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", status);
        return db.update(TABLE_APPOINTMENTS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(appointmentId)}) > 0;
    }

    public Cursor getMedicalRecords(int customerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT mr.*, u.first_name || ' ' || u.last_name as doctor_name " +
                "FROM " + TABLE_MEDICAL_RECORDS + " mr " +
                "JOIN " + TABLE_USERS + " u ON mr.doctor_id = u.id " +
                "WHERE mr.customer_id = ? " +
                "ORDER BY mr.created_at DESC";
        return db.rawQuery(query, new String[]{String.valueOf(customerId)});
    }

    public long addMedicalRecord(int appointmentId, int customerId, int doctorId, String diagnosis, String treatmentPlan, String prescription) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("appointment_id", appointmentId);
        values.put("customer_id", customerId);
        values.put("doctor_id", doctorId);
        values.put("diagnosis", diagnosis);
        values.put("treatment_plan", treatmentPlan);
        values.put("prescription", prescription);
        values.put("created_at", new java.util.Date().toString());
        return db.insert(TABLE_MEDICAL_RECORDS, null, values);
    }

    public Cursor getDoctorSchedules() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT ds.*, u.first_name || ' ' || u.last_name as doctor_name " +
                "FROM " + TABLE_DOCTOR_SCHEDULES + " ds " +
                "JOIN " + TABLE_USERS + " u ON ds.doctor_id = u.id";
        return db.rawQuery(query, null);
    }

    // Queue Management Methods
    public long addQueueItem(int appointmentId, int clinicRoomId, String checkInTime, String queueNumber, int estWaitTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("appointment_id", appointmentId);
        values.put("clinic_room_id", clinicRoomId);
        values.put("check_in_time", checkInTime);
        values.put("status", "WAITING");
        values.put("queue_number", queueNumber);
        values.put("estimated_wait_time", estWaitTime);
        values.put("delay_minutes", 0);
        return db.insert(TABLE_CHECK_IN_QUEUE, null, values);
    }

    public Cursor getQueueList(int clinicRoomId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT q.*, u.first_name || ' ' || u.last_name as patient_name, s.name as service_name, s.duration_minutes " +
                "FROM " + TABLE_CHECK_IN_QUEUE + " q " +
                "JOIN " + TABLE_APPOINTMENTS + " a ON q.appointment_id = a.id " +
                "JOIN " + TABLE_CUSTOMERS + " c ON a.customer_id = c.id " +
                "JOIN " + TABLE_USERS + " u ON c.id = u.id " +
                "JOIN " + TABLE_SERVICES + " s ON a.service_id = s.id " +
                "WHERE q.clinic_room_id = ? " +
                "ORDER BY q.check_in_time ASC";
        return db.rawQuery(query, new String[]{String.valueOf(clinicRoomId)});
    }

    public boolean updateQueueStatus(int queueId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", status);
        return db.update(TABLE_CHECK_IN_QUEUE, values, "id = ?", new String[]{String.valueOf(queueId)}) > 0;
    }

    public Cursor getQueueStatus(int appointmentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_CHECK_IN_QUEUE + " WHERE appointment_id = ?";
        return db.rawQuery(query, new String[]{String.valueOf(appointmentId)});
    }



    public Cursor getCustomerById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT u.*, c.phone, c.reward_points " +
                "FROM " + TABLE_USERS + " u " +
                "JOIN " + TABLE_CUSTOMERS + " c ON u.id = c.id " +
                "WHERE u.id = ?";
        return db.rawQuery(query, new String[]{String.valueOf(userId)});
    }

    // --- Admin Module Methods ---

    public Cursor getRoomsWithWaitingCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT r.name, COUNT(q.id) as waiting_count " +
                       "FROM " + TABLE_CLINIC_ROOMS + " r " +
                       "LEFT JOIN " + TABLE_CHECK_IN_QUEUE + " q ON r.id = q.clinic_room_id AND q.status = 'WAITING' " +
                       "GROUP BY r.id";
        return db.rawQuery(query, null);
    }

    public Cursor getAllCategories() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_SERVICE_CATEGORIES, null);
    }

    public Cursor getServicesByCategory(int categoryId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT s.*, i.image_url " +
                       "FROM " + TABLE_SERVICES + " s " +
                       "LEFT JOIN " + TABLE_SERVICE_IMAGES + " i ON s.id = i.service_id " +
                       "WHERE s.category_id = ? " +
                       "GROUP BY s.id";
        return db.rawQuery(query, new String[]{String.valueOf(categoryId)});
    }

    public long addCategory(String name, String desc) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("description", desc);
        return db.insert(TABLE_SERVICE_CATEGORIES, null, values);
    }

    public long addService(int categoryId, String name, String desc, double price, int duration) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("category_id", categoryId);
        values.put("name", name);
        values.put("description", desc);
        values.put("price", price);
        values.put("duration_minutes", duration);
        values.put("is_active", 1);
        return db.insert(TABLE_SERVICES, null, values);
    }

    public boolean updateServiceStatus(int serviceId, boolean isActive) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_active", isActive ? 1 : 0);
        return db.update(TABLE_SERVICES, values, "id = ?", new String[]{String.valueOf(serviceId)}) > 0;
    }

    public long addServiceImage(int serviceId, String url, int order, boolean isMain) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("service_id", serviceId);
        values.put("image_url", url);
        values.put("display_order", order);
        values.put("is_thumbnail", isMain ? 1 : 0);
        return db.insert(TABLE_SERVICE_IMAGES, null, values);
    }

    public boolean startTreatment(int appointmentId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", "IN_PROGRESS");
        values.put("treatment_start_time", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date()));
        return db.update(TABLE_CHECK_IN_QUEUE, values, "appointment_id = ?", new String[]{String.valueOf(appointmentId)}) > 0;
    }

    public Cursor getAppointmentsByCustomer(int customerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT a.*, s.name as service_name, u.first_name || ' ' || u.last_name as doctor_name " +
                "FROM " + TABLE_APPOINTMENTS + " a " +
                "JOIN " + TABLE_SERVICES + " s ON a.service_id = s.id " +
                "JOIN " + TABLE_USERS + " u ON a.doctor_id = u.id " +
                "WHERE a.customer_id = ? " +
                "ORDER BY a.appointment_datetime DESC";
        return db.rawQuery(query, new String[]{String.valueOf(customerId)});
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOMERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOCTORS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ADMINS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POINT_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SERVICE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SERVICES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SERVICE_IMAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLINIC_ROOMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHIFT_TEMPLATES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOCTOR_SCHEDULES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPOINTMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHECK_IN_QUEUE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDICAL_RECORDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDICAL_RECORD_DETAILS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REVIEWS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVOICES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT_ROOMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT_MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDICINES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRESCRIPTIONS);
        onCreate(db);
    }
}
