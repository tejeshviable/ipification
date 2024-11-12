package com.anios.ipification.Repository;

import com.anios.ipification.Entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelRepo extends JpaRepository<Channel,Integer> {
}
