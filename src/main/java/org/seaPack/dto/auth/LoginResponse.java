package org.seaPack.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    
    private String token;
    
    private Long userId;
    
    private String username;
    
    private String nickName;
    
    private String email;
    
    private String mobile;
}