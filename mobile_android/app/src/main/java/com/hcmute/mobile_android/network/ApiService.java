package com.hcmute.mobile_android.network;

import com.hcmute.mobile_android.network.models.CheckInMyStatusResponse;
import com.hcmute.mobile_android.network.models.CreateAppointmentRequest;
import com.hcmute.mobile_android.network.models.CreateDoctorRequest;
import com.hcmute.mobile_android.network.models.LoginRequest;
import com.hcmute.mobile_android.network.models.PatientMeResponse;
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

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @GET("api/patients/me")
    Call<PatientMeResponse> getPatientMe();

    @GET("api/patients/me/checkin-status")
    Call<CheckInMyStatusResponse> getMyCheckInStatus();

    @GET("api/checkin/qr-token")
    Call<com.hcmute.mobile_android.network.models.QrTokenResponse> getQrToken();

    @POST("api/checkin/scan")
    Call<MessageResponse> scanCheckIn(@Body com.hcmute.mobile_android.network.models.CheckInScanRequest request);

    @GET("api/treatment-plans/my")
    Call<List<TreatmentPlanSummary>> getMyTreatmentPlans();

    @GET("api/notifications/me")
    Call<List<com.hcmute.mobile_android.network.models.NotificationItem>> getMyNotifications();

    @GET("api/patients/me/appointments/upcoming")
    Call<List<com.hcmute.mobile_android.network.models.UpcomingAppointment>> getUpcomingAppointments();

    @GET("api/services")
    Call<List<com.hcmute.mobile_android.network.models.ServiceItem>> getServices();

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

    @GET("api/admin/rooms")
    Call<List<RoomItem>> getRooms();

    // Queue Management APIs
    @GET("api/queue/room/{roomId}")
    Call<List<QueueItem>> getQueueByRoom(@Path("roomId") Long roomId);

    @POST("api/queue/{id}/call")
    Call<Void> callPatient(@Path("id") Long queueId);

    @POST("api/queue/{id}/transfer-xray")
    Call<Void> transferToXRay(@Path("id") Long queueId);

    @PUT("api/queue/{id}/status")
    Call<Void> completePatient(@Path("id") Long queueId);

    // Doctor Workflow APIs
    @GET("api/doctor/patient")
    Call<PatientInfo> lookupPatientByQR(@Query("qr") String qrCode);

    @GET("api/treatment-templates")
    Call<List<TreatmentTemplate>> getTreatmentTemplates();

    @POST("api/treatment-plans/from-template")
    Call<TreatmentPlan> createTreatmentPlanFromTemplate(@Body CreateTreatmentPlanRequest request);

    @GET("api/treatment-plans/{id}")
    Call<TreatmentPlan> getTreatmentPlan(@Path("id") Long planId);

    @PUT("api/treatment-plans/{id}/steps")
    Call<Void> updateTreatmentPlanSteps(@Path("id") Long planId, @Body List<TreatmentPlan.Step> steps);
}
