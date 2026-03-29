# PAYMENT INVOICE MODEL FIX

## Issue Identified

The payment completion feature was failing silently because the Invoice model didn't match the API response structure, causing Gson deserialization to fail and triggering the `onFailure` callback instead of `onResponse`.

## Root Cause

From the logs:
```
2026-03-29 14:23:25.211 okhttp: <-- 200 http://10.20.1.170:8081/api/treatment-plans/1/complete-and-generate-invoice (64ms)
2026-03-29 14:23:25.213 okhttp: {"patientName":"Nguyễn Văn An","totalAmount":100000.0,...}
2026-03-29 14:23:25.228 Toast: show: caller = DoctorWorkflowActivity$15.onFailure:1579
```

API returned HTTP 200 with valid JSON, but Retrofit called `onFailure` instead of `onResponse`.

## Problems Found

### 1. Missing `treatmentPlanId` field
API response includes:
```json
{
  "treatmentPlanId": 1,
  ...
}
```

But Invoice model didn't have this field.

### 2. Date Type Mismatch
API returns ISO 8601 strings:
```json
{
  "createdAt": "2026-03-29T14:22:33.369538",
  "paidAt": null
}
```

But Invoice model used `Date` type, causing Gson parsing to fail.

### 3. InvoiceItem Structure Mismatch
API returns:
```json
{
  "items": [
    {
      "id": 1,
      "serviceName": "Khám và tư vấn răng miệng",
      "toothNumber": "null",
      "quantity": 1,
      "unitPrice": 100000.0,
      "totalPrice": 100000.0,
      "description": ""
    }
  ]
}
```

But Invoice.InvoiceItem had:
- `price` instead of `unitPrice`
- `subtotal` instead of `totalPrice`
- Missing `id`, `toothNumber`, `description`

## Fixes Applied

### 1. Updated Invoice.java

**Added missing field:**
```java
@SerializedName("treatmentPlanId")
private Long treatmentPlanId;
```

**Changed date types from Date to String:**
```java
@SerializedName("paidAt")
private String paidAt;  // Was: Date paidAt

@SerializedName("createdAt")
private String createdAt;  // Was: Date createdAt
```

**Updated InvoiceItem inner class:**
```java
public static class InvoiceItem {
    @SerializedName("id")
    private Long id;
    
    @SerializedName("serviceName")
    private String serviceName;
    
    @SerializedName("toothNumber")
    private String toothNumber;
    
    @SerializedName("quantity")
    private Integer quantity;
    
    @SerializedName("unitPrice")
    private BigDecimal unitPrice;  // Was: price
    
    @SerializedName("totalPrice")
    private BigDecimal totalPrice;  // Was: subtotal
    
    @SerializedName("description")
    private String description;
    
    // Getters and setters...
}
```

### 2. Updated InvoiceItemAdapter.java

Changed method calls to match new field names:
```java
// Before:
holder.tvPrice.setText(formatter.format(item.getPrice()));
holder.tvSubtotal.setText(formatter.format(item.getSubtotal()));

// After:
holder.tvPrice.setText(formatter.format(item.getUnitPrice()));
holder.tvSubtotal.setText(formatter.format(item.getTotalPrice()));
```

Added tooth number display:
```java
String serviceName = item.getServiceName();
if (item.getToothNumber() != null && !item.getToothNumber().equals("null") && !item.getToothNumber().isEmpty()) {
    serviceName += " (Răng " + item.getToothNumber() + ")";
}
holder.tvServiceName.setText(serviceName);
```

## Files Modified

1. `mobile_android/app/src/main/java/com/hcmute/mobile_android/network/models/Invoice.java`
   - Added `treatmentPlanId` field
   - Changed `paidAt` and `createdAt` from `Date` to `String`
   - Completely rewrote `InvoiceItem` inner class

2. `mobile_android/app/src/main/java/com/hcmute/mobile_android/adapters/InvoiceItemAdapter.java`
   - Updated to use `getUnitPrice()` instead of `getPrice()`
   - Updated to use `getTotalPrice()` instead of `getSubtotal()`
   - Added tooth number display logic

## Testing

After rebuilding the APK:
```bash
cd mobile_android
./gradlew assembleDebug
```

Build successful. APK ready for testing.

## Expected Behavior Now

1. User clicks "Thanh toán" button
2. Confirmation dialog appears
3. User confirms
4. Loading dialog shows "Đang tạo hóa đơn..."
5. API call succeeds (HTTP 200)
6. Gson successfully deserializes response
7. `onResponse` callback triggered (not `onFailure`)
8. Success toast: "Đã tạo hóa đơn thành công!"
9. Navigate to PaymentActivity
10. DoctorWorkflowActivity closes

## Next Steps

1. Install updated APK on device
2. Test payment completion flow
3. Verify navigation to PaymentActivity
4. Check invoice details display correctly
5. Verify patient receives notification

---

**Fixed by**: AI Assistant  
**Date**: 2026-03-29  
**Status**: ✅ Ready for Testing
