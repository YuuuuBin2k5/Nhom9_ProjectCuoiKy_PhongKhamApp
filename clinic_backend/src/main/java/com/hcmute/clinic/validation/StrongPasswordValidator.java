package com.hcmute.clinic.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Lớp triển khai logic kiểm tra mật khẩu mạnh dựa trên biểu thức chính quy (Regex).
 */
public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {
    
    private static final String PASSWORD_PATTERN = 
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
    
    /**
     * Kiểm tra xem mật khẩu có hợp lệ hay không dựa trên PASSWORD_PATTERN.
     * 
     * @param password Mật khẩu cần kiểm tra.
     * @param context Ngữ cảnh của trình xác thực.
     * @return true nếu mật khẩu hợp lệ, ngược lại trả về false.
     */
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }
        return password.matches(PASSWORD_PATTERN);
    }
}
