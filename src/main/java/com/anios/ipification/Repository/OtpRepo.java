package com.anios.ipification.Repository;

import com.anios.ipification.Entity.OtpRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OtpRepo extends JpaRepository<OtpRecord, String> {
    OtpRecord findByMobileNumber(String mobileNumber);

}
