package com.hcmute.mobile_android.network;

import com.hcmute.mobile_android.network.models.AddGeneralServiceRequest;
import com.hcmute.mobile_android.network.models.AddToothServiceRequest;
import com.hcmute.mobile_android.network.models.CheckInMyStatusResponse;
import com.hcmute.mobile_android.network.models.CreateAppointmentRequest;
import com.hcmute.mobile_android.network.models.CreateCategoryRequest;
import com.hcmute.mobile_android.network.models.CreateDoctorRequest;
import com.hcmute.mobile_android.network.models.CreateServiceRequest;
import com.hcmute.mobile_android.network.models.GeneralServiceResponse;
import com.hcmute.mobile_android.network.models.LoginRequest;
import com.hcmute.mobile_android.network.models.PatientMeResponse;
import com.hcmute.mobile_android.network.models.ServiceCategory;
import com.hcmute.mobile_android.network.models.ToothServiceResponse;
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
import com.hcmute.mobile_android.network.models.ChatMessagePayload;
import com.hcmute.mobile_android.network.models.ChatSendBody;
import com.hcmute.mobile_android.network.models.DoctorDetailResponse;
import com.hcmute.mobile_android.network.models.AuditLog;
import com.hcmute.mobile_android.network.models.AuditLogResponse;
import com.hcmute.mobile_android.network.models.Receptionist;
import com.hcmute.mobile_android.network.models.ScheduleAppointment;
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

    @PATCH("api/notifications/{id}/read")
    Call<MessageResponse> markNotificationAsRead(@Path("id") long id);

    @PATCH("api/notifications/read-all")
    Call<MessageResponse> markAllNotificationsAsRead();

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

    @GET("api/doctors/{id}")
    Call<DoctorDetailResponse> getDoctorDetail(@Path("id") Long id);

    @GET("api/chat/doctor/{doctorId}/messages")
    Call<List<ChatMessagePayload>> getChatMessages(@Path("doctorId") Long doctorId);

    @POST("api/chat/doctor/{doctorId}/messages")
    Call<ChatMessagePayload> sendChatMessage(@Path("doctorId") Long doctorId, @Body ChatSendBody body);

    @POST("api/appointments")
    Call<com.hcmute.mobile_android.network.models.UpcomingAppointment> createAppointment(@Body CreateAppointmentRequest request);

    @PATCH("api/appointments/{id}/cancel")
    Call<com.hcmute.mobile_android.network.models.UpcomingAppointment> cancelAppointment(@Path("id") Long id);

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

    @PUT("api/admin/doctors/{id}")
    Call<MessageResponse> updateDoctor(@Path("id") Long id, @Body CreateDoctorRequest request);

    @retrofit2.http.DELETE("api/admin/doctors/{id}")
    Call<MessageResponse> deleteDoctor(@Path("id") Long id);

    @GET("api/admin/doctors")
    Call<com.hcmute.mobile_android.network.models.PagedResponse<com.hcmute.mobile_android.network.models.DoctorItem>> getAdminDoctors();

    @GET("api/admin/rooms")
    Call<List<RoomItem>> getRooms();

    @POST("api/admin/rooms")
    Call<RoomItem> createRoom(@Body com.hcmute.mobile_android.network.models.RoomRequest request);

    @PUT("api/admin/rooms/{id}")
    Call<RoomItem> updateRoom(@Path("id") Long id, @Body com.hcmute.mobile_android.network.models.RoomRequest request);

    @PATCH("api/admin/rooms/{id}/status")
    Call<MessageResponse> updateRoomStatus(@Path("id") Long id, @Query("active") boolean active);

    @retrofit2.http.DELETE("api/admin/rooms/{id}")
    Call<MessageResponse> deleteRoom(@Path("id") Long id);

    // Queue Management APIs
    @GET("api/queue/room/{roomId}")
    Call<List<QueueItem>> getQueueByRoom(@Path("roomId") Long roomId);

    @POST("api/queue/{id}/call")
    Call<Void> callPatientToRoom(@Path("id") Long queueId);

    @POST("api/queue/{id}/delay")
    Call<Void> delayPatient(@Path("id") Long queueId);

    @POST("api/queue/{id}/transfer-xray")
    Call<Void> transferToXRay(@Path("id") Long queueId, @Body java.util.Map<String, Long> body);

    @POST("api/reception/queue/{id}/skip")
    Call<Void> skipPatient(@Path("id") Long queueId);

    @PUT("api/queue/{id}/status")
    Call<Void> completePatient(@Path("id") Long queueId);

    // Queue Estimation APIs
    @GET("api/queue/estimate/{queueId}")
    Call<QueueItem> getQueueEstimate(@Path("queueId") Long queueId);

    @GET("api/queue/estimate/appointment/{appointmentId}")
    Call<QueueItem> getQueueEstimateByAppointment(@Path("appointmentId") Long appointmentId);

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

    @POST("api/prescriptions")
    Call<com.hcmute.mobile_android.network.models.PrescriptionResponse> createPrescription(@Body com.hcmute.mobile_android.network.models.request.PrescriptionRequest request);

    @GET("api/prescriptions/appointment/{appointmentId}")
    Call<com.hcmute.mobile_android.network.models.PrescriptionResponse> getPrescriptionByAppointment(@Path("appointmentId") Long appointmentId);

    // Step-level Prescription & Monitoring APIs
    @POST("api/treatment-plans/steps/{stepId}/prescription")
    Call<com.hcmute.mobile_android.network.models.PrescriptionResponse> savePrescriptionForStep(
            @Path("stepId") Long stepId,
            @Body com.hcmute.mobile_android.network.models.SavePrescriptionRequest request);

    @GET("api/treatment-plans/steps/{stepId}/prescription")
    Call<com.hcmute.mobile_android.network.models.PrescriptionResponse> getPrescriptionForStep(@Path("stepId") Long stepId);

    @PATCH("api/treatment-plans/steps/{stepId}/resume")
    Call<MessageResponse> resumeMonitoringStep(@Path("stepId") Long stepId);


    // Admin Service Management
    @POST("api/admin/services/categories")
    Call<ServiceCategory> createServiceCategory(@Body CreateCategoryRequest request);

    @PUT("api/admin/services/categories/{id}")
    Call<MessageResponse> updateCategory(@Path("id") Long id, @Body CreateCategoryRequest request);

    @retrofit2.http.DELETE("api/admin/services/categories/{id}")
    Call<MessageResponse> deleteCategory(@Path("id") Long id);

    @POST("api/admin/services")
    Call<MessageResponse> createService(@Body CreateServiceRequest request);

    @GET("api/admin/services")
    Call<List<com.hcmute.mobile_android.network.models.ServiceItem>> getAdminServices();

    @PATCH("api/admin/services/{id}/status")
    Call<MessageResponse> updateServiceStatus(@Path("id") Long id, @Query("active") boolean active);

    @PUT("api/admin/services/{id}")
    Call<MessageResponse> updateService(@Path("id") Long id, @Body CreateServiceRequest request);

    @retrofit2.http.DELETE("api/admin/services/{id}")
    Call<MessageResponse> deleteService(@Path("id") Long id);

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
    
    @GET("api/admin/reports/top-services")
    Call<List<com.hcmute.mobile_android.network.models.ServiceStats>> getTopServices(@Query("startDate") String startDate, @Query("endDate") String endDate, @Query("limit") int limit);
    
    @GET("api/admin/reports/doctor-performance")
    Call<List<com.hcmute.mobile_android.network.models.DoctorStats>> getDoctorPerformance(@Query("startDate") String startDate, @Query("endDate") String endDate);
    
    // Tooth Service APIs (Odontogram)
    @POST("api/treatment-plans/{planId}/services/teeth/{toothNumber}")
    Call<ToothServiceResponse> addServiceToTooth(
        @Path("planId") Long planId,
        @Path("toothNumber") String toothNumber,
        @Body AddToothServiceRequest request
    );
    
    @POST("api/treatment-plans/{planId}/services/general")
    Call<GeneralServiceResponse> addGeneralService(
        @Path("planId") Long planId,
        @Body AddGeneralServiceRequest request
    );
    
    @GET("api/treatment-plans/{planId}/services/teeth/{toothNumber}")
    Call<List<TreatmentPlan.Step>> getServicesForTooth(
        @Path("planId") Long planId,
        @Path("toothNumber") String toothNumber
    );
    
    @GET("api/treatment-plans/{planId}/services/general")
    Call<List<TreatmentPlan.Step>> getGeneralServices(@Path("planId") Long planId);
    
    @retrofit2.http.DELETE("api/treatment-plans/{planId}/services/steps/{stepId}")
    Call<java.util.Map<String, Object>> removeService(
        @Path("planId") Long planId,
        @Path("stepId") Long stepId
    );
    
    @PUT("api/treatment-plans/{planId}/services/steps/{stepId}/price")
    Call<java.util.Map<String, Object>> updateStepPrice(
        @Path("planId") Long planId,
        @Path("stepId") Long stepId,
        @Body java.util.Map<String, Object> body
    );
    
    @GET("api/treatment-plans/{planId}/services/all")
    Call<java.util.Map<String, Object>> getAllSteps(@Path("planId") Long planId);

    @GET("api/admin/audit-logs")
    Call<AuditLogResponse> getAuditLogs(@Query("page") int page, @Query("size") int size);

    @GET("api/admin/receptionists")
    Call<List<Receptionist>> getAdminReceptionists();

    @POST("api/admin/receptionists")
    Call<Receptionist> createReceptionist(@Body java.util.Map<String, String> body);

    @PATCH("api/admin/receptionists/{id}/status")
    Call<com.hcmute.mobile_android.network.models.MessageResponse> updateReceptionistStatus(@Path("id") Long id, @Query("active") boolean active);

    @retrofit2.http.DELETE("api/admin/receptionists/{id}")
    Call<com.hcmute.mobile_android.network.models.MessageResponse> deleteReceptionist(@Path("id") Long id);

    @GET("api/appointments/doctor/{doctorId}/date/{date}")
    Call<List<ScheduleAppointment>> getDoctorSchedule(@Path("doctorId") Long doctorId, @Path("date") String date);
}
