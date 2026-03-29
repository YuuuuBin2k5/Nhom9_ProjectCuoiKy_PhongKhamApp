package com.hcmute.mobile_android.network;

import com.hcmute.mobile_android.network.models.CheckInMyStatusResponse;
import com.hcmute.mobile_android.network.models.CreateAppointmentRequest;
import com.hcmute.mobile_android.network.models.CreateCategoryRequest;
import com.hcmute.mobile_android.network.models.CreateDoctorRequest;
import com.hcmute.mobile_android.network.models.CreateServiceRequest;
import com.hcmute.mobile_android.network.models.LoginRequest;
import com.hcmute.mobile_android.network.models.PatientMeResponse;
import com.hcmute.mobile_android.network.models.ServiceCategory;
import com.hcmute.mobile_android.network.models.TreatmentPlanSummary;
import com.hcmute.mobile_android.network.models.LoginResponse;
import com.hcmute.mobile_android.network.models.MessageResponse;
import com.hcmute.mobile_android.network.models.OtpRequest;
import com.hcmute.mobile_android.network.models.OtpVerifyRequest;
import com.hcmute.mobile_android.network.models.OtpVerifyResponse;
import com.hcmute.mobile_android.network.models.RegisterRequest;
import com.hcmute.mobile_android.network.models.RegisterResultResponse;
import com.hcmute.mobile_android.network.models.RoomItem;
import com.hcmute.mobile_android.network.models.QueueItem;
import com.hcmute.mobile_android.network.models.PatientInfo;
import com.hcmute.mobile_android.network.models.TreatmentTemplate;
import com.hcmute.mobile_android.network.models.TreatmentPlan;
import com.hcmute.mobile_android.network.models.CreateTreatmentPlanRequest;
import com.hcmute.mobile_android.network.models.UpdatePatientRequest;
import com.hcmute.mobile_android.network.models.UploadResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @GET("api/patients/me")
    Call<PatientMeResponse> getPatientMe();

    @PUT("api/patients/me")
    Call<PatientMeResponse> updatePatientMe(@Body UpdatePatientRequest request);

    @GET("api/patients/me/checkin-status")
    Call<CheckInMyStatusResponse> getMyCheckInStatus();

    @GET("api/patients/me/medical-records")
    Call<List<com.hcmute.mobile_android.network.models.MedicalRecordResponse>> getMyMedicalRecords();

    @GET("api/patients/me/medical-records/{id}")
    Call<com.hcmute.mobile_android.network.models.MedicalRecordDetailResponse> getMedicalRecordDetail(@Path("id") Long id);

    @GET("api/patients/me/prescriptions/{id}")
    Call<com.hcmute.mobile_android.network.models.PrescriptionResponse> getPrescriptionDetail(@Path("id") Long prescriptionId);

    @GET("api/checkin/qr-token")
    Call<com.hcmute.mobile_android.network.models.QrTokenResponse> getQrToken();

    @POST("api/checkin/scan")
    Call<MessageResponse> scanCheckIn(@Body com.hcmute.mobile_android.network.models.CheckInScanRequest request);

    @POST("api/checkin/self-scan")
    Call<MessageResponse> selfCheckIn(@Body com.hcmute.mobile_android.network.models.CheckInScanRequest request);

    @GET("api/treatment-plans/my")
    Call<List<TreatmentPlanSummary>> getMyTreatmentPlans();

    @GET("api/notifications/me")
    Call<List<com.hcmute.mobile_android.network.models.NotificationItem>> getMyNotifications();

    @GET("api/patients/me/appointments/upcoming")
    Call<List<com.hcmute.mobile_android.network.models.UpcomingAppointment>> getUpcomingAppointments();

    @GET("api/services")
    Call<List<com.hcmute.mobile_android.network.models.ServiceItem>> getServices();

    @GET("api/services/categories")
    Call<List<ServiceCategory>> getServiceCategories();

    @GET("api/doctors")
    Call<List<com.hcmute.mobile_android.network.models.DoctorItem>> getDoctors();

    @GET("api/doctors")
    Call<List<com.hcmute.mobile_android.network.models.DoctorItem>> getDoctorsByService(@Query("serviceId") Long serviceId);

    @POST("api/appointments")
    Call<com.hcmute.mobile_android.network.models.UpcomingAppointment> createAppointment(@Body CreateAppointmentRequest request);

    @POST("api/auth/otp/request")
    Call<MessageResponse> requestOtp(@Body OtpRequest request);

    @POST("api/auth/otp/verify")
    Call<OtpVerifyResponse> verifyOtp(@Body OtpVerifyRequest request);

    @POST("api/auth/register")
    Call<RegisterResultResponse> register(@Body RegisterRequest request);

    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    // Admin APIs

    @POST("api/admin/doctors")
    Call<MessageResponse> createDoctor(@Body CreateDoctorRequest request);

    @PATCH("api/admin/doctors/{id}/status")
    Call<MessageResponse> updateDoctorStatus(@Path("id") Long id, @Query("active") boolean active);

    @GET("api/admin/doctors")
    Call<List<com.hcmute.mobile_android.network.models.DoctorItem>> getAdminDoctors();

    @GET("api/admin/rooms")
    Call<List<RoomItem>> getRooms();

    @PATCH("api/admin/rooms/{id}/status")
    Call<MessageResponse> updateRoomStatus(@Path("id") Long id, @Query("active") boolean active);

    // Queue Management APIs
    @GET("api/queue/room/{roomId}")
    Call<List<QueueItem>> getQueueByRoom(@Path("roomId") Long roomId);

    @POST("api/queue/{id}/call")
    Call<Void> callPatientToRoom(@Path("id") Long queueId);

    @POST("api/queue/{id}/delay")
    Call<Void> delayPatient(@Path("id") Long queueId);

    @POST("api/queue/{id}/transfer-xray")
    Call<Void> transferToXRay(@Path("id") Long queueId, @Body java.util.Map<String, Long> body);

    @PUT("api/queue/{id}/status")
    Call<Void> completePatient(@Path("id") Long queueId);

    // Doctor Dashboard APIs
    @GET("api/doctor/me/queue")
    Call<com.hcmute.mobile_android.network.models.DoctorQueueResponse> getDoctorQueue();

    @GET("api/doctor/me/appointments/upcoming")
    Call<List<com.hcmute.mobile_android.network.models.UpcomingAppointment>> getDoctorUpcomingAppointments();

    // Doctor Workflow APIs
    @GET("api/doctor/patient")
    Call<PatientInfo> lookupPatientByQR(@Query("qr") String qrCode);

    @GET("api/doctor/patients/{id}/medical-records")
    Call<List<com.hcmute.mobile_android.network.models.MedicalRecordResponse>> getPatientMedicalRecords(@Path("id") Long patientId);

    @GET("api/treatment-plans/patient/{patientId}")
    Call<List<TreatmentPlan>> getTreatmentPlansByPatient(@Path("patientId") Long patientId);

    @GET("api/treatment-templates")
    Call<List<TreatmentTemplate>> getTreatmentTemplates();

    @POST("api/treatment-plans/from-template")
    Call<TreatmentPlan> createTreatmentPlanFromTemplate(@Body CreateTreatmentPlanRequest request);

    @POST("api/treatment-plans/from-appointment")
    Call<TreatmentPlan> createTreatmentPlanFromAppointment(@Body java.util.Map<String, Long> body);

    @GET("api/treatment-plans/{id}")
    Call<TreatmentPlan> getTreatmentPlan(@Path("id") Long planId);

    @GET("api/treatment-plans/{id}/for-room")
    Call<TreatmentPlan> getTreatmentPlanForRoom(@Path("id") Long planId);

    @PUT("api/treatment-plans/{id}")
    Call<Void> updateTreatmentPlanSteps(@Path("id") Long planId, @Body com.hcmute.mobile_android.network.models.request.UpdatePlanStepsRequest request);

    @POST("api/treatment-plans/{id}/activate")
    Call<MessageResponse> activatePlan(@Path("id") Long planId);

    @PATCH("api/treatment-plans/steps/{stepId}/start")
    Call<MessageResponse> startTreatmentStep(@Path("stepId") Long stepId);

    @PATCH("api/treatment-plans/steps/{stepId}/cancel")
    Call<MessageResponse> cancelTreatmentStep(@Path("stepId") Long stepId);

    @PATCH("api/treatment-plans/steps/{stepId}/complete")
    Call<MessageResponse> completeTreatmentStep(@Path("stepId") Long stepId, @Body java.util.Map<String, Object> body);

    // Prescription APIs
    @POST("api/prescriptions")
    Call<com.hcmute.mobile_android.network.models.PrescriptionResponse> createPrescription(@Body com.hcmute.mobile_android.network.models.request.PrescriptionRequest request);

    @GET("api/prescriptions/appointment/{appointmentId}")
    Call<com.hcmute.mobile_android.network.models.PrescriptionResponse> getPrescriptionByAppointment(@Path("appointmentId") Long appointmentId);

    // Admin Service Management
    @POST("api/admin/services/categories")
    Call<ServiceCategory> createServiceCategory(@Body CreateCategoryRequest request);

    @POST("api/admin/services")
    Call<MessageResponse> createService(@Body CreateServiceRequest request);

    @GET("api/admin/services")
    Call<List<com.hcmute.mobile_android.network.models.ServiceItem>> getAdminServices();

    @PATCH("api/admin/services/{id}/status")
    Call<MessageResponse> updateServiceStatus(@Path("id") Long id, @Query("active") boolean active);

    @retrofit2.http.Multipart
    @POST("api/upload")
    Call<UploadResponse> uploadFile(@retrofit2.http.Part okhttp3.MultipartBody.Part file);

    // Review APIs
    @POST("api/reviews")
    Call<com.hcmute.mobile_android.network.models.Review> createReview(@Body com.hcmute.mobile_android.network.models.ReviewRequest request);

    @GET("api/reviews/my")
    Call<List<com.hcmute.mobile_android.network.models.Review>> getMyReviews();

    @GET("api/reviews/doctor/{doctorId}")
    Call<List<com.hcmute.mobile_android.network.models.Review>> getDoctorReviews(@Path("doctorId") Long doctorId);

    @GET("api/reviews/service/{serviceId}")
    Call<List<com.hcmute.mobile_android.network.models.Review>> getServiceReviews(@Path("serviceId") Long serviceId);

    // Invoice APIs
    @GET("api/invoices/my")
    Call<List<com.hcmute.mobile_android.network.models.Invoice>> getMyInvoices();

    @GET("api/invoices/{id}")
    Call<com.hcmute.mobile_android.network.models.Invoice> getInvoiceDetail(@Path("id") Long id);

    @POST("api/invoices/{id}/pay")
    Call<com.hcmute.mobile_android.network.models.PaymentResponse> processPayment(@Path("id") Long id, @Body com.hcmute.mobile_android.network.models.PaymentRequest request);
    
    @POST("api/treatment-plans/{planId}/complete-and-generate-invoice")
    Call<com.hcmute.mobile_android.network.models.Invoice> completeAndGenerateInvoice(@Path("planId") Long planId);
    
    // Admin Report APIs
    @GET("api/admin/reports/revenue")
    Call<com.hcmute.mobile_android.network.models.RevenueReport> getRevenueReport(@Query("startDate") String startDate, @Query("endDate") String endDate);
    
    @GET("api/admin/reports/services")
    Call<List<com.hcmute.mobile_android.network.models.ServiceStats>> getTopServices(@Query("limit") int limit);
    
    @GET("api/admin/reports/doctors")
    Call<List<com.hcmute.mobile_android.network.models.DoctorStats>> getDoctorPerformance(@Query("startDate") String startDate, @Query("endDate") String endDate);
}
