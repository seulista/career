package com.dalbitlive.mybatis;

//import com.dalbitlive.util.DalbitUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public class ReplicationRoutingDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {

        String dataSourceType = "";

        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        }catch (Exception e){

        }

        dataSourceType = dataSourceType.equals("") ? (TransactionSynchronizationManager.isCurrentTransactionReadOnly() ? "slave" : "master") : dataSourceType;
        log.info("current dataSourceType : {}", dataSourceType);
        return dataSourceType;
    }
}
