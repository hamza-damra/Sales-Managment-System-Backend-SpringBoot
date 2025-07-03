package com.hamza.salesmanagementbackend.payload.response;

import com.hamza.salesmanagementbackend.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SignUpResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private UserInfo user;

    public SignUpResponse(String accessToken, String refreshToken, User user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = new UserInfo(user);
    }

    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String role;
        private LocalDateTime createdAt;

        public UserInfo(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.email = user.getEmail();
            this.firstName = user.getFirstName();
            this.lastName = user.getLastName();
            this.role = user.getRole().name();
            this.createdAt = user.getCreatedAt();
        }
    }
}
