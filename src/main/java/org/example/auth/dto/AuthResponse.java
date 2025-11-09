package org.example.auth.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthResponse {
    Long userId;
    String username;
    Integer tickets;
    Integer arcaneDust;
    Integer matchRating;
}
