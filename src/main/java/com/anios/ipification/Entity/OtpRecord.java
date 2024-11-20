package com.anios.ipification.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "otp_record")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpRecord {
    @Id
    private String mobileNumber;
    private String otp;
}
