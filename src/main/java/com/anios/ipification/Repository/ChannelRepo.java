package com.anios.ipification.Repository;

import com.anios.ipification.Entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChannelRepo extends JpaRepository<Channel,Integer> {

    List<Channel> findByTxnIdAndStatusOrderByPriority(String txnId,String status);


    Channel findByTxnIdAndName(String txnId, String channelName);

    List<Channel> findByNameAndStatusNotAndNumber(String channel, String status,String number);
}
